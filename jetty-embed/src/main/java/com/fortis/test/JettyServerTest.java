package com.fortis.test;

import java.io.IOException;

public class JettyServerTest {
    public static void main(String[] args) throws IOException {
        JettyServer jettyServer = new JettyServer(new SpringJettyContext());
        jettyServer.start();
        System.in.read();
        jettyServer.stop();
    }
}
