package com.example.satisfactory.sync;

import com.example.satisfactory.seed.DocsJsonImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
@Profile("desktop")
@Order(0)
public class DesktopCatalogImportRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DesktopCatalogImportRunner.class);

    private final DocsJsonImportService docsJsonImportService;

    @Value("${app.catalog.import-on-startup:true}")
    private boolean importOnStartup;

    @Value("${app.catalog.file-path:}")
    private String catalogFilePath;

    @Value("${app.catalog.game-version:zh-Hans}")
    private String catalogGameVersion;

    public DesktopCatalogImportRunner(DocsJsonImportService docsJsonImportService) {
        this.docsJsonImportService = docsJsonImportService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!importOnStartup) {
            return;
        }

        Path path = resolveCatalogPath();
        if (path == null) {
            log.info("Desktop catalog file was not found in any expected location. Skipped startup import.");
            return;
        }

        try {
            docsJsonImportService.importDocsJson(path, catalogGameVersion);
            log.info("Desktop catalog import finished from {}.", path);
        } catch (Exception ex) {
            log.error("Desktop catalog import failed from {}: {}", path, ex.getMessage(), ex);
        }
    }

    private Path resolveCatalogPath() {
        List<Path> candidates = new ArrayList<>();
        if (catalogFilePath != null && !catalogFilePath.isBlank()) {
            candidates.add(Path.of(catalogFilePath));
        }
        candidates.add(Path.of("./shared/zh-Hans.json"));
        candidates.add(Path.of("../shared/zh-Hans.json"));
        candidates.add(Path.of("./zh-Hans.json"));
        candidates.add(Path.of("../zh-Hans.json"));

        for (Path candidate : candidates) {
            if (Files.exists(candidate)) {
                return candidate;
            }
        }

        return null;
    }
}
