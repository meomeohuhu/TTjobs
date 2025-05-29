package com.example.cvtt.Data

data class FollowedCompany(
    val id: String = "",          // documentId
    val userId: String = "",      // Người theo dõi
    val companyId: String = "",   // Công ty được theo dõi
    val followedAt: String = ""   // ISO datetime string, ví dụ "2025-05-19T10:23:00Z"
)
