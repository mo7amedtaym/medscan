package com.albarmajy.medscan.ui.screens

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albarmajy.medscan.data.local.entities.MedicationEntity
import com.albarmajy.medscan.data.local.entities.MedicationPlanEntity
import com.albarmajy.medscan.data.local.relation.MedicationWithPlan
import com.albarmajy.medscan.domain.model.DoseUiState
import com.albarmajy.medscan.ui.customUi.DoseTimeItem
import com.albarmajy.medscan.ui.theme.BackgroundLight
import com.albarmajy.medscan.ui.theme.PrimaryBlue
import com.albarmajy.medscan.ui.theme.TextSub
import com.albarmajy.medscan.ui.viewModels.DashboardViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@Composable
fun MedicationPlanScreen(
    medId: Long,
    viewModel: DashboardViewModel = koinViewModel(),
    onBack: () -> Unit,
    onConfirm: (MedicationPlanEntity, String) -> Unit,
) {
    val medicationWithPlan by viewModel.currentMedication.collectAsState()

    LaunchedEffect(medId) {
        viewModel.getMedicationById(medId)
    }
    MedicationPlanContent(
        medicationWithPlan = medicationWithPlan,
        onBack = onBack,
        onConfirm = {
            plan, name -> onConfirm(plan, name)
            viewModel.updateMedicationPlan(medicationWithPlan?.plan, plan)
        }
    )
}


@Composable
fun MedicationPlanContent(
    medicationWithPlan: MedicationWithPlan?,
    onBack: () -> Unit,
    onConfirm: (MedicationPlanEntity, String) -> Unit,
) {
    val medication = medicationWithPlan?.medication
    val plan = medicationWithPlan?.plan
    var doseFrequency by remember { mutableIntStateOf(plan?.timesOfDay?.size ?: 3) }
    var selectedDuration by remember { mutableStateOf(if (plan?.isPermanent == true) "Permanent" else "Fixed") }
    var daysCount by remember { mutableIntStateOf(7) }

    var dosesList by remember {
        mutableStateOf(listOf(DoseUiState(id = 0, hour = 7, amPm = "AM"), DoseUiState(id = 1, hour = 15), DoseUiState(id = 2, hour = 23)))
    }
    Log.d("selectedDuration", selectedDuration)



    LaunchedEffect(doseFrequency) {
        dosesList = when (doseFrequency) {
            1 -> listOf(DoseUiState(id = 0, hour = 8, amPm = "AM"))
            2 -> listOf(DoseUiState(id = 0, hour = 8, amPm = "AM"), DoseUiState(id = 1, hour = 20))
            3 -> listOf(DoseUiState(id = 0, hour = 7, amPm = "AM"), DoseUiState(id = 1, hour = 15), DoseUiState(id = 2, hour = 23))
            4 -> listOf(DoseUiState(id = 0, hour = 6, amPm = "AM"), DoseUiState(id = 1, hour = 12), DoseUiState(id = 2, hour = 18), DoseUiState(id = 3, hour = 23, minute = 59))
            else -> dosesList
        }
    }


    val isDataChanged = remember(doseFrequency, selectedDuration, daysCount, dosesList) {
        plan?.let { old ->
            val newTimes = dosesList.map { it.toLocalTime() }
            val newEndDate = if (selectedDuration == "Fixed") LocalDate.now().plusDays(daysCount.toLong()) else null
            val newIsPermanent = selectedDuration == "Permanent"

            old.timesOfDay != newTimes ||
                    old.endDate != newEndDate ||
                    old.isPermanent != newIsPermanent
        } ?: true
    }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            PlanTopBar(text=if (plan != null) "Update" else "Create" ,onBack = onBack) {
                if (!isDataChanged) {
                    onBack()
                    return@PlanTopBar
                }
                medication?.let {
                    onConfirm(
                        MedicationPlanEntity(
                            medicationId = it.id,
                            startDate = LocalDate.now(),
                            endDate = if (selectedDuration == "Fixed") LocalDate.now().plusDays(daysCount.toLong()) else null,
                            timesOfDay = dosesList.map { dose -> dose.toLocalTime() },
                            isPermanent = selectedDuration == "Permanent"
                        ),
                        it.name
                    )
                }
            }
        },
    ) { padding ->
        if (medication == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { MedicineHeaderCard(medication.name, medication.dosage) }

                item {
                    Text("Daily Dose Frequency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp))
                    FrequencySelector(selectedFrequency = doseFrequency, onFrequencySelected = { doseFrequency = it })
                }

                item {
                    Text("Set Dose Times", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 24.dp, bottom = 16.dp))
                }

                items(
                    items = dosesList,
                    key = { it.id }
                ) { dose ->
                    val displayIndex = dosesList.indexOfFirst { it.id == dose.id } + 1
                    DoseTimeItem(
                        index = displayIndex,
                        doseState = dose,
                        onUpdateDose = { updatedDose ->
                            val targetIndex = dosesList.indexOfFirst { it.id == updatedDose.id }
                            if (targetIndex != -1) {
                                val newList = dosesList.toMutableList()
                                newList[targetIndex] = updatedDose
                                dosesList = newList
                            }
                        }
                    )
                }

                item {
                    DurationPicker(selectedDuration = selectedDuration, daysCount = daysCount, onDurationChange = { selectedDuration = it }, onDaysChange = { daysCount = it })
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }
    }
}

@Composable
fun MedicineHeaderCard(title: String, subtitle: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = PrimaryBlue, shape = RoundedCornerShape(12.dp), modifier = Modifier.size(56.dp)) {
                Icon(Icons.Default.MedicalServices, contentDescription = null, tint = Color.White, modifier = Modifier.padding(12.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(subtitle, color = TextSub, fontSize = 14.sp)
            }
        }
    }
}

@Composable
fun FrequencySelector(selectedFrequency: Int, onFrequencySelected: (Int) -> Unit) {
    val options = listOf(1, 2, 3, 4)
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { count ->
            val isSelected = selectedFrequency == count
            Surface(
                onClick = { onFrequencySelected(count) },
                color = if (isSelected) PrimaryBlue else Color.White,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.height(44.dp),
                shadowElevation = 2.dp
            ) {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(if (count == 1) "$count time" else "$count times", color = if (isSelected) Color.White else Color.Black, fontSize = 14.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium)
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.CheckCircle, null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanTopBar(text: String, onBack: () -> Unit, onConfirm: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        title = {
            Column {
                Text("Medication Plan".uppercase(), style = MaterialTheme.typography.labelSmall, color = TextSub, fontWeight = FontWeight.Bold)
                Text("Schedule Setup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
        actions = { TextButton(onClick = onConfirm) { Text(text, color = PrimaryBlue, fontWeight = FontWeight.SemiBold) } },
    )
}

@Composable
fun DurationPicker(selectedDuration: String, daysCount: Int, onDurationChange: (String) -> Unit, onDaysChange: (Int) -> Unit) {
    val fixedWeight by animateFloatAsState(targetValue = if (selectedDuration == "Fixed") 1.2f else 1f, label = "")
    val permanentWeight by animateFloatAsState(targetValue = if (selectedDuration == "Permanent") 1.2f else 1f, label = "")

    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text("Treatment Duration", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DurationCard(
                modifier = Modifier.weight(fixedWeight),
                title = "Fixed Days",
                subtitle = if (selectedDuration == "Fixed") "$daysCount Days" else "",
                icon = Icons.Default.EventRepeat,
                isSelected = selectedDuration == "Fixed",
                onClick = { onDurationChange("Fixed") }
            ) {
                AnimatedVisibility(visible = selectedDuration == "Fixed") {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                        IconButton(onClick = { if (daysCount > 1) onDaysChange(daysCount - 1) }) { Icon(Icons.Default.Remove, null, tint = Color.Black) }
                        Text("$daysCount", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        IconButton(onClick = { onDaysChange(daysCount + 1) }) { Icon(Icons.Default.Add, null, tint = Color.Black) }
                    }
                }
            }

            DurationCard(
                modifier = Modifier.weight(permanentWeight),
                title = "Permanent",
                subtitle = "Until stopped",
                icon = Icons.Default.AllInclusive,
                isSelected = selectedDuration == "Permanent",
                onClick = { onDurationChange("Permanent") }
            )
        }
    }
}

@Composable
fun DurationCard(modifier: Modifier, title: String, subtitle: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit, content: @Composable (() -> Unit)? = null) {
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(130.dp)
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) PrimaryBlue.copy(alpha = 0.05f) else Color.White,
        border = BorderStroke(2.dp, if (isSelected) PrimaryBlue else Color.Transparent),
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(icon, null, tint = if (isSelected) PrimaryBlue else Color.Gray)
            Text(title, fontWeight = FontWeight.Bold, color = if (isSelected) PrimaryBlue else Color.Black)
            if (content != null) content()
            else Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

// 3. الـ Preview ببيانات وهمية
@Preview(showBackground = true)
@Composable
private fun MedicationPlanScreenPreview() {
    MaterialTheme {
        MedicationPlanContent(
            medicationWithPlan =MedicationWithPlan( MedicationEntity(id = 1, name = "Amoxicillin", dosage = "500mg", isActive = true),null),
            onConfirm = { _, _ -> },
            onBack = {}
        )
    }
}