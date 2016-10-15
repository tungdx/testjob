package vn.john.testjob.service;

import java.io.File;

/**
 * Created by TUNGDX on 10/15/2016.
 */
public interface UploadCallback {
    void onSucceed(File file);

    void onFailed(File file);
}
