package com.example.cvtt.Data

data class Company(
    val id: String = "",
    val logoUrl: String = "",
    val name: String = "",
    val description: String = "",
    val jobtype: String="",
    val jobCount: String = "",
    val location: String="",
    val jobIds: List<String> = emptyList(),
    val countfollow: String="",
    val time: String=""
)