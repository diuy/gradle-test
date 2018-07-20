package com.fortis.test;

import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;
import java.util.*;
//args jetty/jetty-http.xml jetty/jetty-threadpool.xml jetty/jetty.xml
public class TestXml {
    public static void main(final String... args) throws Exception {
        Properties properties = null;

        // If no start.config properties, use clean slate
        if (properties == null) {
            // Add System Properties
            properties = new Properties();
            properties.putAll(System.getProperties());
        }

        // For all arguments, load properties
        for (String arg : args) {
            if (arg.indexOf('=') >= 0) {
                int i = arg.indexOf('=');
                properties.put(arg.substring(0, i), arg.substring(i + 1));
            } else if (arg.toLowerCase(Locale.ENGLISH).endsWith(".properties"))
                properties.load(Resource.newResource(arg).getInputStream());
        }

        ContextHandlerCollection handlerCollection = null;
        // For all arguments, parse XMLs
        XmlConfiguration last = null;
        List<Object> objects = new ArrayList<>(args.length);
        for (int i = 0; i < args.length; i++) {
            if (!args[i].toLowerCase(Locale.ENGLISH).endsWith(".properties") && (args[i].indexOf('=') < 0)) {
                XmlConfiguration configuration = new XmlConfiguration(Resource.newSystemResource(args[i]).getURI().toURL());
                if (last != null)
                    configuration.getIdMap().putAll(last.getIdMap());
                if (properties.size() > 0) {
                    Map<String, String> props = new HashMap<>();
                    for (Object key : properties.keySet()) {
                        props.put(key.toString(), String.valueOf(properties.get(key)));
                    }
                    configuration.getProperties().putAll(props);
                }

                Object obj = configuration.configure();
                handlerCollection =  (ContextHandlerCollection)configuration.getIdMap().get("Contexts");
                if (obj != null && !objects.contains(obj))
                    objects.add(obj);
                last = configuration;
            }
        }

        // For all objects created by XmlConfigurations, start them if they are lifecycles.
        XmlWebApplicationContext servletApplicationContext = new XmlWebApplicationContext();
        servletApplicationContext.setConfigLocation("classpath:spring-servlet.xml");

        ServletHolder servlet = new ServletHolder(new DispatcherServlet(servletApplicationContext));
        ServletContextHandler handler = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        handler.setContextPath("/");
        handler.addServlet(servlet, "/");
        handler.setResourceBase(".");
        handlerCollection.addHandler(handler);
        for (Object obj : objects) {
            if (obj instanceof LifeCycle) {
                LifeCycle lc = (LifeCycle) obj;
                if (!lc.isRunning())
                    lc.start();
            }
        }

    }
}
