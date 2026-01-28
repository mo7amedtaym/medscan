package com.albarmajy.medscan.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albarmajy.medscan.data.local.entities.MedicationEntity
import com.albarmajy.medscan.data.local.entities.MedicationPlanEntity
import com.albarmajy.medscan.data.local.relation.MedicationWithPlan
import com.albarmajy.medscan.ui.viewModels.MedicationsViewModel
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material3.*
import androidx.compose.ui.tooling.preview.Preview
import com.albarmajy.medscan.domain.model.MedicationFilter
import com.albarmajy.medscan.ui.theme.PrimaryBlue
import com.albarmajy.medscan.ui.theme.TextSub
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun MedicinesScreen(
    viewModel: MedicationsViewModel = koinViewModel(),
    onMedicationClick: (MedicationWithPlan) -> Unit
) {
    val activeMeds by viewModel.activeMedications.collectAsState()
    val pausedMeds by viewModel.pausedMedications.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    val selectedFilter by viewModel.selectedFilter.collectAsState()
    MedicinesContent(
        activeMeds = activeMeds,
        pausedMeds = pausedMeds,
        searchQuery = searchQuery,
        onSearchChange = viewModel::onSearchQueryChanged,
        selectedFilter = selectedFilter,
        onFilterSelected = viewModel::onFilterChanged,
        onMedicationClick = onMedicationClick
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicinesContent(
    activeMeds: List<MedicationWithPlan>,
    pausedMeds: List<MedicationWithPlan>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedFilter: MedicationFilter,
    onFilterSelected: (MedicationFilter) -> Unit,
    onMedicationClick: (MedicationWithPlan) -> Unit
) {
    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color(0xFFF5F7F8))) {
                Spacer(modifier = Modifier.height(42.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchChange,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    placeholder = { Text("Search medications...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF1E94F6),
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                LazyRow(
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { FilterChip(selected = selectedFilter == MedicationFilter.ALL, label = { Text("All") }, shape = RoundedCornerShape(18.dp), onClick = {onFilterSelected(MedicationFilter.ALL)}) }
                    item { FilterChip(selected = selectedFilter == MedicationFilter.ACTIVE, label = { Text("Active") }, shape = RoundedCornerShape(18.dp), onClick = {onFilterSelected(MedicationFilter.ACTIVE)}) }
                    item { FilterChip(selected = selectedFilter == MedicationFilter.PAUSED, label = { Text("Paused") }, shape = RoundedCornerShape(18.dp), onClick = {onFilterSelected(MedicationFilter.PAUSED)}) }
                }
            }
        },

    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(Color(0xFFF5F7F8)).padding(padding),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (selectedFilter == MedicationFilter.ALL || selectedFilter == MedicationFilter.ACTIVE) {
                if (activeMeds.isNotEmpty()) {
                    item { SectionHeader("Active Medications") }
                    items(activeMeds) { med -> MedicationCard(med, onMedicationClick) }
                }
            }

            // عرض قسم "المتوقفة" إذا كان الفلتر ALL أو PAUSED
            if (selectedFilter == MedicationFilter.ALL || selectedFilter == MedicationFilter.PAUSED) {
                if (pausedMeds.isNotEmpty()) {
                    item { SectionHeader("Paused Medications") }
                    items(pausedMeds) { med -> MedicationCard(med, onMedicationClick) }
                }
            }
        }
    }
}

@Composable
fun MedicationCard(item: MedicationWithPlan, onMedicationClick: (MedicationWithPlan) -> Unit) {
    val med = item.medication
    val plan = item.plan
    val isActive = med.isActive

    val nextDoseDateTime = item.getNextDoseTime()

    val frequencyText = plan?.timesOfDay?.size?.let { size ->
        when (size) {
            1 -> "Once daily"
            2 -> "Twice daily"
            3 -> "Three times daily"
            else -> "$size times a day"
        }
    } ?: "No schedule set"

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onMedicationClick(item) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp))
                    .background(if (isActive) PrimaryBlue.copy(0.13f) else Color(0xFFF5F5F5)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Medication, contentDescription = null,
                    tint = if (isActive) PrimaryBlue else Color.Gray)
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(med.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    StatusBadge(if (isActive) "Active" else "Paused", isActive)
                }
                Text("${med.dosage} • $frequencyText", color = Color.Gray, fontSize = 14.sp)
                if (isActive) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.Gray)
                        Text(" Next dose: ${formatNextDose(nextDoseDateTime)}", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        modifier = Modifier.fillMaxWidth().padding(start = 4.dp, top = 8.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.5.sp
        ),
        color = Color.Gray
    )
}

@Composable
fun StatusBadge(status: String, isActive: Boolean, activeColor: Color = PrimaryBlue, inactiveColor: Color = TextSub) {
    Surface(
        color = if (isActive) activeColor.copy(0.2f) else inactiveColor.copy(0.2f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            text = status.uppercase(),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
            color = if (isActive) activeColor else inactiveColor
        )
    }
}


fun formatNextDose(nextDose: LocalDateTime?): String {
    if (nextDose == null) return "No upcoming doses"

    val now = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("h:mm a")

    return when {
        nextDose.toLocalDate().isEqual(now.toLocalDate()) -> "Today, ${nextDose.format(formatter)}"
        nextDose.toLocalDate().isEqual(now.toLocalDate().plusDays(1)) -> "Tomorrow, ${nextDose.format(formatter)}"
        else -> nextDose.format(DateTimeFormatter.ofPattern("MMM dd, h:mm a"))
    }
}


@Preview(showBackground = true)
@Composable
fun MedicinesPreview() {
    val mockMeds = listOf(
        MedicationWithPlan(
            medication = MedicationEntity(id = 1, name = "Amoxicillin", dosage = "500mg", isActive = true),
            plan = MedicationPlanEntity(medicationId = 1, startDate = LocalDate.now(), timesOfDay = listOf(LocalTime.NOON, LocalTime.MIDNIGHT))
        ),
        MedicationWithPlan(
            medication = MedicationEntity(id = 2, name = "Vitamin D3", dosage = "2000 IU", isActive = false),
            plan = MedicationPlanEntity(medicationId = 2, startDate = LocalDate.now(), timesOfDay = listOf(LocalTime.NOON))
        )
    )

    MaterialTheme {
        MedicinesContent(
            activeMeds = mockMeds.filter { it.medication.isActive },
            pausedMeds = mockMeds.filter { !it.medication.isActive },
            searchQuery = "",
            onSearchChange = {},
            selectedFilter =  MedicationFilter.ALL,
            onFilterSelected = {},
            onMedicationClick = {}
        )
    }
}