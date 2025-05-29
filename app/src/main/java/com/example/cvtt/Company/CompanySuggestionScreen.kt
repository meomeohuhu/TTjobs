package com.example.cvtt.Company

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.cvtt.R
import com.example.cvtt.Data.Company
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.jvm.java
import com.google.firebase.auth.FirebaseAuth

@Composable
fun CompanySuggestionScreen(navController: NavHostController) {
    val companyList = remember { mutableStateListOf<Company>() }
    val loading = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        FirebaseFirestore.getInstance().collection("companies")
            .get()
            .addOnSuccessListener { result ->
                companyList.clear()
                for (document in result) {
                    val company = document.toObject(Company::class.java).copy(id = document.id)
                    companyList.add(company)
                }
                loading.value = false
            }
            .addOnFailureListener {
                loading.value = false
                // Bạn có thể hiển thị Snackbar hoặc thông báo lỗi ở đây
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
                    title = { Text("Danh sách công ty", fontSize = 20.sp, fontWeight = FontWeight.Bold) },
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
                items(companyList) { company ->
                    CompanyCard(company = company, navController = navController)
                }

            }
        }
    }
}

@Composable
fun CompanyCard(company: Company, navController: NavHostController) {
    val db = FirebaseFirestore.getInstance()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var isFollowing by remember { mutableStateOf(false) }

    // Lấy tài liệu người dùng từ Firestore
    LaunchedEffect(company.id) {
        currentUserId?.let { userId ->
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val savedCompanies = document.get("followedCompanies") as? List<*>
                    isFollowing = savedCompanies?.contains(company.id) == true
                }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                navController.navigate("company_detail/${company.id}")
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = company.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = company.description.take(20),
                    fontSize = 14.sp,
                    color = Color.Gray,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = company.location,
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            // Nút Theo dõi
            Button(
                onClick = {
                    isFollowing = !isFollowing
                    currentUserId?.let { userId ->
                        val userDocRef = db.collection("users").document(userId)
                        val updateAction = if (isFollowing) {
                            FieldValue.arrayUnion(company.id)
                        } else {
                            FieldValue.arrayRemove(company.id)
                        }
                        userDocRef.update("followedCompanies", updateAction)
                    }
                },
                modifier = Modifier
                    .height(40.dp)
                    .clip(RoundedCornerShape(20.dp)),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) Color.Gray else Color(0xFF28C76F)
                )
            ) {
                Text(
                    text = if (isFollowing) "Đã theo dõi" else "+ Theo dõi",
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}
