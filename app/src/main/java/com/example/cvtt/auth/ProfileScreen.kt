package com.example.cvtt.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.cvtt.BottomNavigationBar
import com.example.cvtt.R
import com.example.cvtt.Data.Company
import com.example.cvtt.Data.Job
import com.example.cvtt.Data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ProfileScreen(navController: NavHostController, userId: String, userData: User?) {
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()

    // State để quản lý danh sách chi tiết khi nhấn vào từng mục
    var savedJobsDetails by remember { mutableStateOf<List<Job>>(emptyList()) }
    var appliedJobsDetails by remember { mutableStateOf<List<Job>>(emptyList()) }
    var suitableJobsDetails by remember { mutableStateOf<List<Job>>(emptyList()) }
    var followedCompaniesDetails by remember { mutableStateOf<List<Company>>(emptyList()) }
    var profileViewsDetails by remember { mutableStateOf<List<Company>>(emptyList()) }

    // State để quản lý trạng thái hiển thị danh sách chi tiết
    var showAppliedJobs by remember { mutableStateOf(false) }
    var showSuitableJobs by remember { mutableStateOf(false) }
    var showProfileViews by remember { mutableStateOf(false) }

    // Hàm truy vấn chi tiết công việc từ Firestore
    fun fetchJobs(jobIds: List<String>, onSuccess: (List<Job>) -> Unit) {
        val jobs = mutableListOf<Job>()
        if (jobIds.isEmpty()) {
            onSuccess(jobs)
            return
        }
        jobIds.forEach { jobId ->
            firestore.collection("jobs").document(jobId).get()
                .addOnSuccessListener { document ->
                    document.toObject(Job::class.java)?.let { job ->
                        jobs.add(job.copy(id = document.id))
                        if (jobs.size == jobIds.size) {
                            onSuccess(jobs)
                        }
                    }
                }
                .addOnFailureListener {
                    if (jobs.size == jobIds.size) {
                        onSuccess(jobs)
                    }
                }
        }
    }

    // Hàm truy vấn chi tiết công ty từ Firestore
    fun fetchCompanies(companyIds: List<String>, onSuccess: (List<Company>) -> Unit) {
        val companies = mutableListOf<Company>()
        if (companyIds.isEmpty()) {
            onSuccess(companies)
            return
        }
        companyIds.forEach { companyId ->
            firestore.collection("companies").document(companyId).get()
                .addOnSuccessListener { document ->
                    document.toObject(Company::class.java)?.let { company ->
                        companies.add(company.copy(id = document.id))
                        if (companies.size == companyIds.size) {
                            onSuccess(companies)
                        }
                    }
                }
                .addOnFailureListener {
                    if (companies.size == companyIds.size) {
                        onSuccess(companies)
                    }
                }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Tài khoản",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A3C34),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .background(Color(0xFFF5F5F5))
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Thông tin cá nhân
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (userData?.profilePictureUrl != null) {
                        AsyncImage(
                            model = userData.profilePictureUrl,
                            contentDescription = "Ảnh đại diện",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable { navController.navigate("userProfileScreen") },
                            contentScale = ContentScale.Crop,
                            error = painterResource(R.drawable.picture)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.picture),
                            contentDescription = "Ảnh đại diện",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable { navController.navigate("userProfileScreen") }
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = userData?.name ?: "Không có tên",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            // Quản lý tìm việc
            Text(
                text = "Quản lý tìm việc",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Việc làm đã ứng tuyển
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("apply_job?userId=$userId")
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Việc làm đã ứng tuyển",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = "${userData?.savedApply?.size ?: 0}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Icon(
                            painter = painterResource(id = R.drawable.check),
                            contentDescription = "Check Icon",
                            tint = Color(0xFF28C76F),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // Việc làm đã lưu
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("saved_jobs?userId=$userId")
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Việc làm đã lưu",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = "${userData?.savedJobs?.size ?: 0}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Icon(
                            painter = painterResource(id = R.drawable.bookmark),
                            contentDescription = "Bookmark Icon",
                            tint = Color(0xFF28C76F),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                navController.navigate("followed_companies?userId=$userId")
                            }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Công ty đang theo dõi",
                                fontSize = 14.sp,
                                color = Color(0xFF6B7280)
                            )
                            Text(
                                text = "${userData?.followedCompanies?.size ?: 0}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                        Icon(
                            painter = painterResource(id = R.drawable.company),
                            contentDescription = "Company Icon",
                            tint = Color(0xFF28C76F),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            // Nút Đăng xuất
            Button(
                onClick = { logout(auth, navController, context) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Đăng xuất", fontSize = 16.sp, color = Color.White)
            }
        }
    }
}

// Component cho mỗi công việc đã lưu
@Composable
fun SavedJobItem(
    job: Job,
    userId: String,
    firestore: FirebaseFirestore,
    onApplyClick: () -> Unit,
    onJobClick: () -> Unit,
    savedJobsDetails: List<Job>,
    updateSavedJobsDetails: (List<Job>) -> Unit
) {
    var isFavorite by remember { mutableStateOf(true) } // Ban đầu là true vì đây là danh sách đã lưu

    // Đồng bộ trạng thái yêu thích từ Firestore
    LaunchedEffect(job.id) {
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                val savedJobs = document.get("savedJobs") as? List<*>
                isFavorite = savedJobs?.contains(job.id) == true
            }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onJobClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo công ty
            AsyncImage(
                model = job.logoUrl,
                contentDescription = "Company Logo",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.picture)
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Thông tin công việc
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = job.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Text(
                    text = job.company,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280)
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = job.salary,
                        fontSize = 14.sp,
                        color = Color(0xFF28C76F),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = job.location,
                        fontSize = 14.sp,
                        color = Color(0xFF6B7280)
                    )
                }
            }

            // Cột bên phải (biểu tượng yêu thích và nút ứng tuyển)
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.height(IntrinsicSize.Min)
            ) {
                IconButton(
                    onClick = {
                        isFavorite = !isFavorite
                        // Cập nhật Firestore
                        firestore.collection("users").document(userId).get()
                            .addOnSuccessListener { document ->
                                val userData = document.toObject(User::class.java)
                                val currentSavedJobs = userData?.savedJobs?.toMutableList() ?: mutableListOf()

                                if (isFavorite) {
                                    // Thêm job.id vào savedJobs
                                    if (!currentSavedJobs.contains(job.id)) {
                                        currentSavedJobs.add(job.id)
                                    }
                                } else {
                                    // Xóa job.id khỏi savedJobs
                                    currentSavedJobs.remove(job.id)
                                    // Cập nhật danh sách hiển thị
                                    updateSavedJobsDetails(savedJobsDetails.filter { it.id != job.id })
                                }

                                // Cập nhật lại Firestore
                                firestore.collection("users").document(userId)
                                    .update("savedJobs", currentSavedJobs)
                                    .addOnSuccessListener {
                                        // Thành công
                                    }
                                    .addOnFailureListener {
                                        // Xử lý lỗi (khôi phục trạng thái)
                                        isFavorite = !isFavorite
                                    }
                            }
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        painter = painterResource(id = if (isFavorite) R.drawable.heart else R.drawable.heart),
                        contentDescription = "Favorite Icon",
                        tint = if (isFavorite) Color.Red else Color(0xFF888888)
                    )
                }
                Button(
                    onClick = onApplyClick,
                    modifier = Modifier
                        .height(30.dp)
                        .width(140.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ứng tuyển", fontSize = 12.sp, color = Color.White)
                }
            }
        }
    }
}

// Component cho mỗi công ty
@Composable
fun CompanyItem(company: Company, onCompanyClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onCompanyClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (company.logoUrl != null) {
                AsyncImage(
                    model = company.logoUrl,
                    contentDescription = "Company Logo",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.picture),
                    contentDescription = "Company Logo",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = company.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
        }
    }
}

// Giao diện riêng cho "Việc làm đã lưu"
@Composable
fun SavedJobsScreen(navController: NavHostController, userId: String, firestore: FirebaseFirestore) {
    var savedJobsDetails by remember { mutableStateOf<List<Job>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Hàm truy vấn chi tiết công việc từ Firestore
    fun fetchJobs(jobIds: List<String>, onSuccess: (List<Job>) -> Unit) {
        val jobs = mutableListOf<Job>()
        if (jobIds.isEmpty()) {
            onSuccess(jobs)
            return
        }
        jobIds.forEach { jobId ->
            firestore.collection("jobs").document(jobId).get()
                .addOnSuccessListener { document ->
                    document.toObject(Job::class.java)?.let { job ->
                        jobs.add(job.copy(id = document.id))
                        if (jobs.size == jobIds.size) {
                            onSuccess(jobs)
                        }
                    }
                }
                .addOnFailureListener {
                    if (jobs.size == jobIds.size) {
                        onSuccess(jobs)
                    }
                }
        }
    }

    // Truy vấn danh sách công việc đã lưu khi khởi tạo
    LaunchedEffect(Unit) {
        if (userId.isBlank()) {
            errorMessage = "Không tìm thấy userId. Vui lòng đăng nhập lại."
            return@LaunchedEffect
        }

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    errorMessage = "Không tìm thấy dữ liệu người dùng."
                    return@addOnSuccessListener
                }
                // Ép kiểu savedJobIds thành List<String> một cách rõ ràng
                val savedJobIds = (document.get("savedJobs") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.toList() ?: emptyList()
                fetchJobs(savedJobIds) { jobs: List<Job> ->
                    savedJobsDetails = jobs
                }
            }
            .addOnFailureListener { e ->
                errorMessage = "Lỗi khi truy vấn dữ liệu: ${e.message}"
            }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Việc làm đã lưu",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A3C34),
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Back Icon",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .background(Color(0xFFF5F5F5))
        ) {
            errorMessage?.let { message ->
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
            } ?: run {
                if (savedJobsDetails.isEmpty()) {
                    Text(
                        text = "Không có việc làm đã lưu",
                        fontSize = 14.sp,
                        color = Color(0xFF888888),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn {
                        items(savedJobsDetails) { job ->
                            SavedJobItem(
                                job = job,
                                userId = userId,
                                firestore = firestore,
                                onApplyClick = { /* Handle apply */ },
                                onJobClick = { navController.navigate("job_detail/${job.id}") },
                                savedJobsDetails = savedJobsDetails,
                                updateSavedJobsDetails = { updatedJobs -> savedJobsDetails = updatedJobs }
                            )
                        }
                    }
                }
            }
        }
    }
}

// đã ứng tuyển
// Giao diện riêng cho "Việc làm đã lưu"
@Composable
fun Applyjob(navController: NavHostController, userId: String, firestore: FirebaseFirestore) {
    var savedJobsDetails by remember { mutableStateOf<List<Job>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Hàm truy vấn chi tiết công việc từ Firestore
    fun fetchJobs(jobIds: List<String>, onSuccess: (List<Job>) -> Unit) {
        val jobs = mutableListOf<Job>()
        if (jobIds.isEmpty()) {
            onSuccess(jobs)
            return
        }
        jobIds.forEach { jobId ->
            firestore.collection("jobs").document(jobId).get()
                .addOnSuccessListener { document ->
                    document.toObject(Job::class.java)?.let { job ->
                        jobs.add(job.copy(id = document.id))
                        if (jobs.size == jobIds.size) {
                            onSuccess(jobs)
                        }
                    }
                }
                .addOnFailureListener {
                    if (jobs.size == jobIds.size) {
                        onSuccess(jobs)
                    }
                }
        }
    }

    // Truy vấn danh sách công việc đã lưu khi khởi tạo
    LaunchedEffect(Unit) {
        if (userId.isBlank()) {
            errorMessage = "Không tìm thấy userId. Vui lòng đăng nhập lại."
            return@LaunchedEffect
        }

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    errorMessage = "Không tìm thấy dữ liệu người dùng."
                    return@addOnSuccessListener
                }
                // Ép kiểu savedJobIds thành List<String> một cách rõ ràng
                val savedJobIds = (document.get("savedApply") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.toList() ?: emptyList()
                fetchJobs(savedJobIds) { jobs: List<Job> ->
                    savedJobsDetails = jobs
                }
            }
            .addOnFailureListener { e ->
                errorMessage = "Lỗi khi truy vấn dữ liệu: ${e.message}"
            }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Việc làm đã ứng tuyển",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A3C34),
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Back Icon",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .background(Color(0xFFF5F5F5))
        ) {
            errorMessage?.let { message ->
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
            } ?: run {
                if (savedJobsDetails.isEmpty()) {
                    Text(
                        text = "Không có việc làm đã lưu",
                        fontSize = 14.sp,
                        color = Color(0xFF888888),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn {
                        items(savedJobsDetails) { job ->
                            // 1. Lấy statusKey từ job.statusApplied map với key = userId
                            val statusKey = job.statusApplied[userId] ?: "pending"
                            // 2. Chuyển thành text dễ đọc
                            val statusText = when (statusKey) {
                                "accepted" -> "Được nhận"
                                "rejected" -> "Từ chối"
                                else       -> "Chờ xử lý"
                            }

                            // 3. Hiển thị item + status
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { navController.navigate("job_detail/${job.id}") }
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = job.title, style = MaterialTheme.typography.titleMedium)
                                        Spacer(Modifier.height(4.dp))
                                        Text(text = "Công ty: ${job.company}", style = MaterialTheme.typography.bodyMedium)
                                        Spacer(Modifier.height(4.dp))
                                        Text(text = "Địa điểm: ${job.location}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(
                                        text = statusText,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun FollowedCompaniesScreen(navController: NavHostController, userId: String, firestore: FirebaseFirestore) {
    var followedCompaniesDetails by remember { mutableStateOf<List<Company>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Hàm truy vấn chi tiết công ty từ Firestore
    fun fetchCompanies(companyIds: List<String>, onSuccess: (List<Company>) -> Unit) {
        val companies = mutableListOf<Company>()
        if (companyIds.isEmpty()) {
            onSuccess(companies)
            return
        }
        companyIds.forEachIndexed { index, companyId ->
            firestore.collection("companies").document(companyId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val company = document.toObject(Company::class.java)?.copy(id = document.id)
                        company?.let {
                            companies.add(it)
                            println("Company loaded: $it") // Log dữ liệu công ty
                        }
                    } else {
                        println("Document $companyId does not exist")
                    }
                    // Kiểm tra khi tất cả tài liệu đã được xử lý
                    if (index == companyIds.size - 1) {
                        onSuccess(companies)
                    }
                }
                .addOnFailureListener { e ->
                    println("Error loading company $companyId: ${e.message}")
                    if (index == companyIds.size - 1) {
                        onSuccess(companies) // Tiếp tục với các công ty đã tải được
                    }
                }
        }
    }

    // Truy vấn danh sách công ty đang theo dõi khi khởi tạo
    LaunchedEffect(Unit) {
        if (userId.isBlank()) {
            errorMessage = "Không tìm thấy userId. Vui lòng đăng nhập lại."
            navController.navigate("login") { popUpTo(0) }
            return@LaunchedEffect
        }

        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    errorMessage = "Không tìm thấy dữ liệu người dùng."
                    return@addOnSuccessListener
                }
                // Xử lý trường hợp followedCompanies là null hoặc không tồn tại
                val followedCompanyIds = (document.get("followedCompanies") as? List<*>)
                    ?.filterIsInstance<String>()
                    ?.toList() ?: emptyList()
                println("Followed company IDs: $followedCompanyIds") // Log danh sách ID công ty
                fetchCompanies(followedCompanyIds) { companies ->
                    followedCompaniesDetails = companies
                }
            }
            .addOnFailureListener { e ->
                errorMessage = "Lỗi khi truy vấn dữ liệu: ${e.message}"
            }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Công ty đang theo dõi",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1A3C34),
                    titleContentColor = Color.White
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Back Icon",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(navController)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .background(Color(0xFFF5F5F5))
        ) {
            errorMessage?.let { message ->
                Text(
                    text = message,
                    fontSize = 14.sp,
                    color = Color.Red,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .wrapContentHeight(align = Alignment.CenterVertically),
                    textAlign = TextAlign.Center
                )
            } ?: run {
                if (followedCompaniesDetails.isEmpty()) {
                    Text(
                        text = "Không có công ty nào đang theo dõi",
                        fontSize = 14.sp,
                        color = Color(0xFF888888),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .wrapContentHeight(align = Alignment.CenterVertically),
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn {
                        items(followedCompaniesDetails) { company ->
                            // Giao diện công ty trực tiếp trong LazyColumn
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable { navController.navigate("company_detail/${company.id}") },
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Logo công ty
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(CircleShape)
                                    ) {
                                        if (company.logoUrl.isNotBlank()) {
                                            AsyncImage(
                                                model = company.logoUrl,
                                                contentDescription = "Company Logo",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop,
                                                error = painterResource(R.drawable.picture)
                                            )
                                        } else {
                                            Image(
                                                painter = painterResource(id = R.drawable.picture),
                                                contentDescription = "Company Logo",
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))

                                    // Thông tin công ty
                                    Column(modifier = Modifier.weight(1f)) {
                                        // Tên công ty
                                        Text(
                                            text = company.name.ifEmpty { "Tên không có" },
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF1A3C34),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        // Lĩnh vực (jobtype)
                                        Text(
                                            text = "Lĩnh vực: ${company.jobtype.ifEmpty { "Chưa cập nhật" }}",
                                            fontSize = 14.sp,
                                            color = Color(0xFF6B7280),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )

                                        // Số lượng công việc và Số người theo dõi
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Công việc: ${company.jobCount.ifEmpty { "0" }}",
                                                fontSize = 14.sp,
                                                color = Color(0xFF6B7280)
                                            )
                                            Text(
                                                text = "Theo dõi: ${company.countfollow.ifEmpty { "0" }}",
                                                fontSize = 14.sp,
                                                color = Color(0xFF6B7280)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

