package com.cos.iter.enums;

import lombok.Getter;

@Getter
public enum FileType {
    STATIC_CONTENT(System.getenv("azure.container-name.static-resources"), "static"),
    USER_PROFILE(System.getenv("azure.container-name.user-profiles"), "profile"),
    USER_CONTENT(System.getenv("azure.container-name.user-contents"), "content");

    private final String azureContainerName;
    private final String onPremiseLocation;

    FileType(String azureContainerName, String onPremiseLocation) {
        this.azureContainerName = azureContainerName;
        this.onPremiseLocation = onPremiseLocation + "/";
    }

    public String getUrl(String filename) {
        final String fileUploadMethod = System.getenv("file.upload.method");

        if(fileUploadMethod.equals("ONPREMISE")) {
            return System.getenv("base.url") + "/" + getOnPremiseLocation() + "/" + filename;
        } else if(fileUploadMethod.equals("AZURE")) {
            return System.getenv("azure.blob.url") + "/" + getAzureContainerName() + "/" + filename;
        }

        return null;
    }
}
