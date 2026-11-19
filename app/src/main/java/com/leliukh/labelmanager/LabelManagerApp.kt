package com.leliukh.labelmanager

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.leliukh.labelmanager.features.activity.data.worker.SalesCheckWorker
import com.leliukh.labelmanager.features.activity.presentation.notification.SaleNotifier
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application entry point and the root of the Hilt dependency graph.
 *
 * WorkManager is initialised on demand with Hilt's worker factory, so workers get their
 * dependencies injected; the background sale check is scheduled on every start.
 */
@HiltAndroidApp
class LabelManagerApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var saleNotifier: SaleNotifier

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        saleNotifier.createChannel()
        SalesCheckWorker.schedule(this)
    }
}
