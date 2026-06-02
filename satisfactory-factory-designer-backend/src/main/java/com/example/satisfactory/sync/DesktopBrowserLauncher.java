package com.example.satisfactory.sync;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.swing.JOptionPane;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

@Component
@Profile("desktop")
public class DesktopBrowserLauncher implements ApplicationRunner {
    @Value("${server.port:8080}")
    private int serverPort;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        URI uri = new URI("http://127.0.0.1:" + serverPort);
        try {
            openBrowser(uri);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    null,
                    "程序已经启动，但浏览器未能自动打开。\n请手动访问：" + uri + "\n\n原因：" + ex.getMessage(),
                    "Satisfactory Factory Designer",
                    JOptionPane.INFORMATION_MESSAGE
            );
        }
    }

    private void openBrowser(URI uri) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(uri);
            return;
        }

        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String url = uri.toString();
        if (osName.contains("win")) {
            new ProcessBuilder("cmd", "/c", "start", "", url).start();
            return;
        }
        if (osName.contains("mac")) {
            new ProcessBuilder("open", url).start();
            return;
        }

        new ProcessBuilder("xdg-open", url).start();
    }
}
