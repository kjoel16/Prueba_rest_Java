package com.mycompany.logwriter.interceptor;

import com.mycompany.logwriter.LogWriterLib;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Interceptor que registra en qué controller/método cae la petición
 * y cuándo termina de procesarse en el controller.
 */
public class LogInterceptor implements HandlerInterceptor {

    private static final String START_TIME_ATTR = "logwriter_handler_start";

    @Override
    public boolean preHandle(HttpServletRequest request,
                              HttpServletResponse response,
                              Object handler) {

        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());

        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            String controllerName = hm.getBeanType().getSimpleName();
            String methodName = hm.getMethod().getName();

            LogWriterLib.info(">>> Entrando a controller",
                    controllerName + "." + methodName + "()");
        }

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                            HttpServletResponse response,
                            Object handler,
                            ModelAndView modelAndView) {

        if (handler instanceof HandlerMethod) {
            HandlerMethod hm = (HandlerMethod) handler;
            String controllerName = hm.getBeanType().getSimpleName();
            String methodName = hm.getMethod().getName();

            Long startTime = (Long) request.getAttribute(START_TIME_ATTR);
            long duration = startTime != null ? System.currentTimeMillis() - startTime : -1;

            LogWriterLib.info("<<< Saliendo de controller",
                    controllerName + "." + methodName + "() - " + duration + " ms");
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Object handler,
                                 Exception ex) {
        if (ex != null) {
            LogWriterLib.error("Error no capturado en controller", ex);
        }
    }
}
