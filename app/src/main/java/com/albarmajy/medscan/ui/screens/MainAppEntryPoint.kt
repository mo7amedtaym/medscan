package com.albarmajy.medscan.ui.screens

import android.os.Build
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.albarmajy.medscan.data.local.entities.MedicineReferenceEntity
import com.albarmajy.medscan.ui.customUi.MedicationTimeline
import com.albarmajy.medscan.ui.theme.PrimaryBlue
import com.albarmajy.medscan.ui.viewModels.DashboardViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDateTime

@RequiresApi(Build.VERSION_CODES.S)
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainAppEntryPoint(viewModel: DashboardViewModel = koinViewModel()) {
    var isCameraVisible by remember { mutableStateOf(false) }
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)


    var showDialog by remember { mutableStateOf(false) }
    var scannedName by remember { mutableStateOf("") }
    var scannedMedicine by remember { mutableStateOf<MedicineReferenceEntity?>(null) }

    if (isCameraVisible) {
        BackHandler { isCameraVisible = false }
        if (cameraPermissionState.status.isGranted) {

//            CameraScannerScreen(onTextScanned = { result ->
//                viewModel.onTextScanned(result) { matchedMedicine ->
//                    Log.d("camera","Scanned Medication: $matchedMedicine")
//                    scannedMedicine = matchedMedicine
//                    scannedName = matchedMedicine.trade_name_ar
//                    isCameraVisible = false
//                    showDialog = true
//                }
//            })

        }
        else {
            LaunchedEffect(Unit) {
                cameraPermissionState.launchPermissionRequest()
            }
            Text("برجاء الموافقة على إذن الكاميرا لتتمكن من مسح الدواء")
        }
    }
    else {
//        DashboardScreen(
//            viewModel = viewModel,
//        )
    }
//
//    if (showDialog) {
//        BackHandler { isCameraVisible = false }
//        AddMedicationDialog(
//            medicineName = scannedName,
//            onConfirm = { interval ->
//
//                viewModel.saveMedication(scannedMedicine)
//                showDialog = false
//            },
//            onDismiss = { showDialog = false }
//        )
//    }
}

@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun AddMedicationDialog(
    medicineName: String,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var interval by remember { mutableIntStateOf(8) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Text(
                text = "Dose scheduling",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        },
        title = {

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
                var doseList by remember {
                    mutableStateOf(
                        listOf(
                            LocalDateTime.now().withHour(10).withMinute(0),
                            LocalDateTime.now(),
                            LocalDateTime.now().withHour(22).withMinute(0)
                        )
                    )
                }

                MedicationTimeline(doseList) { i, newTime ->
                    val updatedList = doseList.toMutableList()
                    updatedList[i] = newTime
                    doseList = updatedList

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

@RequiresApi(Build.VERSION_CODES.Q)
@Preview
@Composable
private fun AddMedicationDialogPreview() {
    AddMedicationDialog("medscan",{},{})

}