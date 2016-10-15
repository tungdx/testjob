package vn.john.testjob.auth;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Created by TUNGDX on 10/15/2016.
 */
public class FirebaseAuthen implements Authenticator {
    private FirebaseAuth mAuth;

    @Override
    public void authen(final AuthenCallback callback) {
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null && mAuth.getCurrentUser().isAnonymous()) {
            callback.onSucceed();
            return;
        }
        mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    callback.onSucceed();
                } else {
                    callback.onFailed();
                }
            }
        });
    }
}
