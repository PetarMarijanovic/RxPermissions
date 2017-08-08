/**
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.tbruyelle.rxpermissions2;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.FragmentManager;
import android.os.Build;
import android.support.annotation.NonNull;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import io.reactivex.subjects.PublishSubject;

public class RxPermissions {

  static final String TAG = "RxPermissions";
  static final Object TRIGGER = new Object();

  RxPermissionsFragment mRxPermissionsFragment;

  public RxPermissions(@NonNull Activity activity) {
    mRxPermissionsFragment = getRxPermissionsFragment(activity);
  }

  private RxPermissionsFragment getRxPermissionsFragment(Activity activity) {
    RxPermissionsFragment rxPermissionsFragment = findRxPermissionsFragment(activity);
    boolean isNewInstance = rxPermissionsFragment == null;
    if (isNewInstance) {
      rxPermissionsFragment = new RxPermissionsFragment();
      FragmentManager fragmentManager = activity.getFragmentManager();
      fragmentManager.beginTransaction().add(rxPermissionsFragment, TAG).commitAllowingStateLoss();
      fragmentManager.executePendingTransactions();
    }
    return rxPermissionsFragment;
  }

  private RxPermissionsFragment findRxPermissionsFragment(Activity activity) {
    return (RxPermissionsFragment) activity.getFragmentManager().findFragmentByTag(TAG);
  }

  public void setLogging(boolean logging) {
    mRxPermissionsFragment.setLogging(logging);
  }

  /**
   * Map emitted items from the source observable into {@code true} if permissions in parameters are
   * granted, or {@code false} if not.
   *
   * <p>If one or several permissions have never been requested, invoke the related framework method
   * to ask the user if he allows the permissions.
   */
  @SuppressWarnings("WeakerAccess")
  public <T> ObservableTransformer<T, Boolean> ensure(final String permission) {
    return new ObservableTransformer<T, Boolean>() {
      @Override
      public ObservableSource<Boolean> apply(Observable<T> o) {
        return request(o, permission)
            .flatMap(
                new Function<Permission, ObservableSource<Boolean>>() {
                  @Override
                  public ObservableSource<Boolean> apply(Permission permission) throws Exception {
                    return Observable.just(permission.granted);
                  }
                });
      }
    };
  }

  private Observable<Permission> request(final Observable<?> trigger, final String permission) {
    return trigger.flatMap(
        new Function<Object, Observable<Permission>>() {
          @Override
          public Observable<Permission> apply(Object o) throws Exception {
            return requestImplementation(permission);
          }
        });
  }

  @TargetApi(Build.VERSION_CODES.M)
  private Observable<Permission> requestImplementation(final String permission) {
    String unrequestedPermission = null;

    PublishSubject<Permission> subject = mRxPermissionsFragment.getSubjectByPermission(permission);
    // Create a new subject if not exists
    if (subject == null) {
      unrequestedPermission = permission;
      subject = PublishSubject.create();
      mRxPermissionsFragment.setSubjectForPermission(permission, subject);
    }

    if (unrequestedPermission != null) {
      mRxPermissionsFragment.requestPermissions(new String[] {permission}); // Because of API
    }
    return subject;
  }
}
