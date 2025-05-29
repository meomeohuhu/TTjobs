package com.example.cvtt.CV

import android.content.Context
import android.net.ConnectivityManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.cvtt.Other.DropboxViewModel
import com.example.cvtt.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

@Composable
fun UploadCVScreen(navController: NavHostController, viewModel: DropboxViewModel = viewModel()) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    var fileUri by remember { mutableStateOf<Uri?>(null) }
    var fileInfo by remember { mutableStateOf<String?>(null) }
    var fileValid by remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val context = navController.context
            val contentResolver = context.contentResolver
            val fileName = uri.lastPathSegment?.substringAfterLast('/') ?: "unknown"
            val fileSize = try {
                contentResolver.openAssetFileDescriptor(uri, "r")?.length ?: 0L
            } catch (e: Exception) {
                0L
            }
            val maxSize = 5 * 1024 * 1024 // 5MB

            val extension = contentResolver.getType(uri)?.let {
                when {
                    it.contains("pdf") -> "pdf"
                    it.contains("msword") -> "doc"
                    it.contains("officedocument.wordprocessingml") -> "docx"
                    else -> "unknown"
                }
            } ?: "unknown"

            fileValid = extension in listOf("pdf", "doc", "docx") && fileSize <= maxSize
            fileInfo = "$fileName (${String.format("%.2f", fileSize / 1024f)} KB) - .$extension"
            fileUri = uri

            uploadMessage = if (!fileValid) "❌ File không hợp lệ (chỉ hỗ trợ .doc/.docx/.pdf dưới 5MB)" else null
        }
    }

    val userId = auth.currentUser?.uid ?: ""
    if (userId.isBlank()) {
        navController.navigate("login") { popUpTo(0) }
        return
    }

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Tải CV lên", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A3C34)),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Back Icon",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .background(Color(0xFFF5F5F5)),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Icon(
                painter = painterResource(id = R.drawable.document),
                contentDescription = "Document Icon",
                modifier = Modifier.size(64.dp),
                tint = Color(0xFF28C76F)
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Upload CV để có cơ hội tìm kiếm việc làm tốt hơn",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1A3C34),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "Giảm tới 50% thời gian tìm kiếm việc làm",
                fontSize = 14.sp,
                color = Color(0xFF94A3B8),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (!isUploading) {
                                filePicker.launch("*/*")
                            }
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Hỗ trợ định dạng .doc, .docx, .pdf dưới 5MB",
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Start
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.upload),
                        contentDescription = "Upload Icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF28C76F)
                    )
                }
            }

            AnimatedVisibility(
                visible = fileInfo != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                fileInfo?.let { info ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = info,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1A3C34),
                                    textAlign = TextAlign.Start
                                )
                                if (!fileValid) {
                                    Text(
                                        text = "File CV không hợp lệ, vui lòng chọn lại",
                                        fontSize = 12.sp,
                                        color = Color(0xFFEF4444),
                                        textAlign = TextAlign.Start
                                    )
                                }
                            }
                            IconButton(
                                onClick = {
                                    fileUri = null
                                    fileInfo = null
                                    fileValid = false
                                    uploadMessage = null
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.close),
                                    contentDescription = "Close Icon",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = uploadMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                uploadMessage?.let { message ->
                    Text(
                        text = message,
                        fontSize = 14.sp,
                        color = when {
                            message.contains("✅") -> Color(0xFF28C76F)
                            message.contains("⚠️") -> Color(0xFFFF9800)
                            else -> Color(0xFFEF4444)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            AnimatedVisibility(visible = isUploading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(32.dp)
                        .padding(vertical = 12.dp),
                    color = Color(0xFF28C76F)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    if (!isUploading && fileUri != null && fileValid) {
                        if (isNetworkAvailable(navController.context)) {
                            isUploading = true
                            uploadMessage = "Đang tải file lên..."

                            viewModel.uploadFileToDropbox(
                                context = navController.context,
                                fileUri = fileUri!!,
                                onSuccess = { dropboxUrl ->
                                    val userId = auth.currentUser?.uid ?: ""
                                    if (userId.isNotBlank()) {
                                        // Lấy danh sách CV hiện tại từ Firestore
                                        firestore.collection("users").document(userId)
                                            .get()
                                            .addOnSuccessListener { document ->
                                                // Lấy danh sách cvUrls hiện tại (dưới dạng List<String>)
                                                val currentCvList = document.get("cvUrls") as? List<String> ?: emptyList()
                                                // Thêm URL mới vào danh sách
                                                val updatedCvList = currentCvList + dropboxUrl

                                                // Cập nhật danh sách cvUrls
                                                val data = mapOf("cvUrls" to updatedCvList)
                                                firestore.collection("users").document(userId)
                                                    .set(data, SetOptions.merge())
                                                    .addOnSuccessListener {
                                                        uploadMessage = "✅ Tải và lưu CV thành công!"
                                                        fileUri = null
                                                        fileInfo = null
                                                        fileValid = false
                                                        navController.popBackStack() // Quay lại ManagerCV để hiển thị danh sách
                                                    }
                                                    .addOnFailureListener {
                                                        uploadMessage = "⚠️ Tải lên thành công nhưng không lưu được liên kết: ${it.message}"
                                                        isUploading = false
                                                    }
                                            }
                                            .addOnFailureListener {
                                                uploadMessage = "❌ Lỗi khi lấy danh sách CV hiện tại: ${it.message}"
                                                isUploading = false
                                            }
                                    } else {
                                        uploadMessage = "❌ Không tìm thấy người dùng."
                                        isUploading = false
                                    }
                                },
                                onFailure = { error ->
                                    uploadMessage = "❌ Lỗi khi tải lên Dropbox: $error"
                                    isUploading = false
                                }
                            )
                        } else {
                            uploadMessage = "❌ Không có kết nối Internet."
                        }
                    } else if (fileUri == null) {
                        uploadMessage = "⚠️ Vui lòng chọn file trước khi tải lên."
                    } else if (!fileValid) {
                        uploadMessage = "❌ File không hợp lệ. Vui lòng chọn lại."
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F)),
                shape = RoundedCornerShape(8.dp),
                enabled = fileUri != null && fileValid && !isUploading
            ) {
                Text("Tải CV lên", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetwork = connectivityManager.activeNetworkInfo
    return activeNetwork?.isConnected == true
}