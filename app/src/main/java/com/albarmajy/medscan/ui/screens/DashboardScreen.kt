package com.albarmajy.medscan.ui.screens

import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.albarmajy.medscan.ui.customUi.Label
import com.albarmajy.medscan.ui.customUi.LineProgressIndicator
import com.albarmajy.medscan.ui.theme.PrimaryBlue
import com.albarmajy.medscan.ui.theme.SuccessGreen
import com.albarmajy.medscan.ui.theme.TextSub
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import com.albarmajy.medscan.domain.model.DoseStatus
import com.albarmajy.medscan.data.local.relation.DoseWithMedication
import com.albarmajy.medscan.scheduler.DoseAlarmReceiver
import com.albarmajy.medscan.ui.customUi.MedicationItem
import com.albarmajy.medscan.ui.theme.FailureRed
import com.albarmajy.medscan.ui.viewModels.DashboardViewModel
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneOffset
import kotlin.jvm.java

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = koinViewModel(),
    onCalenderClicked: () -> Unit
    ) {
    val doses by viewModel.todayDoses.collectAsState()
    doses.forEach { it ->
        Log.d("scheduledTime", it.dose.scheduledTime.toEpochSecond(ZoneOffset.UTC).toString())
    }


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 26.dp, 16.dp )
    ) {
        item { HeaderSection() }
        item { DailyOverviewCard(viewModel) }
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Your Schedule", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("View Calendar", color = PrimaryBlue, style = MaterialTheme.typography.labelLarge, modifier = Modifier.clickable { onCalenderClicked() })
            }
        }
        items(doses, key = { it.dose.id }) { dose ->
            MedicationItem(dose,{viewModel.updateDoseState(dose.dose.id, DoseStatus.TAKEN)},{
                if (dose.dose.status == DoseStatus.SKIPPED){
                    viewModel.updateDoseState(dose.dose.id, DoseStatus.PENDING)
                }
                else{
                    viewModel.updateDoseState(dose.dose.id, DoseStatus.SKIPPED)
                }
                                                                          },
                {})
        }
        if (doses.isEmpty()){

            item{
                Column(){
                    Spacer(modifier = Modifier.height(72.dp))
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            "No Active Medication Scheduled",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text("Hello, Atef", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Let's stay on track with your health today.", color = TextSub)
    }
}

@Composable
fun DailyOverviewCard(viewModel: DashboardViewModel) {
    val context = LocalContext.current
    val total by viewModel.totalCount.collectAsState(initial = 0)
    val pending by viewModel.pendingCount.collectAsState(initial = 0)
    val taken by viewModel.takenCount.collectAsState(initial = 0)
    val missed by viewModel.missedCount.collectAsState(initial = 0)
    val skipped by viewModel.skippedCount.collectAsState(initial = 0)
    val failure = total-taken-pending



    val targetProgress = if (total > 0) taken.toFloat() / total.toFloat() else 0f
    val failureProgress = if (total > 0) (total.toFloat()-taken.toFloat()-pending.toFloat())/total.toFloat() else 0f
    val targetPendingProgress = if (total > 0) pending.toFloat() / total.toFloat() else 0f

    Log.d("Progress", "total: $total, taken: $taken, pending: $pending")
    Log.d("Progress", "targetProgress: $targetProgress, failureProgress: $failureProgress, targetPendingProgress: $targetPendingProgress")

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "MainProgress"
    )
    val animatedFailureProgress by animateFloatAsState(
        targetValue = failureProgress+ targetProgress,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "FailProgress"
    )

    val animatedPendingProgress by animateFloatAsState(
        targetValue = targetPendingProgress,
        animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
        label = "PendingProgress"
    )

    val today = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("EEEE, MMM d", Locale.ENGLISH)
    val formattedDate = today.format(formatter)

    Box(modifier = Modifier.fillMaxWidth()) {
        // زخرفة الخلفية (الدائرة)
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .offset(x = 40.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(PrimaryBlue.copy(alpha = 0.15f))
                .zIndex(1f)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .zIndex(2f)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Today's Overview", style = MaterialTheme.typography.labelSmall, color = TextSub)
                        Text(text = formattedDate, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    }

                    val isDone = taken == total
                    val failure = skipped + missed !=0
                    if (isDone) {
                        Label(text = "Done")
                    }
                    else if(failure){
                        if (skipped == 0){
                            Label(text = "$missed Missed", color = FailureRed)
                        }
                        else if (missed == 0){
                            Label(text = "$skipped Skipped", color = TextSub)
                        }
                        else{
                            Label(text = "$missed Missed &, $skipped Skipped", color = FailureRed)
                        }
                    }

                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // المؤشر الدائري مع أنيميشن
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                        CircularProgressIndicator(
                            progress = 1f,
                            color = Color(0x14575757),
                            strokeWidth = 8.dp,
                            modifier = Modifier.fillMaxSize()
                        )

                        CircularProgressIndicator(
                            progress = animatedFailureProgress,
                            color = FailureRed,
                            strokeWidth = 8.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        CircularProgressIndicator(
                            progress = animatedProgress,
                            color = SuccessGreen,
                            strokeWidth = 8.dp,
                            modifier = Modifier.fillMaxSize()
                        )
                        Text("${(animatedProgress * 100).toInt()}%", fontWeight = FontWeight.Bold)
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        // صف الـ Taken
                        ProgressDataRow("Taken", taken, animatedProgress, SuccessGreen)

                        Spacer(modifier = Modifier.height(12.dp))

                        // صف الـ Upcoming
                        ProgressDataRow("Upcoming", pending, animatedPendingProgress, PrimaryBlue) // لون برتقالي للتنبيه
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressDataRow(label: String, count: Int, progress: Float, color: Color) {

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = TextSub)
            Text(
                if (count > 1) "$count doses" else "$count dose",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))

        LineProgressIndicator(
            progress = progress,
            color = color
        )

    }
}

@RequiresApi(Build.VERSION_CODES.S)
@Preview
@Composable
private fun DashboardScreenPreview() {
//    MedicationItem()

}