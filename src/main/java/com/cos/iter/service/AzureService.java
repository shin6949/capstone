package com.cos.iter.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Log4j2
public class AzureService {
    @Value("${azure.connect-string}")
    private String connectString;

    @Value("${file.temp-upload-path}")
    private String tempPath;

    @Value("${azure.container-name.user-contents}")
    private String toUploadContainerName;

    // TODO: FileUploadService에서 filename 받아서 Upload 하도록 변경해야함.
    public boolean uploadToCloudAndReturnFileName(String imageFileName) throws IOException {
        final BlobContainerClient container = new BlobContainerClientBuilder()
                .connectionString(connectString)
                .containerName(toUploadContainerName)
                .buildClient();

        final BlobClient blob = container.getBlobClient(imageFileName);
        blob.uploadFromFile(tempPath + imageFileName);

        return true;
    }
}
