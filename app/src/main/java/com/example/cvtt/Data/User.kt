package com.example.cvtt.Data


data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val level: Int = 0,
    val profilePictureUrl: String? = null,
    val appliedJobs: List<String> = emptyList(),
    val savedJobs: List<String> = emptyList(),
    val savedApply: List<String> = emptyList(),
    val followedCompanies: List<String> = emptyList(),
    val profileViews: List<String> = emptyList(),
    val cvUrls: List<String> = emptyList(),
    val time: String="",
    val fcmToken: String? = null

)