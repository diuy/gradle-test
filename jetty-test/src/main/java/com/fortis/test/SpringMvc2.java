package com.fortis.test;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.ConfigurableWebApplicationContext;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@SuppressWarnings("All")
public class SpringMvc2 {
    public static void main(String[] args) throws Exception {

        XmlWebApplicationContext applicationContext = new XmlWebApplicationContext();
        applicationContext.setConfigLocation("classpath:spring.xml");
        Server server = new Server(8080);
        ServletHolder servlet = new ServletHolder(DispatcherServlet.class);
        servlet.setInitParameter("contextConfigLocation", "classpath:spring-servlet.xml");
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.setContextPath("/");
        handler.addServlet(servlet, "/");
        handler.addEventListener(new ContextLoaderListener(applicationContext));
       // handler.setInitParameter("contextConfigLocation", "classpath:spring.xml");
        handler.setResourceBase("."); //设置静态资源目录
        server.setHandler(handler);
        server.start();
        System.out.println("start...in 8080");
    }
}
