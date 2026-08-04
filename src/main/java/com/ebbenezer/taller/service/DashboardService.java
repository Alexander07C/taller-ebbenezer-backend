package com.ebbenezer.taller.service;

import com.ebbenezer.taller.dto.DashboardResponse;
import com.ebbenezer.taller.dto.ResumenMetodoResponse;
import com.ebbenezer.taller.model.EstadoVenta;
import com.ebbenezer.taller.model.Venta;
import com.ebbenezer.taller.repository.VentaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final VentaRepository ventaRepository;

    public DashboardResponse obtenerVentas(String periodo) {
        LocalDateTime inicio = switch (periodo) {
            case "diario" -> LocalDate.now().atStartOfDay();
            case "semanal" -> LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
            case "mensual" -> LocalDate.now().withDayOfMonth(1).atStartOfDay();
            case "anual" -> LocalDate.now().withDayOfYear(1).atStartOfDay();
            default -> throw new IllegalArgumentException("Periodo invalido: " + periodo);
        };
        LocalDateTime fin = LocalDateTime.now();

        List<Venta> ventas = ventaRepository.findByFechaBetweenAndActivoTrueOrderByFechaAsc(inicio, fin).stream()
                .filter(v -> v.getEstado() != EstadoVenta.ANULADO)
                .toList();
        List<DashboardResponse.DashboardItem> detalle = agruparPorPeriodo(ventas, periodo);

        BigDecimal totalVentas = ventas.stream()
                .map(Venta::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new DashboardResponse(periodo, totalVentas, ventas.size(), detalle);
    }

    private List<DashboardResponse.DashboardItem> agruparPorPeriodo(List<Venta> ventas, String periodo) {
        Map<String, List<Venta>> agrupadas = switch (periodo) {
            case "diario" -> ventas.stream().collect(Collectors.groupingBy(
                    v -> v.getFecha().toLocalDate().toString()));
            case "semanal" -> ventas.stream().collect(Collectors.groupingBy(
                    v -> v.getFecha().toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toString()));
            case "mensual" -> ventas.stream().collect(Collectors.groupingBy(
                    v -> v.getFecha().getYear() + "-" + String.format("%02d", v.getFecha().getMonthValue())));
            case "anual" -> ventas.stream().collect(Collectors.groupingBy(
                    v -> String.valueOf(v.getFecha().getYear())));
            default -> throw new IllegalArgumentException("Periodo invalido: " + periodo);
        };

        return agrupadas.entrySet().stream()
                .map(e -> {
                    BigDecimal total = e.getValue().stream()
                            .map(Venta::getTotal)
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    return new DashboardResponse.DashboardItem(e.getKey(), total, e.getValue().size());
                })
                .sorted(Comparator.comparing(DashboardResponse.DashboardItem::getLabel))
                .toList();
    }

    public List<ResumenMetodoResponse> resumenPorMetodo() {
        List<Venta> ventas = ventaRepository.findByActivoTrue().stream()
                .filter(v -> v.getEstado() != EstadoVenta.ANULADO)
                .toList();
        return ventas.stream()
                .collect(Collectors.groupingBy(
                        v -> {
                            String mp = v.getMetodoPago();
                            return mp != null ? mp.toUpperCase() : "SIN_ASIGNAR";
                        },
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                lista -> {
                                    BigDecimal total = lista.stream()
                                            .map(Venta::getTotal)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    return new ResumenMetodoResponse(
                                            lista.get(0).getMetodoPago() != null
                                                    ? lista.get(0).getMetodoPago().toUpperCase()
                                                    : "SIN_ASIGNAR",
                                            total,
                                            lista.size()
                                    );
                                }
                        )
                ))
                .values().stream()
                .sorted(Comparator.comparing(ResumenMetodoResponse::getTotal).reversed())
                .toList();
    }
}
