package com.example.cvtt.CV

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.example.cvtt.R
import com.example.cvtt.ui.theme.CVTTTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

data class CVItem(val url: String, val name: String)

@Composable
fun ManagerCV(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    var selectedTab by remember { mutableStateOf(0) }
    var cvList by remember { mutableStateOf<List<CVItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var cvToDelete by remember { mutableStateOf<CVItem?>(null) }

    // Fetch CV data from Firestore
    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            firestore.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val cvUrls = document.get("cvUrls") as? List<String> ?: emptyList()
                    cvList = cvUrls.mapNotNull { url ->
                        if (url.isNotBlank()) {
                            val cvName = url.substringAfterLast("/").substringBeforeLast(".")
                            CVItem(url, cvName)
                        } else {
                            Log.w("ManagerCV", "Invalid CV URL: $url")
                            null
                        }
                    }
                    isLoading = false
                    Log.d("ManagerCV", "Fetched CV List: $cvList")
                }
                .addOnFailureListener {
                    Log.e("ManagerCV", "Error fetching CV", it)
                    isLoading = false
                }
        }
    }

    // Xóa CV khỏi Firestore
    fun deleteCV(cv: CVItem) {
        val updatedCvUrls = cvList.map { it.url }.toMutableList().apply { remove(cv.url) }
        firestore.collection("users").document(userId)
            .update("cvUrls", updatedCvUrls)
            .addOnSuccessListener {
                cvList = cvList.filter { it.url != cv.url }
                showDeleteDialog = false
                Log.d("ManagerCV", "CV deleted successfully")
            }
            .addOnFailureListener {
                Log.e("ManagerCV", "Error deleting CV", it)
                showDeleteDialog = false
            }
    }

    Scaffold(
        bottomBar = {
            BottomNavigationBarCV(navController)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("upcv") },
                containerColor = Color(0xFF28C76F),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.add),
                    contentDescription = "Add CV",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Title
            Text(
                text = "Quản lý CV",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                textAlign = TextAlign.Center
            )

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = Color(0xFF28C76F),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("CV") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Cover Letter") }
                )
            }

            // CV Content
            if (selectedTab == 0) {
                Text(
                    text = "CV ĐÃ TẢI LÊN TopCV",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF6B7280),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                } else if (cvList.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.cv),
                                contentDescription = "CV Placeholder",
                                modifier = Modifier
                                    .size(120.dp)
                                    .padding(bottom = 8.dp)
                            )
                            Text(
                                text = "Chưa có CV nào được tải lên",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Tải lên CV có sẵn trong thiết bị để tiếp cận với nhà tuyển dụng",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Button(
                                onClick = { navController.navigate("upcv") },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F))
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.plus),
                                    contentDescription = "Add Icon",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Tải CV ngay")
                            }
                        }
                    }
                } else {
                    LazyColumn {
                        items(cvList) { cv ->
                            var showDeleteButton by remember { mutableStateOf(false) }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .longPressable(
                                        onLongPress = { showDeleteButton = !showDeleteButton }
                                    ),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.cv),
                                        contentDescription = "CV Icon",
                                        modifier = Modifier
                                            .size(40.dp)
                                            .padding(end = 12.dp)
                                    )
                                    Column(
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = cv.name,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF1A3C34)
                                        )
                                    }
                                    AnimatedVisibility(visible = showDeleteButton) {
                                        IconButton(
                                            onClick = {
                                                cvToDelete = cv
                                                showDeleteDialog = true
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(id = R.drawable.delete),
                                                contentDescription = "Delete CV",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    IconButton(onClick = { /* Xem hoặc tải CV */ }) {
                                        Icon(
                                            painter = painterResource(id = R.drawable.download),
                                            contentDescription = "Download",
                                            tint = Color(0xFF28C76F),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Cover Letter Tab (Placeholder)
                Text(
                    text = "Chưa có Cover Letter",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.Center)
                )
            }
        }

        // Dialog xác nhận xóa
        if (showDeleteDialog && cvToDelete != null) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Xác nhận xóa") },
                text = { Text("Bạn có chắc muốn xóa CV '${cvToDelete!!.name}' không?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            cvToDelete?.let { deleteCV(it) }
                        }
                    ) {
                        Text("Xóa", color = Color(0xFFEF4444))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Hủy")
                    }
                }
            )
        }
    }
}

@Composable
fun BottomNavigationBarCV(navController: NavHostController) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = Color(0xFF28C76F)
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.home),
                    contentDescription = "Trang chủ"
                )
            },
            label = { Text("Trang chủ") },
            selected = false,
            onClick = { navController.navigate("home") }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.cv),
                    contentDescription = "CV & Profile"
                )
            },
            label = { Text("CV & Profile") },
            selected = true,
            onClick = { navController.navigate("cv") }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.connect),
                    contentDescription = "Top Connect"
                )
            },
            label = { Text("Top Connect") },
            selected = false,
            onClick = { navController.navigate("top_connect") }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.profile),
                    contentDescription = "Tài khoản"
                )
            },
            label = { Text("Tài khoản") },
            selected = false,
            onClick = { navController.navigate("profile") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ManagerCVPreview() {
    CVTTTheme {
        val navController = rememberNavController()
        ManagerCV(navController = navController)
    }
}

// Thêm modifier longPressable
@OptIn(ExperimentalFoundationApi::class)
fun Modifier.longPressable(
    onLongPress: () -> Unit
) = this.then(
    Modifier
        .combinedClickable(
            onClick = {},
            onLongClick = onLongPress
        )
)