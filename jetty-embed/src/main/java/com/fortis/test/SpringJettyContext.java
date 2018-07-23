package com.fortis.test;

import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.session.SessionHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.resource.Resource;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;

public class SpringJettyContext implements JettyContext {

    private String contextPath = "/";

    private String servletPath = "/";

    private String resourcePath = "webapp";

    private boolean enableSessions = false;

    private String servletConfigLocation = "classpath:spring.xml";

    private String listenConfigLocation = "classpath:spring-servlet.xml";


    @Override
    public void setup(ContextHandlerCollection handlerCollection) {

        if (contextPath == null || servletPath == null || resourcePath == null )
            throw new IllegalArgumentException("contextPath==null||servletPath==null||resourcePath==null");

        ServletContextHandler handler = new ServletContextHandler(enableSessions ? ServletContextHandler.SESSIONS : ServletContextHandler.NO_SESSIONS);
        handler.setContextPath(contextPath);
        handler.setResourceBase(resourcePath);
        ServletHolder servlet = new ServletHolder(DispatcherServlet.class);
        servlet.setInitParameter("contextConfigLocation", servletConfigLocation);
        handler.addServlet(servlet, servletPath);
        handler.addEventListener(new ContextLoaderListener());
        handler.setInitParameter("contextConfigLocation", listenConfigLocation);
        handlerCollection.addHandler(handler);
    }


//    @Override
//    public void setup(ContextHandlerCollection handlerCollection) {
//        XmlWebApplicationContext servletApplicationContext = new XmlWebApplicationContext();
//        servletApplicationContext.setConfigLocation("classpath:spring-servlet.xml");
//
//        ServletHolder servlet = new ServletHolder(new DispatcherServlet(servletApplicationContext));
//        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
//        handler.setContextPath("/");
//        handler.addServlet(servlet, "/");
//        handler.setResourceBase(".");
//        handlerCollection.addHandler(handler);
//    }
}
