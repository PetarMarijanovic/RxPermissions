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

import android.app.Activity;
import android.app.FragmentManager;
import android.support.annotation.NonNull;

import io.reactivex.Observable;

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

  public Observable<Permission> requestPermission(final String permission) {
    return mRxPermissionsFragment.start(permission);
  }
}
