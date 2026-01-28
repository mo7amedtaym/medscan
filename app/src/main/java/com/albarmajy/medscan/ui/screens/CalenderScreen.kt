package com.albarmajy.medscan.ui.screens

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albarmajy.medscan.ui.customUi.MedicationItem
import com.albarmajy.medscan.ui.theme.PrimaryBlue
import com.albarmajy.medscan.ui.viewModels.CalendarViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.S)
@Composable
fun MedicationCalendarScreen(
    viewModel: CalendarViewModel = koinViewModel()
) {
    val today = LocalDate.now()
    val month = today.month.name.lowercase().capitalize(Locale.ROOT)
    val year = today.year
    val listState = rememberLazyListState()

    val scope = rememberCoroutineScope()
    val doses by viewModel.dateDoses.collectAsState()
    LaunchedEffect(Unit) {
        listState.scrollToItem(7)
    }


    Scaffold() { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F7F8))
                .padding(padding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = Color(0xFF207FF3))
                    Spacer(Modifier.width(8.dp))
                    Text("$month $year", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = {
                        viewModel.onDateSelected(today)
                        val todayIndex = viewModel.dateList.indexOf(today)
                        if (todayIndex != -1) {
                            scope.launch {
                                listState.animateScrollToItem(index = todayIndex, scrollOffset = -200)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF207FF3).copy(0.1f)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Today", color = Color(0xFF207FF3))
                }
            }

            CalendarDateStrip(viewModel, listState)

            Spacer(Modifier.height(24.dp))

            LazyColumn( modifier = Modifier
                .fillMaxSize()
                .padding(16.dp, 26.dp, 16.dp, 16.dp))
            {
                items(doses, key = { it.dose.id }) { dose ->
                    MedicationItem(dose, {},{},{})
                }
            }

        }
    }
}

@Composable
fun CalendarDateStrip(viewModel: CalendarViewModel, listState: LazyListState) {

    Column {
        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(viewModel.dateList) { date ->
                val isSelected = date == viewModel.selectedDate.collectAsState().value
                Log.d("SelectedDate", "isSelected: $isSelected, date: $date, selectedDate: ${viewModel.selectedDate}")

                DateCard(
                    day = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH),
                    date = date.dayOfMonth.toString(),
                    isSelected = isSelected,
                    hasStatus = true,
                    onClick = { viewModel.onDateSelected(date) }
                )
            }
        }
    }
}



@Composable
fun DateCard(
    day: String,
    date: String,
    isSelected: Boolean,
    hasStatus: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) PrimaryBlue else Color(0xFFF5F7F8)
    val contentColor = if (isSelected) Color.White else Color.Black
    val subTextColor = if (isSelected) Color.White.copy(alpha = 0.7f) else Color.Gray

    Column(
        modifier = Modifier
            .width(60.dp)
            .height(85.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = day, style = MaterialTheme.typography.labelMedium, color = subTextColor)
        Text(
            text = date,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
        if (hasStatus) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Box(modifier = Modifier
                    .size(4.dp)
                    .background(if (isSelected) Color.White else Color.Green, CircleShape))
                Box(modifier = Modifier
                    .size(4.dp)
                    .background(if (isSelected) Color.White else Color.Blue, CircleShape))
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = Color.Gray,
        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp
    )
}

