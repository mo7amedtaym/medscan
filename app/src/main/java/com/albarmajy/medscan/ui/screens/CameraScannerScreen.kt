@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.albarmajy.medscan.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.LifecycleOwner
import com.albarmajy.medscan.data.local.entities.MedicineReferenceEntity
import com.albarmajy.medscan.ui.scanner.TextRecognitionAnalyzer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.common.util.concurrent.ListenableFuture

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScannerScreen(
    onTextScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
            Camera(onTextScanned)
        } else {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "برجاء الموافقة على إذن الكاميرا لتتمكن من مسح الدواء",
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(onClick = {
                    // التحقق: هل الرفض نهائي أم يمكن الطلب مرة أخرى؟
                    if (cameraPermissionState.status.shouldShowRationale) {
                        // يمكن الطلب مرة أخرى (الرفض لم يكن نهائياً بعد)
                        cameraPermissionState.launchPermissionRequest()
                    } else {
                        // الرفض نهائي، يجب الذهاب للإعدادات
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                }) {
                    Text(if (cameraPermissionState.status.shouldShowRationale) "طلب الإذن" else "فتح الإعدادات")
                }
            }

            // اطلب الإذن تلقائياً فقط إذا كان لم يُرفض نهائياً بعد
            LaunchedEffect(Unit) {
                if (cameraPermissionState.status.shouldShowRationale) {
                    cameraPermissionState.launchPermissionRequest()
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun CameraPreview() {
    CameraScannerScreen {  }
}


@Composable
fun Camera(onTextScanned: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        it.setAnalyzer(executor, TextRecognitionAnalyzer { text ->
                            Log.d("Camera", "Text scanned: $text")
                            onTextScanned(text)
                        })
                    }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (e: Exception) {
                    Log.e("Camera", "Binding failed", e)
                }
            }, executor)
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val rectLeft = width * 0.1f
        val rectRight = width * 0.9f
        val rectTop = height * 0.45f
        val rectBottom = height * 0.55f

        val path = Path().apply {
            addRect(Rect(0f, 0f, width, height))

            addRoundRect(
                RoundRect(
                    left = rectLeft,
                    top = rectTop,
                    right = rectRight,
                    bottom = rectBottom,
                    cornerRadius = CornerRadius(12.dp.toPx())
                )
            )

            fillType = PathFillType.EvenOdd
        }

        drawPath(path = path, color = Color.Black.copy(alpha = 0.6f))

        drawRoundRect(
            color = Color.Cyan,
            topLeft = Offset(rectLeft, rectTop),
            size = Size(rectRight - rectLeft, rectBottom - rectTop),
            style = Stroke(width = 3.dp.toPx()),
            cornerRadius = CornerRadius(12.dp.toPx())
        )
    }

}
