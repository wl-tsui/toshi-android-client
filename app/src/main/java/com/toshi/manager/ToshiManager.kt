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

package com.toshi.manager


import android.widget.Toast
import com.toshi.R
import com.toshi.crypto.HDWallet
import com.toshi.crypto.signal.SignalPreferences
import com.toshi.manager.store.DbMigration
import com.toshi.util.ImageUtil
import com.toshi.util.logging.LogUtil
import com.toshi.util.sharedPrefs.AppPrefs
import com.toshi.view.BaseApplication
import io.realm.Realm
import io.realm.RealmConfiguration
import rx.Completable
import rx.Single
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subjects.BehaviorSubject
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class ToshiManager(
        val balanceManager: BalanceManager = BalanceManager(),
        val sofaMessageManager: SofaMessageManager = SofaMessageManager(),
        val transactionManager: TransactionManager = TransactionManager(),
        val userManager: UserManager = UserManager(),
        val recipientManager: RecipientManager = RecipientManager(),
        val reputationManager: ReputationManager =  ReputationManager(),
        val dappManager: DappManager = DappManager(),
        private val singleExecutor: ExecutorService = Executors.newSingleThreadExecutor()
) {

    companion object {
        const val CACHE_TIMEOUT = (1000 * 60 * 5).toLong()
    }

    private val walletSubject = BehaviorSubject.create<HDWallet>()

    private var areManagersInitialised = false
    private var realmConfig: RealmConfiguration? = null
    private var wallet: HDWallet? = null

    init {
        walletSubject.onNext(null)
        tryEarlyInit()
    }

    private fun tryEarlyInit() {
        tryInit()
                .subscribe(
                        { },
                        { handleInitException(it) }
                )
    }

    private fun handleInitException(throwable: Throwable) {
        LogUtil.exception("Early init failed.", throwable)
    }

    val realm: Single<Realm>
        get() = Single.fromCallable {
            while (realmConfig == null) {
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    throw RuntimeException(e)
                }

            }
            Realm.getDefaultInstance()
        }

    // Ignores any data that may be stored on disk and always creates a new wallet.
    fun initNewWallet(): Completable {
        return if (wallet != null && areManagersInitialised) {
            Completable.complete()
        } else HDWallet()
                .createWallet()
                .doOnSuccess { setWallet(it) }
                .doOnSuccess { AppPrefs.setHasOnboarded(false) }
                .flatMapCompletable { initManagers(wallet) }
                .doOnError { signOut() }
                .doOnError { LogUtil.exception("Error while initiating new wallet", it) }
                .subscribeOn(Schedulers.from(singleExecutor))

    }

    fun init(wallet: HDWallet): Completable {
        setWallet(wallet)
        return initManagers(wallet)
                .doOnError { signOut() }
                .doOnError { LogUtil.exception("Error while initiating wallet", it) }
                .subscribeOn(Schedulers.from(singleExecutor))
    }

    fun tryInit(): Completable {
        return if (wallet != null && areManagersInitialised) {
            Completable.complete()
        } else HDWallet()
                .existingWallet
                .doOnSuccess { setWallet(it) }
                .doOnError { clearUserSession() }
                .flatMapCompletable { initManagers(wallet) }
                .doOnError { LogUtil.exception("Error while trying to init wallet", it) }
                .subscribeOn(Schedulers.from(singleExecutor))
    }

    private fun setWallet(wallet: HDWallet?) {
        this.wallet = wallet
        walletSubject.onNext(wallet)
    }

    private fun initManagers(wallet: HDWallet?): Completable {
        if (wallet == null) throw IllegalStateException("Wallet is null when initManagers")

        return if (areManagersInitialised) Completable.complete()
        else Completable.fromAction {
            initRealm(wallet)
            transactionManager.init(wallet)
        }
        .onErrorComplete()
        .andThen(Completable.mergeDelayError(
                balanceManager.init(wallet),
                sofaMessageManager.init(wallet),
                userManager.init(wallet)
        ))
        .observeOn(AndroidSchedulers.mainThread())
        .doOnError { handleInitManagersError(it) }
        .doOnCompleted { areManagersInitialised = true }
    }

    private fun handleInitManagersError(throwable: Throwable) {
        LogUtil.exception("Error while initiating managers $throwable")
        Toast.makeText(
                BaseApplication.get(),
                R.string.init_manager_error,
                Toast.LENGTH_SHORT
        ).show()
    }

    private fun initRealm(wallet: HDWallet) {
        if (realmConfig != null) return

        val key = wallet.generateDatabaseEncryptionKey()
        Realm.init(BaseApplication.get())
        realmConfig = RealmConfiguration.Builder()
                .schemaVersion(21)
                .migration(DbMigration(wallet))
                .name(wallet.ownerAddress)
                .encryptionKey(key)
                .build()

        realmConfig?.let {
            Realm.setDefaultConfiguration(it)
        } ?: throw IllegalStateException("realmConfig is null when initRealm")
    }

    fun getWallet(): Single<HDWallet> {
        return walletSubject
                .filter { wallet != null }
                .first()
                .toSingle()
                .timeout(30, TimeUnit.SECONDS)
                .doOnError { LogUtil.exception("Wallet is null", it) }
                .onErrorReturn { null }
    }

    fun signOut() {
        clearWalletAndSignal()
        clearMessageSession()
        clearUserSession()
        setSignedOutAndClearUserPrefs()
    }

    private fun clearWalletAndSignal() {
        wallet?.clear()
        SignalPreferences.clear()
    }

    private fun clearMessageSession() = sofaMessageManager.deleteSession()

    private fun clearUserSession() {
        sofaMessageManager.clear()
        userManager.clear()
        recipientManager.clear()
        balanceManager.clear()
        transactionManager.clear()
        areManagersInitialised = false
        closeDatabase()
        ImageUtil.clear()
        setWallet(null)
    }

    private fun closeDatabase() {
        realmConfig = null
        Realm.removeDefaultConfiguration()
    }

    private fun setSignedOutAndClearUserPrefs() {
        AppPrefs.setSignedOut()
        AppPrefs.clear()
    }
}
