package com.albarmajy.medscan.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EventRepeat
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RestaurantMenu
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.albarmajy.medscan.domain.model.MedicationFilter
import com.albarmajy.medscan.ui.customUi.CustomStatusSwitch
import com.albarmajy.medscan.ui.navigation.Routes
import com.albarmajy.medscan.ui.theme.FailureRed
import com.albarmajy.medscan.ui.theme.PrimaryBlue
import com.albarmajy.medscan.ui.theme.TextSub
import com.albarmajy.medscan.ui.viewModels.MedicationDetailsViewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter


@Composable
fun MedicationDetailsScreen(
    medId: Long,
    onBack: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {

    val viewModel: MedicationDetailsViewModel = koinViewModel(
        parameters = { parametersOf(medId) }
    )
    val medicationWithPlan by viewModel.medicationState.collectAsStateWithLifecycle()

    when (val item = medicationWithPlan) {
        null -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        }
        else -> {

            val med = item.medication
            val plan = item.plan

            Scaffold(
                containerColor = Color(0xFFF5F7F8),
                topBar = {
                    DetailsTopBar(
                        isActive = med.isActive,
                        onBack = onBack,
                        onDelete = onDelete,
                        onSwitchChange = { isActive ->
                            viewModel.toggleMedicationStatus(med.id,isActive)
                        }
                    )
                },
                bottomBar = {
                    EditBottomButton(onEdit = onEdit)
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    HeroSection(med.name, "${med.dosage} • Tablet")

                    InfoGrid(
                        dosage = med.dosage,
                        frequency = "${plan?.timesOfDay?.size ?: 0}x daily",
                        instruction = "After food"
                    )

                    PlanSection(plan)

//                    InventorySection(remaining = 15, total = 40)

                    Spacer(modifier = Modifier.height(100.dp)) // مساحة للزر السفلي
                }
            }


        }
    }



}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsTopBar(isActive: Boolean, onSwitchChange: (Boolean) -> Unit, onBack: () -> Unit, onDelete: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        title = {

            Text(
                "Medication Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CustomStatusSwitch(
                    isActive = isActive,
                    onStatusChange = { onSwitchChange(isActive) }
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = FailureRed)
                }
            }
        },

    )
}

@Composable
fun InfoGrid(dosage: String, frequency: String, instruction: String) {
    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        InfoCard(Icons.Default.MedicalServices, "Dosage", dosage, Modifier.weight(1f))
        InfoCard(Icons.Default.EventRepeat, "Frequency", frequency, Modifier.weight(1f))
        InfoCard(Icons.Default.RestaurantMenu, "Take", instruction, Modifier.weight(1f))
    }
}

@Composable
fun InfoCard(icon: ImageVector, label: String, value: String, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.LightGray.copy(0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = Color(0xFF2094f3))
            Spacer(Modifier.height(8.dp))
            Text(label.uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PlanSection(plan: MedicationPlanEntity?) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Medication Plan", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                // عرض البداية والنهاية
                TimelineItem("Start: ${plan?.startDate}", "First dose scheduled", true)
                TimelineItem("End: ${plan?.endDate ?: "Permanent"}", "Expected completion date", false)

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                Text("DAILY SCHEDULE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.Gray)
                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    plan?.timesOfDay?.forEach { time ->
                        ScheduleChip(time.toString())
                    }
                }
            }
        }
    }
}
@Composable
fun HeroSection(name: String, subTitle: String) {
    Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFF2094f3).copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Medication,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF2094f3)
            )
        }
        Spacer(Modifier.width(20.dp))
        Column {
            Text(name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(subTitle, color = Color(0xFF49779c), fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun TimelineItem(title: String, subtitle: String, isStart: Boolean) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (isStart) Color(0xFF2094f3).copy(0.1f) else Color.LightGray.copy(0.3f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isStart) Icons.Default.CalendarToday else Icons.Default.EventAvailable,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (isStart) Color(0xFF2094f3) else Color.Gray
                )
            }
            if (isStart) {
                Box(modifier = Modifier
                    .width(2.dp)
                    .height(40.dp)
                    .background(Color.LightGray.copy(0.5f)))
            }
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}

@Composable
fun EditBottomButton(onEdit: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color(0xFFF5F7F8)),
                    startY = 0f
                )
            )
            .padding(24.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Button(
            onClick = onEdit,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2094f3)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.EditNote, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Edit Medication Plan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun InventorySection(remaining: Int, total: Int) {
    val progress = remaining.toFloat() / total.toFloat()

    Column(modifier = Modifier.padding(16.dp)) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = Color(0xFF2094f3))
                        Text(" Inventory Tracking", fontWeight = FontWeight.Bold)
                    }
                    Text("$remaining pills left", color = Color(0xFF2094f3), fontWeight = FontWeight.Bold)
                }
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape),
                    color = Color(0xFF2094f3),
                    trackColor = Color.LightGray.copy(0.3f)
                )
            }
        }
    }
}

@Composable
fun ScheduleChip(time: String) {
    Surface(
        color = Color(0xFF2094f3),
        shape = RoundedCornerShape(12.dp), // حواف دائرية كاملة
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text(time, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

