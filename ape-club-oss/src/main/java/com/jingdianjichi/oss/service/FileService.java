package com.jingdianjichi.oss.service;

import com.jingdianjichi.oss.adapter.StorageAdapter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.UUID;

/**
 * 文件存储service
 *
 * @author: ChickenWing
 * @date: 2023/10/14
 */
@Service
public class FileService {

    private final StorageAdapter storageAdapter;

    public FileService(StorageAdapter storageAdapter) {
        this.storageAdapter = storageAdapter;
    }

    /**
     * 列出所有桶
     */
    public List<String> getAllBucket() {
        return storageAdapter.getAllBucket();
    }

    /**
     * 获取文件路径
     */
    public String getUrl(String bucketName,String objectName) {
        return storageAdapter.getUrl(bucketName,objectName);
    }

    /**
     * 上传文件
     */
    public String uploadFile(MultipartFile uploadFile, String bucket, String objectName){
        String fileName = buildFileName(uploadFile);
        String fullObjectName = buildObjectName(objectName, fileName);
        storageAdapter.uploadFile(uploadFile,bucket,fullObjectName);
        return buildPreviewUrl(bucket, fullObjectName);
    }

    public InputStream downLoad(String bucket, String objectName) {
        return storageAdapter.downLoad(bucket, objectName);
    }

    public String buildPreviewUrl(String bucket, String objectName) {
        try {
            return "/oss/preview?bucket=" + URLEncoder.encode(bucket, "UTF-8")
                    + "&objectName=" + URLEncoder.encode(objectName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    private String buildObjectName(String objectName, String fileName) {
        return StringUtils.isBlank(objectName) ? fileName : objectName + "/" + fileName;
    }

    private String buildFileName(MultipartFile uploadFile) {
        String originalFilename = uploadFile != null ? uploadFile.getOriginalFilename() : null;
        if (StringUtils.isNotBlank(originalFilename)) {
            return originalFilename;
        }
        return "upload-" + UUID.randomUUID() + ".bin";
    }
}
