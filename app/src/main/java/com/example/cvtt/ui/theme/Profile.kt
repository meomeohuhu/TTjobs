package com.example.cvtt.ui.theme


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cvtt.R
import com.example.cvtt.ui.theme.CVTTTheme

class AccountActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CVTTTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AccountScreen()
                }
            }
        }
    }
}

@Composable
fun AccountScreen() {
    var jobSearchStatus by remember { mutableStateOf(false) }
    var allowContact by remember { mutableStateOf(true) }
    var allowTopConnect by remember { mutableStateOf(true) }
    var allowEmailPhone by remember { mutableStateOf(true) }

    Scaffold(
        bottomBar = {
            BottomNavigationBar()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Section
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
                    Image(
                        painter = painterResource(id = R.drawable.profile), // Thay bằng hình profile placeholder
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Thịnh Nguyễn Thanh",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Mã ứng viên: 9300203",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    Button(
                        onClick = { /* Handle logout */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            contentColor = Color(0xFF1A3C34)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Text("Đăng xuất")
                    }
                }
            }

            // Work Experience
            Text(
                text = "Kinh nghiệm làm việc",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dưới 1 năm",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Sửa",
                        fontSize = 14.sp,
                        color = Color(0xFF28C76F), // Màu xanh TopCV
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Desired Job
            Text(
                text = "Công việc mong muốn",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "IT",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Sửa",
                        fontSize = 14.sp,
                        color = Color(0xFF28C76F),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Desired Location
            Text(
                text = "Địa điểm làm việc mong muốn",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Đà Nẵng - Gia Lai",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Sửa",
                        fontSize = 14.sp,
                        color = Color(0xFF28C76F),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Manage Profile
            Text(
                text = "Quản lý hồ sơ",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // Job Search Status
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.search), // Thay bằng icon job search
                        contentDescription = "Job Search Icon",
                        tint = Color(0xFF28C76F),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Trạng thái tìm việc",
                        fontSize = 14.sp,
                        color = Color.Black,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = jobSearchStatus,
                        onCheckedChange = { jobSearchStatus = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF28C76F),
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray
                        )
                    )
                }
            }

            // Allow Contact
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.contact), // Thay bằng icon contact
                            contentDescription = "Contact Icon",
                            tint = Color(0xFF28C76F),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Cho phép NTD liên hệ",
                            fontSize = 14.sp,
                            color = Color.Black,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = allowContact,
                            onCheckedChange = { allowContact = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF28C76F),
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = Color.Gray
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "NTD có thể liên hệ tới qua:",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = allowTopConnect,
                            onCheckedChange = { allowTopConnect = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF28C76F))
                        )
                        Text(
                            text = "Nhắn tin qua TopConnect",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = allowEmailPhone,
                            onCheckedChange = { allowEmailPhone = it },
                            colors = CheckboxDefaults.colors(checkedColor = Color(0xFF28C76F))
                        )
                        Text(
                            text = "Email và số điện thoại",
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar() {
    NavigationBar(
        containerColor = Color.White,
        contentColor = Color(0xFF28C76F) // Màu xanh cho item được chọn
    ) {
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.home), // Thay bằng icon home
                    contentDescription = "Trang chủ"
                )
            },
            label = { Text("Trang chủ") },
            selected = false,
            onClick = { /* Navigate to Home */ }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.cv), // Thay bằng icon CV
                    contentDescription = "CV & Profile"
                )
            },
            label = { Text("CV & Profile") },
            selected = false,
            onClick = { /* Navigate to CV */ }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.connect), // Thay bằng icon connect
                    contentDescription = "Top Connect"
                )
            },
            label = { Text("Top Connect") },
            selected = false,
            onClick = { /* Navigate to Top Connect */ }
        )
        NavigationBarItem(
            icon = {
                BadgedBox(badge = {
                    Badge { Text("1") }
                }) {
                    Icon(
                        painter = painterResource(id = R.drawable.notification), // Thay bằng icon notification
                        contentDescription = "Thông báo"
                    )
                }
            },
            label = { Text("Thông báo") },
            selected = false,
            onClick = { /* Navigate to Notifications */ }
        )
        NavigationBarItem(
            icon = {
                Icon(
                    painter = painterResource(id = R.drawable.profile), // Thay bằng icon profile
                    contentDescription = "Tài khoản"
                )
            },
            label = { Text("Tài khoản") },
            selected = true,
            onClick = { /* Stay on this screen */ }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AccountScreenPreview() {
    CVTTTheme {
        AccountScreen()
    }
}