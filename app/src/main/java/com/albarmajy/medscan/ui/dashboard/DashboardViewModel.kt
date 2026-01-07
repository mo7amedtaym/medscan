package com.albarmajy.medscan.ui.dashboard

import android.util.Log
import android.widget.Toast
import androidx.camera.core.impl.utils.ContextUtil.getApplicationContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albarmajy.medscan.data.local.entities.DoseLogEntity
import com.albarmajy.medscan.data.local.entities.MedicationEntity
import com.albarmajy.medscan.data.local.entities.MedicineReferenceEntity
import com.albarmajy.medscan.domain.model.DoseStatus
import com.albarmajy.medscan.domain.model.RecurrenceType
import com.albarmajy.medscan.domain.repository.MedicationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime

class DashboardViewModel(
    private val repository: MedicationRepository
) : ViewModel() {


    val todayDoses: StateFlow<List<DoseLogEntity>> = repository.getTodayDoses(
        startOfDay = LocalDate.now().atStartOfDay(),
        endOfDay = LocalDate.now().plusDays(1).atStartOfDay()
    ).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun getMedicationById(id: Int, onResult: (MedicationEntity?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val medication = repository.getMedicationById(id)
            withContext(Dispatchers.Main) {
                onResult(medication)
            }
        }
    }



    fun processScannedText(rawText: String): String {

        val lines = rawText.split("\n").filter { it.isNotBlank() }
        return lines.firstOrNull() ?: "Unknown Medicine"
    }

//    fun saveMedication(name: String, intervalHours: Int) {
//        viewModelScope.launch {
//            val now = LocalDateTime.now()
//
//            val medication = MedicationEntity(
//                name = name,
//                dosage = "As scanned",
//                recurrenceType = RecurrenceType.FIXED,
//                intervalHours = intervalHours,
//                startDate = now
//            )
//
//            val doses = mutableListOf<DoseLogEntity>()
//            for (i in 1..3) {
//                doses.add(
//                    DoseLogEntity(
//                        medicationId = 0,
//                        scheduledTime = now.plusHours((intervalHours * i).toLong()),
//                        status = DoseStatus.PENDING
//                    )
//                )
//            }
//
//            repository.addNewMedication(medication, doses)
//        }
//    }

    fun saveMedication(medicine: MedicineReferenceEntity?, intervalHours: Int) {
        // 1. استخدام Dispatchers.IO لحل مشكلة الـ IllegalStateException
        viewModelScope.launch(Dispatchers.IO) {

            // 2. التحقق من أن الكائن ليس null بطريقة آمنة
            medicine?.let { med ->
                val now = LocalDateTime.now()

                val medication = MedicationEntity(
                    name = med.trade_name_en,
                    dosage = med.strength,
                    recurrenceType = RecurrenceType.FIXED,
                    intervalHours = intervalHours,
                    startDate = now
                )

                // توليد الجرعات القادمة
                val doses = (1..3).map { i ->
                    DoseLogEntity(
                        medicationId = 0,
                        scheduledTime = now.plusHours((intervalHours * i).toLong()),
                        status = DoseStatus.PENDING
                    )
                }

                // 3. الحفظ الآن سيتم بنجاح لأننا في خيط الـ IO
                repository.addNewMedication(medication, doses)

                // اختيارياً: العودة للخيط الرئيسي إذا كنت تريد إظهار رسالة نجاح (Toast)
                withContext(Dispatchers.Main) {
                    Log.d("MedicationSaved", "Medication saved successfully!")

                }
            }
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
                    if (it.id != lastMatchedId) {
                        lastMatchedId = it.id
                        onResultFound(it)
                        recognitionBuffer.clear()
                    }
                }
            }
        }
    }



}


