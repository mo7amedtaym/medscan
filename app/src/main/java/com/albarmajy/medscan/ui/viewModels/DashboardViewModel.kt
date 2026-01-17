package com.albarmajy.medscan.ui.viewModels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albarmajy.medscan.data.local.entities.DoseLogEntity
import com.albarmajy.medscan.data.local.entities.MedicationEntity
import com.albarmajy.medscan.data.local.entities.MedicineReferenceEntity
import com.albarmajy.medscan.data.local.relation.DoseWithMedication
import com.albarmajy.medscan.domain.model.DoseStatus
import com.albarmajy.medscan.domain.repository.MedicationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

class DashboardViewModel(
    private val repository: MedicationRepository
) : ViewModel() {


    val todayDoses: StateFlow<List<DoseWithMedication>> = repository.getDosesWithMedicationForToday(
        startOfDay = LocalDate.now().atStartOfDay(),
        endOfDay = LocalDate.now().plusDays(1).atStartOfDay()
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _currentMedication = MutableStateFlow<MedicationEntity?>(null)
    val currentMedication: StateFlow<MedicationEntity?> = _currentMedication.asStateFlow()

    // دالة جلب الدواء حسب الـ ID
    fun getMedicationById(id: Long) {
        viewModelScope.launch {
            // نستخدم IO للتعامل مع قاعدة البيانات
            val medication = withContext(Dispatchers.IO) {
                repository.getMedicationById(id)
            }
            _currentMedication.value = medication
        }
    }


    fun saveMedication(medicine: MedicineReferenceEntity?, intervalHours: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            medicine?.let { med ->
                val now = LocalDateTime.now()
                val medication = MedicationEntity(
                    id = med.id.toLong(),
                    name = med.trade_name_en,
                    dosage = med.strength,
//                    recurrenceType = RecurrenceType.FIXED,
//                    intervalHours = intervalHours,
//                    startDate = now
                )

                val doses = (1..3).map { i ->
                    DoseLogEntity(
                        medicationId = medication.id,
                        scheduledTime = now.plusHours((intervalHours * (i - 1)).toLong()),
                        status = DoseStatus.PENDING
                    )
                }

                repository.addNewMedication(medication, doses)
                withContext(Dispatchers.Main) {
                    Log.d("MedicationSaved", "Medication saved successfully!")

                }
            }
        }
    }

    // scanned text handling
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