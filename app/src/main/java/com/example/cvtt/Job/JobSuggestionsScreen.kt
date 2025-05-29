package com.example.cvtt.Job

import android.R.attr.text
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.cvtt.R
import com.example.cvtt.ui.theme.CVTTTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

data class Job(
    val id: String = "",
    val logoUrl: String = "",
    val title: String = "",
    val description: String = "",
    val salary: String = "",
    val location: String = "",
    val company: String = "",
    val requirements: String = "",
    val generalInfo: String = "",
    val workLocation: String = "",
    val applyDeadline: String = "",
    val jobCount: String = "",
    val companyId:String=""
)

@Composable
fun JobSuggestionScreen(navController: NavHostController) {
    val jobList = remember { mutableStateListOf<Job>() }
    val loading = remember { mutableStateOf(true) }

    // Logic lấy dữ liệu từ Firestore (giữ nguyên)
    LaunchedEffect(Unit) {
        Firebase.firestore.collection("jobs")
            .get()
            .addOnSuccessListener { result ->
                jobList.clear()
                for (document in result) {
                    val job = document.toObject(Job::class.java).copy(id = document.id)
                    jobList.add(job)
                }
                loading.value = false
            }
            .addOnFailureListener {
                loading.value = false
            }
    }

    if (loading.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        @OptIn(ExperimentalMaterial3Api::class)
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Gợi ý việc làm phù hợp",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF1A3C34),
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF5F5F5)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(jobList) { job ->
                    JobCard(job = job, navController = navController)
                }
            }
        }
    }
}

@Composable
fun JobCard(job: Job, navController: NavHostController) {
    val db = Firebase.firestore
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var isFavorite by remember { mutableStateOf(false) }

    // Đồng bộ trạng thái yêu thích từ Firestore
    LaunchedEffect(job.id) {
        currentUserId?.let { userId ->
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val savedJobs = document.get("savedJobs") as? List<*>
                    isFavorite = savedJobs?.contains(job.id) == true
                }
        }
        Log.d("JobLogoURL", job.logoUrl)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                navController.navigate("job_detail/${job.id}")
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = job.logoUrl,  // Đảm bảo job.logoUrl là URL hợp lệ
                contentDescription = "Company Logo",
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.picture)

            )



            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = job.title.take(16) + if (job.title.length > 20) "..." else "",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.salary), // icon lương (đặt trong drawable)
                        contentDescription = null,
                        tint = Color(0xFF28C76F), // màu xanh lá
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lương: " + job.salary,
                        fontSize = 14.sp,
                        color = Color(0xFF888888)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.location), // icon vị trí
                        contentDescription = null,
                        tint = Color(0xFF28C76F),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Địa điểm: " + job.location,
                        fontSize = 14.sp,
                        color = Color(0xFF888888)
                    )
                }

            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                IconButton(
                    onClick = {
                        isFavorite = !isFavorite
                        currentUserId?.let { userId ->
                            val userDocRef = db.collection("users").document(userId)
                            val updateAction = if (isFavorite) {
                                FieldValue.arrayUnion(job.id)
                            } else {
                                FieldValue.arrayRemove(job.id)
                            }
                            userDocRef.update("savedJobs", updateAction)
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        painter = painterResource(id = if (isFavorite) R.drawable.filter else R.drawable.heart),
                        contentDescription = "Favorite Icon",
                        tint = if (isFavorite) Color.Red else Color(0xFF888888)
                    )
                }

                Button(
                    onClick = {navController.navigate("apply_job/${job.id}")},
                    modifier = Modifier
                        .height(32.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F)),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Text(text = "Ứng tuyển ngay", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun JobSuggestionScreenPreview() {
    CVTTTheme {
        val navController = rememberNavController()
        JobSuggestionScreen(navController = navController)
    }
}