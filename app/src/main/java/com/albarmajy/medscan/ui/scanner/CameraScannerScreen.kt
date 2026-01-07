package com.albarmajy.medscan.ui.scanner

import android.R.attr.fillType
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.albarmajy.medscan.ui.theme.PrimaryBlue
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas

@Composable
fun CameraScannerScreen(
    onTextScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }


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

            // تحديد أبعاد المستطيل (نفس النسب المستخدمة في الـ Analyzer)
            val rectLeft = width * 0.1f
            val rectRight = width * 0.9f
            val rectTop = height * 0.45f
            val rectBottom = height * 0.55f

            // 1. إنشاء المسار وتفريغ المستطيل
            val path = Path().apply {
                // إضافة مستطيل كامل الشاشة
                addRect(Rect(0f, 0f, width, height))

                // إضافة المستطيل الصغير مع زوايا دائرية
                addRoundRect(
                    RoundRect(
                        left = rectLeft,
                        top = rectTop,
                        right = rectRight,
                        bottom = rectBottom,
                        cornerRadius = CornerRadius(12.dp.toPx())
                    )
                )
                // تحديد نوع الملء ليكون "تفريغ" الجزء الداخلي
                fillType = PathFillType.EvenOdd
            }

            // 2. رسم التعتيم (المنطقة خارج المستطيل)
            drawPath(path = path, color = Color.Black.copy(alpha = 0.6f))

            // 3. رسم الإطار المضيء (الحدود)
            drawRoundRect(
                color = Color.Cyan,
                topLeft = Offset(rectLeft, rectTop),
                size = Size(rectRight - rectLeft, rectBottom - rectTop),
                style = Stroke(width = 3.dp.toPx()),
                cornerRadius = CornerRadius(12.dp.toPx())
            )
        }
    }
}

