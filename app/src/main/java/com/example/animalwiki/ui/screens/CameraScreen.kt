package com.example.animalwiki.ui.screens
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.animalwiki.ui.viewmodel.AnimalViewModel
import com.example.animalwiki.ui.viewmodel.RecognitionResult
import java.io.File
@Composable
fun CameraScreen(
    navController: NavController,
    viewModel: AnimalViewModel = viewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isRecognizing by viewModel.isRecognizing.collectAsState()
    val recognitionResults by viewModel.recognitionResults.collectAsState()
    val recognitionError by viewModel.recognitionError.collectAsState()
    var hasCameraPermission by remember { mutableStateOf(false) }
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isCapturing by remember { mutableStateOf(false) }
    var isFlashOn by remember { mutableStateOf(false) }
    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val previewView = remember { PreviewView(context) }
    val executor = remember { ContextCompat.getMainExecutor(context) }
    LaunchedEffect(Unit) {
        hasCameraPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            Toast.makeText(context, "需要相机权限", Toast.LENGTH_SHORT).show()
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(
                        ImageDecoder.createSource(context.contentResolver, it)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                }
                capturedBitmap = bitmap
                viewModel.recognizeImage(bitmap)
            } catch (e: Exception) {
                Toast.makeText(context, "图片加载失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    DisposableEffect(hasCameraPermission, lifecycleOwner) {
        if (!hasCameraPermission) return@DisposableEffect onDispose {}
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            val cameraProvider = try {
                cameraProviderFuture.get()
            } catch (_: Exception) {
                return@Runnable
            }
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                val cam = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
                cameraInstance = cam
                cam.cameraControl.enableTorch(isFlashOn)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        cameraProviderFuture.addListener(listener, executor)
        onDispose {
            try {
                cameraProviderFuture.get().unbindAll()
            } catch (_: Exception) { }
        }
    }
    LaunchedEffect(isFlashOn, cameraInstance) {
        cameraInstance?.cameraControl?.enableTorch(isFlashOn)
    }
    val scanY = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scanY.animateTo(
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(3000),
                repeatMode = RepeatMode.Reverse
            )
        )
    }
    fun resetState() {
        capturedBitmap = null
        viewModel.clearRecognitionState()
    }
    fun takePhotoAndRecognize() {
        if (isCapturing || isRecognizing) return
        isCapturing = true
        val outputDir = context.externalCacheDir ?: context.cacheDir
        val photoFile = File(outputDir, "animal_photo_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            executor,
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    isCapturing = false
                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                    if (bitmap != null) {
                        capturedBitmap = bitmap
                        viewModel.recognizeImage(bitmap)
                    } else {
                        Toast.makeText(context, "图片处理失败", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onError(exc: ImageCaptureException) {
                    isCapturing = false
                    Toast.makeText(context, "拍照失败: ${exc.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }
    Box(modifier = Modifier.fillMaxSize()) {
        if (hasCameraPermission && recognitionResults.isEmpty() && capturedBitmap == null) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )
        } else if (!hasCameraPermission) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onSurface),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("需要相机权限", color = MaterialTheme.colorScheme.onPrimary)
                    TextButton(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("授予权限")
                    }
                }
            }
        }
        if (hasCameraPermission && recognitionResults.isEmpty() && !isRecognizing && capturedBitmap == null) {
            Box(
                modifier = Modifier
                    .size(288.dp)
                    .align(Alignment.Center)
                    .border(2.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            ) {
                Box(modifier = Modifier.size(32.dp).align(Alignment.TopStart).border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 12.dp)))
                Box(modifier = Modifier.size(32.dp).align(Alignment.TopEnd).border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(topEnd = 12.dp)))
                Box(modifier = Modifier.size(32.dp).align(Alignment.BottomStart).border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(bottomStart = 12.dp)))
                Box(modifier = Modifier.size(32.dp).align(Alignment.BottomEnd).border(4.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(bottomEnd = 12.dp)))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 8.dp)
                        .offset(y = (scanY.value * 288).dp)
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "关闭",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    .padding(8.dp)
                    .clickable {
                        viewModel.clearRecognitionState()
                        navController.popBackStack()
                    }
            )
            Row(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            if (isRecognizing || isCapturing) Color.Yellow else MaterialTheme.colorScheme.primary,
                            CircleShape
                        )
                )
                Text(
                    text = when {
                        isCapturing -> "正在拍照..."
                        isRecognizing -> "正在识别..."
                        else -> "AI 识别已就绪"
                    },
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = if (isFlashOn) "关闭闪光灯" else "打开闪光灯",
                tint = if (isFlashOn) Color.Yellow else MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                    .padding(8.dp)
                    .clickable { isFlashOn = !isFlashOn }
            )
        }
        if (recognitionResults.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isRecognizing) {
                    Row(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "对准动物并保持稳定，点击快门识别",
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { galleryLauncher.launch("image/*") }
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = "相册",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(28.dp)
                        )
                        Text(
                            text = "相册",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(4.dp, MaterialTheme.colorScheme.onPrimary)
                            .clickable {
                                if (hasCameraPermission) {
                                    takePhotoAndRecognize()
                                } else {
                                    permissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isCapturing || isRecognizing) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.onPrimary
                                )
                        )
                    }
                    Box(modifier = Modifier.size(48.dp))
                }
            }
        }
        if (isRecognizing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(64.dp),
                        strokeWidth = 4.dp
                    )
                    Text(
                        text = "AI 正在识别...",
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(top = 16.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        if (recognitionError != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
                    .align(Alignment.Center),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "⚠️ 识别失败",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = recognitionError!!,
                        modifier = Modifier.padding(top = 12.dp, bottom = 16.dp),
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                    TextButton(
                        onClick = { resetState() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                            .padding(vertical = 12.dp)
                    ) {
                        Text("重新拍摄", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        if (recognitionResults.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 80.dp)
                    .align(Alignment.Center),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "🔍 识别结果",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    capturedBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "拍摄照片",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .padding(vertical = 12.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        items(recognitionResults) { result ->
                            RecognitionResultItem(
                                result = result,
                                onClick = {
                                    val targetId = result.matchedAnimal?.id
                                    if (targetId != null) {
                                        navController.navigate("detail/$targetId")
                                        resetState()
                                    }
                                }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TextButton(onClick = { resetState() }) {
                            Text("重新拍摄", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        TextButton(
                            onClick = {
                                recognitionResults.firstOrNull()?.matchedAnimal?.let { animal ->
                                    navController.navigate("detail/${animal.id}")
                                    resetState()
                                }
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text("查看最佳匹配", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
@Composable
private fun RecognitionResultItem(
    result: RecognitionResult,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = result.name,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            result.matchedAnimal?.let { animal ->
                Text(
                    text = animal.latinName,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp)
                )
                Text(
                    text = "✅ 已匹配本地数据库",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            result.baikeInfo?.description?.let { desc ->
                Text(
                    text = desc.take(40) + if (desc.length > 40) "..." else "",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${(result.confidence * 100).toInt()}%",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "置信度",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.4f),
                fontSize = 10.sp
            )
        }
    }
}
