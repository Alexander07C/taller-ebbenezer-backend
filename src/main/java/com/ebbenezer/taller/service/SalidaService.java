package com.ebbenezer.taller.service;

import com.ebbenezer.taller.dto.AnularSalidaRequest;
import com.ebbenezer.taller.dto.SalidaRequest;
import com.ebbenezer.taller.dto.VentaDetalleResponse;
import com.ebbenezer.taller.model.Cliente;
import com.ebbenezer.taller.model.EstadoVenta;
import com.ebbenezer.taller.model.Producto;
import com.ebbenezer.taller.model.Usuario;
import com.ebbenezer.taller.model.Venta;
import com.ebbenezer.taller.model.VentaItem;
import com.ebbenezer.taller.repository.ClienteRepository;
import com.ebbenezer.taller.repository.ProductoRepository;
import com.ebbenezer.taller.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SalidaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;
    private final ProductoService productoService;
    private final VentaService ventaService;

    @Transactional
    public VentaDetalleResponse registrarSalida(SalidaRequest request) {
        BigDecimal montoServicio = request.getMontoServicio() != null
                ? request.getMontoServicio()
                : BigDecimal.ZERO;

        if (montoServicio.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El monto de servicio no puede ser negativo");
        }

        List<SalidaRequest.ItemRequest> productos =
                request.getProductos() != null ? request.getProductos() : List.of();

        if (productos.isEmpty() && montoServicio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Una salida debe tener al menos un producto o un monto de servicio mayor a 0");
        }

        List<Producto> catalogo = cargarCatalogo(productos);
        validarStock(catalogo, productos);

        Cliente cliente = null;
        if (request.getClienteId() != null) {
            cliente = clienteRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
        }

        String nombreMostrador = cliente == null && request.getNombreCliente() != null
                && !request.getNombreCliente().isBlank()
                ? request.getNombreCliente().trim()
                : null;

        boolean pagada = Boolean.TRUE.equals(request.getPagada());

        Venta venta = new Venta();
        venta.setEsSalida(true);
        venta.setMontoServicio(montoServicio);
        venta.setConcepto(request.getConcepto() != null && !request.getConcepto().isBlank()
                ? request.getConcepto().trim()
                : null);
        venta.setCliente(cliente);
        if (cliente == null) {
            venta.setNombreCliente(nombreMostrador != null ? nombreMostrador : "Mostrador");
        }
        venta.setEstado(pagada ? EstadoVenta.PAGADA : EstadoVenta.PENDIENTE);
        venta.setMetodoPago(pagada ? "EFECTIVO" : "FIADO");
        venta.setEsFiado(!pagada && cliente != null);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Usuario usuario) {
            venta.setUsuario(usuario);
        }

        BigDecimal subtotalProductos = BigDecimal.ZERO;
        int indice = 0;
        for (SalidaRequest.ItemRequest itemReq : productos) {
            Producto producto = catalogo.get(indice++);

            // El precio unitario siempre viene del catalogo del servidor.
            BigDecimal precioUnitario = producto.getPrecio();
            productoService.descontarStock(producto, itemReq.getCantidad(), "Salida de mecanico");

            VentaItem item = new VentaItem();
            item.setVenta(venta);
            item.setProducto(producto);
            item.setProductoNombre(producto.getNombre());
            item.setCantidad(itemReq.getCantidad());
            item.setPrecioUnitario(precioUnitario);
            venta.getItems().add(item);

            subtotalProductos = subtotalProductos.add(
                    precioUnitario.multiply(BigDecimal.valueOf(itemReq.getCantidad())));
        }

        BigDecimal total = montoServicio.add(subtotalProductos);
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El total de la salida debe ser mayor a 0");
        }

        venta.setTotal(total);
        venta.setNumeroVenta(siguienteNumeroVenta());

        Venta guardada = ventaRepository.save(venta);

        if (!pagada && cliente != null) {
            cliente.setSaldoPendiente(cliente.getSaldoPendiente().add(total));
            clienteRepository.save(cliente);
        }

        return new VentaDetalleResponse(guardada, ventaService.calcularSaldoPendiente(guardada));
    }

    @Transactional
    public VentaDetalleResponse anularSalida(UUID id, AnularSalidaRequest request) {
        Venta venta = ventaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Salida no encontrada"));

        if (!venta.isEsSalida()) {
            throw new IllegalArgumentException("El registro indicado no es una salida");
        }

        if (venta.getEstado() == EstadoVenta.ANULADO) {
            throw new IllegalStateException("Esta salida ya esta anulada");
        }

        String motivo = request.getMotivoAnulacion() != null ? request.getMotivoAnulacion().trim() : "";
        if (motivo.isEmpty()) {
            throw new IllegalArgumentException("Debe indicar el motivo de la anulacion");
        }
        venta.setMotivoAnulacion(motivo);

        BigDecimal saldoPendiente = ventaService.calcularSaldoPendiente(venta);

        if (venta.getCliente() != null && saldoPendiente.compareTo(BigDecimal.ZERO) > 0) {
            Cliente cliente = venta.getCliente();
            BigDecimal nuevoSaldo = cliente.getSaldoPendiente().subtract(saldoPendiente);
            cliente.setSaldoPendiente(nuevoSaldo.max(BigDecimal.ZERO));
            clienteRepository.save(cliente);
        }

        for (VentaItem item : venta.getItems()) {
            if (item.getProducto() != null) {
                productoService.registrarEntrada(item.getProducto().getId(), item.getCantidad(),
                        "Salida anulada " + (venta.getNumeroVenta() != null ? venta.getNumeroVenta() : ""));
            }
        }

        venta.setEstado(EstadoVenta.ANULADO);
        venta.setActivo(false);

        Venta guardada = ventaRepository.save(venta);
        return new VentaDetalleResponse(guardada, ventaService.calcularSaldoPendiente(guardada));
    }

    private List<Producto> cargarCatalogo(List<SalidaRequest.ItemRequest> productos) {
        List<Producto> catalogo = new ArrayList<>(productos.size());
        for (SalidaRequest.ItemRequest itemReq : productos) {
            if (itemReq.getCantidad() == null || itemReq.getCantidad() <= 0) {
                throw new IllegalArgumentException("La cantidad de cada producto debe ser mayor a 0");
            }
            catalogo.add(productoRepository.findById(itemReq.getProductoId())
                    .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado")));
        }
        return catalogo;
    }

    private void validarStock(List<Producto> catalogo, List<SalidaRequest.ItemRequest> productos) {
        Map<UUID, Integer> faltantes = new LinkedHashMap<>();

        for (int i = 0; i < productos.size(); i++) {
            Producto producto = catalogo.get(i);
            int cantidad = productos.get(i).getCantidad();
            Integer acumulado = faltantes.get(producto.getId());
            int requerida = (acumulado != null ? acumulado : 0) + cantidad;
            if (producto.getStockActual() < requerida) {
                faltantes.put(producto.getId(), requerida);
            }
        }

        if (faltantes.isEmpty()) {
            return;
        }

        List<String> mensajes = new ArrayList<>();
        for (Map.Entry<UUID, Integer> entry : faltantes.entrySet()) {
            Producto producto = productoRepository.findById(entry.getKey()).orElse(null);
            String nombre = producto != null ? producto.getNombre() : "Producto";
            int disponible = producto != null ? producto.getStockActual() : 0;
            mensajes.add("No hay stock suficiente de " + nombre + " (disponible: " + disponible + ")");
        }

        throw new IllegalArgumentException(String.join("; ", mensajes));
    }

    private long siguienteNumeroVenta() {
        List<Long> numeros = new ArrayList<>();
        for (Venta existente : ventaRepository.findAll()) {
            if (existente.getNumeroVenta() != null) {
                numeros.add(existente.getNumeroVenta());
            }
        }
        return numeros.stream().mapToLong(Long::longValue).max().orElse(0L) + 1;
    }
}