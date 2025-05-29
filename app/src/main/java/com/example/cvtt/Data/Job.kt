package com.example.cvtt.Data


    data class Job(
        val id: String = "",
        val logoUrl: String = "",
        val title: String = "",
        val description: String = "",
        val salary: String = "",
        val location: String = "",  // Kiểm tra rằng trường này có trong Firestore
        val company: String = "",
        val requirements: String = "",
        val generalInfo: String = "",
        val workLocation: String = "",
        val applyDeadline: String = "",
        val jobCount: String="",
        val companyId: String="",
        val appliedCVs: Map<String, List<String>> = emptyMap(), // <userId, List<CV_URL>>
        val statusApplied: Map<String, String> = emptyMap(),
        val appliedTime: Map<String, String> = emptyMap(),
        val time : String=""
    )
