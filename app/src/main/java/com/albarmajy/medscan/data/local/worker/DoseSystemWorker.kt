package com.albarmajy.medscan.data.local.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.albarmajy.medscan.domain.repository.MedicationRepository
import java.time.LocalDate

class DoseSystemWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val repository: MedicationRepository
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val permanentPlans = repository.getAllPermanentPlans()
            permanentPlans.forEach { plan ->
                val lastDoseDate = repository.getLastDoseDateForPlan(plan.id)
                val today = LocalDate.now()
                if (lastDoseDate == null || lastDoseDate.isBefore(today.plusDays(7))) {

                    val startDate = lastDoseDate?.plusDays(1) ?: today
                    val endDate = startDate.plusDays(30)

                    repository.generateDosesRange(plan, startDate, endDate)
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}