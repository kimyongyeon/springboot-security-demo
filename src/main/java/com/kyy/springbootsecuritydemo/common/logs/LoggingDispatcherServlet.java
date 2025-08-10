package com.kyy.springbootsecuritydemo.common.logs;

import jakarta.servlet.http.*;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.DispatcherServlet;

public class LoggingDispatcherServlet extends DispatcherServlet {
    private static final Logger log = LoggerFactory.getLogger(LoggingDispatcherServlet.class);

    @Override
    protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        IndentMdc.push("servlet");
        try {
            log.info("enter servlet doDispatch");
            super.doDispatch(request, response);
            log.info("leave servlet doDispatch");
        } finally {
            IndentMdc.pop();
        }
    }
}
