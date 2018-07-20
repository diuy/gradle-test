package com.fortis.test;

import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.xml.XmlConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.StringUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

import static org.eclipse.jetty.util.Loader.getResource;

public class JettyServer {
    private JettyContext jettyContext;
    private static String defaultProperty = "classpath:jetty.properties";
    private static String defaultXmlDir = "classpath:jetty/";
    private List<Object> objects;
    private ContextHandlerCollection handlerCollection;
    private PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();


    public JettyServer(JettyContext jettyContext) {
        this.jettyContext = jettyContext;
    }

    public JettyServer() {

    }

    public void start() {
        if (objects != null)
            return;
        if (!config())
            return;

        if (handlerCollection == null) {
            System.out.println("Contexts not found");
        } else if (jettyContext != null) {
            jettyContext.setup(handlerCollection);
        }
        if (!doStart()) {
            doStop();
        }
    }

    private boolean config() {
        Properties properties = initProperties();

        String dependence = properties.getProperty("jetty.model.dependence");
        if (dependence == null || dependence.isEmpty()) {
            System.out.println("jetty.model.dependence has not config");
            return false;
        }
        String[] models = StringUtils.tokenizeToStringArray(dependence, ",");
        List<URL> urls = new ArrayList<>();
        for (String model:models){
            if(model!=null && !model.isEmpty()){
                URL url = getModelUrl(model);
                if(url!=null)
                    urls.add(url);
                else {
                    System.out.println("cannot find model :" + model);
                    return false;
                }
            }
        }
        if (urls.isEmpty()) {
            System.out.println("no xml");
            return false;
        }
        XmlConfiguration last = null;
        objects = new ArrayList<>(urls.size());
        try {
            for (URL url : urls) {
                XmlConfiguration configuration = new XmlConfiguration(url);
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
                handlerCollection = (ContextHandlerCollection) configuration.getIdMap().get("Contexts");
                if (obj != null && !objects.contains(obj))
                    objects.add(obj);
                last = configuration;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("load xml failed");
        }
        return false;
    }

    private boolean doStart() {
        try {
            for (Object obj : objects) {
                if (obj instanceof LifeCycle) {
                    LifeCycle lc = (LifeCycle) obj;
                    if (!lc.isRunning()) {
                        lc.start();
                    }
                }
            }
            return true;
        } catch (Exception e) {
            System.out.println("start failed");
            e.printStackTrace();
        }
        return false;
    }

    private void doStop() {
        ListIterator<Object> iterator = objects.listIterator(objects.size());
        while (iterator.hasPrevious()) {
            Object obj = iterator.previous();
            if (obj instanceof LifeCycle) {
                LifeCycle lc = (LifeCycle) obj;
                if (lc.isRunning()) {
                    try {
                        lc.stop();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
    }

    public void stop() {
        if (objects == null)
            return;
        doStop();
    }

    private Properties initProperties() {
        Properties properties = new Properties();

        Resource resource = resourcePatternResolver.getResource(defaultProperty);
        if (resource.exists()) {
            InputStream inputStream = null;
            try {
                inputStream = resource.getInputStream();
                properties.load(inputStream);
            } catch (Exception e) {
                System.out.println("load resource failed");
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException ignore) {
                    }
                }
            }
        }
        properties.putAll(System.getProperties());
        return properties;
    }

    private URL getModelUrl(String name) {
        try {
            return resourcePatternResolver.getResource(defaultXmlDir + name + ".xml").getURL();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


}
