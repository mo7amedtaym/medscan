package com.albarmajy.medscan.ui.customUi

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun MedicationTimeline(
    doses: List<LocalDateTime>,
    onDoseTimeChanged: (Int, LocalDateTime) -> Unit
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }

    // متغير لتخزين عرض الخط بالبكسل بعد قياسه
    var timelineWidthPx by remember { mutableStateOf(0f) }

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        // أيقونات الشمس والقمر
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(Icons.Rounded.KeyboardArrowUp, contentDescription = null, tint = Color.Gray)
            Icon(Icons.Rounded.KeyboardArrowDown, contentDescription = null, tint = Color.Gray)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // استخدام Box عادي بدلاً من BoxWithConstraints
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 24.dp)
                // هنا نقوم بقياس العرض الفعلي بمجرد ظهور العنصر على الشاشة
                .onGloballyPositioned { coordinates ->
                    timelineWidthPx = coordinates.size.width.toFloat()
                },
            contentAlignment = Alignment.CenterStart
        ) {
            // رسم الخط الرمادي
            Canvas(modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)) {
                drawLine(
                    color = Color.LightGray,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // رسم الجرعات فقط إذا تم قياس العرض بنجاح
            if (timelineWidthPx > 0) {
                doses.forEachIndexed { index, dose ->
                    val dayMinutes = 24 * 60f
                    val currentMinutes = dose.hour * 60 + dose.minute
                    val targetX = (currentMinutes / dayMinutes) * timelineWidthPx

                    var dragX by remember(index) { mutableStateOf(0f) }
                    var lastVibratedMinutes by remember { mutableStateOf(currentMinutes) }

                    // نستخدم rememberUpdatedState للوصول لأحدث قيم القائمة دون إعادة ضبط الـ pointerInput
                    val updatedDoses by rememberUpdatedState(doses)

                    LaunchedEffect(targetX) { dragX = targetX }

                    Box(
                        modifier = Modifier
                            .offset { IntOffset(dragX.toInt() - 40.dp.roundToPx(), 0) }
                            .pointerInput(timelineWidthPx, index) {
                                detectDragGestures(
                                    onDragStart = {
                                        dragX = (updatedDoses[index].hour * 60 + updatedDoses[index].minute) / dayMinutes * timelineWidthPx
                                        lastVibratedMinutes = updatedDoses[index].hour * 60 + updatedDoses[index].minute
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()

                                        // حساب الحدود من النسخة المحدثة (UpdatedState)
                                        val minMinutes = if (index > 0) {
                                            (updatedDoses[index - 1].hour * 60 + updatedDoses[index - 1].minute) + 15
                                        } else 0

                                        val maxMinutes = if (index < updatedDoses.size - 1) {
                                            (updatedDoses[index + 1].hour * 60 + updatedDoses[index + 1].minute) - 15
                                        } else (24 * 60 - 1)

                                        val minX = (minMinutes / dayMinutes) * timelineWidthPx
                                        val maxX = (maxMinutes / dayMinutes) * timelineWidthPx

                                        // تحديث الموضع بسلاسة
                                        dragX = (dragX + dragAmount.x).coerceIn(minX, maxX)

                                        val newMinutesRaw = ((dragX / timelineWidthPx) * (dayMinutes - 1)).toInt()
                                        val snappedMinutes = (newMinutesRaw / 15 * 15).coerceIn(minMinutes, maxMinutes)

                                        if (snappedMinutes != lastVibratedMinutes) {
                                            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                                            lastVibratedMinutes = snappedMinutes

                                            val newTime = dose.withHour(snappedMinutes / 60)
                                                .withMinute(snappedMinutes % 60)

                                            onDoseTimeChanged(index, newTime)
                                        }
                                    }
                                )
                            }
                    ) {
                        DoseBadge(time = dose)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun DynamicMedicationTimeline(
    doses: List<LocalDateTime>,
    onDoseTimeChanged: (Int, LocalDateTime) -> Unit
) {
    val context = LocalContext.current
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }
    val updatedDoses by rememberUpdatedState(doses)

    // 1. حساب النطاق الزمني الحالي (أقل ساعة وأكبر ساعة في الجرعات)
    val startMinutes = (updatedDoses.minOfOrNull { it.hour * 60 + it.minute } ?: 0) - 60 // ساعة قبل
    val endMinutes = (updatedDoses.maxOfOrNull { it.hour * 60 + it.minute } ?: 1439) + 60 // ساعة بعد

    // ضمان أن النطاق لا يخرج عن حدود اليوم 0-1439
    val timelineStart = startMinutes.coerceAtLeast(0)
    val timelineEnd = endMinutes.coerceAtMost(1439)
    val totalVisibleMinutes = (timelineEnd - timelineStart).coerceAtLeast(60) // بحد أدنى ساعة واحدة

    var timelineWidthPx by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .padding(horizontal = 32.dp)
            .onGloballyPositioned { timelineWidthPx = it.size.width.toFloat() },
        contentAlignment = Alignment.CenterStart
    ) {
        // رسم الخط
        Canvas(modifier = Modifier.fillMaxWidth().height(4.dp)) {
            drawLine(
                color = Color.LightGray,
                start = Offset(0f, size.height / 2),
                end = Offset(size.width, size.height / 2),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
        }

        if (timelineWidthPx > 0) {
            updatedDoses.forEachIndexed { index, dose ->
                val currentMinutes = dose.hour * 60 + dose.minute

                val xOffset = ((currentMinutes - timelineStart).toFloat() / totalVisibleMinutes) * timelineWidthPx

                var dragX by remember(index) { mutableStateOf(0f) }
                var lastVibratedMinutes by remember { mutableStateOf(currentMinutes) }

                LaunchedEffect(xOffset) { dragX = xOffset }

                Box(
                    modifier = Modifier
                        .offset { IntOffset(dragX.toInt() - 40.dp.roundToPx(), 0) }
                        .pointerInput(timelineWidthPx, index) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()

                                // حدود الجرعات المجاورة
                                val minLimit = if (index > 0)
                                    (updatedDoses[index - 1].hour * 60 + updatedDoses[index - 1].minute) + 15
                                else 0
                                val maxLimit = if (index < updatedDoses.size - 1)
                                    (updatedDoses[index + 1].hour * 60 + updatedDoses[index + 1].minute) - 15
                                else 1439

                                // تحديث الحركة
                                dragX = (dragX + dragAmount.x).coerceIn(0f, timelineWidthPx)

                                // تحويل الإزاحة إلى دقائق داخل النطاق المرئي
                                val calculatedMinutes = timelineStart + ((dragX / timelineWidthPx) * totalVisibleMinutes).toInt()
                                val snappedMinutes = (calculatedMinutes / 15 * 15).coerceIn(minLimit, maxLimit)

                                if (snappedMinutes != lastVibratedMinutes) {
                                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                                    lastVibratedMinutes = snappedMinutes
                                    onDoseTimeChanged(index, dose.withHour(snappedMinutes / 60).withMinute(snappedMinutes % 60))
                                }
                            }
                        }
                ) {
                    DoseBadge(time = dose)
                }
            }
        }
    }
}

@Composable
fun DoseBadge(time: LocalDateTime) {
    val formatter = remember {
        DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)
    }

    Surface(
        color = Color(0xFF2BADEE),
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp
    ) {
        Text(
            text = time.format(formatter).lowercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            color = Color.White,
            style = MaterialTheme.typography.labelMedium
        )
    }
}


@RequiresApi(Build.VERSION_CODES.Q)
@Preview
@Composable
private fun MedicationTimelinePreview() {
    DynamicMedicationTimeline(listOf(LocalDateTime.now(), LocalDateTime.MAX,LocalDateTime.MIN)) { i, j ->

    }
}
