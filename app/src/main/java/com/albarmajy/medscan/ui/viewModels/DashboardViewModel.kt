package com.albarmajy.medscan.ui.viewModels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albarmajy.medscan.data.local.entities.DoseLogEntity
import com.albarmajy.medscan.data.local.entities.MedicationEntity
import com.albarmajy.medscan.data.local.entities.MedicationPlanEntity
import com.albarmajy.medscan.data.local.entities.MedicineReferenceEntity
import com.albarmajy.medscan.data.local.relation.DoseWithMedication
import com.albarmajy.medscan.domain.model.DoseStatus
import com.albarmajy.medscan.domain.repository.MedicationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class DashboardViewModel(
    application: Application,
    private val repository: MedicationRepository
) : AndroidViewModel(application) {

    val todayDoses: StateFlow<List<DoseWithMedication>> = repository.getDosesWithMedicationForToday(
        startOfDay = LocalDate.now().atStartOfDay(),
        endOfDay = LocalDate.now().plusDays(1).atStartOfDay()
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val pendingCount: Flow<Int> = todayDoses.map { list ->
        list.count { it.dose.status == DoseStatus.PENDING }
    }
    val takenCount: Flow<Int> = todayDoses.map { list ->
        list.count { it.dose.status == DoseStatus.TAKEN }
    }
    val skippedCount: Flow<Int> = todayDoses.map { list ->
        list.count { it.dose.status == DoseStatus.SKIPPED }
    }
    val missedCount: Flow<Int> = todayDoses.map { list ->
        list.count { it.dose.status == DoseStatus.MISSED }
    }

    val totalCount: Flow<Int> = todayDoses.map { list -> list.size }


    private val _currentMedication = MutableStateFlow<MedicationEntity?>(null)
    val currentMedication: StateFlow<MedicationEntity?> = _currentMedication.asStateFlow()

    fun getMedicationById(id: Long) {
        viewModelScope.launch {
            val medication = withContext(Dispatchers.IO) {
                repository.getMedicationById(id)
            }
            _currentMedication.value = medication
        }
    }


    fun saveMedication(medicine: MedicineReferenceEntity?) {
        viewModelScope.launch(Dispatchers.IO) {
            medicine?.let { med ->
                val medication = MedicationEntity(
                    id = med.id.toLong(),
                    name = med.trade_name_en,
                    dosage = med.strength,

                )

                repository.addNewMedication(medication)
                withContext(Dispatchers.Main) {
                    Log.d("MedicationSaved", "Medication saved successfully!")

                }
            }
        }
    }

    fun saveMedicationPlan(plan: MedicationPlanEntity, medicineName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addNewMedicationWithSchedule(plan, "1 spoonful", false)

        }
    }



    fun updateDoseState(doseId: Long, status: DoseStatus) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateDoseStatus(doseId, status)
        }
    }

    private val recognitionBuffer = mutableListOf<String>()
    private var lastMatchedId: Int? = null
    fun onTextScanned(rawText: String, onResultFound: (MedicineReferenceEntity) -> Unit) {
        viewModelScope.launch {
            val cleanedText = rawText.trim().lowercase()
            if (cleanedText.length < 3) return@launch
            val noiseWords = setOf("tablet", "capsule", "mg", "ml", "expiry", "batch", "date")
            val filteredText = cleanedText.split(" ")
                .filter { it !in noiseWords }
                .joinToString(" ")


            recognitionBuffer.add(filteredText)
            if (recognitionBuffer.size > 3) {
                recognitionBuffer.removeAt(0)
            }

            if (recognitionBuffer.size == 3 && recognitionBuffer.all { it == filteredText }) {

                val matchedMedicine = repository.searchMedicineInReference(filteredText)

                matchedMedicine?.let {
                    lastMatchedId = it.id
                    onResultFound(it)
                    recognitionBuffer.clear()
                }
            }
        }
    }



}