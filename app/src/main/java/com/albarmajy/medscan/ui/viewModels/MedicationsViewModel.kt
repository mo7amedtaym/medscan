package com.albarmajy.medscan.ui.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.albarmajy.medscan.data.local.entities.MedicationEntity
import com.albarmajy.medscan.data.local.relation.MedicationWithPlan
import com.albarmajy.medscan.domain.model.MedicationFilter
import com.albarmajy.medscan.domain.repository.MedicationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MedicationsViewModel(private val repository: MedicationRepository) : ViewModel() {
    private val _selectedFilter = MutableStateFlow(MedicationFilter.ALL)
    val selectedFilter: StateFlow<MedicationFilter> = _selectedFilter.asStateFlow()

    fun onFilterChanged(filter: MedicationFilter) {
        _selectedFilter.value = filter
    }


    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val activeMedications: StateFlow<List<MedicationWithPlan>> = repository.getActiveMedicationsWithPlans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val pausedMedications: StateFlow<List<MedicationWithPlan>> = repository.getPausedMedicationsWithPlans()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val searchResults: StateFlow<List<MedicationWithPlan>> = _searchQuery
        .debounce(300) // انتظر 300ms بعد التوقف عن الكتابة لتوفير الموارد
        .flatMapLatest { query ->
            if (query.isEmpty()) flowOf(emptyList())
            else repository.searchMedicationsWithPlans(query)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun deleteMedication(medication: MedicationEntity) {
        viewModelScope.launch {
            // حذف الدواء سيؤدي لحذف الخطة والجرعات (بسبب CASCADE)
            // ولكن يجب إلغاء التنبيهات يدوياً من الـ AlarmManager
            repository.deleteMedication(medication.id)
        }
    }
    fun onSearchQueryChanged(newQuery: String) {
        _searchQuery.value = newQuery
    }

}