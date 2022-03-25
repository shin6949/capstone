package com.cos.iter.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Log4j2
@RequiredArgsConstructor
public class FileUploadService {
    @Value("${file.upload.method}")
    private String fileUploadMethod;

    @Value("${file.path}")
    private String tempFilePath;

    private final AzureService azureService;

    public boolean uploadFile(MultipartFile file) throws IOException {
        final UUID uuid = UUID.randomUUID();
        final String imageFilename = uuid + "_" + file.getOriginalFilename();
        final Path imageFilepath = Paths.get(tempFilePath + imageFilename);

        // Temp Save
        try {
            Files.write(imageFilepath, file.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // METHOD에 따라서 분기
        if(fileUploadMethod.equals("AZUREBLOB")) {
            azureService.uploadToCloudAndReturnFileName(file, "test");
        }

        // 결과에 무관하게 폐기 (실패 시에는 실패했다는 메시지가 Return 되므로)
        try {
            Files.delete(imageFilepath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }
}
