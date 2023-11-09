package com.example.videoupload.service;

import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedFileDownload;
import software.amazon.awssdk.transfer.s3.model.CompletedFileUpload;
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest;
import software.amazon.awssdk.transfer.s3.model.FileDownload;
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest;
import software.amazon.awssdk.transfer.s3.progress.LoggingTransferListener;
import software.amazon.awssdk.transfer.s3.model.FileUpload;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@Service
public class VideoStreamService {

    private static final Logger logger = LoggerFactory.getLogger(VideoStreamService.class);

    public URL getPresignUrl(String key) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

        GeneratePresignedUrlRequest playlistReq = new GeneratePresignedUrlRequest("toktik-bucket", key)
            .withMethod(HttpMethod.GET)
            .withExpiration(new Date(System.currentTimeMillis() + 3600000));

        URL playlistUrl = s3Client.generatePresignedUrl(playlistReq);
        return playlistUrl;
    }

    public Long downloadFile(S3TransferManager transferManager, String bucketName, String key, String downloadedFileWithPath) {
        DownloadFileRequest downloadFileRequest =
            DownloadFileRequest.builder()
                .getObjectRequest(b -> b.bucket(bucketName).key(key))
                .addTransferListener(LoggingTransferListener.create())
                .destination(Paths.get(downloadedFileWithPath))
                .build();

        FileDownload downloadFile = transferManager.downloadFile(downloadFileRequest);

        CompletedFileDownload downloadResult = downloadFile.completionFuture().join();
        logger.info("Content length [{}]", downloadResult.response().contentLength());
        return downloadResult.response().contentLength();
    }

    public String uploadFile(S3TransferManager transferManager, String bucketName, String key, String filePath) {
        UploadFileRequest uploadFileRequest =
            UploadFileRequest.builder()
                .putObjectRequest(b -> b.bucket(bucketName).key(key))
                .addTransferListener(LoggingTransferListener.create())
                .source(Paths.get(filePath))
                .build();

        FileUpload fileUpload = transferManager.uploadFile(uploadFileRequest);

        CompletedFileUpload uploadResult = fileUpload.completionFuture().join();
        return uploadResult.response().eTag();
    }

    public void modifyM3u8(String m3u8, String key) {
        try (S3Presigner presigner = S3Presigner.create()) {
            BufferedReader reader = new BufferedReader(new FileReader(m3u8));
            List<String> lines = new ArrayList<>();
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.endsWith(".ts")) {
                    // Get a presigned url for the chunks and replace it with the original chunk name
                    String presigned = getPresignUrl("hls/" + key + "/" + line).toString();
                    line = presigned;

                    // line = "new_" + line;
                }
                lines.add(line);
            }
            reader.close();

            BufferedWriter writer = new BufferedWriter(new FileWriter(m3u8));
            for (String modifiedLine : lines) {
                writer.write(modifiedLine);
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public S3Object readFile(String key) {
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();

        GetObjectRequest getObjectRequest = new GetObjectRequest("toktik-bucket", "hls/" + key + "/playlist.m3u8");
        S3Object s3Object = s3Client.getObject(getObjectRequest);

        return s3Object;
    }

}
