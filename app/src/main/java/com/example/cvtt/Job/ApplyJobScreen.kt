package com.example.cvtt.Job

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.cvtt.Data.User
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplyJobScreen(navController: NavHostController, jobId: String, user: User?) {
    var selectedCV by remember { mutableStateOf<String?>(null) } // CV được chọn từ dropdown (URL đầy đủ)
    var expanded by remember { mutableStateOf(false) } // Trạng thái dropdown

    // Lấy danh sách cvUrls từ user
    val cvList = user?.cvUrls ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("CV ứng tuyển", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))

        // Dropdown menu cho cvUrls, hiển thị tên tệp
        Box {
            OutlinedTextField(
                value = selectedCV?.let { getFileNameFromUrl(it) } ?: "Chọn CV",
                onValueChange = { /* Read-only, không cho phép chỉnh sửa trực tiếp */ },
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                label = { Text("Chọn CV") },
                trailingIcon = {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Mở dropdown",
                        modifier = Modifier.clickable { expanded = true }
                    )
                }
            )
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                if (cvList.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("Không có CV nào được tìm thấy") },
                        onClick = { expanded = false }
                    )
                } else {
                    cvList.forEachIndexed { index, cvUrl ->
                        DropdownMenuItem(
                            text = { Text("CV ${index + 1}: ${getFileNameFromUrl(cvUrl)}") }, // Hiển thị tên tệp
                            onClick = {
                                selectedCV = cvUrl // Lưu URL đầy đủ
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Nút Ứng tuyển ---
        Button(
            onClick = {
                val userId = user?.id
                val selectedCVUrl = selectedCV
                val applyDate = SimpleDateFormat("yyyy-MM-dd").format(Date())
                if (userId != null && selectedCVUrl != null) {
                    val db = FirebaseFirestore.getInstance()
                    val userRef = db.collection("users").document(userId)
                    val jobRef = db.collection("jobs").document(jobId)

                    // Bước 1: Lưu jobId vào user.savedApply
                    userRef.update("savedApply", FieldValue.arrayUnion(jobId))
                        .addOnSuccessListener {
                            // Bước 2: Cập nhật appliedUserIds và appliedCVs trong Job
                            jobRef.update(
                                mapOf(
                                    "appliedUserIds" to FieldValue.arrayUnion(userId),
                                    "statusApplied.$userId" to "pending",
                                    "appliedCVs.$userId" to FieldValue.arrayUnion(selectedCVUrl),
                                    "appliedTime.$userId" to applyDate
                                )
                            ).addOnSuccessListener {
                                Toast.makeText(
                                    navController.context,
                                    "Ứng tuyển thành công với CV: ${getFileNameFromUrl(selectedCVUrl)}",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.popBackStack()
                            }.addOnFailureListener { e ->
                                Log.e("ApplyJobScreen", "Lỗi khi ghi vào Job: ${e.message}")
                                Toast.makeText(navController.context, "Lỗi khi ghi vào công việc: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("ApplyJobScreen", "Lỗi khi cập nhật user: ${e.message}")
                            Toast.makeText(navController.context, "Lỗi khi lưu thông tin ứng tuyển: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else if (userId == null) {
                    Log.e("ApplyJobScreen", "Người dùng không tồn tại")
                    Toast.makeText(navController.context, "Người dùng không tồn tại", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(navController.context, "Vui lòng chọn một CV để ứng tuyển", Toast.LENGTH_SHORT).show()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
            modifier = Modifier.fillMaxWidth(),
            enabled = cvList.isNotEmpty()
        ) {
            Text("Ứng tuyển", color = Color.White)
        }

    }
}

// Hàm trích xuất tên tệp từ URL
fun getFileNameFromUrl(url: String): String {
    val fileNameWithExtension = url.substringAfterLast("/").takeIf { it.isNotEmpty() } ?: "Unknown File"
    return fileNameWithExtension.substringBeforeLast(".").takeIf { it.isNotEmpty() } ?: fileNameWithExtension
}