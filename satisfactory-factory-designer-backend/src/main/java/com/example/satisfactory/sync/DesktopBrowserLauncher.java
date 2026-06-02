package com.example.satisfactory.sync;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.awt.Desktop;
import java.net.URI;

@Component
@Profile("desktop")
public class DesktopBrowserLauncher implements ApplicationRunner {
    @Value("${server.port:8080}")
    private int serverPort;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!Desktop.isDesktopSupported()) {
            return;
        }
        Desktop.getDesktop().browse(new URI("http://127.0.0.1:" + serverPort));
    }
}
