package com.ebbenezer.taller.service;

import com.ebbenezer.taller.dto.AbonoRequest;
import com.ebbenezer.taller.dto.VentaDetalleResponse;
import com.ebbenezer.taller.dto.VentaRequest;
import com.ebbenezer.taller.dto.VentaResumenResponse;
import com.ebbenezer.taller.model.*;
import com.ebbenezer.taller.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final AbonoRepository abonoRepository;
    private final ProductoService productoService;

    @Transactional
    public VentaDetalleResponse registrarVenta(VentaRequest request) {
        Venta venta = new Venta();
        venta.setEsFiado(Boolean.TRUE.equals(request.getEsFiado()));

        Cliente cliente = null;
        if (request.getClienteId() != null) {
            cliente = clienteRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
            venta.setCliente(cliente);
        } else if (request.getClienteNombre() != null && !request.getClienteNombre().isBlank()) {
            venta.setClienteNombre(request.getClienteNombre().trim());
            venta.setEsFiado(false);
        } else {
            throw new IllegalArgumentException("Debe enviar un clienteId o un clienteNombre");
        }

        if (venta.isEsFiado() && cliente == null) {
            throw new IllegalArgumentException("Una venta fiada requiere un clienteId");
        }

        venta.setDescripcion(request.getDescripcion());

        if (venta.isEsFiado()) {
            venta.setEstado(EstadoVenta.PENDIENTE);
            venta.setMetodoPago("FIADO");
        } else {
            venta.setEstado(EstadoVenta.PAGADA);
            String mp = request.getMetodoPago();
            if (mp == null || mp.isBlank()) {
                throw new IllegalArgumentException("Debe especificar el metodo de pago (EFECTIVO, YAPE, TARJETA)");
            }
            venta.setMetodoPago(mp.toUpperCase());
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Usuario usuario) {
            venta.setUsuario(usuario);
        }

        BigDecimal total = BigDecimal.ZERO;

        for (VentaRequest.ItemRequest itemReq : request.getItems()) {
            Producto producto = productoRepository.findById(itemReq.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado"));

            productoService.descontarStock(producto, itemReq.getCantidad(),
                    "Venta " + (venta.isEsFiado() ? "(fiado)" : ""));

            BigDecimal precioUnitario = itemReq.getPrecioUnitario() != null
                    ? itemReq.getPrecioUnitario()
                    : producto.getPrecio();

            VentaItem item = new VentaItem();
            item.setVenta(venta);
            item.setProducto(producto);
            item.setProductoNombre(itemReq.getProductoNombre() != null
                    ? itemReq.getProductoNombre()
                    : producto.getNombre());
            item.setCantidad(itemReq.getCantidad());
            item.setPrecioUnitario(precioUnitario);
            venta.getItems().add(item);

            total = total.add(precioUnitario.multiply(BigDecimal.valueOf(itemReq.getCantidad())));
        }

        venta.setNumeroVenta(ventaRepository.count() + 1);
        venta.setTotal(total);

        Venta guardada = ventaRepository.save(venta);

        if (venta.isEsFiado() && cliente != null) {
            cliente.setSaldoPendiente(cliente.getSaldoPendiente().add(total));
            clienteRepository.save(cliente);
        }

        return new VentaDetalleResponse(guardada, calcularSaldoPendiente(guardada));
    }

    @Transactional
    public VentaDetalleResponse registrarAbono(AbonoRequest request) {
        Venta venta = ventaRepository.findById(request.getVentaId())
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));

        if (venta.getEstado() == EstadoVenta.PAGADA) {
            throw new IllegalStateException("Esta venta ya esta pagada");
        }

        if (venta.getEstado() == EstadoVenta.ANULADO) {
            throw new IllegalStateException("Esta venta esta anulada");
        }

        BigDecimal saldoPendiente = calcularSaldoPendiente(venta);
        if (request.getMonto().compareTo(saldoPendiente) > 0) {
            throw new IllegalArgumentException("El abono supera el saldo pendiente (" + saldoPendiente + ")");
        }

        Abono abono = new Abono();
        abono.setVenta(venta);
        abono.setCliente(venta.getCliente());
        abono.setMonto(request.getMonto());
        abonoRepository.save(abono);
        venta.getAbonos().add(abono);

        if (venta.getCliente() != null) {
            Cliente cliente = venta.getCliente();
            BigDecimal nuevoSaldo = cliente.getSaldoPendiente().subtract(request.getMonto());
            cliente.setSaldoPendiente(nuevoSaldo.max(BigDecimal.ZERO));
            clienteRepository.save(cliente);
        }

        BigDecimal nuevoSaldo = saldoPendiente.subtract(request.getMonto());

        if (nuevoSaldo.compareTo(BigDecimal.ZERO) == 0) {
            venta.setEstado(EstadoVenta.PAGADA);
            if (venta.getCliente() != null) {
                Cliente cliente = venta.getCliente();
                cliente.setScoreConfianza(Math.min(100, cliente.getScoreConfianza() + 5));
                clienteRepository.save(cliente);
            }
        } else {
            venta.setEstado(EstadoVenta.PARCIAL);
        }

        Venta guardada = ventaRepository.save(venta);
        return new VentaDetalleResponse(guardada, calcularSaldoPendiente(guardada));
    }

    public BigDecimal calcularSaldoPendiente(Venta venta) {
        if (venta.getEstado() == EstadoVenta.PAGADA || venta.getEstado() == EstadoVenta.ANULADO) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalAbonado = venta.getAbonos().stream()
                .map(Abono::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return venta.getTotal().subtract(totalAbonado);
    }

    @Transactional
    public void eliminarVenta(UUID id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
        venta.setActivo(false);
        ventaRepository.save(venta);
    }

    @Transactional
    public VentaDetalleResponse anularVenta(UUID id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean esDueno = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_DUENO"));
        if (!esDueno) {
            throw new AccessDeniedException("Solo el dueño puede anular ventas");
        }

        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));

        if (venta.getEstado() == EstadoVenta.ANULADO) {
            throw new IllegalStateException("Esta venta ya esta anulada");
        }

        BigDecimal saldoPendiente = calcularSaldoPendiente(venta);

        if (venta.getCliente() != null && saldoPendiente.compareTo(BigDecimal.ZERO) > 0) {
            Cliente cliente = venta.getCliente();
            BigDecimal nuevoSaldo = cliente.getSaldoPendiente().subtract(saldoPendiente);
            cliente.setSaldoPendiente(nuevoSaldo.max(BigDecimal.ZERO));
            clienteRepository.save(cliente);
        }

        for (VentaItem item : venta.getItems()) {
            if (item.getProducto() != null) {
                productoService.registrarEntrada(item.getProducto().getId(), item.getCantidad(),
                        "Venta anulada " + (venta.getNumeroVenta() != null ? venta.getNumeroVenta() : ""));
            }
        }

        venta.setEstado(EstadoVenta.ANULADO);
        Venta guardada = ventaRepository.save(venta);
        return new VentaDetalleResponse(guardada, calcularSaldoPendiente(guardada));
    }

    public BigDecimal calcularDeudaTotalCliente(UUID clienteId) {
        List<Venta> pendientes = ventaRepository.findByClienteIdAndEstadoInAndActivoTrue(
                clienteId, List.of(EstadoVenta.PENDIENTE, EstadoVenta.PARCIAL));
        return pendientes.stream()
                .map(this::calcularSaldoPendiente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<VentaResumenResponse> listarPendientesDeCliente(UUID clienteId) {
        List<Venta> ventas = ventaRepository.findByClienteIdAndEstadoInAndActivoTrue(
                clienteId, List.of(EstadoVenta.PENDIENTE, EstadoVenta.PARCIAL));
        return ventas.stream()
                .map(v -> new VentaResumenResponse(v, calcularSaldoPendiente(v)))
                .toList();
    }

    public List<VentaResumenResponse> listarTodas() {
        return ventaRepository.findAllByActivoTrueOrderByFechaDesc().stream()
                .map(v -> new VentaResumenResponse(v, calcularSaldoPendiente(v)))
                .toList();
    }

    public List<VentaResumenResponse> listarPorCliente(UUID clienteId) {
        return ventaRepository.findByClienteIdAndActivoTrueOrderByFechaDesc(clienteId).stream()
                .map(v -> new VentaResumenResponse(v, calcularSaldoPendiente(v)))
                .toList();
    }

    public List<VentaResumenResponse> listarDiarias() {
        return ventaRepository.findByEsFiadoFalseAndActivoTrueOrderByFechaDesc().stream()
                .map(v -> new VentaResumenResponse(v, calcularSaldoPendiente(v)))
                .toList();
    }

    public List<VentaResumenResponse> listarFiadas() {
        return ventaRepository.findByEsFiadoTrueAndActivoTrueOrderByFechaDesc().stream()
                .map(v -> new VentaResumenResponse(v, calcularSaldoPendiente(v)))
                .toList();
    }

    public List<VentaResumenResponse> listarPendientes() {
        return ventaRepository.findByEstadoAndActivoTrue(EstadoVenta.PENDIENTE).stream()
                .map(v -> new VentaResumenResponse(v, calcularSaldoPendiente(v)))
                .toList();
    }

    public VentaDetalleResponse obtenerDetalle(UUID id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));
        return new VentaDetalleResponse(venta, calcularSaldoPendiente(venta));
    }

    @Transactional
    public VentaDetalleResponse pagarCompleto(UUID id) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Venta no encontrada"));

        if (venta.getEstado() == EstadoVenta.PAGADA) {
            throw new IllegalStateException("Esta venta ya esta pagada");
        }

        if (venta.getEstado() == EstadoVenta.ANULADO) {
            throw new IllegalStateException("Esta venta esta anulada");
        }

        BigDecimal saldoPendiente = calcularSaldoPendiente(venta);

        Abono abono = new Abono();
        abono.setVenta(venta);
        abono.setCliente(venta.getCliente());
        abono.setMonto(saldoPendiente);
        abonoRepository.save(abono);
        venta.getAbonos().add(abono);

        venta.setEstado(EstadoVenta.PAGADA);

        if (venta.getCliente() != null) {
            Cliente cliente = venta.getCliente();
            BigDecimal nuevoSaldo = cliente.getSaldoPendiente().subtract(saldoPendiente);
            cliente.setSaldoPendiente(nuevoSaldo.max(BigDecimal.ZERO));
            cliente.setScoreConfianza(Math.min(100, cliente.getScoreConfianza() + 5));
            clienteRepository.save(cliente);
        }

        Venta guardada = ventaRepository.save(venta);
        return new VentaDetalleResponse(guardada, calcularSaldoPendiente(guardada));
    }
}
