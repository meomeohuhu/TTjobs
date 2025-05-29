package com.example.cvtt.Chat

import android.util.Log
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.cvtt.R
import com.example.cvtt.Data.ChatSummary
import com.example.cvtt.Data.Company
import com.example.cvtt.Data.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(navController: NavHostController) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val currentId = auth.currentUser?.uid.orEmpty()
    var chatSummaries by remember { mutableStateOf(emptyList<ChatSummary>()) }
    var isLoading by remember { mutableStateOf(true) }

    fun fetchMessages(field: String) {
        firestore.collection("messages")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) {
                    isLoading = false
                    return@addSnapshotListener
                }

                val summaries = mutableMapOf<String, ChatSummary>()

                for (doc in snapshot.documents) {
                    val msg = doc.toObject(Message::class.java) ?: continue
                    if (msg.senderId != currentId && msg.receiverId != currentId) continue

                    val otherId = if (msg.senderId == currentId) msg.receiverId else msg.senderId ?: continue

                    val existing = summaries[otherId]
                    if (existing == null || msg.timestamp > existing.lastMessageTime) {
                        summaries[otherId] = ChatSummary(
                            companyId = otherId,
                            company = null,
                            lastMessage = msg.content,
                            lastMessageTime = msg.timestamp
                        )
                    }
                }

                chatSummaries = summaries.values.sortedByDescending { it.lastMessageTime }
                isLoading = false
            }

    }

    LaunchedEffect(currentId) {
        if (currentId.isNotBlank()) {
            fetchMessages("senderId")
            fetchMessages("receiverId")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Tin nhắn", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A3C34)),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Back Icon",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFF5F5F5))
        ) {
            when {
                isLoading -> CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )

                chatSummaries.isEmpty() -> Text(
                    "Chưa có tin nhắn nào",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
                )

                else -> LazyColumn {
                    items(chatSummaries) { summary ->
                        ChatSummaryItem(summary) {
                            navController.navigate("chat/${summary.companyId}")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatSummaryItem(summary: ChatSummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.company),
                contentDescription = "Avatar",
                modifier = Modifier.size(48.dp).clip(CircleShape).padding(end = 12.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = summary.company?.name ?: "Người dùng",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A3C34)
                )
                Text(
                    text = summary.lastMessage,
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    maxLines = 1,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Text(
                text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(summary.lastMessageTime)),
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
        }
    }
}
