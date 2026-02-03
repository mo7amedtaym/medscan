package com.albarmajy.medscan.ui.viewModels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albarmajy.medscan.data.local.relation.DoseWithMedication
import com.albarmajy.medscan.domain.model.DoseStatus
import com.albarmajy.medscan.domain.repository.MedicationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class CalendarViewModel(private val repository: MedicationRepository) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val dateDoses: StateFlow<List<DoseWithMedication>> = _selectedDate
        .flatMapLatest { date ->
            repository.getDosesWithMedicationForDate(date)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val dateList: List<LocalDate> = (-7..30).map {
        LocalDate.now().plusDays(it.toLong())
    }

    fun onDateSelected(date: LocalDate) {
        _selectedDate.value = date
    }

    fun updateDoseState(doseId: Long, status: DoseStatus) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateDoseStatus(doseId, status)
        }
    }
}