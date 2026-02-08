package com.albarmajy.medscan.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Pix
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albarmajy.medscan.data.local.entities.MedicationEntity
import com.albarmajy.medscan.ui.theme.BackgroundLight
import com.albarmajy.medscan.ui.theme.PrimaryBlue

@Composable
fun ResolveMatchesScreen(
    scannedName: String,
    matches: List<MedicationEntity>,
    onBack: () -> Unit,
    onAddManually: () -> Unit,
    onConfirm: (MedicationEntity) -> Unit
) {
    var selectedMatch by remember { mutableStateOf<MedicationEntity?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            ResolveMatchesTopBar(scannedName, searchQuery, onQueryChange = { searchQuery = it }, onBack = onBack)
        },
        bottomBar = {
            ConfirmSelectionBottomBar(
                isEnabled = selectedMatch != null,
                onConfirm = { selectedMatch?.let { onConfirm(it) } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            // قائمة الأدوية المتطابقة
            items(matches.filter { it.name.contains(searchQuery, ignoreCase = true) }) { medication ->
                MatchCard(
                    medication = medication,
                    isSelected = selectedMatch?.id == medication.id,
                    onClick = { selectedMatch = medication }
                )
            }

            // جزء الإضافة اليدوية (Dashed Border)
            item {
                ManualEntryPlaceholder(onAddManually = onAddManually)
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
}

@Composable
fun MatchCard(
    medication: MedicationEntity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = if (isSelected) BorderStroke(2.dp, PrimaryBlue) else BorderStroke(1.dp, Color.LightGray.copy(0.3f)),
        shadowElevation = if (isSelected) 4.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // أيقونة الدواء
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (isSelected) PrimaryBlue.copy(0.1f) else Color.LightGray.copy(0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (medication.dosage.contains("ml", true)) Icons.Default.Opacity else Icons.Default.Pix,
                    contentDescription = null,
                    tint = if (isSelected) PrimaryBlue else Color.Gray
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(medication.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(medication.dosage, color = Color.Gray, fontSize = 14.sp)
            }

            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = PrimaryBlue)
            } else {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .border(2.dp, Color.LightGray.copy(0.5f), CircleShape)
                )
            }
        }
    }
}

@Composable
fun ManualEntryPlaceholder(onAddManually: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
            .dashedBorder( // استخدام الـ Modifier الذي صممناه سابقاً
                color = Color.LightGray.copy(alpha = 0.5f),
                strokeWidth = 2.dp,
                dashWidth = 8.dp,
                gapWidth = 6.dp,
                cornerRadius = 16.dp
            )
            .background(Color.LightGray.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
            .clickable { onAddManually() }
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Don't see your medicine?", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add it manually", color = PrimaryBlue, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResolveMatchesTopBar(
    scannedName: String,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .background(BackgroundLight.copy(alpha = 0.9f))
            .statusBarsPadding()
            .padding(bottom = 8.dp)
    ) {
        // جزء العنوان والرجوع
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = PrimaryBlue, modifier = Modifier.size(32.dp))
            }
            Text(
                text = "Multiple Matches Found",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.width(48.dp)) // لموازنة زر الرجوع
        }

        // نص الوصف
        Text(
            text = "We found several versions of $scannedName. Please pick the correct one:",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        // حقل البحث (Search Bar) كما في الـ HTML
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Filter results...", fontSize = 14.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = Color.LightGray.copy(0.4f)
            ),
            singleLine = true
        )
    }
}

@Composable
fun ConfirmSelectionBottomBar(
    isEnabled: Boolean,
    onConfirm: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.White.copy(alpha = 0.9f),
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .padding(bottom = 12.dp) // مساحة إضافية للـ Navigation Bar
        ) {
            Button(
                onClick = onConfirm,
                enabled = isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    disabledContainerColor = Color.LightGray
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Confirm Selection",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White)
                }
            }
        }
    }
}

@Preview
@Composable
private fun ResolveMatchesScreenPreview() {
    ResolveMatchesScreen(
        scannedName = "Paracetamol",
        matches = emptyList(),
        onBack = {},
        onAddManually = {},
        onConfirm = {}
    )
}