package com.tbruyelle.rxpermissions2;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class RxPermissionsFragment extends Fragment {

  private static final int PERMISSIONS_REQUEST_CODE = 42;

  private Map<String, PublishSubject<Permission>> mSubjects = new HashMap<>();

  @SuppressLint("NewApi")
  public PublishSubject<Permission> requestPermission(@NonNull String permission) {
    PublishSubject<Permission> subject = PublishSubject.create();
    mSubjects.put(permission, subject);
    requestPermissions(new String[] {permission}, PERMISSIONS_REQUEST_CODE);
    return subject;
  }

  @TargetApi(Build.VERSION_CODES.M)
  public void onRequestPermissionsResult(
      int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    if (requestCode != PERMISSIONS_REQUEST_CODE) return;

    String permission = permissions[0];
    boolean granted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

    PublishSubject<Permission> subject = mSubjects.remove(permission);
    subject.onNext(new Permission(permission, granted, false));
    subject.onComplete();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);
  }

  public Observable<Permission> start(String permission) {
    return mSubjects.containsKey(permission)
        ? mSubjects.get(permission)
        : requestPermission(permission);
  }
}
