package com.albarmajy.medscan.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albarmajy.medscan.data.local.relation.MedicationWithPlan
import com.albarmajy.medscan.domain.repository.MedicationRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicationDetailsViewModel(
    private val repository: MedicationRepository,
    private val medId: Long
) : ViewModel() {

    val medicationState: StateFlow<MedicationWithPlan?> = repository.getMedicationWithPlanById(medId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    fun toggleMedicationStatus(planId: Long, currentStatus: Boolean) {
        viewModelScope.launch {
            repository.updateMedicationStatus(planId, !currentStatus)
        }
    }
    fun deleteMedication() {
        viewModelScope.launch {
            repository.deleteMedication(medId)
        }
    }
    fun endMedicationPlan() {
        viewModelScope.launch {
            repository.updatePlanEndDate(medId)
            repository.deleteFutureDoses(medId)
        }
    }
}
