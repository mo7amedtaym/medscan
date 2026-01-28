package com.albarmajy.medscan.di

import androidx.room.Room
import com.albarmajy.medscan.data.local.AppDatabase
import com.albarmajy.medscan.data.local.worker.DoseSystemWorker
import com.albarmajy.medscan.data.repository.MedicationRepositoryImpl
import com.albarmajy.medscan.domain.repository.MedicationRepository
import com.albarmajy.medscan.scheduler.MedicationAlarmScheduler
import com.albarmajy.medscan.ui.viewModels.CalendarViewModel
import com.albarmajy.medscan.ui.viewModels.DashboardViewModel
import com.albarmajy.medscan.ui.viewModels.MedicationDetailsViewModel
import com.albarmajy.medscan.ui.viewModels.MedicationsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val appModule = module {

    worker { DoseSystemWorker(get(), get(), get()) }

    single {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "mediscan_db"
        ).build()
    }
    single { AppDatabase.getDatabase(androidContext()) }
    single { get<AppDatabase>().medicationDao() }
    single { get<AppDatabase>().doseLogDao() }
    single { MedicationAlarmScheduler(androidContext()) }
    single<MedicationRepository> {
        MedicationRepositoryImpl(get(), get(), get())
    }

    viewModel { DashboardViewModel(get(), get()) }
    viewModel { CalendarViewModel(get()) }
    viewModel { MedicationsViewModel(get()) }
    viewModel { (id: Long) -> MedicationDetailsViewModel(get(), id) }
}