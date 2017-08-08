package com.tbruyelle.rxpermissions2.sample;

import android.Manifest.permission;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;

import com.tbruyelle.rxpermissions2.RxPermissions;

import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

  private static final String TAG = "RxPermissionsSample";

  private Camera camera;
  private SurfaceView surfaceView;
  private RxPermissions rxPermissions;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    rxPermissions = new RxPermissions(this);
    rxPermissions.setLogging(true);

    setContentView(R.layout.act_main);
    surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

    findViewById(R.id.enableCamera)
        .setOnClickListener(
            new OnClickListener() {
              @Override
              public void onClick(View v) {
                requestPermission();
              }
            });
  }

  @Override
  protected void onStop() {
    super.onStop();
    releaseCamera();
  }

  private void requestPermission() {
    Observable.just(true)
        // Ask for permissions when button is clicked
        .compose(rxPermissions.ensure(permission.CAMERA))
        .subscribe(
            new Consumer<Boolean>() {
              @Override
              public void accept(Boolean permission) {
                Log.i(TAG, "Permission result " + permission);
              }
            },
            new Consumer<Throwable>() {
              @Override
              public void accept(Throwable t) {
                Log.e(TAG, "onError", t);
              }
            },
            new Action() {
              @Override
              public void run() {
                Log.i(TAG, "OnComplete");
              }
            });
  }

  private void releaseCamera() {
    if (camera != null) {
      camera.release();
      camera = null;
    }
  }
}
