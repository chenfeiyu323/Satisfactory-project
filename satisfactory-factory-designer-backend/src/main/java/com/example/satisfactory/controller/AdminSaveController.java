package com.example.satisfactory.controller;

import com.example.satisfactory.dto.MessageResponse;
import com.example.satisfactory.sync.SaveSyncService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/admin/save")
public class AdminSaveController {
    private final SaveSyncService saveSyncService;
    private final ObjectMapper objectMapper;

    public AdminSaveController(SaveSyncService saveSyncService, ObjectMapper objectMapper) {
        this.saveSyncService = saveSyncService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/export")
    public ResponseEntity<String> exportSave() throws Exception {
        String body = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(saveSyncService.exportSave());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=satisfactory-save.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MessageResponse importSave(@RequestParam("file") MultipartFile file,
                                      @RequestParam(value = "overwrite", defaultValue = "true") boolean overwrite) throws Exception {
        SaveSyncService.SaveFile saveFile = objectMapper.readValue(file.getInputStream(), SaveSyncService.SaveFile.class);
        return saveSyncService.importSave(saveFile, overwrite);
    }
}
