package com.mycompany.logwriter.config;

import com.mycompany.logwriter.LogWriterLib;
import com.mycompany.logwriter.interceptor.LogInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.PostConstruct;

/**
 * Auto-configuración de la librería de logs.
 * 
 * Propiedades configurables en application.properties:
 *   logwriter.base-path=C:/logs          (ruta base)
 *   logwriter.project-name=MiProyecto    (nombre del proyecto)
 */
@Configuration
public class LogAutoConfiguration implements WebMvcConfigurer {

    @Value("${logwriter.base-path:C:/logs}")
    private String basePath;

    @Value("${logwriter.project-name:MiProyecto}")
    private String projectName;

    @PostConstruct
    public void init() {
        LogWriterLib.setBasePath(basePath);
        LogWriterLib.setProjectName(projectName);
    }

    @Bean
    @ConditionalOnMissingBean
    public CachingFilter cachingFilter() {
        return new CachingFilter();
    }

    @Bean
    public FilterRegistrationBean<CachingFilter> cachingFilterRegistration(CachingFilter filter) {
        FilterRegistrationBean<CachingFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        registration.addUrlPatterns("/*");
        return registration;
    }

    @Bean
    @ConditionalOnMissingBean
    public LogInterceptor logInterceptor() {
        return new LogInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(logInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/actuator/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                );
    }
}
