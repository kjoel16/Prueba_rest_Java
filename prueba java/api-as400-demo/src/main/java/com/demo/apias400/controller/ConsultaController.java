/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.demo.apias400.controller;

import com.demo.apias400.dto.ConsultaRequest;
import com.demo.apias400.dto.ConsultaResponse;
import com.demo.apias400.service.ConsultaService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consulta")
public class ConsultaController {

    private final ConsultaService service;

    public ConsultaController(ConsultaService service) {
        this.service = service;
    }

    @PostMapping
    public ConsultaResponse consultar(@RequestBody ConsultaRequest request) {
        return service.procesarTrama(request.getTrama());
    }
}

