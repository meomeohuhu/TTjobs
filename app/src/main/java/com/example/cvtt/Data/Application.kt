package com.example.cvtt.Data

data class Application(
    val id: String = "",        // documentId
    val jobId: String = "",
    val userId: String = "",
    val cvUrl: String = "",
    val status: String = "pending" // "pending" | "accepted" | "rejected"
)
