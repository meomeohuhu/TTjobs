package com.example.cvtt.auth

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.cvtt.Data.User
import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.cvtt.R

@Composable
fun UserProfileScreen(user: User?, onSave: (User) -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(user?.name ?: "") }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
    var profilePictureUrl by remember { mutableStateOf(user?.profilePictureUrl ?: "") }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        profilePictureUri = uri
        uri?.let { uploadImageToCloudinary12(it, context) { url -> profilePictureUrl = url } }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Thông tin tài khoản",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Hiển thị và chỉnh sửa ảnh đại diện
        Box(contentAlignment = Alignment.Center) {
            AsyncImage(
                model = (profilePictureUri ?: profilePictureUrl.takeIf { it.isNotBlank() }?.let { Uri.parse(it) }) ?: R.drawable.picture,
                contentDescription = "Ảnh đại diện",
                modifier = Modifier
                    .size(150.dp)
                    .clip(CircleShape)
                    .padding(bottom = 8.dp),
                contentScale = ContentScale.Crop
            )
            IconButton(
                onClick = { launcher.launch("image/*") },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(40.dp)
                    .offset(x = 10.dp, y = 10.dp)
            ) {
                Icon(
                    painterResource(id = R.drawable.camera),
                    "Thay đổi ảnh đại diện",
                    tint = Color(0xFF28C76F),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Chỉnh sửa tên
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Tên người dùng") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(8.dp)
        )

        // Nút đổi mật khẩu
        Button(
            onClick = { showChangePasswordDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6200EE))
        ) {
            Text("Đổi mật khẩu", fontSize = 16.sp, color = Color.White)
        }

        // Nút lưu và hủy
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = {
                    if (name.isNotBlank() && profilePictureUrl.isNotBlank()) {
                        val updatedUser = user?.copy(
                            name = name,
                            profilePictureUrl = profilePictureUrl
                        ) ?: User(
                            id = "",
                            name = name,
                            level = 0,
                            profilePictureUrl = profilePictureUrl,
                            appliedJobs = emptyList(),
                            savedJobs = emptyList(),
                            savedApply = emptyList(),
                            followedCompanies = emptyList(),
                            profileViews = emptyList(),
                            cvUrls = emptyList()
                        )
                        onSave(updatedUser)
                    } else {
                        Toast.makeText(context, "Vui lòng điền tên và chọn ảnh đại diện", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F)),
                enabled = name.isNotBlank() && profilePictureUrl.isNotBlank()
            ) {
                Text("Lưu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { onCancel() },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
            ) {
                Text("Hủy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }

    // Dialog đổi mật khẩu
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Đổi mật khẩu") },
            text = {
                Column {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Mật khẩu hiện tại") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Mật khẩu mới") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Xác nhận mật khẩu mới") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (currentPassword.isNotBlank() && newPassword.isNotBlank() && confirmPassword.isNotBlank()) {
                            if (newPassword == confirmPassword) {
                                // Logic đổi mật khẩu (giả lập thành công)
                                Toast.makeText(context, "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                                showChangePasswordDialog = false
                                currentPassword = ""
                                newPassword = ""
                                confirmPassword = ""
                            } else {
                                Toast.makeText(context, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

fun uploadImageToCloudinary12(uri: Uri, context: Context, onSuccess: (String) -> Unit) {
    MediaManager.get().upload(uri).option("resource_type", "image").option("folder", "cvtt").callback(object : UploadCallback {
        override fun onStart(requestId: String?) { Toast.makeText(context, "Đang tải ảnh...", Toast.LENGTH_SHORT).show() }
        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
        override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
            val secureUrl = resultData?.get("secure_url") as? String
            if (secureUrl != null) onSuccess(secureUrl) else Toast.makeText(context, "Không lấy được link ảnh HTTPS", Toast.LENGTH_SHORT).show()
        }
        override fun onError(requestId: String?, error: ErrorInfo?) { Toast.makeText(context, "Lỗi tải ảnh: ${error?.description}", Toast.LENGTH_LONG).show() }
        override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
    }).dispatch()
}