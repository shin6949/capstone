package com.cos.iter.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class AzureService {
    @Value("${azure.connect-string?:}")
    private String connectString;

    @Value("${file.temp-upload-path}")
    private String tempPath;

    public void uploadFile(String imageFileName, String containerName) {
        final BlobContainerClient container = new BlobContainerClientBuilder()
                .connectionString(connectString)
                .containerName(containerName)
                .buildClient();

        final BlobClient blob = container.getBlobClient(imageFileName);
        blob.uploadFromFile(tempPath + imageFileName);
    }
}
