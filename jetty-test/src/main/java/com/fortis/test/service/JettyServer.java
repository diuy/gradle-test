package com.fortis.test.service;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@Service
public class JettyServer implements InitializingBean, DisposableBean, ApplicationContextAware {
    private ApplicationContext applicationContext;
    private Server server;


    @Override
    public void afterPropertiesSet() throws Exception {
        XmlWebApplicationContext servletApplicationContext = new XmlWebApplicationContext();
        servletApplicationContext.setConfigLocation("classpath:spring-servlet.xml");
        servletApplicationContext.setParent(this.applicationContext);

        ServletHolder servlet = new ServletHolder(new DispatcherServlet(servletApplicationContext));

        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.setContextPath("/");
        handler.addServlet(servlet, "/");
        handler.setResourceBase("."); //设置静态资源目录

        server = new Server(8080);
        server.setHandler(handler);
        server.start();
    }

    @Override
    public void destroy() throws Exception {
        server.stop();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
