package com.example.cvtt.Manager
import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.cvtt.Other.MailSender
import com.example.cvtt.R
import com.example.cvtt.Data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.get
import kotlin.jvm.java
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import android.view.ViewGroup
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.cloudinary.AccessControlRule.AccessType.token
import com.google.android.gms.tasks.Tasks
import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.firestore.DocumentSnapshot
import okhttp3.Call
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import java.io.IOException
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody


import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import java.util.Date

@Composable
fun AdminScreen1(navController: NavHostController) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Đăng", "Quản lý Job","Ứng tuyển","Thống kê")
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
                                        4->R.drawable.cv
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
            0 -> PostScreen1(padding, navController)
            1 -> AdminJobScreen1(padding, navController)
            2 -> CVListForJobScreen(padding, navController)
            3->JobCVBarChart(padding,navController)
        }
    }
}

@Composable
fun PostScreen1(padding: PaddingValues, navController: NavHostController) {
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
        }
        Spacer(modifier = Modifier.height(30.dp))
        when (selectedSection) {
            "Job" -> PostJobScreenContent1(padding, navController)
            "Company" -> AddCompanyScreenContent1(padding, navController)
        }
    }
}

@Composable
fun PostJobScreenContent1(padding: PaddingValues, navController: NavHostController) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid
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
        uri?.let { uploadImageToCloudinary1(it, context) { url -> logoUrl = url } }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Đăng tin tuyển dụng", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Tiêu đề") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Mô tả công việc") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
                OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Địa điểm") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
                OutlinedTextField(value = salary, onValueChange = { salary = it }, label = { Text("Mức lương") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
                OutlinedTextField(value = workLocation, onValueChange = { workLocation = it }, label = { Text("Địa điểm làm việc") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
                OutlinedTextField(value = jobCount, onValueChange = { jobCount = it }, label = { Text("Số lượng tuyển") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
                OutlinedTextField(value = requirements, onValueChange = { requirements = it }, label = { Text("Yêu cầu") }, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))

                IconButton(onClick = { launcher.launch("image/*") }, modifier = Modifier.size(48.dp).padding(bottom = 16.dp)) {
                    Icon(painterResource(id = R.drawable.camera), contentDescription = "Chọn Logo", tint = Color(0xFF28C76F), modifier = Modifier.size(32.dp))
                }

                logoUri?.let {
                    AsyncImage(
                        model = it,
                        contentDescription = "Preview Logo",
                        modifier = Modifier.size(100.dp).clip(RoundedCornerShape(8.dp)).padding(bottom = 16.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }

        val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        item {
            Button(
                onClick = {
                    if (logoUrl.isNotBlank() && title.isNotBlank() && description.isNotBlank() &&
                        location.isNotBlank() && salary.isNotBlank() && requirements.isNotBlank() &&
                        jobCount.isNotBlank() && workLocation.isNotBlank()
                    ) {
                        val jobRef = Firebase.firestore.collection("jobs").document()
                        val job = Job(
                            id = jobRef.id,
                            title = title,
                            description = description,
                            location = location,
                            salary = salary,
                            logoUrl = logoUrl,
                            requirements = requirements,
                            jobCount = jobCount,
                            workLocation = workLocation,
                            companyId = userId ?: "",
                            time = currentTime
                        )

                        jobRef.set(job).addOnSuccessListener {
                            getFollowerTokens(job.companyId) { tokens ->
                                tokens.forEach { token ->
                                    sendFCMHttpV1Notification(
                                        title = "Công ty bạn theo dõi vừa đăng job!",
                                        body = job.title,
                                        token = token,
                                        context = context,
                                        jobId = job.id // 🔥 Gửi đúng jobId
                                    )
                                }
                            }

                            Toast.makeText(context, "Đăng tin thành công", Toast.LENGTH_SHORT).show()
                            title = ""; description = ""; location = ""; salary = ""; logoUrl = ""
                            navController.popBackStack()
                        }.addOnFailureListener {
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


fun sendFCMHttpV1Notification(
    title: String,
    body: String,
    token: String,
    context: Context,
    jobId: String
) {
    Thread {
        try {
            val inputStream = context.assets.open("service_account.json")
            val credentials = GoogleCredentials.fromStream(inputStream)
                .createScoped(listOf("https://www.googleapis.com/auth/firebase.messaging"))
            credentials.refreshIfExpired()
            val accessToken = credentials.accessToken.tokenValue

            val json = JSONObject().apply {
                put("message", JSONObject().apply {
                    put("token", token)
                    put("notification", JSONObject().apply {
                        put("title", title)
                        put("body", body)
                    })
                    put("data", JSONObject().apply {
                        put("jobId", jobId)
                        put("title", title)
                        put("body", body)
                    })
                })
            }

            val requestBody = json.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("https://fcm.googleapis.com/v1/projects/"your_project"/messages:send")
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val client = OkHttpClient()
            val response = client.newCall(request).execute()
            Log.d("FCM", "Code: ${response.code}")
            Log.d("FCM", "Body: ${response.body?.string()}")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }.start()
}



fun getFollowerTokens(companyId: String, onTokensFetched: (List<String>) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val tokens = mutableListOf<String>()

    db.collection("users")
        .whereArrayContains("followedCompanies", companyId)
        .get()
        .addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot) {
                val token = document.getString("fcmToken")
                if (!token.isNullOrEmpty()) {
                    tokens.add(token)
                }
            }
            onTokensFetched(tokens)
        }
        .addOnFailureListener {
            onTokensFetched(emptyList()) // Gọi callback kể cả khi thất bại
        }
}



@Composable
fun AddCompanyScreenContent1(padding: PaddingValues, navController: NavHostController) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid ?: ""
    var company by remember { mutableStateOf<Company?>(null) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var jobType by remember { mutableStateOf("") }
    var jobCount by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var logoUri by remember { mutableStateOf<Uri?>(null) }
    var logoUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val currentTime = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        .format(java.util.Date())

    LaunchedEffect(userId) {
        if (userId.isNotBlank()) {
            FirebaseFirestore.getInstance()
                .collection("companies")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    company = document.toObject(Company::class.java)?.copy(id = document.id)
                    company?.let {
                        name = it.name
                        description = it.description
                        jobType = it.jobtype
                        jobCount = it.jobCount
                        location = it.location
                        logoUrl = it.logoUrl
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    Log.e("AddCompanyScreen", "Error fetching company: $it")
                    isLoading = false
                }
        }
    }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        logoUri = uri
        uri?.let { uploadImageToCloudinary1(it, context) { url -> logoUrl = url } }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Thông tin công ty",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Tên công ty") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp)
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Mô tả công ty") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp)
        )
        OutlinedTextField(
            value = jobType,
            onValueChange = { jobType = it },
            label = { Text("Loại hình công việc") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp)
        )
        OutlinedTextField(
            value = jobCount,
            onValueChange = { jobCount = it },
            label = { Text("Số lượng tuyển") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp)
        )
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Địa điểm làm việc") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Logo công ty", fontSize = 14.sp, color = Color.Gray, modifier = Modifier.align(Alignment.Start))

        IconButton(onClick = { launcher.launch("image/*") }) {
            Icon(
                painter = painterResource(id = R.drawable.camera),
                contentDescription = "Chọn logo",
                tint = Color(0xFF28C76F),
                modifier = Modifier.size(40.dp)
            )
        }

        (logoUri ?: logoUrl.takeIf { it.isNotBlank() }?.let { Uri.parse(it) })?.let {
            AsyncImage(
                model = it,
                contentDescription = "Logo xem trước",
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp)),
                contentScale = ContentScale.Crop
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (logoUrl.isNotBlank() && name.isNotBlank() && description.isNotBlank() &&
                    jobType.isNotBlank() && jobCount.isNotBlank() && location.isNotBlank()) {
                    val updatedCompany = Company(
                        name = name,
                        description = description,
                        jobtype = jobType,
                        jobCount = jobCount,
                        location = location,
                        logoUrl = logoUrl,
                        id = userId,
                        time = currentTime
                    )
                    Firebase.firestore
                        .collection("companies")
                        .document(userId)
                        .set(updatedCompany)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Lưu thông tin công ty thành công", Toast.LENGTH_SHORT).show()
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
            Text("Lưu thông tin", fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize()) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}




@Composable
fun AdminJobScreen1(padding: PaddingValues, navController: NavHostController) {
    var showAddJob by remember { mutableStateOf(false) }
    var selectedJob by remember { mutableStateOf<Job?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Quản lý Job", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        if (showAddJob || selectedJob != null) {
            AddEditJobScreen1(selectedJob, onSave = { showAddJob = false; selectedJob = null }, onCancel = { showAddJob = false; selectedJob = null })
        } else {
            JobListScreenWithManage1(padding) { selectedJob = it }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { showAddJob = true }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F))) {
                Text("Thêm Job", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun JobListScreenWithManage1(padding: PaddingValues, onJobSelected: (Job) -> Unit) {
    val jobList = remember { mutableStateListOf<Job>() }
    val loading = remember { mutableStateOf(true) }
    val context = LocalContext.current
    val userId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(Unit) {
        if (userId != null) {
            Firebase.firestore.collection("jobs")
                .whereEqualTo("companyId", userId)
                .get()
                .addOnSuccessListener { result ->
                    jobList.clear()
                    for (doc in result) {
                        jobList.add(doc.toObject(Job::class.java).copy(id = doc.id))
                    }
                    loading.value = false
                }
                .addOnFailureListener {
                    loading.value = false
                }
        } else {
            loading.value = false
        }
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
fun AddEditJobScreen1(job: Job?, onSave: () -> Unit, onCancel: () -> Unit) {
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
        uri?.let { uploadImageToCloudinary1(it, context) { url -> logoUrl = url } }
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
fun AdminCompanyScreen1(padding: PaddingValues, navController: NavHostController) {
    var showAddCompany by remember { mutableStateOf(false) }
    var selectedCompany by remember { mutableStateOf<Company?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Quản lý Company", fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
        if (showAddCompany || selectedCompany != null) {
            AddEditCompanyScreen1(selectedCompany, onSave = { showAddCompany = false; selectedCompany = null }, onCancel = { showAddCompany = false; selectedCompany = null })
        } else {
            CompanyListScreenWithManage1(padding) { selectedCompany = it }
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { showAddCompany = true }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F))) {
                Text("Thêm Company", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
fun CompanyListScreenWithManage1(padding: PaddingValues, onCompanySelected: (Company) -> Unit) {
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
fun AddEditCompanyScreen1(company: Company?, onSave: () -> Unit, onCancel: () -> Unit) {
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
        uri?.let { uploadImageToCloudinary1(it, context) { url -> logoUrl = url } }
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CVListForJobScreen(
    padding: PaddingValues,
    navController: NavHostController
) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val companyId = currentUser?.uid ?: return
    val coroutineScope = rememberCoroutineScope()
    val db = FirebaseFirestore.getInstance()
    var appliedCVs by remember { mutableStateOf<Map<String, List<String>>>(emptyMap()) }
    var userNames by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var userAvatars by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var userEmails by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var statusApplied by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var userJobTitles by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var userJobDocIds by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var elapsedTime by remember { mutableStateOf(0L) }
    var selectedUserId by remember { mutableStateOf<String?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showCvOptions by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    LaunchedEffect(loading) {
        if (loading) {
            val start = System.currentTimeMillis()
            while (loading && elapsedTime < 5000L) {
                delay(100)
                elapsedTime = System.currentTimeMillis() - start
            }
        } else {
            elapsedTime = 0L
        }
    }

    LaunchedEffect(companyId) {
        db.collection("jobs")
            .whereEqualTo("companyId", companyId)
            .addSnapshotListener { querySnapshot, exception ->
                if (exception != null || querySnapshot == null) {
                    loading = false
                    Toast.makeText(context, "Lỗi tải danh sách CV", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                val cvMap = mutableMapOf<String, MutableList<String>>()
                val titleMap = mutableMapOf<String, String>()
                val docIdMap = mutableMapOf<String, String>()
                val statusMap = mutableMapOf<String, String>()

                for (doc in querySnapshot.documents) {
                    val jobId = doc.id
                    val job = doc.toObject(Job::class.java)
                    val jobTitle = job?.title ?: "Vị trí không xác định"

                    job?.appliedCVs?.forEach { (userId, urls) ->
                        val existing = cvMap.getOrDefault(userId, mutableListOf())
                        existing.addAll(urls)
                        cvMap[userId] = existing

                        if (!titleMap.containsKey(userId)) titleMap[userId] = jobTitle
                        if (!docIdMap.containsKey(userId)) docIdMap[userId] = jobId

                        job.statusApplied[userId]?.let { statusMap[userId] = it }
                    }
                }

                appliedCVs = cvMap
                userJobTitles = titleMap
                userJobDocIds = docIdMap
                statusApplied = statusMap

                val userIds = cvMap.keys.toList()
                val nameMap = mutableMapOf<String, String>()
                val avatarMap = mutableMapOf<String, String>()
                val emailMap = mutableMapOf<String, String>()

                val chunks = userIds.chunked(10)
                var fetched = 0
                chunks.forEach { chunk ->
                    db.collection("users")
                        .whereIn(FieldPath.documentId(), chunk)
                        .get()
                        .addOnSuccessListener { snaps ->
                            snaps.documents.forEach { udoc ->
                                udoc.toObject(User::class.java)?.let { user ->
                                    nameMap[user.id] = user.name
                                    user.profilePictureUrl?.let { avatarMap[user.id] = it }
                                    emailMap[user.id] = user.email
                                }
                            }
                            fetched++
                            if (fetched == chunks.size) {
                                userNames = nameMap
                                userAvatars = avatarMap
                                userEmails = emailMap
                                loading = false
                            }
                        }
                        .addOnFailureListener {
                            loading = false
                            Toast.makeText(context, "Lỗi tải user data", Toast.LENGTH_SHORT).show()
                        }
                }
            }
    }

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
    ) {
        Text(
            text = "Danh sách CV ứng tuyển",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text("Tìm kiếm theo tên hoặc email") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        when {
            loading && elapsedTime < 5000L -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            loading && elapsedTime >= 5000L -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không có ai tuyển dụng lúc này", style = MaterialTheme.typography.titleMedium)
                }
            }
            else -> {
                val filteredList = appliedCVs.entries.filter { (userId, _) ->
                    val name = userNames[userId]?.lowercase() ?: ""
                    val email = userEmails[userId]?.lowercase() ?: ""
                    val query = searchQuery.lowercase()
                    name.contains(query) || email.contains(query)
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredList) { (userId, cvUrls) ->
                        val status = statusApplied[userId]
                        val name = userNames[userId] ?: "ID: $userId"
                        val email = userEmails[userId] ?: ""
                        val avatar = userAvatars[userId]

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .combinedClickable(
                                    onClick = {
                                        selectedUserId = userId
                                        showDialog = true
                                    },
                                    onLongClick = {
                                        selectedUserId = userId
                                        showCvOptions = true
                                    }
                                )
                        ) {
                            if (avatar != null) {
                                AsyncImage(
                                    model = avatar,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.Gray)
                                )
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Ứng viên: $name", style = MaterialTheme.typography.titleMedium)
                                Text("Email: $email", style = MaterialTheme.typography.bodyMedium)
                            }

                            when (status) {
                                "accepted" -> Icon(
                                    painter = painterResource(id = R.drawable.check),
                                    contentDescription = "Đã được nhận",
                                    tint = Color(0xFF28C76F),
                                    modifier = Modifier.size(20.dp)
                                )
                                "rejected" -> Icon(
                                    painter = painterResource(id = R.drawable.close),
                                    contentDescription = "Đã từ chối",
                                    tint = Color.Red,
                                    modifier = Modifier.size(20.dp)
                                )
                                else -> Unit
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCvOptions && selectedUserId != null) {
        val userId = selectedUserId!!
        val cvLinks = appliedCVs[userId] ?: emptyList()
        val jobDocId = userJobDocIds[userId] ?: return

        AlertDialog(
            onDismissRequest = { showCvOptions = false },
            title = { Text("Chọn thao tác") },
            text = { Text("Bạn muốn làm gì với ứng viên này?") },
            confirmButton = {
                TextButton(onClick = {
                    if (cvLinks.isNotEmpty()) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(cvLinks.first()))
                        context.startActivity(intent)
                    } else {
                        Toast.makeText(context, "Không có CV để mở", Toast.LENGTH_SHORT).show()
                    }
                    showCvOptions = false
                }) { Text("Mở CV") }
            },
            dismissButton = {
                TextButton(onClick = {
                    db.collection("jobs").document(jobDocId)
                        .update(
                            mapOf(
                                "appliedCVs.$userId" to FieldValue.delete(),
                                "statusApplied.$userId" to FieldValue.delete()
                            )
                        ).addOnSuccessListener {
                            Toast.makeText(context, "Đã xóa ứng viên", Toast.LENGTH_SHORT).show()
                        }
                    showCvOptions = false
                }) { Text("Xóa") }
            }
        )
    }

    if (showDialog && selectedUserId != null) {
        val name = userNames[selectedUserId] ?: ""
        val email = userEmails[selectedUserId] ?: ""
        val jobTitle = userJobTitles[selectedUserId] ?: ""
        val jobDocId = userJobDocIds[selectedUserId] ?: return

        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Thông tin ứng viên: $name") },
            text = {
                Column {
                    Text("Bạn có muốn xử lý hồ sơ ứng tuyển vị trí: $jobTitle ?")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    db.collection("jobs").document(jobDocId)
                        .update("statusApplied.$selectedUserId", "accepted")

                    statusApplied = statusApplied.toMutableMap().apply {
                        put(selectedUserId!!, "accepted")
                    }

                    coroutineScope.launch {
                        try {
                            MailSender.send(
                                toEmail = email,
                                subject = "🎉 Thông báo trúng tuyển vị trí $jobTitle!",
                                body = "Chúc mừng $name! Bạn đã được nhận vào vị trí $jobTitle tại công ty."
                            )
                        } catch (e: Exception) {
                            Log.e("EmailError", "Không gửi được email nhận việc: ${e.message}")
                            Toast.makeText(context, "Email này không tồn tại: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }

                    showDialog = false
                }) { Text("Được nhận") }
            },
            dismissButton = {
                TextButton(onClick = {
                    db.collection("jobs").document(jobDocId)
                        .update("statusApplied.$selectedUserId", "rejected")

                    statusApplied = statusApplied.toMutableMap().apply {
                        put(selectedUserId!!, "rejected")
                    }

                    coroutineScope.launch {
                        try {
                            MailSender.send(
                                toEmail = email,
                                subject = "Kết quả tuyển dụng",
                                body = "Xin chào $name,\nRất tiếc hồ sơ của bạn chưa phù hợp với vị trí $jobTitle."
                            )
                        } catch (e: Exception) {
                            Log.e("EmailError", "Không gửi được email từ chối: ${e.message}")
                            Toast.makeText(context, "Email của ứng viên không tồn tại", Toast.LENGTH_LONG).show()
                        }
                    }

                    showDialog = false
                }) { Text("Từ chối") }
            }
        )
    }
}


@Composable
fun JobCVBarChart(padding: PaddingValues,navController: NavHostController) {
    val context = LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val currentUser = FirebaseAuth.getInstance().currentUser ?: return
    val companyId = currentUser.uid

    var chartData by remember { mutableStateOf(listOf<Pair<String, Int>>()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        db.collection("jobs")
            .whereEqualTo("companyId", companyId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val data = querySnapshot.documents.mapNotNull { doc ->
                    val job = doc.toObject(Job::class.java)
                    val title = job?.title ?: return@mapNotNull null
                    val count = job.appliedCVs.size
                    title to count
                }
                chartData = data
                isLoading = false
            }
            .addOnFailureListener {
                Toast.makeText(context, "Lỗi tải dữ liệu biểu đồ", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
    }

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Biểu đồ số lượng CV theo từng công việc", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            if (chartData.isEmpty()) {
                Text("Không có dữ liệu ứng tuyển.", style = MaterialTheme.typography.bodyLarge)
            } else {
                val entries = chartData.mapIndexed { index, (title, count) ->
                    BarEntry(index.toFloat(), count.toFloat())
                }
                val labels = chartData.map { it.first.take(10) } // rút gọn tên job

                AndroidView(factory = { context ->
                    BarChart(context).apply {
                        axisLeft.apply {
                            granularity = 1f
                            setDrawGridLines(true)
                            axisMinimum = 0f
                        }
                        layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600)
                        description.isEnabled = false
                        legend.isEnabled = false
                        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
                        xAxis.position = XAxis.XAxisPosition.BOTTOM
                        xAxis.granularity = 1f
                        axisRight.isEnabled = false
                        data = BarData(BarDataSet(entries, "Số CV").apply {
                            color = android.graphics.Color.rgb(40, 199, 111)
                            valueTextSize = 12f
                        })
                        invalidate()
                    }
                })
            }
        }
    }
}





fun updateCVStatus(jobId: String, userId: String, newStatus: String) {
    val jobRef = FirebaseFirestore.getInstance().collection("jobs").document(jobId)
    jobRef.update("statusApplied.$userId", newStatus)
        .addOnSuccessListener {
            Log.d("CV_STATUS", "Trạng thái CV được cập nhật thành công.")
        }
        .addOnFailureListener { e ->
            Log.e("CV_STATUS", "Lỗi khi cập nhật trạng thái CV", e)
        }
}


// Hàm trích xuất tên tệp từ URL, bỏ phần đuôi mở rộng
fun getFileNameFromUrl1(url: String): String {
    val fileNameWithExtension = url.substringAfterLast("/").takeIf { it.isNotEmpty() } ?: "Unknown File"
    return fileNameWithExtension.substringBeforeLast(".").takeIf { it.isNotEmpty() } ?: fileNameWithExtension
}




fun uploadImageToCloudinary1(uri: Uri, context: Context, onSuccess: (String) -> Unit) {
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

