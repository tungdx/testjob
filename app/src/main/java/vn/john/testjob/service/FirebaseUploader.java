package vn.john.testjob.service;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

import vn.john.testjob.Config;

/**
 * Created by TUNGDX on 10/15/2016.
 */
public class FirebaseUploader implements Uploader {
    private FirebaseAuth mAuth;

    @Override
    public void upload(final File file, final UploadCallback callback) {


        Uri uri = Uri.fromFile(file);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(Config.FIREBASE_STORAGE_REFERENCE);
        StorageReference riversRef = storageRef.child("videos/" + uri.getLastPathSegment());
        UploadTask uploadTask = riversRef.putFile(uri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                callback.onFailed(file);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                callback.onSucceed(file);
            }
        });
    }
}
