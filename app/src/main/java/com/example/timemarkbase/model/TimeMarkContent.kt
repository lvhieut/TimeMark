package com.example.timemarkbase.model

data class TimeMarkContent(
    val time: String = "",
    val date: String = "",
    val day: String = "",
    val address: String = "",
    val fullName: String = "",
    val companyName: String = "Npp : ...",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val showFullName: Boolean = true,
    val showCompanyName: Boolean = true,
    val showLogo: Boolean = false,
    val showVerifiedText: Boolean = false,
    val showGoogleMap: Boolean = false
)
