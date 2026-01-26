/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.demo.apias400.service;

import com.demo.apias400.dto.ConsultaResponse;
import org.springframework.stereotype.Service;

@Service
public class ConsultaService {

    public ConsultaResponse procesarTrama(String trama) {

        ConsultaResponse response = new ConsultaResponse();

        if (trama == null || !trama.contains("=")) {
            response.setCodigo("99");
            response.setMensaje("Trama inválida");
            return response;
        }

        // Ejemplo: CTA=1234567890
        String cuenta = trama.split("=")[1];

        response.setCodigo("00");
        response.setMensaje("Trama procesada correctamente");
        response.setCuenta(cuenta);

        return response;
    }
}
