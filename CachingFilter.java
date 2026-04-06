package com.mycompany.logwriter.config;

import com.mycompany.logwriter.LogWriterLib;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Filtro que captura la petición DESDE QUE ENTRA y también al salir.
 * Usa CachedBodyRequestWrapper para leer el body en la entrada
 * sin consumir el InputStream.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CachingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {

        // Generar ID único para rastrear esta petición
        String requestId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Wrappear request y response para poder leer/cachear el body
        CachedBodyRequestWrapper wrappedRequest = new CachedBodyRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        // Determinar el endpoint y configurar el contexto del hilo
        String endpoint = request.getRequestURI();
        LogWriterLib.setCurrentEndpoint(endpoint);
        LogWriterLib.setCurrentRequestId(requestId);

        long startTime = System.currentTimeMillis();

        try {
            // ============ LOG DE ENTRADA (ANTES de procesar) ============
            String requestBody = wrappedRequest.getCachedBody();
            String headers = extractHeaders(request);

            LogWriterLib.logRequestEntry(
                    request.getMethod(),
                    endpoint,
                    request.getQueryString(),
                    headers,
                    requestBody,
                    requestId
            );

            // ============ EJECUTAR LA PETICIÓN ============
            filterChain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            // ============ LOG DE SALIDA (DESPUÉS de procesar) ============
            long duration = System.currentTimeMillis() - startTime;

            String responseBody = new String(
                    wrappedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);

            LogWriterLib.logRequestExit(
                    request.getMethod(),
                    endpoint,
                    wrappedResponse.getStatus(),
                    responseBody,
                    duration,
                    requestId
            );

            // Copiar el body al response real (importante, si no el cliente no recibe nada)
            wrappedResponse.copyBodyToResponse();

            // Limpiar el ThreadLocal
            LogWriterLib.clear();
        }
    }

    private String extractHeaders(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .map(name -> "  " + name + ": " + request.getHeader(name))
                .collect(Collectors.joining("\n"));
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        // No filtrar recursos estáticos ni actuator
        return path.startsWith("/actuator")
                || path.startsWith("/swagger")
                || path.startsWith("/v3/api-docs")
                || path.endsWith(".css")
                || path.endsWith(".js")
                || path.endsWith(".ico");
    }
}
