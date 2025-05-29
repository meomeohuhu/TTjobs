package com.example.cvtt.auth

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.getSystemService
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.cloudinary.AccessControlRule.AccessType.token
import com.example.cvtt.ui.theme.CVTTTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.example.cvtt.Data.User
import com.cloudinary.android.MediaManager
import com.cloudinary.Cloudinary
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.DbxUserFilesRequests
import com.example.cvtt.MainNav
import com.example.cvtt.R
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    companion object {
        lateinit var cloudinary: Cloudinary
            private set
    }
    object DropboxClient {
        private var dropboxClient: DbxClientV2? = null

        fun init(accessToken: String) {
            val requestConfig = DbxRequestConfig.newBuilder("TTJOB").build()
            dropboxClient = DbxClientV2(requestConfig, accessToken)
        }

        fun files(): DbxUserFilesRequests {
            return dropboxClient!!.files()
        }

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            MediaManager.get()
        } catch (e: IllegalStateException) {
            val config = hashMapOf(
                "cloud_name" to "your_cloud_key",
                "api_key" to "your_api_key",
                "api_secret" to "your_api_secret"
            )
            MediaManager.init(this, config)
        }

        val jobIdFromIntent = intent?.getStringExtra("jobId")

        setContent {
            CVTTTheme {
                val navController = rememberNavController()
                var hasNavigated by remember { mutableStateOf(false) }

                // Gọi MainNav trước
                MainNav(navController = navController)

                // Sau đó mới gọi điều hướng
                LaunchedEffect(navController, jobIdFromIntent) {
                    delay(300) // ⏱️ đợi 1 chút để đảm bảo MainNav đã gắn graph
                    if (!hasNavigated && jobIdFromIntent != null) {
                        navController.navigate("job_detail/$jobIdFromIntent")
                        hasNavigated = true
                    }
                }
            }
        }


        createNotificationChannel(this)
    }



}


@Composable
fun LoginScreen(
    auth: FirebaseAuth,
    googleSignInClient: GoogleSignInClient,
    onLoginSuccess: () -> Unit,
    firestore: FirebaseFirestore,
    navController: NavHostController
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isChecked by remember { mutableStateOf(true) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account.idToken!!, auth, context, onLoginSuccess, firestore)
        } catch (e: Exception) {
            Toast.makeText(context, "Google Sign-In thất bại", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = "TTJOBs",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A3C34),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Chào mừng bạn đến với TTJOBs",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Text(
            text = "Đăng nhập",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Start
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_email),
                    contentDescription = "Email Icon",
                    tint = Color.Gray
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1A3C34),
                unfocusedBorderColor = Color.Gray
            )
        )

        var passwordVisible by remember { mutableStateOf(false) }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Nhập mật khẩu") },
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.lock),
                    contentDescription = "Password Icon",
                    tint = Color.Gray
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) R.drawable.eye else R.drawable.eye
                        ),
                        contentDescription = "Toggle Password Visibility",
                        tint = Color.Gray
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF1A3C34),
                unfocusedBorderColor = Color.Gray
            )
        )


        Text(
            text = "Quên mật khẩu?",
            fontSize = 14.sp,
            color = Color(0xFF1A3C34),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.End
        )

        Button(
            onClick = {
                if (email.isNotBlank() && password.isNotBlank()) {
                    loginWithEmail(email, password, context, auth, onLoginSuccess, firestore)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF28C76F))
        ) {
            Text(
                text = "Đăng nhập",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Divider(modifier = Modifier.weight(1f), color = Color.Gray)
            Divider(modifier = Modifier.weight(1f), color = Color.Gray)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isChecked,
                onCheckedChange = { isChecked = it },
                colors = CheckboxDefaults.colors(checkedColor = Color(0xFF28C76F))
            )
            Text(
                text = "Bằng việc đăng nhập, tôi đã đọc và đồng ý với Điều khoản dịch vụ và Chính sách bảo mật của TTJOBs",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Bạn chưa có tài khoản? ",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Đăng ký ngay",
                fontSize = 14.sp,
                color = Color(0xFF1A3C34),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    navController.navigate("register")
                }
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    CVTTTheme {
        val context = LocalContext.current
        val auth = FirebaseAuth.getInstance()
        val googleSignInClient = remember {
            GoogleSignIn.getClient(
                context,
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            )
        }
        val firestore = FirebaseFirestore.getInstance()

        LoginScreen(
            auth = auth,
            googleSignInClient = googleSignInClient,
            onLoginSuccess = {},
            firestore = firestore,
            navController = rememberNavController()
        )
    }
}

fun loginWithFacebook() {
    println("Facebook login clicked")
    // TODO: Thực hiện Facebook Login ở đây
}

fun loginWithEmail(
    email: String,
    password: String,
    context: Context,
    auth: FirebaseAuth,
    onLoginSuccess: () -> Unit,
    firestore: FirebaseFirestore
) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                if (userId != null) {
                    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                        firestore.collection("users").document(userId).update("fcmToken", token)
                    }
                }
                Toast.makeText(context, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                onLoginSuccess.invoke()
            } else {
                Toast.makeText(context, "Thất bại: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
}

fun firebaseAuthWithGoogle(
    idToken: String,
    auth: FirebaseAuth,
    context: Context,
    onLoginSuccess: () -> Unit,
    firestore: FirebaseFirestore
) {
    val credential = GoogleAuthProvider.getCredential(idToken, null)
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    fetchUserData(userId, context, firestore) { user ->
                        // Không cần làm gì thêm, dữ liệu đã được lưu vào state trong MainNav
                        saveFcmTokenToFirestore(userId)
                    }
                }
                Toast.makeText(context, "Đăng nhập Google thành công", Toast.LENGTH_SHORT).show()
                onLoginSuccess.invoke()
            } else {
                Toast.makeText(context, "Lỗi: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
}

fun fetchUserData(userId: String, context: Context, firestore: FirebaseFirestore, onSuccess: (User?) -> Unit) {
    firestore.collection("users")
        .document(userId)
        .get()
        .addOnSuccessListener { document ->
            val user = if (document.exists()) {
                document.toObject(User::class.java)?.copy(id = document.id)
            } else {
                null
            }
            onSuccess.invoke(user)
            if (user == null) {
                Toast.makeText(context, "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show()
                val newUser = User(
                    id = userId,
                    name = FirebaseAuth.getInstance().currentUser?.displayName ?: "Người dùng mới",
                    profilePictureUrl = FirebaseAuth.getInstance().currentUser?.photoUrl?.toString(),
                    level = 0
                )
                firestore.collection("users").document(userId).set(newUser)
                    .addOnSuccessListener {
                        onSuccess.invoke(newUser)
                        Toast.makeText(context, "Đã tạo tài khoản mới", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Lỗi tạo tài khoản: ${it.message}", Toast.LENGTH_LONG).show()
                    }
            }
        }
        .addOnFailureListener { exception ->
            Toast.makeText(context, "Lỗi tải thông tin: ${exception.message}", Toast.LENGTH_LONG).show()
            onSuccess.invoke(null)
        }
}

fun logout(auth: FirebaseAuth, navController: NavHostController, context: Context) {
    auth.signOut()
    Toast.makeText(context, "Đã đăng xuất", Toast.LENGTH_SHORT).show()
    navController.navigate("login") { popUpTo(0) }
}

private fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channelId = "job_channel"
        val channelName = "Job Notifications"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(channelId, channelName, importance).apply {
            description = "Kênh thông báo khi có job mới"
        }

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }
}

fun saveFcmTokenToFirestore(userId: String) {
    FirebaseMessaging.getInstance().token
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "New token: $token")
                // Gửi token này lên server hoặc Firestore để cập nhật
            }
        }

}
