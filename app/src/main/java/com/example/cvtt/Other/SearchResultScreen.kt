package com.example.cvtt.Other

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.cvtt.FeaturedCompanyCard
import com.example.cvtt.Job.Job
import com.example.cvtt.Job.JobCard
import com.example.cvtt.Data.Company
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchResultScreen(
    navController: NavHostController, // 🔄 Đổi từ NavController -> NavHostController
    query: String
) {
    val jobResults = remember { mutableStateListOf<Job>() }
    val companyResults = remember { mutableStateListOf<Company>() }
    val loading = remember { mutableStateOf(true) }

    LaunchedEffect(query) {
        val db = Firebase.firestore
        jobResults.clear()
        companyResults.clear()
        loading.value = true

        // Tìm kiếm Job theo title
        db.collection("jobs")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val job = doc.toObject(Job::class.java)
                    if (job.title.contains(query, ignoreCase = true)) {
                        jobResults.add(job)
                    }
                }
                loading.value = false
            }

        // Tìm kiếm Company theo name
        db.collection("companies")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val company = doc.toObject(Company::class.java)
                    if (company.name.contains(query, ignoreCase = true)) {
                        companyResults.add(company)
                    }
                }
                loading.value = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text("Kết quả tìm kiếm", fontSize = 20.sp)
            })
        }
    ) { padding ->
        if (loading.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (jobResults.isNotEmpty()) {
                    item {
                        Text("Công việc", fontSize = 18.sp, color = Color.Black)
                    }
                    items(jobResults) { job ->
                        JobCard(job = job, navController = navController)
                    }
                }

                if (companyResults.isNotEmpty()) {
                    item {
                        Text("Công ty", fontSize = 18.sp, color = Color.Black)
                    }
                    items(companyResults) { company ->
                        FeaturedCompanyCard(company = company, navController = navController)
                    }
                }

                if (jobResults.isEmpty() && companyResults.isEmpty()) {
                    item {
                        Text("Không tìm thấy kết quả.", color = Color.Gray)
                    }
                }
            }
        }
    }
}
