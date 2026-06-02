package com.example.satisfactory.sync;

import com.example.satisfactory.seed.RecipeSeedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("desktop")
@Order(1)
public class DesktopTransportSeedRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DesktopTransportSeedRunner.class);

    private final RecipeSeedService seedService;

    public DesktopTransportSeedRunner(RecipeSeedService seedService) {
        this.seedService = seedService;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            int count = seedService.seedTransportLevels("data/seed/transport_levels.json");
            log.info("Desktop transport_levels seed completed, {} entries written.", count);
        } catch (Exception ex) {
            log.error("Failed to seed transport_levels on desktop startup: {}", ex.getMessage(), ex);
        }
    }
}
