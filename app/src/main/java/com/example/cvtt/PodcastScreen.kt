package com.example.cvtt

import android.media.MediaPlayer
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.example.cvtt.Data.Podcast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@Composable
fun PostCardScreen(navController: NavHostController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Podcast",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        PodcastCardList(navController)
    }
}

@Composable
fun PodcastCardList(navController: NavHostController) {
    val podcastList = remember { mutableStateListOf<Podcast>() }
    val loading = remember { mutableStateOf(true) }
    val context = LocalContext.current

    // Quản lý trạng thái toàn cục của player
    var currentPlayingId by remember { mutableStateOf<String?>(null) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    LaunchedEffect(Unit) {
        Firebase.firestore.collection("podcasts")
            .get()
            .addOnSuccessListener { result ->
                podcastList.clear()
                for (doc in result) {
                    val duration = doc.getString("duration") ?: "Không có thời gian"
                    val podcast = doc.toObject(Podcast::class.java).copy(
                        id = doc.id,
                        duration = duration
                    )
                    podcastList.add(podcast)
                }
                loading.value = false
            }
            .addOnFailureListener {
                Toast.makeText(context, "Lỗi tải danh sách: ${it.message}", Toast.LENGTH_LONG).show()
                loading.value = false
            }
    }

    if (loading.value) {
        Box(Modifier.fillMaxSize(), Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(podcastList) { podcast ->
                PodcastCard(
                    podcast = podcast,
                    navController = navController,
                    isPlaying = currentPlayingId == podcast.id,
                    onPlayPauseClick = {
                        if (currentPlayingId == podcast.id) {
                            mediaPlayer?.pause()
                            currentPlayingId = null
                        } else {
                            mediaPlayer?.release()
                            mediaPlayer = MediaPlayer().apply {
                                setDataSource(podcast.url)
                                prepareAsync()
                                setOnPreparedListener {
                                    it.start()
                                    currentPlayingId = podcast.id
                                }
                                setOnCompletionListener {
                                    currentPlayingId = null
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    // Dọn tài nguyên khi Composable bị hủy
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer?.release()
        }
    }
}

@Composable
fun PodcastCard(
    podcast: Podcast,
    navController: NavHostController,
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            AsyncImage(
                model = podcast.thumbnailUrl,
                contentDescription = "Podcast Thumbnail",
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = podcast.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    maxLines = 1
                )
                Text(
                    text = podcast.duration ?: "Không có thời gian",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }

            IconButton(onClick = onPlayPauseClick) {
                Icon(
                    painter = painterResource(id = if (isPlaying) R.drawable.pause else R.drawable.play),
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }


        }
    }
}
