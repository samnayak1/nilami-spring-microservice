package com.nilami.catalogservice.services.serviceAbstractions;

import java.net.URL;


//TODO make this tie to an item

public interface FileUploadService {
    public URL generatePresignedUrl(String objectName, String objectId);
    public URL generateDownloadPresignedUrl(String objectKey);
}
