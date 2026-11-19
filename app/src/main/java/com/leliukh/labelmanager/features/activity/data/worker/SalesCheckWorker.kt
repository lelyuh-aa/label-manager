package com.leliukh.labelmanager.features.activity.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.leliukh.labelmanager.core.network.domain.ApiResult
import com.leliukh.labelmanager.core.session.domain.repository.SessionRepository
import com.leliukh.labelmanager.core.settings.domain.repository.SettingsRepository
import com.leliukh.labelmanager.features.activity.domain.interactor.ActivityInteractor
import com.leliukh.labelmanager.features.activity.presentation.notification.SaleNotifier
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

private const val WORK_NAME = "sales-check"
private const val INTERVAL_MINUTES = 15L

/**
 * Looks for sales of the user's artists in the background and announces them, so the user
 * hears about a sale without opening the app. Does nothing while signed out or with sale
 * alerts turned off; a failed check is retried with WorkManager's backoff.
 *
 * @param context the application context
 * @param params parameters of this run
 * @param sessionRepository whether someone is signed in
 * @param settingsRepository whether sale alerts are on
 * @param activityInteractor finds sales that were not announced yet
 * @param saleNotifier posts the notifications
 */
@HiltWorker
class SalesCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository,
    private val activityInteractor: ActivityInteractor,
    private val saleNotifier: SaleNotifier,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (sessionRepository.current() == null) return Result.success()
        if (!settingsRepository.settings.first().isSaleAlertsEnabled) return Result.success()
        return when (val result = activityInteractor.newSales()) {
            is ApiResult.Success -> {
                result.data.forEach(saleNotifier::notify)
                Result.success()
            }

            is ApiResult.Failure -> Result.retry()
        }
    }

    companion object {

        /**
         * Schedules the check every 15 minutes, the shortest period WorkManager allows. Keeps
         * an existing schedule, so it is safe to call on every start.
         *
         * @param context any context
         */
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<SalesCheckWorker>(
                INTERVAL_MINUTES,
                TimeUnit.MINUTES,
            )
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
                )
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
