package com.fortis.test;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.servlet.DispatcherServlet;

public class SpringMvc {
    public static void main(String[] args) throws Exception {
        Server server = new Server(8080);
        ServletHolder servlet = new ServletHolder(DispatcherServlet.class);
        servlet.setInitParameter("contextConfigLocation", "classpath:spring-servlet.xml");
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.setContextPath("/");
        handler.addServlet(servlet, "/");
        handler.addEventListener(new ContextLoaderListener());
        handler.setInitParameter("contextConfigLocation", "classpath:spring.xml");
        server.setHandler(handler);
        server.start();
        System.out.println("start...in 8080");
    }
}
