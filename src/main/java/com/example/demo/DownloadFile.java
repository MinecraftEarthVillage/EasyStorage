package com.example.demo;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Mgazul by MohistMC
 * @date 2023/10/5 20:01:51
 */
@RestController
@RequestMapping("/download")
public class DownloadFile {

    @Value("${download.speed-limit:1048576}") // 默认1MB/s
    private long speedLimit;

    @GetMapping("/{variable}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String variable) {
        File targetFile = null;
        try {
            File folder = new File("files", variable);

            // 文件查找逻辑
            if (folder.exists() && folder.isDirectory()) {
                File[] files = folder.listFiles();
                if (files != null && files.length > 0) {
                    targetFile = files[0]; // 取目录下第一个文件
                } else {
                    return ResponseEntity.notFound().build();
                }
            } else if (folder.isFile()) {
                targetFile = folder;
            }

            if (targetFile == null || !targetFile.exists()) {
                return ResponseEntity.notFound().build();
            }

            // 限速输入流
            InputStream throttledStream = new ThrottledInputStream(
                    new BufferedInputStream(new FileInputStream(targetFile)),
                    speedLimit
            );

            InputStreamResource resource = new InputStreamResource(throttledStream);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + targetFile.getName() + "\"")
                    .contentLength(targetFile.length())
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
