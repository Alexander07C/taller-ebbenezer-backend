package com.ebbenezer.taller.controller;

import com.ebbenezer.taller.dto.DashboardResponse;
import com.ebbenezer.taller.dto.ResumenMetodoResponse;
import com.ebbenezer.taller.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/ventas")
    public DashboardResponse ventas(@RequestParam(defaultValue = "diario") String periodo) {
        return dashboardService.obtenerVentas(periodo);
    }

    @GetMapping("/resumen")
    public Map<String, Object> resumen() {
        Map<String, Object> resumen = new LinkedHashMap<>();
        resumen.put("ventas", dashboardService.obtenerVentas("diario"));
        resumen.put("porMetodo", dashboardService.resumenPorMetodo());
        return resumen;
    }

    @GetMapping("/resumen-por-metodo")
    public List<ResumenMetodoResponse> resumenPorMetodo() {
        return dashboardService.resumenPorMetodo();
    }
}
