package com.example.cvtt.Company

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.cvtt.Job.Job
import com.example.cvtt.Job.JobCard
import com.example.cvtt.R
import com.example.cvtt.Data.Company
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun CompanyDetailScreen(navController: NavHostController, companyId: String?) {
    val companyDetailState = remember { mutableStateOf<Company?>(null) }
    val jobList = remember { mutableStateListOf<Job>() }
    val loading = remember { mutableStateOf(true) }

    LaunchedEffect(companyId) {
        if (companyId != null) {
            // Lấy thông tin công ty
            FirebaseFirestore.getInstance().collection("companies").document(companyId)
                .get()
                .addOnSuccessListener { document ->
                    val companyDetail = document.toObject(Company::class.java)
                    if (companyDetail != null) {
                        companyDetailState.value = companyDetail
                        // Lấy danh sách công việc thuộc công ty
                        fetchJobsForCompany(companyDetail.jobIds, jobList)
                    }
                    loading.value = false
                }
                .addOnFailureListener { exception ->
                    println("Error getting company detail: $exception")
                    loading.value = false
                }
        }
    }

    if (loading.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        val company = companyDetailState.value
        if (company != null) {
            @OptIn(ExperimentalMaterial3Api::class)
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(company.name, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
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
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(Color(0xFFF5F5F5))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        // Logo và thông tin cơ bản
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = company.logoUrl,
                                contentDescription = "Company Logo",
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                                error = painterResource(R.drawable.user)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(company.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(company.location, fontSize = 14.sp, color = Color.Gray)
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Thông tin", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF00C853))
                            Text("Công việc", fontSize = 16.sp, color = Color.Gray)
                        }
                    }

                    item {
                        Text("Mô tả công ty", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(company.description, fontSize = 14.sp, color = Color.Gray)
                    }

                    item {
                        Text("Công việc đang tuyển", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    if (jobList.isEmpty()) {
                        item {
                            Text("Không có công việc nào", fontSize = 14.sp, color = Color.Gray)
                        }
                    } else {
                        items(jobList) { job ->
                            JobCard(job = job, navController = navController)
                        }
                    }
                }

            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Không tìm thấy công ty", fontSize = 16.sp, color = Color.Gray)
            }
        }
    }
}

// Hàm lấy danh sách công việc dựa trên jobIds
fun fetchJobsForCompany(jobIds: List<String>, jobList: MutableList<Job>) {
    jobList.clear()
    if (jobIds.isNotEmpty()) {
        FirebaseFirestore.getInstance().collection("jobs")
            .whereIn("id", jobIds)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val job = document.toObject(Job::class.java).copy(id = document.id)
                    jobList.add(job)
                }
            }
            .addOnFailureListener { exception ->
                println("Error fetching jobs: $exception")
            }
    }
}