package org.example.klifurapp.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageStorageService {
    public String store(MultipartFile file) throws IOException;
    public Resource load(String filename) throws IOException;
    public void delete(String filename) throws IOException;
}
