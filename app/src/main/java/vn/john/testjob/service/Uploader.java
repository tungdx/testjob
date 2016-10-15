package vn.john.testjob.service;

import java.io.File;

/**
 * Created by TUNGDX on 10/15/2016.
 */
public interface Uploader {
    void upload(File file, UploadCallback callback);
}
