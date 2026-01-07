package com.albarmajy.medscan.ui.scanner

import android.annotation.SuppressLint
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class TextRecognitionAnalyzer(
    private val onTextDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

//    @SuppressLint("UnsafeOptInUsageError")
//    override fun analyze(imageProxy: ImageProxy) {
//        val mediaImage = imageProxy.image
//        if (mediaImage != null) {
//            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
//            recognizer.process(image)
//                .addOnSuccessListener { visionText ->
//                    onTextDetected(visionText.text)
//                }
//                .addOnCompleteListener {
//                    imageProxy.close()
//                }
//        }
//    }


    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image

        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val image = InputImage.fromMediaImage(mediaImage!!, rotationDegrees)
        if (mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->

                    val imgWidth = if (rotationDegrees == 90 || rotationDegrees == 270) imageProxy.height else imageProxy.width
                    val imgHeight = if (rotationDegrees == 90 || rotationDegrees == 270) imageProxy.width else imageProxy.height


                    val leftLimit = imgWidth * 0.1f
                    val rightLimit = imgWidth * 0.9f
                    val topLimit = imgHeight * 0.45f
                    val bottomLimit = imgHeight * 0.55f

                    for (block in visionText.textBlocks) {
                        for (line in block.lines) {
                            val box = line.boundingBox ?: continue

                            val centerX = box.centerX().toFloat()
                            val centerY = box.centerY().toFloat()

                            if (centerX in leftLimit..rightLimit && centerY in topLimit..bottomLimit) {
                                onTextDetected(line.text)
                            }
                        }
                    }
                }
                .addOnCompleteListener { imageProxy.close() }
        }
    }
}