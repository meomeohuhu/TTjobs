package com.example.cvtt.Data

data class SavedJob(
    val userId: String = "",
    val jobId: String = "",
    val savedAt: Long = System.currentTimeMillis()
)
