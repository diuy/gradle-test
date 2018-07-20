package com.fortis.test.service;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

@Service
@DependsOn("testServer")
public class JettyServer implements InitializingBean, DisposableBean, ApplicationContextAware {
    private ApplicationContext applicationContext;
    private Server server;

    @Autowired
    TestServer testServer;


    @Override
    public void afterPropertiesSet() throws Exception {
        XmlWebApplicationContext servletApplicationContext = new XmlWebApplicationContext();
        servletApplicationContext.setConfigLocation("classpath:spring-servlet.xml");
        servletApplicationContext.setParent(this.applicationContext);

       ServletHolder servlet = new ServletHolder(new DispatcherServlet(servletApplicationContext));
        // ServletHolder servlet = new ServletHolder(new DefaultServlet());

        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.setContextPath("/");
        handler.addServlet(servlet, "/");
        handler.setResourceBase("webapp"); //设置静态资源目录
      //  handler.setWelcomeFiles(new String[]{"index.html"});
        server = new Server(8080);
        server.setHandler(handler);
        server.start();
        System.out.println("JettyServer afterPropertiesSet");
    }

    @Override
    public void destroy() throws Exception {
        server.stop();
        System.out.println("JettyServer destroy");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
