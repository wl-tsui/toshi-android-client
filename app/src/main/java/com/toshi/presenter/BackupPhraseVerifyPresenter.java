/*
 * 	Copyright (c) 2017. Toshi Inc
 *
 * 	This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.toshi.presenter;

import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.toshi.crypto.HDWallet;
import com.toshi.util.logging.LogUtil;
import com.toshi.util.sharedPrefs.AppPrefs;
import com.toshi.view.BaseApplication;
import com.toshi.view.activity.BackupPhraseVerifyActivity;
import com.toshi.view.activity.MainActivity;

import java.util.Arrays;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class BackupPhraseVerifyPresenter implements Presenter<BackupPhraseVerifyActivity> {

    private BackupPhraseVerifyActivity activity;
    private CompositeSubscription subscriptions;
    private boolean firstTimeAttaching = true;

    @Override
    public void onViewAttached(BackupPhraseVerifyActivity view) {
        this.activity = view;

        if (this.firstTimeAttaching) {
            this.firstTimeAttaching = false;
            initLongLivingObjects();
            addBackupPhrase();
        }

        initListeners();
    }

    private void initLongLivingObjects() {
        this.subscriptions = new CompositeSubscription();
    }

    private void addBackupPhrase() {
        final Subscription sub =
                BaseApplication.get()
                .getToshiManager()
                .getWallet()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        this::handleBackupPhraseCallback,
                        this::handleBackupPhraseError
                );

        this.subscriptions.add(sub);
    }

    private void handleBackupPhraseCallback(final HDWallet wallet) {
        final String[] backupPhrase = wallet.getMasterSeed().split(" ");
        this.activity.getBinding().dragAndDropView.setBackupPhrase(Arrays.asList(backupPhrase));
    }

    private void handleBackupPhraseError(final Throwable throwable) {
        LogUtil.exception("Error while getting wallet", throwable);
    }

    private void initListeners() {
        this.activity.getBinding().closeButton.setOnClickListener(this::handleCloseButtonClosed);
        this.activity.getBinding().dragAndDropView.setOnFinishListener(this::handleFinishedListener);
    }

    private void handleFinishedListener() {
        AppPrefs.INSTANCE.setHasBackedUpPhrase();

        final Intent intent = new Intent(this.activity, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(MainActivity.EXTRA__ACTIVE_TAB, MainActivity.ME_TAB);
        this.activity.startActivity(intent);
        ActivityCompat.finishAffinity(this.activity);
    }

    private void handleCloseButtonClosed(final View v) {
        this.activity.finish();
    }

    @Override
    public void onViewDetached() {
        this.subscriptions.clear();
        this.activity = null;
    }

    @Override
    public void onDestroyed() {
        this.subscriptions = null;
    }
}
