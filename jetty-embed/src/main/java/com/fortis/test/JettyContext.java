package com.fortis.test;

import org.eclipse.jetty.server.handler.ContextHandlerCollection;

public interface JettyContext {
    void setup(ContextHandlerCollection handlerCollection);
}
