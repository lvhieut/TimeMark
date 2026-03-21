package com.example.timemarkbase.model

object FeatureData {
    var fullName: String? = null
    var date: String? = null
    var day: String? = null
    var time: String? = null
    var address: String? = null
    var latLon: Pair<Double, Double>? = null

    var isEnableFullName: Boolean = true
    var isEnableLogo: Boolean = false
    var isEnableVerifiedText: Boolean = true
    var isEnableGoogleMap: Boolean = false
}