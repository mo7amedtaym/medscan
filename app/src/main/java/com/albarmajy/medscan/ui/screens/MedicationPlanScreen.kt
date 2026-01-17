package com.albarmajy.medscan.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albarmajy.medscan.domain.model.DoseUiState
import com.albarmajy.medscan.ui.customUi.DoseTimeItem
import com.albarmajy.medscan.ui.theme.BackgroundLight
import com.albarmajy.medscan.ui.theme.PrimaryBlue
import com.albarmajy.medscan.ui.theme.TextSub
import com.albarmajy.medscan.ui.viewModels.DashboardViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MedicationPlanScreen(
    medId: Long,
    viewModel: DashboardViewModel = koinViewModel(),
    onBack: () -> Unit,
    onConfirm: () -> Unit,
) {
    val medication by viewModel.currentMedication.collectAsState()

    LaunchedEffect(medId) {
        viewModel.getMedicationById(medId)
    }
    var doseFrequency by remember { mutableIntStateOf(2) }
    var selectedDuration by remember { mutableStateOf("Fixed") } // Fixed or Permanent
    var daysCount by remember { mutableIntStateOf(7) }

    var dosesList by remember { mutableStateOf(listOf<DoseUiState>()) }

    LaunchedEffect(doseFrequency) {
        dosesList = (0 until doseFrequency).map { index ->
            dosesList.getOrNull(index) ?: DoseUiState(id = index)
        }
    }
    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            PlanTopBar(onBack = onBack){
                if(medication != null){
                    onConfirm()
                }
            }
        },
//        bottomBar = {
//            SaveScheduleButton(onClick = onConfirm)
//        }
    ) { padding ->
        if (medication == null) {

            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        }

        else{
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)

            ) {
                item { MedicineHeaderCard(medication!!.name, medication!!.dosage) }
                item {
                    Text(
                        "Daily Dose Frequency",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    FrequencySelector(
                        selectedFrequency = doseFrequency,
                        onFrequencySelected = { doseFrequency = it }
                    )
                }

                item {
                    Text(
                        "Set Dose Times",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
                    )
                }

                items(
                    count = doseFrequency,
                    key = { index -> "dose_$index" } // أضف هذا السطر لضمان استقرار القائمة
                ) { index ->
                    DoseTimeItem(
                        index = index + 1,
                        doseState = dosesList[index],
                    ){ updatedDose ->

                        val newList = dosesList.toMutableList()
                        newList[index] = updatedDose
                        dosesList = newList
                    }
                }


                item {
                    DurationPicker(
                        selectedDuration = selectedDuration,
                        daysCount = daysCount,
                        onDurationChange = { selectedDuration = it },
                        onDaysChange = { daysCount = it }
                    )
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
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = PrimaryBlue,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.padding(12.dp)
                )
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
fun DoseTimeItem1(index: Int, label: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.1f))
                .border(2.dp, BackgroundLight, CircleShape)
        ) {
            Text("$index", color = PrimaryBlue, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Button(
                        onClick = { /* Open TimePicker */ },
                        colors = ButtonDefaults.buttonColors(containerColor = BackgroundLight),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("08:00 AM", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {

                    Text("DOSE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Button(
                        onClick = { /* Open TimePicker */ },
                        colors = ButtonDefaults.buttonColors(containerColor = BackgroundLight),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("1 Pill", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                }
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
        verticalArrangement = Arrangement.spacedBy(8.dp),
        maxItemsInEachRow = Int.MAX_VALUE
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
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (count == 1) "$count time" else "$count times",
                        color = if (isSelected) Color.White else Color.Black,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

}

//@Composable
//fun SaveScheduleButton(onClick: () -> Unit) {
//    Box(
//        modifier = Modifier
//            .fillMaxWidth()
//            .background(
//                Brush.verticalGradient(
//                    colors = listOf(Color.Transparent, BackgroundLight),
//                    startY = 0f,
//                    endY = 50f
//                )
//            )
//            .padding(16.dp)
//    ) {
//        Button(
//            onClick = onClick,
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(56.dp),
//            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
//            shape = RoundedCornerShape(24.dp),
//            elevation = ButtonDefaults.buttonElevation(8.dp)
//        ) {
//            Icon(Icons.Default.Verified, null)
//            Spacer(modifier = Modifier.width(8.dp))
//            Text("Save Schedule & Activate", fontSize = 16.sp, fontWeight = FontWeight.Bold)
//        }
//    }
//}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PlanTopBar(onBack: () -> Unit, onConfirm: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        title = {
            Column {
                Text(
                    "Medication Plan".uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSub,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Schedule Setup",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            TextButton(onClick = onConfirm) {
                Text("Confirm", color = PrimaryBlue, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}


@Composable
fun DurationPicker(
    selectedDuration: String,
    daysCount: Int,
    onDurationChange: (String) -> Unit,
    onDaysChange: (Int) -> Unit
) {
    val fixedWeight by animateFloatAsState(
        targetValue = if (selectedDuration == "Fixed") 3f else 2f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "FixedCardWeight"
    )

    val permanentWeight by animateFloatAsState(
        targetValue = if (selectedDuration == "Permanent") 3f else 2f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "PermanentCardWeight"
    )

    Column(modifier = Modifier.padding(top = 24.dp)) {
        Text(
            "Treatment Duration",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DurationCard(
                modifier = Modifier.weight(fixedWeight),
                title = "Fixed Days",
                subtitle = if (selectedDuration == "Fixed") "$daysCount Days" else "",
                icon = Icons.Default.EventRepeat,
                isSelected = selectedDuration == "Fixed",
                onClick = { onDurationChange("Fixed") }
            ) {
                AnimatedVisibility(visible = selectedDuration == "Fixed") {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceAround,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(
                                onClick = { if (daysCount > 1) onDaysChange(daysCount - 1) },
//                                modifier = Modifier
//                                    .size(24.dp)
//                                    .background(PrimaryBlue, CircleShape)
                            ) {
                                Icon(Icons.Default.Remove, null, tint = Color.Black, modifier = Modifier.size(26.dp))
                            }
                            Text("$daysCount", modifier = Modifier.padding(horizontal = 8.dp), fontWeight = FontWeight.Bold, fontSize = 24.sp)
                            IconButton(
                                onClick = { onDaysChange(daysCount + 1) },
//                                modifier = Modifier
//                                    .size(26.dp)
//                                    .background(PrimaryBlue, CircleShape)
                            ) {
                                Icon(Icons.Default.Add, null, tint = Color.Black, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            DurationCard(
                modifier = Modifier.weight(permanentWeight), // استخدام الوزن المتحرك
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
fun DurationCard(
    modifier: Modifier,
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    content: @Composable (() -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(120.dp).animateContentSize().padding(4.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) PrimaryBlue.copy(alpha = 0.05f) else Color.White,
        border = BorderStroke(2.dp, if (isSelected) PrimaryBlue else Color.Transparent),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, null, tint = if (isSelected) PrimaryBlue else Color.Gray)
            Text(title, fontWeight = FontWeight.Bold, color = if (isSelected) PrimaryBlue else Color.Black)
            if (content != null) content()
            else Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Preview
@Composable
private fun MedicationPlanScreenPreview() {
    MedicationPlanScreen(
        medId = 1,
        onConfirm = {},
        onBack = {}
    )
}