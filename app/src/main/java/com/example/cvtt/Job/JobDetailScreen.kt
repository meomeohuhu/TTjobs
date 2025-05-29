package com.example.cvtt.Job
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.cvtt.R
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun JobDetailScreen(navController: NavHostController, jobId: String?) {
    val jobDetailState = remember { mutableStateOf<Job?>(null) }
    val loading = remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(jobId) {
        if (jobId != null) {
            FirebaseFirestore.getInstance().collection("jobs").document(jobId)
                .get()
                .addOnSuccessListener { document ->
                    val jobDetail = document.toObject(Job::class.java)
                    if (jobDetail != null) {
                        jobDetailState.value = jobDetail
                    }
                    loading.value = false
                }
                .addOnFailureListener { exception ->
                    println("Error getting job detail: $exception")
                    loading.value = false
                }
        }
    }

    if (loading.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val job = jobDetailState.value
        if (job != null) {
            @OptIn(ExperimentalMaterial3Api::class)
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(job.title, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF1A3C34),
                            titleContentColor = Color.White,
                            navigationIconContentColor = Color.White
                        ),
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    painter = painterResource(id = R.drawable.back),
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = job.logoUrl,
                                contentDescription = "Company Logo",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                                error = painterResource(R.drawable.user)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = job.company,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = job.salary,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00C853),
                                        modifier = Modifier
                                            .background(Color(0xFFE8F5E9), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = job.location,
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "Thông tin",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00C853),
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(end = 8.dp),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Công ty",
                                fontSize = 16.sp,
                                color = Color.Gray,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val companyId = job.companyId
                                        if (!companyId.isNullOrEmpty()) {
                                            navController.navigate("company_detail/$companyId")
                                        } else {
                                            Toast.makeText(context, "Không tìm thấy công ty", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    .padding(start = 8.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Yêu cầu ứng viên", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(job.requirements, fontSize = 14.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Thông tin chung", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(job.description, fontSize = 14.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Địa điểm làm việc", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(job.workLocation, fontSize = 14.sp, color = Color.Gray)

                        Spacer(modifier = Modifier.height(16.dp))

                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Button(
                            onClick = { navController.navigate("chat/${job.companyId}") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_email),
                                contentDescription = "Message",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Nhắn tin", color = Color.White, fontSize = 16.sp)
                        }

                        Button(
                            onClick = { navController.navigate("apply_job/${jobId}") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853)),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.heart),
                                contentDescription = "Apply",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ứng tuyển ngay", color = Color.White, fontSize = 12.sp)
                        }
                    }
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy công việc", fontSize = 16.sp, color = Color.Gray)
            }
        }
    }
}
