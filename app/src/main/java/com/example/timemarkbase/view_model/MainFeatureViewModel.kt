package com.example.timemarkbase.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.timemarkbase.time_mark.TimeMarkStyle

class MainFeatureViewModel: ViewModel() {
    private val _isEnableFullName = MutableLiveData(true)
    val isEnableFullName: LiveData<Boolean> get() = _isEnableFullName

    private val _isEnableFullNameCompany = MutableLiveData(true)
    val isEnableFullNameCompany: LiveData<Boolean> get() = _isEnableFullNameCompany

    private val _isEnableLogo = MutableLiveData(false)
    val isEnableLogo: LiveData<Boolean> get() = _isEnableLogo

    private val _fullName = MutableLiveData<String>()
    val fullName: LiveData<String> get() = _fullName

    private val _companyName = MutableLiveData("Npp : ...")
    val companyName: LiveData<String> get() = _companyName

    private val _time = MutableLiveData<String>()
    val time: LiveData<String> get() = _time

    private val _day = MutableLiveData<String>()
    val day: LiveData<String> get() = _day

    private val _date = MutableLiveData<String>()
    val date: LiveData<String> get() = _date

    private val _address = MutableLiveData<String>()
    val address: LiveData<String> get() = _address

    private val _latLon = MutableLiveData<Pair<Double, Double>>()
    val latLon: LiveData<Pair<Double, Double>> get() = _latLon

    private val _isEnableVerifiedText = MutableLiveData(false)
    val isEnableVerifiedText: LiveData<Boolean> get() = _isEnableVerifiedText

    private val _isEnableGoogleMap = MutableLiveData(false)
    val isEnableGoogleMap: LiveData<Boolean> get() = _isEnableGoogleMap

    private val _selectedTimeMarkStyle = MutableLiveData(TimeMarkStyle.CLASSIC)
    val selectedTimeMarkStyle: LiveData<TimeMarkStyle> get() = _selectedTimeMarkStyle

    fun setTime(time: String) { _time.value = time }
    fun setDate(date: String) { _date.value = date }
    fun setDay(day: String) { _day.value = day }
    fun setAddress(address: String) { _address.value = address }
    fun setFullName(name: String) { _fullName.value = name }
    fun setCompanyName(name: String) { _companyName.value = name }
    fun setLatLon(lat: Double, lon: Double) {
        _latLon.value = Pair(lat, lon)
    }
    fun setEnableFullName(enable: Boolean) {
        _isEnableFullName.value = enable
    }

    fun setEnableFullNameCompany(enable: Boolean) {
        _isEnableFullNameCompany.value = enable
    }

    fun setEnableLogo(enable: Boolean) {
        _isEnableLogo.value = enable
    }

    fun setEnableVerifiedText(enable: Boolean) {
        _isEnableVerifiedText.value = enable
    }

    fun setEnableImageGoogleMap(enable: Boolean) {
        _isEnableGoogleMap.value = enable
    }

    fun setSelectedTimeMarkStyle(style: TimeMarkStyle) {
        _selectedTimeMarkStyle.value = style
    }
}
