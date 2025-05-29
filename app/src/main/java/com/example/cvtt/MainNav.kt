package com.example.cvtt

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import android.widget.Toast
import androidx.navigation.NavType
import com.example.cvtt.CV.ManagerCV
import com.example.cvtt.CV.UploadCVScreen
import com.example.cvtt.Chat.ChatScreen
import com.example.cvtt.Chat.MessagesScreen
import com.example.cvtt.Company.CompanyDetailScreen
import com.example.cvtt.Company.CompanySuggestionScreen
import com.example.cvtt.Job.ApplyJobScreen
import com.example.cvtt.Job.JobDetailScreen
import com.example.cvtt.Job.JobSuggestionScreen
import com.example.cvtt.Manager.AdminScreen
import com.example.cvtt.Manager.AdminScreen1
import com.example.cvtt.auth.Applyjob
import com.example.cvtt.auth.FollowedCompaniesScreen
import com.example.cvtt.auth.LoginScreen
import com.example.cvtt.auth.ProfileScreen
import com.example.cvtt.auth.RegisterScreen
import com.example.cvtt.auth.SavedJobsScreen
import com.example.cvtt.auth.UserProfileScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.cvtt.Data.User

@Composable
fun MainNav(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    var userId by remember { mutableStateOf(auth.currentUser?.uid ?: "") }
    var userData by remember { mutableStateOf<User?>(null) }
    var isInitialized by remember { mutableStateOf(false) }
    var shouldNavigateToLogin by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Đồng bộ userId và userData khi trạng thái đăng nhập thay đổi
    LaunchedEffect(Unit) {
        auth.addAuthStateListener { authState ->
            userId = authState.currentUser?.uid ?: ""
            if (userId.isNotBlank()) {
                firestore.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        userData = document.toObject(User::class.java)
                        isInitialized = true
                    }
                    .addOnFailureListener {
                        isInitialized = true // Đảm bảo khởi tạo ngay cả khi có lỗi
                    }
            } else {
                shouldNavigateToLogin = true
                isInitialized = true
            }
        }
    }

    // Chờ cho đến khi trạng thái được khởi tạo
    if (!isInitialized) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Đang tải...")
        }
        return
    }
    NavHost(navController = navController, startDestination = if (shouldNavigateToLogin) "login" else "home") {
        composable("home") {
            HomeScreen(
                navController = navController,
                userData = userData,
                onAdminClick = {
                    if (userData?.level == 1) {
                        navController.navigate("admin")
                    } else {
                        Toast.makeText(context, "Bạn không có quyền truy cập trang quản trị", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        composable("userProfileScreen") {
            UserProfileScreen(
                user = userData,
                onSave = { updatedUser ->
                    // Xử lý lưu thông tin người dùng
                    navController.popBackStack()
                },
                onCancel = { navController.popBackStack() }
            )
        }


        composable("upcv") {
            UploadCVScreen(navController)
        }

            composable("messages") {
                MessagesScreen(navController)
            }
            composable("chat/{companyId}") { backStackEntry ->
                val companyId = backStackEntry.arguments?.getString("companyId") ?: ""
                ChatScreen(navController, companyId)
            }

        composable(
            "apply_job/{jobId}",
            arguments = listOf(navArgument("jobId") { defaultValue = "" })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId") ?: ""
            val user = userData // Lấy dữ liệu người dùng từ `userData` đã có trong `MainNav`

            if (jobId.isNotEmpty()) {
                ApplyJobScreen(navController, jobId, user) // Truyền user vào ApplyJobScreen
            } else {
                Toast.makeText(LocalContext.current, "Job ID không hợp lệ", Toast.LENGTH_SHORT).show()
            }
        }



        composable("regi") {
            RegisterScreen(navController)
        }
        composable("job_suggestion") {
            JobSuggestionScreen(navController)
        }

        composable(
            route = "job_detail/{jobId}",
            arguments = listOf(navArgument("jobId") {
                type = NavType.StringType
            })
        ) { backStackEntry ->
            val jobId = backStackEntry.arguments?.getString("jobId")
            JobDetailScreen(navController, jobId ?: "")
        }




        composable("company_suggestion") {
            CompanySuggestionScreen(navController)
        }
        composable("podcast") {
            PostCardScreen(navController)
        }
        composable(
            "company_detail/{companyId}",
            arguments = listOf(navArgument("companyId") {})
        ) { backStackEntry ->
            val companyId = backStackEntry.arguments?.getString("companyId")
            CompanyDetailScreen(navController, companyId)
        }
        composable("cv") {
            ManagerCV(navController)
        }
        composable("company") {
            AdminScreen1(navController)
        }
        composable("top_connect") {
            Column {
                Text("Top Connect Screen")
            }
        }
        composable("notifications") {
            Column {
                Text("Notifications Screen")
            }
        }
        composable("profile") {
            ProfileScreen(navController, userId, userData)
        }
        composable("news_detail/{newsId}") { backStackEntry ->
            NewsDetailScreen(
                newsId = backStackEntry.arguments?.getString("newsId") ?: "",
                navController = navController
            )
        }
        composable("login") {
            LoginScreen(
                auth = auth,
                googleSignInClient = GoogleSignIn.getClient(
                    navController.context,
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(navController.context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build()
                ),
                onLoginSuccess = {
                    navController.navigate("home") {
                        popUpTo(0)
                        launchSingleTop = true
                    }
                },
                firestore = firestore,
                navController = navController
            )
        }
        composable("register") {
            RegisterScreen(navController = navController)
        }
        composable(
            route = "saved_jobs?userId={userId}",
            arguments = listOf(navArgument("userId") {
                defaultValue = ""
                nullable = true
            })
        ) { backStackEntry ->
            val userIdParam = backStackEntry.arguments?.getString("userId") ?: ""
            val effectiveUserId = if (userIdParam.isNotBlank()) userIdParam else auth.currentUser?.uid ?: ""
            if (effectiveUserId.isBlank()) {
                navController.navigate("login") {
                    popUpTo(0)
                    launchSingleTop = true
                }
            } else {
                SavedJobsScreen(
                    navController = navController,
                    userId = effectiveUserId,
                    firestore = firestore
                )
            }
        }

        composable(
            route = "apply_job?userId={userId}",
            arguments = listOf(navArgument("userId") {
                defaultValue = ""
                nullable = true
            })
        ) { backStackEntry ->
            val userIdParam = backStackEntry.arguments?.getString("userId") ?: ""
            val effectiveUserId = if (userIdParam.isNotBlank()) userIdParam else auth.currentUser?.uid ?: ""
            if (effectiveUserId.isBlank()) {
                navController.navigate("login") {
                    popUpTo(0)
                    launchSingleTop = true
                }
            } else {
                Applyjob(
                    navController = navController,
                    userId = effectiveUserId,
                    firestore = firestore
                )
            }
        }

        composable(
            route = "followed_companies?userId={userId}",
            arguments = listOf(navArgument("userId") {
                defaultValue = ""
                nullable = true
            })
        ) { backStackEntry ->
            val userIdParam = backStackEntry.arguments?.getString("userId") ?: ""
            val effectiveUserId = if (userIdParam.isNotBlank()) userIdParam else auth.currentUser?.uid ?: ""
            if (effectiveUserId.isBlank()) {
                navController.navigate("login") {
                    popUpTo(0)
                    launchSingleTop = true
                }
            } else {
                FollowedCompaniesScreen(
                    navController = navController,
                    userId = effectiveUserId,
                    firestore = firestore
                )
            }
        }
            composable("admin") {
            if (userData?.level == 1) {
                AdminScreen(navController = navController)
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Bạn không có quyền truy cập trang này!",
                        fontSize = 18.sp,
                        color = Color.Red
                    )
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Quay lại")
                    }
                }
            }
        }
    }
}
fun fetchUser(
    uid: String,
    onSuccess: (User) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(uid).get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                val user = document.toObject(User::class.java)
                user?.let { onSuccess(it) }
            }
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}

