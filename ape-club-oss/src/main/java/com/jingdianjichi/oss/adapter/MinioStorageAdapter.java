package com.jingdianjichi.oss.adapter;

import com.jingdianjichi.oss.entity.FileInfo;
import com.jingdianjichi.oss.util.MinioUtil;
import org.apache.commons.lang3.StringUtils;
import lombok.SneakyThrows;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * minioIO存储适配器
 *
 * @author: ChickenWing
 * @date: 2023/10/14
 */
public class MinioStorageAdapter implements StorageAdapter {

    private final MinioUtil minioUtil;

    private final String url;

    public MinioStorageAdapter(MinioUtil minioUtil, String url) {
        this.minioUtil = minioUtil;
        this.url = url;
    }

    @Override
    @SneakyThrows
    public void createBucket(String bucket) {
        minioUtil.createBucket(bucket);
    }

    @Override
    @SneakyThrows
    public void uploadFile(MultipartFile uploadFile, String bucket, String objectName) {
        minioUtil.createBucket(bucket);
        String fileName = StringUtils.defaultIfBlank(uploadFile.getOriginalFilename(), "upload-file");
        String fullObjectName = StringUtils.isBlank(objectName) ? fileName : objectName;
        minioUtil.uploadFile(uploadFile.getInputStream(), bucket, fullObjectName);
    }

    @Override
    @SneakyThrows
    public List<String> getAllBucket() {
        return minioUtil.getAllBucket();
    }

    @Override
    @SneakyThrows
    public List<FileInfo> getAllFile(String bucket) {
        return minioUtil.getAllFile(bucket);
    }

    @Override
    @SneakyThrows
    public InputStream downLoad(String bucket, String objectName) {
        return minioUtil.downLoad(bucket, objectName);
    }

    @Override
    @SneakyThrows
    public void deleteBucket(String bucket) {
        minioUtil.deleteBucket(bucket);
    }

    @Override
    @SneakyThrows
    public void deleteObject(String bucket, String objectName) {
        minioUtil.deleteObject(bucket, objectName);
    }

    @Override
    @SneakyThrows
    public String getUrl(String bucket, String objectName) {
        return url + "/" + bucket + "/" + objectName;
    }

}
