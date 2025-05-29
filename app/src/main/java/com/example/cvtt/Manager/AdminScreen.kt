package com.example.cvtt.Manager

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.cvtt.Data.Company
import com.example.cvtt.Data.Job
import com.example.cvtt.Data.Podcast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.cvtt.R
import com.example.cvtt.Data.News
import com.example.cvtt.Data.User
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AdminScreen(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Đăng", "Quản lý Job", "Quản lý Company", "Quản lý Podcast","Quản lý tài khoản","Đăng bài news")
    @OptIn(ExperimentalMaterial3Api::class)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trang quản trị", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF28C76F)),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painterResource(id = R.drawable.back), "Back", tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White, contentColor = Color(0xFF333333)) {
                tabs.forEachIndexed { index, title ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                painterResource(
                                    when (index) {
                                        0 -> R.drawable.job
                                        1 -> R.drawable.job
                                        2 -> R.drawable.company
                                        3 -> R.drawable.job
                                        4-> R.drawable.user
                                        5-> R.drawable.news
                                        else -> R.drawable.job
                                    }
                                ),
                                contentDescription = title
                            )
                        },
                        label = { Text(title, fontSize = 12.sp) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            0 -> PostScreen(padding, navController)
            1 -> AdminJobScreen(padding, navController)
            2 -> AdminCompanyScreen(padding, navController)
            3 -> AdminPodcastScreen(padding, navController)
            4 ->AdminUserScreen(padding,navController)
            5->AdminNewsScreen(padding,navController)
        }
    }
}

@Composable
fun PostScreen(padding: PaddingValues, navController: NavHostController) {
    var selectedSection by remember { mutableStateOf("Job") }

    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Đăng", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            Button(
                onClick = { selectedSection = "Job" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedSection == "Job") Color(0xFF28C76F) else Color.Gray)
            ) { Text("Job", color = Color.White) }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { selectedSection = "Company" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedSection == "Company") Color(0xFF28C76F) else Color.Gray)
            ) { Text("Công ty", color = Color.White) }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { selectedSection = "Podcast" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedSection == "Podcast") Color(0xFF28C76F) else Color.Gray)
            ) { Text("Podcast", color = Color.White, fontSize=10.sp) }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { selectedSection = "News" },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = if (selectedSection == "Podcast") Color(0xFF28C76F) else Color.Gray)
            ) { Text("News", color = Color.White) }
        }
        Spacer(modifier = Modifier.height(30.dp))
        when (selectedSection) {
            "Job" -> PostJobScreenContent(padding, navController)
            "Company" -> AddCompanyScreenContent(padding, navController)
            "Podcast" -> AddPodcastScreen(padding, navController)
            "News"-> PostNewsScreenContent(padding,navController)


        }
    }
}
@Composable
fun PostNewsScreenContent(padding: PaddingValues, navController: NavHostController) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }

    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uploadImageToCloudinary(it, context) { url ->
                imageUrl = url
                Toast.makeText(context, "Tải ảnh thành công", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Đăng tin tức",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Mô tả") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                if (imageUrl.isNotBlank()) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .height(150.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Crop
                    )
                }

                Button(
                    onClick = {
                        imagePickerLauncher.launch("image/*")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text("Chọn ảnh")
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (title.isNotBlank() && description.isNotBlank()) {
                        val news = News(title = title, description = description, url = imageUrl)
                        Firebase.firestore.collection("news")
                            .add(news)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Đăng tin tức thành công", Toast.LENGTH_SHORT).show()
                                title = ""
                                description = ""
                                imageUrl = ""
                                navController.popBackStack()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(context, "Vui lòng nhập đầy đủ tiêu đề và mô tả", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F))
            ) {
                Text(
                    text = "Đăng tin",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}


@Composable
fun PostJobScreenContent(padding: PaddingValues, navController: NavHostController) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var workLocation by remember { mutableStateOf("") }
    var jobCount by remember { mutableStateOf("") }
    var requirements by remember { mutableStateOf("") }
    var salary by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var logoUrl by remember { mutableStateOf("") }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        logoUri = uri
        uri?.let { uploadImageToCloudinary(it, context) { url -> logoUrl = url } }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Đăng tin tuyển dụng", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Tiêu đề") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Mô tả công việc") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Địa điểm") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Mức lương") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = workLocation, onValueChange = { workLocation = it }, label = { Text("Địa điểm làm việc") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = jobCount, onValueChange = { jobCount = it }, label = { Text("Số lượng công việc") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = requirements, onValueChange = { requirements = it }, label = { Text("Yêu cầu công việc") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))

                IconButton(onClick = { launcher.launch("image/*") }, modifier = Modifier.size(48.dp).padding(bottom = 16.dp)) {
                    Icon(painterResource(id = R.drawable.camera), "Chọn Logo", tint = Color(0xFF28C76F), modifier = Modifier.size(32.dp))
                }

                logoUri?.let {
                    AsyncImage(model = it, "Preview Logo", modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)).padding(bottom = 16.dp), contentScale = ContentScale.Crop)
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (logoUrl.isNotBlank() && title.isNotBlank() && description.isNotBlank() &&
                        location.isNotBlank() && salary.isNotBlank() && requirements.isNotBlank() &&
                        jobCount.isNotBlank() && workLocation.isNotBlank()
                    ) {
                        val currentTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                            .format(java.util.Date())

                        val job = Job(
                            title = title,
                            description = description,
                            location = location,
                            salary = salary,
                            logoUrl = logoUrl,
                            requirements = requirements,
                            jobCount = jobCount,
                            workLocation = workLocation,
                            time = currentTime
                        )

                        Firebase.firestore.collection("jobs").add(job)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Đăng tin thành công", Toast.LENGTH_SHORT).show()
                                title = ""; description = ""; location = ""; salary = ""; logoUrl = ""
                                navController.popBackStack()
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(context, "Vui lòng điền đầy đủ thông tin và chọn logo", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F)),
                enabled = logoUrl.isNotBlank()
            ) {
                Text("Đăng tin", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}


@Composable
fun AddCompanyScreenContent(padding: PaddingValues, navController: NavHostController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var jobType by remember { mutableStateOf("") }
    var jobCount by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var logoUrl by remember { mutableStateOf("") }
    val currentTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        .format(java.util.Date())

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        logoUri = uri
        uri?.let { uploadImageToCloudinary(it, context) { url -> logoUrl = url } }
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Thêm công ty", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên công ty") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Mô tả công ty") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = jobType, onValueChange = { jobType = it }, label = { Text("Loại hình công việc") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = jobCount, onValueChange = { jobCount = it }, label = { Text("Số lượng công việc") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Địa điểm") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                IconButton(onClick = { launcher.launch("image/*") }, modifier = Modifier.size(48.dp).padding(bottom = 16.dp)) {
                    Icon(painterResource(id = R.drawable.camera), "Chọn Logo", tint = Color(0xFF28C76F), modifier = Modifier.size(48.dp))
                }
                logoUri?.let {
                    AsyncImage(model = it, "Preview Logo", modifier = Modifier.size(200.dp).clip(RoundedCornerShape(8.dp)).padding(bottom = 16.dp), contentScale = ContentScale.Crop)
                }
            }
        }
        item {
            Button(
                onClick = {
                    if (logoUrl.isNotBlank() && name.isNotBlank() && description.isNotBlank() && jobType.isNotBlank() && jobCount.isNotBlank() && location.isNotBlank()) {
                        val company = Company(name = name, description = description, jobtype = jobType, jobCount = jobCount, location = location, logoUrl = logoUrl,time=currentTime)
                        Firebase.firestore.collection("companies").add(company).addOnSuccessListener {
                            Toast.makeText(context, "Thêm công ty thành công", Toast.LENGTH_SHORT).show()
                            name = ""; description = ""; jobType = ""; jobCount = ""; location = ""; logoUri = null; logoUrl = ""
                            navController.popBackStack()
                        }.addOnFailureListener { Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show() }
                    } else {
                        Toast.makeText(context, "Vui lòng điền đầy đủ thông tin và chọn logo", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F)),
                enabled = logoUrl.isNotBlank()
            ) { Text("Thêm công ty", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }
        }
    }
}

@Composable
fun AddPodcastScreen(padding: PaddingValues, navController: NavHostController) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var audioUri by remember { mutableStateOf<Uri?>(null) }
    var audioUrl by remember { mutableStateOf("") }
    var thumbnailUri by remember { mutableStateOf<Uri?>(null) }
    var thumbnailUrl by remember { mutableStateOf("") }
    var duration by remember { mutableStateOf("") }

    val currentTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        .format(java.util.Date())
    val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        audioUri = uri
        uri?.let {
            // Tính thời gian của file âm thanh khi người dùng chọn
            val mediaPlayer = MediaPlayer()
            mediaPlayer.setDataSource(context, it)
            mediaPlayer.prepare()
            val durationMillis = mediaPlayer.duration.toLong()  // Chuyển đổi từ Int sang Long
            duration = formatDuration(durationMillis) // Chuyển đổi sang định dạng hh:mm:ss
            mediaPlayer.release()

            // Tiến hành upload audio
            uploadAudioToCloudinary(it, context) { url -> audioUrl = url }
        }
    }

    val thumbnailLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        thumbnailUri = uri
        uri?.let { uploadImageToCloudinary(it, context) { url -> thumbnailUrl = url } }
    }

    // Hàm chuyển đổi thời gian từ mili giây sang định dạng hh:mm:ss
    fun formatDuration(durationInMillis: Int): String {
        val seconds = (durationInMillis / 1000) % 60
        val minutes = (durationInMillis / (1000 * 60)) % 60
        val hours = (durationInMillis / (1000 * 60 * 60)) % 24
        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Đăng Podcast", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Tiêu đề") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                IconButton(onClick = { audioLauncher.launch("audio/*") }, modifier = Modifier.size(48.dp).padding(bottom = 16.dp)) {
                    Icon(painterResource(id = R.drawable.audio), "Chọn file âm thanh", tint = Color(0xFF28C76F), modifier = Modifier.size(32.dp))
                }

                if (audioUrl.isNotBlank()) {
                    Text("File âm thanh: $audioUrl", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
                    Text("Thời gian: $duration", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
                }

                IconButton(onClick = { thumbnailLauncher.launch("image/*") }, modifier = Modifier.size(48.dp).padding(bottom = 16.dp)) {
                    Icon(painterResource(id = R.drawable.camera), "Chọn hình ảnh", tint = Color(0xFF28C76F), modifier = Modifier.size(32.dp))
                }

                thumbnailUri?.let {
                    AsyncImage(model = it, "Preview Thumbnail", modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)).padding(bottom = 16.dp), contentScale = ContentScale.Crop)
                }
            }
        }

        item {
            Button(
                onClick = {
                    if (title.isNotBlank() && audioUrl.isNotBlank() && thumbnailUrl.isNotBlank()) {
                        val podcast = Podcast(
                            title = title,
                            url = audioUrl,
                            thumbnailUrl = thumbnailUrl,
                            duration = duration, // Lưu duration vào podcast,
                            time = currentTime
                        )
                        Firebase.firestore.collection("podcasts").add(podcast).addOnSuccessListener {
                            Toast.makeText(context, "Đăng podcast thành công", Toast.LENGTH_SHORT).show()
                            title = ""
                            audioUri = null
                            audioUrl = ""
                            thumbnailUri = null
                            thumbnailUrl = ""
                            duration = "" // Reset duration

                            navController.popBackStack()
                        }.addOnFailureListener {
                            Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F)),
                enabled = audioUrl.isNotBlank() && thumbnailUrl.isNotBlank()
            ) {
                Text("Đăng Podcast", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}



@Composable
fun AdminJobScreen(padding: PaddingValues, navController: NavHostController) {
    var showAddJob by remember { mutableStateOf(false) }
    var selectedJob by remember { mutableStateOf<Job?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Quản lý Job", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        if (showAddJob || selectedJob != null) {
            AddEditJobScreen(selectedJob, onSave = { showAddJob = false; selectedJob = null }, onCancel = { showAddJob = false; selectedJob = null })
        } else {
            JobListScreenWithManage(padding) { selectedJob = it }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { showAddJob = true }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F))) {
                Text("Thêm Job", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun JobListScreenWithManage(padding: PaddingValues, onJobSelected: (Job) -> Unit) {
    val jobList = remember { mutableStateListOf<Job>() }
    val loading = remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Firebase.firestore.collection("jobs").get().addOnSuccessListener { result ->
            jobList.clear()
            for (doc in result) jobList.add(doc.toObject(Job::class.java).copy(id = doc.id))
            loading.value = false
        }.addOnFailureListener { loading.value = false }
    }

    if (loading.value) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text("Danh sách tin tuyển dụng", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp)) }
            items(jobList) { job ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(job.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Địa điểm: ${job.location}", fontSize = 14.sp, color = Color.Gray)
                            Text("Lương: ${job.salary}", fontSize = 14.sp, color = Color.Gray)
                        }
                        Row {
                            IconButton(onClick = { onJobSelected(job) }) {
                                Icon(painterResource(id = R.drawable.edit), "Sửa Job", tint = Color.Blue, modifier = Modifier.size(24.dp))
                            }
                            IconButton(onClick = {
                                Firebase.firestore.collection("jobs").document(job.id).delete().addOnSuccessListener {
                                    Toast.makeText(context, "Xóa job thành công", Toast.LENGTH_SHORT).show()
                                    jobList.remove(job)
                                }.addOnFailureListener { Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show() }
                            }) {
                                Icon(painterResource(id = R.drawable.delete), "Xóa Job", tint = Color.Red, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminUserScreen(padding: PaddingValues, navController: NavHostController) {
    var showAddUser by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Quản lý Tài khoản Người dùng",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (showAddUser || selectedUser != null) {
            AddEditUserScreen(
                user = selectedUser,
                onSave = { showAddUser = false; selectedUser = null },
                onCancel = { showAddUser = false; selectedUser = null }
            )
        } else {
            UserListScreenWithManage(
                padding = padding,
                onUserSelected = { selectedUser = it }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showAddUser = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F))
            ) {
                Text(
                    "Thêm Tài khoản",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun AddEditJobScreen(job: Job?, onSave: () -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(job?.title ?: "") }
    var description by remember { mutableStateOf(job?.description ?: "") }
    var generalInfo by remember { mutableStateOf(job?.generalInfo ?: "") }

    var location by remember { mutableStateOf(job?.location ?: "") }
    var salary by remember { mutableStateOf(job?.salary ?: "") }
    var workLocation by remember { mutableStateOf(job?.workLocation ?: "") }
    var jobCount by remember { mutableStateOf(job?.jobCount ?: "") }
    var requirements by remember { mutableStateOf(job?.requirements ?: "") }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var logoUrl by remember { mutableStateOf(job?.logoUrl ?: "") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        logoUri = uri
        uri?.let { uploadImageToCloudinary(it, context) { url -> logoUrl = url } }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (job == null) "Thêm Job" else "Sửa Job", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Tiêu đề") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Mô tả công việc") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Địa điểm") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Mức lương") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = workLocation, onValueChange = { workLocation = it }, label = { Text("Địa điêểm làm việc") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = jobCount, onValueChange = { jobCount = it }, label = { Text("Số lượng công việc") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = requirements, onValueChange = { requirements = it }, label = { Text("Yêu cầu công việc") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))

                IconButton(onClick = { launcher.launch("image/*") }, modifier = Modifier.size(48.dp).padding(bottom = 16.dp)) {
                    Icon(painterResource(id = R.drawable.camera), "Chọn Logo", tint = Color(0xFF28C76F), modifier = Modifier.size(32.dp))
                }
                logoUri?.let {
                    AsyncImage(model = it, "Preview Logo", modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)).padding(bottom = 16.dp), contentScale = ContentScale.Crop)
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = {
                        if (logoUrl.isNotBlank() && title.isNotBlank() && description.isNotBlank() && location.isNotBlank() && salary.isNotBlank() && requirements.isNotBlank() && jobCount.isNotBlank() && workLocation.isNotBlank()) {
                            val newJob = Job(title = title, description = description, location = location, salary = salary, logoUrl = logoUrl,requirements=requirements, jobCount = jobCount, workLocation = workLocation)
                            if (job == null) {
                                Firebase.firestore.collection("jobs").add(newJob).addOnSuccessListener {
                                    Toast.makeText(context, "Thêm job thành công", Toast.LENGTH_SHORT).show()
                                    onSave()
                                }.addOnFailureListener { Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show() }
                            } else {
                                Firebase.firestore.collection("jobs").document(job.id).set(newJob).addOnSuccessListener {
                                    Toast.makeText(context, "Cập nhật job thành công", Toast.LENGTH_SHORT).show()
                                    onSave()
                                }.addOnFailureListener { Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show() }
                            }
                        } else {
                            Toast.makeText(context, "Vui lòng điền đầy đủ thông tin và chọn logo", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F)),
                    enabled = logoUrl.isNotBlank()
                ) { Text(if (job == null) "Thêm" else "Lưu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { onCancel() }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                    Text("Hủy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun AdminCompanyScreen(padding: PaddingValues, navController: NavHostController) {
    var showAddCompany by remember { mutableStateOf(false) }
    var selectedCompany by remember { mutableStateOf<Company?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Quản lý Company", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        if (showAddCompany || selectedCompany != null) {
            AddEditCompanyScreen(selectedCompany, onSave = { showAddCompany = false; selectedCompany = null }, onCancel = { showAddCompany = false; selectedCompany = null })
        } else {
            CompanyListScreenWithManage(padding) { selectedCompany = it }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { showAddCompany = true }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F))) {
                Text("Thêm Company", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun CompanyListScreenWithManage(padding: PaddingValues, onCompanySelected: (Company) -> Unit) {
    val companyList = remember { mutableStateListOf<Company>() }
    val loading = remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Firebase.firestore.collection("companies").get().addOnSuccessListener { result ->
            companyList.clear()
            for (doc in result) companyList.add(doc.toObject(Company::class.java).copy(id = doc.id))
            loading.value = false
        }.addOnFailureListener { loading.value = false }
    }

    if (loading.value) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Text("Danh sách công ty", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp)) }
            items(companyList) { company ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(company.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Loại hình: ${company.jobtype}", fontSize = 14.sp, color = Color.Gray)
                        }
                        Row {
                            IconButton(onClick = { onCompanySelected(company) }) {
                                Icon(painterResource(id = R.drawable.edit), "Sửa Company", tint = Color.Blue, modifier = Modifier.size(24.dp))
                            }
                            IconButton(onClick = {
                                Firebase.firestore.collection("companies").document(company.id).delete().addOnSuccessListener {
                                    Toast.makeText(context, "Xóa company thành công", Toast.LENGTH_SHORT).show()
                                    companyList.remove(company)
                                }.addOnFailureListener { Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show() }
                            }) {
                                Icon(painterResource(id = R.drawable.delete), "Xóa Company", tint = Color.Red, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AddEditCompanyScreen(company: Company?, onSave: () -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(company?.name ?: "") }
    var description by remember { mutableStateOf(company?.description ?: "") }
    var jobType by remember { mutableStateOf(company?.jobtype ?: "") }
    var jobCount by remember { mutableStateOf(company?.jobCount ?: "") }
    var location by remember { mutableStateOf(company?.location ?: "") }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var logoUrl by remember { mutableStateOf(company?.logoUrl ?: "") }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        logoUri = uri
        uri?.let { uploadImageToCloudinary(it, context) { url -> logoUrl = url } }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (company == null) "Thêm Company" else "Sửa Company", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Tên công ty") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Mô tả công ty") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = jobType, onValueChange = { jobType = it }, label = { Text("Loại hình công việc") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = jobCount, onValueChange = { jobCount = it }, label = { Text("Số lượng công việc") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Địa điểm") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                IconButton(onClick = { launcher.launch("image/*") }, modifier = Modifier.size(48.dp).padding(bottom = 16.dp)) {
                    Icon(painterResource(id = R.drawable.camera), "Chọn Logo", tint = Color(0xFF28C76F), modifier = Modifier.size(48.dp))
                }
                logoUri?.let {
                    AsyncImage(model = it, "Preview Logo", modifier = Modifier.size(200.dp).clip(RoundedCornerShape(8.dp)).padding(bottom = 16.dp), contentScale = ContentScale.Crop)
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = {
                        if (logoUrl.isNotBlank() && name.isNotBlank() && description.isNotBlank() && jobType.isNotBlank() && jobCount.isNotBlank() && location.isNotBlank()) {
                            val newCompany = Company(name = name, description = description, jobtype = jobType, jobCount = jobCount, location = location, logoUrl = logoUrl)
                            if (company == null) {
                                Firebase.firestore.collection("companies").add(newCompany).addOnSuccessListener {
                                    Toast.makeText(context, "Thêm company thành công", Toast.LENGTH_SHORT).show()
                                    onSave()
                                }.addOnFailureListener { Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show() }
                            } else {
                                Firebase.firestore.collection("companies").document(company.id).set(newCompany).addOnSuccessListener {
                                    Toast.makeText(context, "Cập nhật company thành công", Toast.LENGTH_SHORT).show()
                                    onSave()
                                }.addOnFailureListener { Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show() }
                            }
                        } else {
                            Toast.makeText(context, "Vui lòng điền đầy đủ thông tin và chọn logo", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F)),
                    enabled = logoUrl.isNotBlank()
                ) { Text(if (company == null) "Thêm" else "Lưu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { onCancel() }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                    Text("Hủy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}





@Composable
fun UserListScreenWithManage(padding: PaddingValues, onUserSelected: (User) -> Unit) {
    val userList = remember { mutableStateListOf<User>() }
    val loading = remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Firebase.firestore.collection("users").get().addOnSuccessListener { result ->
            userList.clear()
            for (doc in result) {
                userList.add(doc.toObject(User::class.java).copy(id = doc.id))
            }
            loading.value = false
        }.addOnFailureListener {
            loading.value = false
            Toast.makeText(context, "Lỗi tải danh sách người dùng: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    if (loading.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    "Danh sách tài khoản người dùng",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            items(userList) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            user.profilePictureUrl?.takeIf { it.isNotBlank() }?.let {
                                AsyncImage(
                                    model = it,
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .padding(end = 12.dp),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column {
                                Text(user.name, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Cấp độ: ${user.level}", fontSize = 14.sp, color = Color.Gray)

                            }
                        }
                        Row {
                            IconButton(onClick = { onUserSelected(user) }) {
                                Icon(
                                    painterResource(id = R.drawable.edit),
                                    "Sửa tài khoản",
                                    tint = Color.Blue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            IconButton(onClick = {
                                Firebase.firestore.collection("users").document(user.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Xóa tài khoản thành công", Toast.LENGTH_SHORT).show()
                                        userList.remove(user)
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                            }) {
                                Icon(
                                    painterResource(id = R.drawable.delete),
                                    "Xóa tài khoản",
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AddEditUserScreen(user: User?, onSave: () -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(user?.name ?: "") }
    var level by remember { mutableStateOf(user?.level?.toString() ?: "0") }
    var profilePictureUri by remember { mutableStateOf<Uri?>(null) }
    var profilePictureUrl by remember { mutableStateOf(user?.profilePictureUrl ?: "") }
    val firebaseUser = FirebaseAuth.getInstance().currentUser // Lấy người dùng hiện tại
    var email by remember { mutableStateOf(firebaseUser?.email ?: "") } // Lấy email từ FirebaseAuth

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        profilePictureUri = uri
        uri?.let { uploadImageToCloudinary(it, context) { url -> profilePictureUrl = url } }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (user == null) "Thêm tài khoản" else "Sửa tài khoản",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Tên người dùng") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                OutlinedTextField(
                    value = level,
                    onValueChange = { level = it },
                    label = { Text("Cấp độ") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(8.dp)
                )

                // Hiển thị email đã đăng ký từ Firebase Authentication
                if (email.isNotBlank()) {
                    Text(
                        text = "Email đã đăng ký: $email",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Normal,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                IconButton(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier
                        .size(48.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Icon(
                        painterResource(id = R.drawable.camera),
                        "Chọn ảnh đại diện",
                        tint = Color(0xFF28C76F),
                        modifier = Modifier.size(48.dp)
                    )
                }
                (profilePictureUri ?: profilePictureUrl.takeIf { it.isNotBlank() }?.let { Uri.parse(it) })?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = "Preview Profile Picture",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .padding(bottom = 16.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if (profilePictureUrl.isNotBlank() && name.isNotBlank() && level.isNotBlank()) {
                            val newUser = User(
                                id = user?.id ?: "",
                                name = name,
                                level = level.toIntOrNull() ?: 0,
                                profilePictureUrl = profilePictureUrl,
                                appliedJobs = user?.appliedJobs ?: emptyList(),
                                savedJobs = user?.savedJobs ?: emptyList(),
                                savedApply = user?.savedApply ?: emptyList(),

                                followedCompanies = user?.followedCompanies ?: emptyList(),
                                profileViews = user?.profileViews ?: emptyList(),
                                cvUrls = user?.cvUrls ?: emptyList()
                            )
                            if (user == null) {
                                Firebase.firestore.collection("users").add(newUser)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Thêm tài khoản thành công", Toast.LENGTH_SHORT).show()
                                        onSave()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                            } else {
                                Firebase.firestore.collection("users").document(user.id)
                                    .set(newUser)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Cập nhật tài khoản thành công", Toast.LENGTH_SHORT).show()
                                        onSave()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        } else {
                            Toast.makeText(context, "Vui lòng điền đầy đủ thông tin và chọn ảnh đại diện", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F)),
                    enabled = profilePictureUrl.isNotBlank()
                ) {
                    Text(
                        if (user == null) "Thêm" else "Lưu",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
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
                    Text(
                        "Hủy",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}




@Composable
fun AdminPodcastScreen(padding: PaddingValues, navController: NavHostController) {
    var showAddPodcast by remember { mutableStateOf(false) }
    var selectedPodcast by remember { mutableStateOf<Podcast?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Quản lý Podcast", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        if (showAddPodcast || selectedPodcast != null) {
            AddEditPodcastScreen(selectedPodcast, onSave = { showAddPodcast = false; selectedPodcast = null }, onCancel = { showAddPodcast = false; selectedPodcast = null })
        } else {
            PodcastListScreen { selectedPodcast = it }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { showAddPodcast = true }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F))) {
                Text("Thêm Podcast", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PodcastListScreen(onPodcastSelected: (Podcast) -> Unit) {
    val podcastList = remember { mutableStateListOf<Podcast>() }
    val loading = remember { mutableStateOf(true) }
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var podcastToDelete by remember { mutableStateOf<Podcast?>(null) }

    LaunchedEffect(Unit) {
        Firebase.firestore.collection("podcasts").get().addOnSuccessListener { result ->
            podcastList.clear()
            for (doc in result) podcastList.add(doc.toObject(Podcast::class.java).copy(id = doc.id))
            loading.value = false
        }.addOnFailureListener { loading.value = false }
    }

    if (loading.value) {
        Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(podcastList) { podcast ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = { onPodcastSelected(podcast) },
                            onLongClick = {
                                podcastToDelete = podcast
                                showDeleteDialog = true
                            }
                        ),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(podcast.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("URL: ${podcast.url}", fontSize = 14.sp, color = Color.Gray)
                            Text("Thumbnail: ${podcast.thumbnailUrl}", fontSize = 14.sp, color = Color.Gray)
                        }
                        Icon(
                            painterResource(id = R.drawable.edit),
                            contentDescription = "Sửa Podcast",
                            tint = Color.Blue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

    if (showDeleteDialog && podcastToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc muốn xóa podcast này không?") },
            confirmButton = {
                TextButton(onClick = {
                    Firebase.firestore.collection("podcasts").document(podcastToDelete!!.id).delete()
                        .addOnSuccessListener {
                            Toast.makeText(context, "Xóa podcast thành công", Toast.LENGTH_SHORT).show()
                            podcastList.remove(podcastToDelete)
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show()
                        }
                    showDeleteDialog = false
                }) {
                    Text("Xóa", color = Color.Red)
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


@Composable
fun AddEditPodcastScreen(podcast: Podcast?, onSave: () -> Unit, onCancel: () -> Unit) {
    val context = LocalContext.current
    var duration by remember { mutableStateOf(podcast?.duration ?: "") }
    var title by remember { mutableStateOf(podcast?.title ?: "") }
    var audioUrl by remember { mutableStateOf(podcast?.url ?: "") }
    var thumbnailUrl by remember { mutableStateOf(podcast?.thumbnailUrl ?: "") }
    var audioUri by remember { mutableStateOf<Uri?>(null) }
    var thumbnailUri by remember { mutableStateOf<Uri?>(null) }

    val audioLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        audioUri = uri
        uri?.let {
            // Tính thời lượng
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(context, it)
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val minutes = durationMs / 1000 / 60
            val seconds = (durationMs / 1000 % 60)
            duration = String.format("%d:%02d", minutes, seconds)
            retriever.release()

            // Upload lên Cloudinary
            uploadAudioToCloudinary(it, context) { url -> audioUrl = url }
        }
    }
    val thumbnailLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        thumbnailUri = uri
        uri?.let { uploadImageToCloudinary(it, context) { url -> thumbnailUrl = url } }
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (podcast == null) "Thêm Podcast" else "Sửa Podcast", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Tiêu đề") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), shape = RoundedCornerShape(8.dp))
                IconButton(onClick = { audioLauncher.launch("audio/*") }, modifier = Modifier.size(48.dp).padding(bottom = 16.dp)) {
                    Icon(painterResource(id = R.drawable.camera), "Chọn file âm thanh", tint = Color(0xFF28C76F), modifier = Modifier.size(32.dp))
                }
                if (audioUrl.isNotBlank()) Text("File âm thanh: $audioUrl", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 16.dp))
                IconButton(onClick = { thumbnailLauncher.launch("image/*") }, modifier = Modifier.size(48.dp).padding(bottom = 16.dp)) {
                    Icon(painterResource(id = R.drawable.camera), "Chọn hình ảnh", tint = Color(0xFF28C76F), modifier = Modifier.size(32.dp))
                }
                thumbnailUri?.let {
                    AsyncImage(model = it, "Preview Thumbnail", modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)).padding(bottom = 16.dp), contentScale = ContentScale.Crop)
                }
            }
        }
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = {
                        if (title.isNotBlank() && audioUrl.isNotBlank() && thumbnailUrl.isNotBlank()) {
                            val newPodcast = Podcast(id = podcast?.id ?: "", title = title, url = audioUrl, thumbnailUrl = thumbnailUrl, duration = duration)
                            if (podcast == null) {
                                Firebase.firestore.collection("podcasts").add(newPodcast).addOnSuccessListener {
                                    Toast.makeText(context, "Thêm podcast thành công", Toast.LENGTH_SHORT).show()
                                    onSave()
                                }.addOnFailureListener { Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show() }
                            } else {
                                Firebase.firestore.collection("podcasts").document(podcast.id).set(newPodcast).addOnSuccessListener {
                                    Toast.makeText(context, "Cập nhật podcast thành công", Toast.LENGTH_SHORT).show()
                                    onSave()
                                }.addOnFailureListener { Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show() }
                            }
                        } else {
                            Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F)),
                    enabled = audioUrl.isNotBlank() && thumbnailUrl.isNotBlank()
                ) { Text(if (podcast == null) "Thêm" else "Lưu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { onCancel() }, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)) {
                    Text("Hủy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}


fun uploadImageToCloudinary(uri: Uri, context: Context, onSuccess: (String) -> Unit) {
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

fun uploadAudioToCloudinary(uri: Uri, context: Context, onSuccess: (String) -> Unit) {
    MediaManager.get().upload(uri).option("resource_type", "video").option("folder", "cvtt/podcasts").callback(object : UploadCallback {
        override fun onStart(requestId: String?) { Toast.makeText(context, "Đang tải file âm thanh...", Toast.LENGTH_SHORT).show() }
        override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
        override fun onSuccess(requestId: String?, resultData: MutableMap<Any?, Any?>?) {
            val secureUrl = resultData?.get("secure_url") as? String
            if (secureUrl != null) onSuccess(secureUrl) else Toast.makeText(context, "Không lấy được link âm thanh HTTPS", Toast.LENGTH_SHORT).show()
        }
        override fun onError(requestId: String?, error: ErrorInfo?) { Toast.makeText(context, "Lỗi tải file âm thanh: ${error?.description}", Toast.LENGTH_LONG).show() }
        override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
    }).dispatch()
}

fun formatDuration(durationInMillis: Long): String {
    val seconds = (durationInMillis / 1000) % 60
    val minutes = (durationInMillis / (1000 * 60)) % 60
    val hours = (durationInMillis / (1000 * 60 * 60)) % 24

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}




@Composable
fun AdminNewsScreen(padding: PaddingValues, navController: NavHostController) {
    var showAddNews by remember { mutableStateOf(false) }
    var selectedNews by remember { mutableStateOf<News?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Quản lý News", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

        if (showAddNews || selectedNews != null) {
            AddEditNewsScreen(
                news = selectedNews,
                onSave = { showAddNews = false; selectedNews = null },
                onCancel = { showAddNews = false; selectedNews = null }
            )
        } else {
            NewsListScreenWithManage(padding) { selectedNews = it }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showAddNews = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F))
            ) {
                Text("Thêm News", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}


@Composable
fun NewsListScreenWithManage(padding: PaddingValues, onNewsSelected: (News) -> Unit) {
    val newsList = remember { mutableStateListOf<News>() }
    val loading = remember { mutableStateOf(true) }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Firebase.firestore.collection("news").get()
            .addOnSuccessListener { result ->
                newsList.clear()
                for (doc in result) {
                    newsList.add(doc.toObject(News::class.java).copy(id = doc.id))
                }
                loading.value = false
            }
            .addOnFailureListener {
                loading.value = false
            }
    }

    if (loading.value) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Danh sách News", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
            }

            items(newsList) { news ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(news.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(news.description, fontSize = 14.sp, color = Color.Gray)
                        }
                        if (news.url.isNotBlank()) {
                            AsyncImage(
                                model = news.url,
                                contentDescription = null,
                                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Row {
                            IconButton(onClick = { onNewsSelected(news) }) {
                                Icon(painterResource(id = R.drawable.edit), contentDescription = "Sửa News", tint = Color.Blue)
                            }
                            IconButton(onClick = {
                                Firebase.firestore.collection("news").document(news.id).delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Xóa News thành công", Toast.LENGTH_SHORT).show()
                                        newsList.remove(news)
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                            }) {
                                Icon(painterResource(id = R.drawable.delete), contentDescription = "Xóa News", tint = Color.Red)
                            }
                        }
                    }
                }
            }

        }
    }
}
@Composable
fun AddEditNewsScreen(news: News?, onSave: () -> Unit, onCancel: () -> Unit) {
    var title by remember { mutableStateOf(news?.title ?: "") }
    var description by remember { mutableStateOf(news?.description ?: "") }
    var imageUrl by remember { mutableStateOf(news?.url ?: "") }
    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            uploadImageToCloudinary(it, context) { url ->
                imageUrl = url
                Toast.makeText(context, "Tải ảnh thành công", Toast.LENGTH_SHORT).show()
            }
        }
    }



    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        item {
            Text(if (news == null) "Thêm News" else "Sửa News", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Tiêu đề") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Mô tả") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp)
            )

            if (imageUrl.isNotBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .height(150.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .padding(bottom = 8.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Button(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Text("Chọn Ảnh")
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                Button(
                    onClick = {
                        if (title.isNotBlank() && description.isNotBlank()) {
                            val newNews = News(title = title, description = description, url = imageUrl)
                            if (news == null) {
                                Firebase.firestore.collection("news").add(newNews)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Thêm News thành công", Toast.LENGTH_SHORT).show()
                                        onSave()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                            } else {
                                Firebase.firestore.collection("news").document(news.id).set(newNews)
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Cập nhật News thành công", Toast.LENGTH_SHORT).show()
                                        onSave()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Lỗi: ${it.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                        } else {
                            Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F))
                ) {
                    Text(if (news == null) "Thêm" else "Lưu", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = { onCancel() },
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Hủy", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}



