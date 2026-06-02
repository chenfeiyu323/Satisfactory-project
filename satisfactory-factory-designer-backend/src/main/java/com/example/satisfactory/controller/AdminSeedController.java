package com.example.satisfactory.controller;

import com.example.satisfactory.dto.MessageResponse;
import com.example.satisfactory.seed.RecipeSeedService;
import com.example.satisfactory.seed.DocsJsonImportService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/seed")
public class AdminSeedController {
    private final RecipeSeedService seedService;
    private final DocsJsonImportService docsJsonImportService;

    public AdminSeedController(RecipeSeedService seedService, DocsJsonImportService docsJsonImportService) {
        this.seedService = seedService;
        this.docsJsonImportService = docsJsonImportService;
    }

    @PostMapping("/all")
    public MessageResponse seedAll() {
        return seedService.seedAll();
    }

    @PostMapping(value = "/docs-json", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MessageResponse seedDocsJson(@RequestParam("file") MultipartFile file,
                                        @RequestParam(value = "gameVersion", required = false) String gameVersion) {
        return docsJsonImportService.importDocsJson(file, gameVersion);
    }
}
