package com.cos.iter.service;

import com.cos.iter.enums.FileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

@RequiredArgsConstructor
@Service
@Log4j2
public class OnPremSaveService {
    @Value("${file.temp-upload-path}")
    private String tempPath;

    @Async
    public void saveFile(String fileName, FileType fileType) throws IOException {
        final File tempFile = new File(tempPath + fileName);
        final File toStoreFile = new File(fileType.getOnPremiseLocation() + fileName);

        FileInputStream fileInputStream = new FileInputStream(tempFile);
        FileOutputStream fileOutputStream = new FileOutputStream(toStoreFile);

        byte[] buf = new byte[1024];
        int readData;
        while ((readData = fileInputStream.read(buf)) > 0) {
            fileOutputStream.write(buf, 0, readData);
        }

        fileInputStream.close();
        fileOutputStream.close();
    }
}
