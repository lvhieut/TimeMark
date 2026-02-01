package com.example.timemarkbase.view_model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainFeatureViewModel: ViewModel() {
    private val _isEnableFullName = MutableLiveData(true)
    val isEnableFullName: LiveData<Boolean> get() = _isEnableFullName

    private val _isEnableLogo = MutableLiveData(false)
    val isEnableLogo: LiveData<Boolean> get() = _isEnableLogo

    private val _fullName = MutableLiveData<String>()
    val fullName: LiveData<String> get() = _fullName

    private val _time = MutableLiveData<String>()
    val time: LiveData<String> get() = _time

    private val _day = MutableLiveData<String>()
    val day: LiveData<String> get() = _day

    private val _date = MutableLiveData<String>()
    val date: LiveData<String> get() = _date

    private val _address = MutableLiveData<String>()
    val address: LiveData<String> get() = _address

    fun setTime(time: String) { _time.value = time }
    fun setDate(date: String) { _date.value = date }
    fun setDay(day: String) { _day.value = day }
    fun setAddress(address: String) { _address.value = address }
    fun setFullName(name: String) { _fullName.value = name }
    fun setEnableFullName(enable: Boolean) {
        _isEnableFullName.value = enable
    }

    fun setEnableLogo(enable: Boolean) {
        _isEnableLogo.value = enable
    }
}