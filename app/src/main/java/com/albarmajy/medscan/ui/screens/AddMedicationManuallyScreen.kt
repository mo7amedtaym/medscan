package com.albarmajy.medscan.ui.screens

import androidx.compose.foundation.BorderStroke
import com.albarmajy.medscan.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Sanitizer
import androidx.compose.material.icons.filled.Vaccines
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.albarmajy.medscan.data.local.entities.MedicationEntity
import com.albarmajy.medscan.ui.theme.BackgroundLight
import com.albarmajy.medscan.ui.theme.MedScanTheme
import com.albarmajy.medscan.ui.theme.PrimaryBlue


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationManuallyScreen(
    onBack: () -> Unit,
    onContinue: (MedicationEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var selectedForm by remember { mutableStateOf("Tablet") }
    var instruction by remember { mutableStateOf("After food") }
    var currentStock by remember { mutableIntStateOf(30) }
    var lowStockAlert by remember { mutableIntStateOf(5) }


    val forms = listOf(
        Pair("Tablet", R.drawable.pill),
        Pair("Capsule", Icons.Default.Vaccines),
        Pair("Syrup", Icons.Default.Opacity),
        Pair("Injection", R.drawable.syringe),
        Pair("Cream", Icons.Default.Sanitizer)
    )

    Scaffold(
        containerColor = BackgroundLight,
        topBar = {
            ManualTopBar(onBack = onBack)
        },
        bottomBar = {
            ManualBottomButton(
                isEnabled = name.isNotBlank() && dosage.isNotBlank(),
                onContinue = {
                    val newMed = MedicationEntity(
                        name = name,
                        dosage = dosage,
                        isActive = true // سيتم تفعيل الخطة لاحقاً
                    )
                    onContinue(newMed)
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // القسم الأول: المعلومات الأساسية
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    ManualInputField("Medication Name", name, "e.g., Paracetamol") { name = it }
                    ManualInputField("Dosage / Strength", dosage, "e.g., 500mg") { dosage = it }
                }
            }

            Column {
                Text("Form Factor", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
//                Text("Form Factor", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                LazyRow(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(forms) { (label, iconData) ->
                        FilterChip(
                            selected = selectedForm == label,
                            onClick = { selectedForm = label },
                            label = { Text(label) },
                            leadingIcon = {
                                when (iconData) {
                                    is ImageVector -> {
                                        Icon(
                                            imageVector = iconData,
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    is Int -> {
                                        Icon(
                                            painter = painterResource(id = iconData),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                          },
                            shape = RoundedCornerShape(24.dp)
                        )
                    }
                }
            }

            // القسم الثالث: التعليمات (Dropdown)
            ManualDropdownField("Instructions", instruction) { instruction = it }

            // القسم الرابع: المخزون (Inventory)
            InventorySection(
                currentStock = currentStock,
                onStockChange = { currentStock = it },
                lowStock = lowStockAlert,
                onLowStockChange = { lowStockAlert = it }
            )

            // نصيحة (Tip)
            TipBox("Setting a low stock alert helps ensure you never run out of your important medications.")

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun ManualInputField(label: String, value: String, placeholder: String, onValueChange: (String) -> Unit) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            placeholder = { Text(placeholder) },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
    }
}

@Composable
fun InventorySection(currentStock: Int, onStockChange: (Int) -> Unit, lowStock: Int, onLowStockChange: (Int) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Inventory2, null, tint = PrimaryBlue)
                Spacer(Modifier.width(8.dp))
                Text("Inventory", fontWeight = FontWeight.Bold)
            }
            InventoryCounter("Current Stock", "Remaining pills/doses", currentStock, onStockChange)
            InventoryCounter("Low Stock Alert", "Notify me when stock reaches", lowStock, onLowStockChange)
        }
    }
}

@Composable
fun InventoryCounter(title: String, sub: String, count: Int, onValueChange: (Int) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(sub, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
            .background(BackgroundLight, RoundedCornerShape(8.dp))
            .padding(4.dp)) {
            IconButton(onClick = { if (count > 0) onValueChange(count - 1) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Remove, null)
            }
            Text("$count", modifier = Modifier.padding(horizontal = 12.dp), fontWeight = FontWeight.Bold)
            IconButton(onClick = { onValueChange(count + 1) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Add, null)
            }
        }
    }
}

@Composable
fun TipBox(text: String) {
    Surface(
        color = PrimaryBlue.copy(alpha = 0.05f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.1f))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Text(text, style = MaterialTheme.typography.bodySmall, color = Color.Gray, lineHeight = 18.sp)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualDropdownField(label: String, selectedOption: String, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("After food", "Before food", "With food", "On empty stomach")

    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.padding(top = 4.dp)
        ) {
            OutlinedTextField(
                value = selectedOption,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = PrimaryBlue)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ManualBottomButton(isEnabled: Boolean, onContinue: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier
            .padding(16.dp)
            .padding(bottom = 24.dp)) {
            Button(
                onClick = onContinue,
                enabled = isEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Continue to Schedule", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualTopBar(onBack: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        title = {
            Text(
                "Add Medication Manually",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Back", tint = PrimaryBlue)
            }
        }
    )
}

@Preview
@Composable
private fun AddMedicationManuallyScreenPreview() {
    MedScanTheme {
        AddMedicationManuallyScreen(onBack = {}, onContinue = { _, -> })
    }
}