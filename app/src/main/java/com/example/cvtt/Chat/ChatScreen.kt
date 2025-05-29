package com.example.cvtt.Chat

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import com.example.cvtt.Data.Company
import com.example.cvtt.Data.Message
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavHostController, companyId: String) {
    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid.orEmpty()

    var company by remember { mutableStateOf<Company?>(null) }
    var messages by remember { mutableStateOf<List<Message>>(emptyList()) }
    var newMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    val listState = rememberLazyListState()

    LaunchedEffect(companyId) {
        if (companyId.isNotBlank()) {
            firestore.collection("companies")
                .document(companyId)
                .get()
                .addOnSuccessListener { doc ->
                    company = doc.toObject(Company::class.java)?.copy(id = doc.id)
                }
                .addOnFailureListener {
                    Log.e("ChatScreen", "Error fetching company: $it")
                }
        }
    }

    LaunchedEffect(userId, companyId) {
        if (userId.isNotBlank() && companyId.isNotBlank()) {
            val allMessages = mutableListOf<Message>()

            firestore.collection("messages")
                .whereEqualTo("senderId", userId)
                .whereEqualTo("receiverId", companyId)
                .addSnapshotListener { snapshot1, error1 ->
                    if (error1 == null && snapshot1 != null) {
                        val list1 = snapshot1.documents.mapNotNull { it.toObject(Message::class.java)?.copy(id = it.id) }
                        allMessages.removeAll { it.senderId == userId && it.receiverId == companyId }
                        allMessages.addAll(list1)
                        allMessages.sortBy { it.timestamp }
                        messages = allMessages.toList()
                        isLoading = false
                    }
                }

            firestore.collection("messages")
                .whereEqualTo("senderId", companyId)
                .whereEqualTo("receiverId", userId)
                .addSnapshotListener { snapshot2, error2 ->
                    if (error2 == null && snapshot2 != null) {
                        val list2 = snapshot2.documents.mapNotNull { it.toObject(Message::class.java)?.copy(id = it.id) }
                        allMessages.removeAll { it.senderId == companyId && it.receiverId == userId }
                        allMessages.addAll(list2)
                        allMessages.sortBy { it.timestamp }
                        messages = allMessages.toList()
                        isLoading = false
                    }
                }
        }
    }

    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.scrollToItem(messages.size - 1)
        }
    }

    fun sendMessage() {
        val content = newMessage.trim()
        if (content.isEmpty()) return

        val msg = Message(
            senderId = userId,
            receiverId = companyId,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        firestore.collection("messages")
            .add(msg)
            .addOnSuccessListener {
                newMessage = ""
            }
            .addOnFailureListener {
                Log.e("ChatScreen", "Error sending message: $it")
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.company),
                            contentDescription = null,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .padding(end = 8.dp)
                        )
                        Text(
                            text = company?.name ?: "Công ty",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A3C34)),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = painterResource(id = R.drawable.back),
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF5F5F5))
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    items(messages) { message ->
                        MessageItem(message, isSentByUser = message.senderId == userId)
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = newMessage,
                        onValueChange = { newMessage = it },
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.White, RoundedCornerShape(20.dp))
                            .padding(12.dp),
                        decorationBox = { inner ->
                            if (newMessage.isEmpty()) {
                                Text("Nhập tin nhắn...", color = Color.Gray, fontSize = 14.sp)
                            }
                            inner()
                        }
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { sendMessage() },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFF28C76F), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.send),
                            contentDescription = "Send",
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MessageItem(message: Message, isSentByUser: Boolean) {
    val bg = if (isSentByUser) Color(0xFF28C76F) else Color.White
    val txtColor = if (isSentByUser) Color.White else Color.Black
    val align = if (isSentByUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = align
    ) {
        Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = bg),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                fontSize = 14.sp,
                color = txtColor
            )
        }
        val time = SimpleDateFormat("HH:mm - dd/MM/yyyy", Locale.getDefault())
            .format(Date(message.timestamp))
        Text(
            text = time,
            fontSize = 12.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
