package com.agm.agrimitra.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {

    String uploadFile(MultipartFile file, Long fieldId);
}
