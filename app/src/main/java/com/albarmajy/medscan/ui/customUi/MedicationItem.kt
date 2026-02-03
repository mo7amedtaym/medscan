package com.albarmajy.medscan.ui.customUi

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.albarmajy.medscan.data.local.relation.DoseWithMedication
import com.albarmajy.medscan.domain.model.DoseStatus
import com.albarmajy.medscan.ui.theme.FailureRed
import com.albarmajy.medscan.ui.theme.PrimaryBlue
import com.albarmajy.medscan.ui.theme.SuccessGreen
import com.albarmajy.medscan.ui.theme.TextSub
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MedicationItem(
    dose: DoseWithMedication,
    onTaken: () -> Unit,
    onSkipped: () -> Unit,
    onReschedule: () -> Unit
) {

    var clockPulse by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(30_000)
            clockPulse = System.currentTimeMillis()
        }
    }

    var now = LocalDateTime.now()
    var doseTime = dose.dose.scheduledTime
    var duration = Duration.between(now, doseTime)
    var minutesDifference = duration.toMinutes()


    val isDoseWindow = minutesDifference in -15..15
    val isTooEarly = minutesDifference > 15
    val isMissed = minutesDifference < -15 && dose.dose.status == DoseStatus.PENDING

    val formatter = DateTimeFormatter.ofPattern("hh:mm a")

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isMissed) Color(0xFFFFEBEE) else Color.White 
        ),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DoseStatusIcon(dose.dose.status)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = dose.medication.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    textDecoration = if (dose.dose.status == DoseStatus.TAKEN || dose.dose.status == DoseStatus.SKIPPED) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                    // اختياري: تغيير لون النص ليصبح باهتاً عند الشطب
                    color = if (dose.dose.status == DoseStatus.TAKEN) TextSub else if(dose.dose.status == DoseStatus.SKIPPED) TextSub else Color.Black

                )

                val statusText = remember(clockPulse, dose.dose.status, dose.dose.actualTime) {
                    now = LocalDateTime.now()
                    doseTime = dose.dose.scheduledTime
                    duration = Duration.between(now, doseTime)
                    minutesDifference = duration.toMinutes()

                    when {
                        dose.dose.status == DoseStatus.TAKEN -> getTakenStatusText(dose.dose.actualTime)
                        dose.dose.status == DoseStatus.SKIPPED -> "Skipped - Scheduled at ${doseTime.format(formatter)}"
                        dose.dose.status == DoseStatus.MISSED  -> {
                            val duration = Duration.between(doseTime, now)
                            val minutes = duration.toMinutes()
                            val hours = duration.toHours()

                            val timeAgoText = when {
                                minutes < 60 -> "$minutes minutes ago"
                                hours < 12 -> "$hours hours ago"
                                else -> "Missed - at ${doseTime.format(formatter)}"

                            }
                           return@remember timeAgoText

                        }
                        isDoseWindow -> "Time to take it (Now)"
                        isTooEarly ->if (minutesDifference > 120){
                           "at ${doseTime.format(formatter)}"
                        }
                        else if (minutesDifference > 60){
                            "Starts in ${minutesDifference / 60}h ${minutesDifference % 60}m"
                        }
                        else{
                            "Starts in ${minutesDifference % 60}m"
                        }
                        else -> "${doseTime.format(DateTimeFormatter.ofPattern("hh:mm a"))} • Scheduled"
                    }
                }
                
                Text(
                    text = statusText,
                    color = if (isMissed) Color.Red else TextSub,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Box {
                when {
                    dose.dose.status == DoseStatus.TAKEN -> {
                        Icon(Icons.Default.Verified, "Done", tint = SuccessGreen)
                    }

                    
                    isDoseWindow -> {
                        ModernCheckbox(onChecked = onTaken)
                    }

                    else -> {
                        ActionMenu(dose.dose.status,onTaken, onSkipped, onReschedule)
                    }


                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.S)
fun getTakenStatusText(actualTime: LocalDateTime?): String {
    if (actualTime == null) return "Taken"

    val now = LocalDateTime.now()
    val duration = Duration.between(actualTime, now)
    val minutes = duration.toMinutes()
    val hours = duration.toHours()

    return when {
        minutes < 1 -> "Taken recently"
        minutes < 60 -> "Taken $minutes minutes ago"
        else -> {
            val formatter = DateTimeFormatter.ofPattern("hh:mm a")
            "Taken at ${actualTime.format(formatter)}"
        }
    }
}

@Composable
fun DoseStatusIcon(status: DoseStatus) {
    val (backgroundColor, contentColor, icon) = when {
        status == DoseStatus.TAKEN -> Triple(
            SuccessGreen.copy(alpha = 0.1f),
            SuccessGreen,
            Icons.Default.CheckCircle
        )
        status == DoseStatus.SKIPPED -> Triple(
            TextSub.copy(alpha = 0.1f),
            TextSub,
            Icons.Default.SkipNext
        )
        status == DoseStatus.MISSED -> Triple(
            FailureRed.copy(alpha = 0.1f),
            FailureRed,
            Icons.Default.TimerOff
        )
        else -> Triple(
            PrimaryBlue.copy(alpha = 0.1f),
            PrimaryBlue,
            Icons.Default.Notifications
        )
    }

    Surface(
        color = backgroundColor,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.size(48.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.padding(12.dp)
        )
    }
}

@Composable
fun ModernCheckbox(onChecked: () -> Unit) {
    Surface(
        onClick = onChecked,
        shape = CircleShape,
        color = Color.Transparent,
        border = BorderStroke(2.dp, PrimaryBlue),
        modifier = Modifier.size(28.dp)
    ) {
    }
}

@Composable
fun ActionMenu1(onTaken: () -> Unit, onSkipped: () -> Unit, onReschedule: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Options")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, shape = RoundedCornerShape(8.dp), containerColor = Color.White) {
            DropdownMenuItem(
                text = { Text("Mark as Taken") },
                onClick = { onTaken(); expanded = false },
                leadingIcon = { Icon(Icons.Default.Check, null) }
            )
            DropdownMenuItem(
                text = { Text("Skip Dose") },
                onClick = { onSkipped(); expanded = false },
                leadingIcon = { Icon(Icons.Default.Block, null) }
            )
            DropdownMenuItem(
                text = { Text("Reschedule") },
                onClick = { onReschedule(); expanded = false },
                leadingIcon = { Icon(Icons.Default.EditCalendar, null) }
            )
        }
    }
}

@Composable
fun ActionMenu(status: DoseStatus,onTaken: () -> Unit, onSkipped: () -> Unit, onReschedule: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Options")
        }

        if (expanded) {
            Popup(
                alignment = Alignment.TopEnd,
                onDismissRequest = { expanded = false },
                properties = PopupProperties(focusable = true)
            ) {
                var isVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { isVisible = true }

                AnimatedVisibility(
                    visible = isVisible,
                    enter = scaleIn(transformOrigin = TransformOrigin(1f, 0f)) + fadeIn(),
                    exit = scaleOut(transformOrigin = TransformOrigin(1f, 0f)) + fadeOut()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(12.dp),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .padding(end = 16.dp, top = 8.dp)
                            .width(180.dp)
                            .border(1.dp, Color.LightGray.copy(0.3f), RoundedCornerShape(16.dp))
                    ) {
                        Column(modifier = Modifier.padding(6.dp)) {
                            if (status == DoseStatus.PENDING || status == DoseStatus.MISSED){
                                ActionMenuItem(
                                    text = "Mark as Taken",
                                    icon = Icons.Default.CheckCircle,
                                    color = SuccessGreen,
                                    onClick = { isVisible = false; onTaken(); expanded = false }
                                )
                                ActionMenuItem(
                                    text = "Skip Dose",
                                    icon = Icons.Default.Block,
                                    color = Color.Red,
                                    onClick = { isVisible = false; onSkipped(); expanded = false }
                                )
                                ActionMenuItem(
                                    text = "Reschedule",
                                    icon = Icons.Default.EditCalendar,
                                    color = PrimaryBlue,
                                    onClick = {
                                        isVisible = false; onReschedule(); expanded = false
                                    }
                                )
                            }
                            else if (status == DoseStatus.SKIPPED){
                                ActionMenuItem(
                                    text = "Mark as Taken",
                                    icon = Icons.Default.CheckCircle,
                                    color = SuccessGreen,
                                    onClick = { isVisible = false; onTaken(); expanded = false }
                                )
                                ActionMenuItem(
                                    text = "Don't Skip Dose",
                                    icon = Icons.Default.ArrowBackIosNew,
                                    color = Color.Red,
                                    onClick = { isVisible = false; onSkipped(); expanded = false }
                                )
                                ActionMenuItem(
                                    text = "Reschedule",
                                    icon = Icons.Default.EditCalendar,
                                    color = PrimaryBlue,
                                    onClick = {
                                        isVisible = false; onReschedule(); expanded = false
                                    }
                                )
                            }
                            else{
                                ActionMenuItem(
                                    text = "Reschedule",
                                    icon = Icons.Default.EditCalendar,
                                    color = PrimaryBlue,
                                    onClick = {
                                        isVisible = false; onReschedule(); expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionMenuItem(text: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(text, color = Color.Black, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

