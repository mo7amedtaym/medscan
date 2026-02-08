@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.albarmajy.medscan.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import com.albarmajy.medscan.data.local.entities.MedicineReferenceEntity
import com.albarmajy.medscan.ui.scanner.TextRecognitionAnalyzer
import com.albarmajy.medscan.ui.theme.PrimaryBlue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.common.util.concurrent.ListenableFuture

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScannerScreen(
    onTextScanned: (String) -> Unit,
    onAddManually: () -> Unit,
    onClose: () -> Unit
) {
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        if (cameraPermissionState.status.isGranted) {
            // شاشة الكاميرا والطبقات العلوية
            AutoScannerCamera(
                onTextScanned = onTextScanned,
                onAddManually = onAddManually,
                onClose = onClose
            )
        } else {
            PermissionDenyView(cameraPermissionState)
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionDenyView(permissionState: PermissionState) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CameraAlt,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Camera Access Required",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "To scan your medication, please allow camera access in your device settings.",
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (permissionState.status.shouldShowRationale) {
                    permissionState.launchPermissionRequest()
                } else {
                    // فتح إعدادات التطبيق في الهاتف
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text(
                text = if (permissionState.status.shouldShowRationale) "Grant Permission" else "Open Settings",
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
fun AutoScannerCamera(
    onTextScanned: (String) -> Unit,
    onAddManually: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // متغيرات الحالة للتحكم في الفلاش
    var isFlashOn by remember { mutableStateOf(false) }
    var cameraControl by remember { mutableStateOf<CameraControl?>(null) }
    var isTextDetected by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
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
                        .build().also {
                            it.setAnalyzer(executor, TextRecognitionAnalyzer { text ->
                                isTextDetected = text.isNotBlank()
                                if (isTextDetected) onTextScanned(text)
                            })
                        }

                    try {
                        cameraProvider.unbindAll()
                        // تخزين كائن الكاميرا للوصول للتحكم
                        val camera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                        cameraControl = camera.cameraControl // الحصول على التحكم بالفلاش
                    } catch (e: Exception) {
                        Log.e("Camera", "Binding failed", e)
                    }
                }, executor)
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        ScannerOverlay(isTextDetected = isTextDetected)

        // تمرير حالة الفلاش والتحكم للـ TopBar
        TopControlBar(
            isFlashOn = isFlashOn,
            onFlashClick = {
                isFlashOn = !isFlashOn
                cameraControl?.enableTorch(isFlashOn)
            },
            onClose = onClose
        )

        BottomControlBar(onAddManually = onAddManually)
    }
}

@Composable
fun ScannerOverlay(isTextDetected: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "Laser")
    val laserOffsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 200.dp.value, // طول المستطيل
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "LaserAnim"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // رسم الخلفية المعتمة حول المستطيل
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val rectWidth = 320.dp.toPx()
            val rectHeight = 200.dp.toPx()
            val left = (width - rectWidth) / 2
            val top = (height - rectHeight) / 2

            val backgroundPath = Path().apply {
                addRect(Rect(0f, 0f, width, height))
                addRoundRect(RoundRect(
                    rect = Rect(left, top, left + rectWidth, top + rectHeight),
                    cornerRadius = CornerRadius(24.dp.toPx())
                ))
                fillType = PathFillType.EvenOdd
            }
            drawPath(path = backgroundPath, color = Color.Black.copy(alpha = 0.7f))
        }

        // إطار المسح المركزي
        Box(
            modifier = Modifier
                .size(320.dp, 200.dp)
                .align(Alignment.Center)
                .dashedBorder(
                    color = if (isTextDetected) PrimaryBlue else Color.White.copy(alpha = 0.5f),
                    strokeWidth = 3.dp,
                    cornerRadius = 24.dp
                ),
            contentAlignment = Alignment.Center
        ) {
            // خط الليزر المتحرك
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .offset(y = laserOffsetY.dp - 100.dp)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, PrimaryBlue, Color.Transparent)
                        )
                    )
            )

            Text(
                text = "Align Medication Name Here",
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.alpha(if (isTextDetected) 0f else 1f)
            )
        }
    }
}

@Composable
fun TopControlBar(
    isFlashOn: Boolean,
    onFlashClick: () -> Unit,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(24.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // زر الفلاش المحدث
        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = if (isFlashOn) PrimaryBlue else Color.Black.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            onClick = onFlashClick
        ) {
            Icon(
                imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                contentDescription = "Toggle Flash",
                tint = Color.White,
                modifier = Modifier.padding(12.dp)
            )
        }

        Surface(
            modifier = Modifier.size(48.dp),
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.4f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
            onClick = onClose
        ) {
            Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.padding(8.dp))
        }
    }
}


@Composable
fun BottomControlBar(onAddManually: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(bottom = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Text(
            "AUTO-CAPTURE ACTIVE",
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.5f),
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // زر إضافة يدوية (تصميم زجاجي عريض)
        Surface(
            onClick = onAddManually,
            shape = RoundedCornerShape(18.dp), // كبسولة
            color = Color.Black.copy(alpha = 0.5f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.AddCircle, null, tint = PrimaryBlue)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Add Manually", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScannerScreen1(
    onTextScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    Box(modifier = Modifier.fillMaxSize()) {
        if (cameraPermissionState.status.isGranted) {
            Camera(onTextScanned)
        }
        else {
            LaunchedEffect(Unit) {
                cameraPermissionState.launchPermissionRequest()
            }
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

                    if (cameraPermissionState.status.shouldShowRationale) {
                        cameraPermissionState.launchPermissionRequest()
                    } else {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.fromParts("package", context.packageName, null)
                        }
                        context.startActivity(intent)
                    }
                }) {
                    Text(if (cameraPermissionState.status.shouldShowRationale) "طلب الإذن" else "فتح الإعدادات")
                }
            }
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
    CameraScannerScreen(onTextScanned = {}, onAddManually = {}, onClose = {})
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
