package com.example.cvtt

import android.media.MediaPlayer
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.cvtt.Job.Job
import com.example.cvtt.Job.JobCard
import com.example.cvtt.Data.Company
import com.example.cvtt.Data.Podcast
import com.example.cvtt.Data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

data class News(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val url: String = ""
)

@Composable
fun HomeScreen(
    navController: NavHostController,
    userData: User?,
    onAdminClick: () -> Unit
) {
    val jobList = remember { mutableStateListOf<Job>() }
    val podList = remember { mutableStateListOf<Podcast>() }
    val newsList = remember { mutableStateListOf<News>() }
    val companyList = remember { mutableStateListOf<Company>() }
    val loading = remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }

    // Filter jobs and companies based on search query
    val filteredJobs = remember(searchQuery, jobList) {
        if (searchQuery.isEmpty()) emptyList() else
            jobList.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }
    val filteredCompanies = remember(searchQuery, companyList) {
        if (searchQuery.isEmpty()) emptyList() else
            companyList.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    // Fetch jobs
    LaunchedEffect(Unit) {
        Firebase.firestore.collection("jobs")
            .get()
            .addOnSuccessListener { result ->
                jobList.clear()
                for (doc in result) {
                    val job = doc.toObject(Job::class.java).copy(id = doc.id)
                    jobList.add(job)
                }
                loading.value = false
            }
            .addOnFailureListener {
                loading.value = false
            }
    }

    // Fetch companies
    LaunchedEffect(Unit) {
        Firebase.firestore.collection("companies")
            .get()
            .addOnSuccessListener { result ->
                companyList.clear()
                for (doc in result) {
                    val company = doc.toObject(Company::class.java).copy(id = doc.id)
                    companyList.add(company)
                }
                loading.value = false
            }
            .addOnFailureListener {
                loading.value = false
            }
    }

    // Fetch podcasts
    LaunchedEffect(Unit) {
        Firebase.firestore.collection("podcasts")
            .get()
            .addOnSuccessListener { result ->
                podList.clear()
                for (doc in result) {
                    val podcast = doc.toObject(Podcast::class.java).copy(id = doc.id)
                    podList.add(podcast)
                }
            }
    }

    // Fetch news
    LaunchedEffect(Unit) {
        Firebase.firestore.collection("news")
            .get()
            .addOnSuccessListener { result ->
                newsList.clear()
                for (doc in result) {
                    val news = doc.toObject(News::class.java).copy(id = doc.id)
                    newsList.add(news)
                }
            }
            .addOnFailureListener {
                // Handle error if needed
            }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "TTJOBs Logo",
                            modifier = Modifier.size(32.dp),
                            tint = Color.Unspecified
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "TTJOBs",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF28C76F),
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = { BottomNavigationBar(navController) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            // Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp, color = Color.Black),
                    decorationBox = { inner ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.search),
                                contentDescription = "Search",
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFF888888)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (searchQuery.isEmpty()) {
                                Text(
                                    "Tìm kiếm công việc hoặc công ty",
                                    fontSize = 16.sp,
                                    color = Color(0xFF888888)
                                )
                            }
                            inner()
                        }
                    }
                )
            }

            if (loading.value) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFFF5F5F5)),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (searchQuery.isNotEmpty()) {
                        // Search Results
                        item {
                            Text(
                                "Kết quả tìm kiếm: \"$searchQuery\"",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        if (filteredJobs.isNotEmpty()) {
                            item {
                                Text(
                                    "Công việc",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(filteredJobs) { job ->
                                JobSearchCard(job = job, navController = navController)
                            }
                        }

                        if (filteredCompanies.isNotEmpty()) {
                            item {
                                Text(
                                    "Công ty",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                            items(filteredCompanies) { company ->
                                CompanySearchCard(company = company, navController = navController)
                            }
                        }

                        if (filteredJobs.isEmpty() && filteredCompanies.isEmpty()) {
                            item {
                                Text(
                                    "Không tìm thấy kết quả",
                                    fontSize = 16.sp,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    } else {
                        // Default Home Screen Content
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                NavigationItem("Việc làm", R.drawable.job) { navController.navigate("job_suggestion") }
                                NavigationItem("Công ty", R.drawable.company) { navController.navigate("company_suggestion") }
                                NavigationItem("Podcast", R.drawable.podcast) { navController.navigate("podcast") }
                            }
                        }

                        item {
                            Button(
                                onClick = { /* TODO */ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .height(48.dp),
                                shape = RoundedCornerShape(24.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F))
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        painter = painterResource(R.drawable.map),
                                        contentDescription = "Map",
                                        modifier = Modifier.size(20.dp),
                                        tint = Color.White
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "TTJOBs vì 1 tương lai tốt đẹp",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        item {
                            Text(
                                "Gợi ý việc làm phù hợp",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }

                        items(jobList.take(4)) { job ->
                            JobCard(job = job, navController = navController)
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Trung tâm công ty tiêu biểu",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    "Xem tất cả",
                                    fontSize = 14.sp,
                                    color = Color(0xFF28C76F),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .clickable { navController.navigate("company_suggestion") }
                                        .padding(8.dp),
                                    textDecoration = TextDecoration.Underline
                                )
                            }
                        }

                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(380.dp)
                            ) {
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(2),
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    items(companyList.take(4)) { company ->
                                        FeaturedCompanyCard(company = company, navController = navController)
                                    }
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Tin tức",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                newsList.take(3).forEach { news ->
                                    NewsCard(news = news, navController = navController)
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Podcast",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    "Xem tất cả",
                                    fontSize = 14.sp,
                                    color = Color(0xFF28C76F),
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .clickable { navController.navigate("podcast") }
                                        .padding(8.dp),
                                    textDecoration = TextDecoration.Underline
                                )
                            }
                        }
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                podList.take(3).forEach { podcast ->
                                    PodcastCardd(podcast = podcast)
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
fun JobSearchCard(job: Job, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("job_detail/${job.id}") },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = job.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = job.title ?: "Không có tên công ty",
                fontSize = 14.sp,
                color = Color(0xFF6B7280),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CompanySearchCard(company: Company, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { navController.navigate("company_detail/${company.id}") },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                if (company.logoUrl.isNotBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(model = company.logoUrl),
                        contentDescription = "Company Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.picture),
                        contentDescription = "Default Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = company.name.ifEmpty { "Tên không có" },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = company.jobtype.ifEmpty { "Chưa cập nhật" },
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun NavigationItem(text: String, iconRes: Int, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = text,
            modifier = Modifier.size(32.dp),
            tint = Color.Unspecified
        )
        Spacer(Modifier.height(4.dp))
        Text(text, fontSize = 14.sp, color = Color(0xFF333333))
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val db = Firebase.firestore
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var userLevel by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(currentUserId) {
        currentUserId?.let { uid ->
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        userLevel = document.getLong("level")?.toInt()
                    }
                }
        }
    }

    NavigationBar(
        containerColor = Color.White,
        contentColor = Color(0xFF333333)
    ) {
        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.home), null) },
            label = { Text("Trang chủ") },
            selected = true,
            onClick = { navController.navigate("home") }
        )
        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.cv), null) },
            label = { Text("CV & Profile") },
            selected = false,
            onClick = { navController.navigate("cv") }
        )
        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.connect), null) },
            label = { Text("Top Connect") },
            selected = false,
            onClick = { navController.navigate("messages") }
        )
        NavigationBarItem(
            icon = { Icon(painterResource(R.drawable.user), null) },
            label = { Text("Tài khoản") },
            selected = false,
            onClick = { navController.navigate("profile") }
        )

        if (userLevel == 1) {
            NavigationBarItem(
                icon = { Icon(painterResource(R.drawable.user), null) },
                label = { Text("Admin") },
                selected = false,
                onClick = { navController.navigate("admin") }
            )
        }
        if (userLevel == 2) {
            NavigationBarItem(
                icon = { Icon(painterResource(R.drawable.user), null) },
                label = { Text("Company") },
                selected = false,
                onClick = { navController.navigate("company") }
            )
        }
    }
}

@Composable
fun FeaturedCompanyCard(company: Company, navController: NavHostController) {
    val db = Firebase.firestore
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
    var isFollowing by remember { mutableStateOf(false) }

    LaunchedEffect(company.id) {
        currentUserId?.let { userId ->
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    val followedCompanies = document.get("followedCompanies") as? List<*>
                    isFollowing = followedCompanies?.contains(company.id) == true
                }
        }
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable { navController.navigate("company_detail/${company.id}") },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                if (company.logoUrl.isNotBlank()) {
                    Image(
                        painter = rememberAsyncImagePainter(model = company.logoUrl),
                        contentDescription = "Company Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.picture),
                        contentDescription = "Default Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = company.name.ifEmpty { "Tên không có" },
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A3C34),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = company.jobtype.ifEmpty { "Chưa cập nhật" },
                fontSize = 12.sp,
                color = Color(0xFF6B7280),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
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
                    .height(32.dp)
                    .width(150.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) Color.Gray else Color(0xFF28C76F)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (isFollowing) "Đã theo dõi" else "+ Theo dõi",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun NewsCard(news: News, navController: NavHostController) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { navController.navigate("news_detail/${news.id}") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = news.url,
                contentDescription = news.title,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop,
                error = painterResource(R.drawable.picture)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = news.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    color = Color.Black,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = news.description,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun PodcastCardd(podcast: Podcast) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AsyncImage(
                model = podcast.thumbnailUrl,
                contentDescription = podcast.title,
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = podcast.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = podcast.duration ?: "Không có thời gian",
                    fontSize = 14.sp,
                    maxLines = 1,
                    color = Color.Gray
                )
            }

            IconButton(
                onClick = {
                    if (isPlaying) {
                        mediaPlayer?.pause()
                        isPlaying = false
                    } else {
                        mediaPlayer?.release()
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(podcast.url)
                            prepareAsync()
                            setOnPreparedListener {
                                it.start()
                                isPlaying = true
                            }
                            setOnCompletionListener {
                                isPlaying = false
                            }
                        }
                    }
                }
            ) {
                Icon(
                    painter = painterResource(id = if (isPlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.heart),
                contentDescription = "Favorite",
                tint = Color.Green,
                modifier = Modifier.size(24.dp)
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }
}

@Composable
fun NewsDetailScreen(newsId: String, navController: NavHostController) {
    var news by remember { mutableStateOf<News?>(null) }
    val context = LocalContext.current
    val db = Firebase.firestore

    LaunchedEffect(newsId) {
        db.collection("news").document(newsId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    news = document.toObject(News::class.java)?.copy(id = document.id)
                }
            }
            .addOnFailureListener {
                // Handle error if needed
            }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tin tức",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF28C76F),
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (news == null) {
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
                    .padding(16.dp)
                    .background(Color(0xFFF5F5F5)),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    AsyncImage(
                        model = news!!.url,
                        contentDescription = news!!.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop,
                        error = painterResource(R.drawable.picture)
                    )
                }
                item {
                    Text(
                        text = news!!.title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                item {
                    Text(
                        text = news!!.description,
                        fontSize = 16.sp,
                        color = Color(0xFF333333)
                    )
                }
            }
        }
    }
}