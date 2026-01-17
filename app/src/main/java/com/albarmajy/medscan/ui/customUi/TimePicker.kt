package com.albarmajy.medscan.ui.customUi

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albarmajy.medscan.domain.model.DoseUiState
import com.albarmajy.medscan.ui.theme.BackgroundLight
import com.albarmajy.medscan.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch


@Composable
fun DoseTimeItem(
    index: Int,
    doseState: DoseUiState,
    onUpdateDose: (DoseUiState) -> Unit
) {
    var isEditingTime by remember { mutableStateOf(false) }

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
            modifier = Modifier
                .weight(1f)
                .animateContentSize()
                .padding(4.dp),

            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            AnimatedContent(
                targetState = isEditingTime,
                label = "TimeSwitch",
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },

            ) { editing ->
                if (editing) {

                    InlineTimePicker(
                        hour = doseState.hour,
                        minute = doseState.minute,
                        amPm = doseState.amPm,
                        onConfirm = { h, m, ap ->

                            onUpdateDose(doseState.copy(hour = h, minute = m, amPm = ap))
                            isEditingTime = false
                        }
                    )
                } else {

                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // الجزء الأيسر: عرض الوقت
                        Column {
                            Text(
                                text = if (doseState.amPm == "AM") "Morning" else "Evening",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Button(
                                onClick = {
                                    Log.d("Time", "Opening picker for dose $index")
                                    isEditingTime = true // لفتح الـ Time Picker
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BackgroundLight),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = PrimaryBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                // عرض الوقت المخزن في الـ State
                                Text(
                                    text = "${processTime(doseState.hour)}:${processTime(doseState.minute)} ${doseState.amPm}",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("DOSE", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            Button(
                                onClick = {
                                    val nextAmount = if (doseState.amount >= 3) 1 else doseState.amount + 1
                                    onUpdateDose(doseState.copy(amount = nextAmount))
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BackgroundLight),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                val pillText = if (doseState.amount == 1) "Pill" else "Pills"
                                Text("${doseState.amount} $pillText", color = Color.Black, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
fun processTime(time: Int): String {
    return if (time < 10) "0$time" else time.toString()
}

@Composable
fun InlineTimePicker(
    hour: Int,
    minute: Int,
    amPm: String,
    onConfirm: (Int, Int, String) -> Unit
) {
    var h by remember { mutableIntStateOf(hour) }
    var m by remember { mutableIntStateOf(minute) }
    var ap by remember { mutableStateOf(amPm) }

    Column(
        modifier = Modifier
            .padding(12.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)) {
            Text("SET TIME", style = MaterialTheme.typography.labelSmall)
            Text("Save", style = MaterialTheme.typography.labelLarge, color = PrimaryBlue, modifier = Modifier.clickable { onConfirm(h, m, ap) })
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            WheelPicker(items = (1..12).toList(), initialItem = h) { h = it }
            Text(":", fontWeight = FontWeight.Bold, fontSize = 24.sp)
            // عجلة الدقائق (0-59)
            WheelPicker(items = (0..59).toList(), initialItem = m, format = "%02d") { m = it }
            // عجلة AM/PM
            WheelPicker(items = listOf("AM", "PM"), initialItem = ap) { ap = it }
        }

//        Button(
//            onClick = { onConfirm(h, m, ap) },
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(40.dp),
//            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
//            shape = RoundedCornerShape(8.dp)
//        ) {
//            Text("Done", fontSize = 14.sp)
//        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> WheelPicker(
    items: List<T>,
    initialItem: T,
    format: String = "%s",
    onItemSelected: (T) -> Unit
) {
    val context = LocalContext.current
    val itemHeightPx = with(LocalDensity.current) { 40.dp.toPx() } // يجب أن يطابق الـ height في الـ Modifier
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = items.indexOf(initialItem))
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val scope = rememberCoroutineScope()


    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) 0
            else {
                val fullyVisibleCenter = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2
                visibleItemsInfo.minByOrNull { Math.abs((it.offset + it.size / 2) - fullyVisibleCenter) }?.index ?: 0
            }
        }
    }
    LaunchedEffect(centerIndex) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        }
    }

    // 2. تحديث الحالة فقط عندما يتغير الـ centerIndex
    LaunchedEffect(centerIndex) {
        if (centerIndex in items.indices) {
            onItemSelected(items[centerIndex])
        }
    }

    Box(modifier = Modifier
        .width(65.dp)
        .height(120.dp), contentAlignment = Alignment.Center) {
        // مؤشر التحديد (الخلفية)
        Box(modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(BackgroundLight, RoundedCornerShape(8.dp)))

        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            // نستخدم Padding عمودي لضمان أن أول وأخر عنصر يمكن أن يصلا للمنتصف
            contentPadding = PaddingValues(vertical = 40.dp),
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(items) { index, item ->
                val text = if (item is Int) String.format(format, item) else item.toString()
                val isSelected = index == centerIndex

                Text(
                    text = text,
                    fontSize = if (isSelected) 22.sp else 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) PrimaryBlue else Color.LightGray.copy(alpha = 0.6f),
                    modifier = Modifier
                        .height(40.dp) // تثبيت الارتفاع ضروري جداً لدقة الحساب
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            scope.launch {
                                listState.animateScrollToItem(index)
                            }
                        },
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview
@Composable
private fun DoseTimeItemPreview() {
    DoseTimeItem(index = 1, doseState = DoseUiState(id = 1), onUpdateDose = {})
}