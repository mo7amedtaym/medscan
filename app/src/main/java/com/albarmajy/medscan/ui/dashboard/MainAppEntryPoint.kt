package com.albarmajy.medscan.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.albarmajy.medscan.data.local.entities.MedicineReferenceEntity
import com.albarmajy.medscan.ui.scanner.CameraScannerScreen
import com.albarmajy.medscan.ui.theme.PrimaryBlue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainAppEntryPoint(viewModel: DashboardViewModel = koinViewModel()) {
    var isCameraVisible by remember { mutableStateOf(false) }
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)
    val dashboardViewModel: DashboardViewModel = koinViewModel()

    var showDialog by remember { mutableStateOf(false) }
    var scannedName by remember { mutableStateOf("") }
    var scannedMedicine by remember { mutableStateOf<MedicineReferenceEntity?>(null) }

    if (isCameraVisible) {
        if (cameraPermissionState.status.isGranted) {

            CameraScannerScreen(onTextScanned = { result ->
                viewModel.onTextScanned(result) { matchedMedicine ->
                    scannedMedicine = matchedMedicine
                    scannedName = matchedMedicine.trade_name_ar
                    isCameraVisible = false
                    showDialog = true
                }
            })

//            CameraScannerScreen(onTextScanned = { result ->
//                if (result.isNotBlank()) {
//                    scannedName = dashboardViewModel.processScannedText(result)
//                    isCameraVisible = false
//                    showDialog = true // إظهار واجهة التأكيد
//                }
//            })
        }
        else {
            // طلب الإذن إذا لم يكن ممنوحاً
            LaunchedEffect(Unit) {
                cameraPermissionState.launchPermissionRequest()
            }
            Text("برجاء الموافقة على إذن الكاميرا لتتمكن من مسح الدواء")
        }
    }
    else {
        DashboardScreen(
            viewModel = dashboardViewModel,
            onScanClick = { isCameraVisible = true }
        )
    }

    if (showDialog) {
        AddMedicationDialog(
            medicineName = scannedName,
            onConfirm = { interval ->

                dashboardViewModel.saveMedication(scannedMedicine, interval)
                showDialog = false
            },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
fun AddMedicationDialog(
    medicineName: String,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var interval by remember { mutableIntStateOf(8) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryBlue) },
        title = {
            Text(
                text = "تأكيد إضافة الدواء",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "تم رصد دواء: $medicineName",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "كم مرة تأخذ هذا الدواء في اليوم؟", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//                    val intervals = listOf(6, 8, 12)
                    FilterChip(
                        selected = interval == 8,
                        onClick = { interval = 8 },
                        label = { Text("ثلاث مرات") }
                    )
                    FilterChip(
                        selected = interval == 12,
                        onClick = { interval = 12 },
                        label = { Text("مرتين") }
                    )
                    FilterChip(
                        selected = interval == 24,
                        onClick = { interval = 24 },
                        label = { Text("مره") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(interval) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
            ) {
                Text("حفظ الدواء")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = Color.Red)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}