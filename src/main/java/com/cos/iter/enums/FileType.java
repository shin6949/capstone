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
        final String onPremiseRootPath = System.getenv("onpremise.store-path");

        this.azureContainerName = azureContainerName;
        this.onPremiseLocation = onPremiseLocation + onPremiseRootPath + "/";
    }
}
