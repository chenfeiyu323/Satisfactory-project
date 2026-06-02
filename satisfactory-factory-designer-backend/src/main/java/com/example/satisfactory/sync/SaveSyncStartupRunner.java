package com.example.satisfactory.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;

import java.nio.file.Files;
import java.nio.file.Path;

@Component
@Order(1)
public class SaveSyncStartupRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(SaveSyncStartupRunner.class);

    private final SaveSyncService saveSyncService;

    @Value("${app.sync.auto-import-on-startup:false}")
    private boolean autoImportOnStartup;

    @Value("${app.sync.overwrite-on-import:true}")
    private boolean overwriteOnImport;

    @Value("${app.sync.save-file-path:}")
    private String saveFilePath;

    public SaveSyncStartupRunner(SaveSyncService saveSyncService) {
        this.saveSyncService = saveSyncService;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!autoImportOnStartup) {
            return;
        }
        if (saveFilePath == null || saveFilePath.isBlank()) {
            log.info("Save sync startup import is enabled, but save file path is empty. Skipped.");
            return;
        }

        Path path = Path.of(saveFilePath);
        if (!Files.exists(path)) {
            log.info("Save sync file not found at {}. Skipped startup import.", path);
            return;
        }

        try {
            saveSyncService.importFromFile(path, overwriteOnImport);
            log.info("Startup save import finished from {} (overwrite={}).", path, overwriteOnImport);
        } catch (Exception ex) {
            log.error("Startup save import failed from {}: {}", path, ex.getMessage(), ex);
        }
    }
}
