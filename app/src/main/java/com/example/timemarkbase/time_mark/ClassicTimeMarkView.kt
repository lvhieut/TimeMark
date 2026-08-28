package com.example.timemarkbase.time_mark

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.timemarkbase.R
import com.example.timemarkbase.custom_view.TimeDigitView
import com.example.timemarkbase.model.TimeMarkContent
import java.util.Locale

class ClassicTimeMarkView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : BaseTimeMarkView(context, attrs) {

    private val txtTime: TimeDigitView?
    private val txtDate: TextView?
    private val txtDay: TextView?
    private val txtAddress: TextView?
    private val txtLatAndLon: TextView?
    private val txtNameUser: TextView?

    init {
        LayoutInflater.from(context).inflate(R.layout.view_time_mark_classic, this, true)
        txtTime = findViewById(R.id.txtTime)
        txtDate = findViewById(R.id.txtDateFormater)
        txtDay = findViewById(R.id.txtDay)
        txtAddress = findViewById(R.id.txtAddress)
        txtLatAndLon = findViewById(R.id.txtLatAndLon)
        txtNameUser = findViewById(R.id.txtNameUser)
        applyCommonStyle()
    }

    override fun bind(content: TimeMarkContent) {
        txtTime?.setTime(content.time)
        txtDate?.text = content.date
        txtDay?.text = content.day
        txtAddress?.text = content.address
        txtNameUser?.text = content.fullName
        txtNameUser?.isVisible = content.showFullName
        txtLatAndLon?.text = formatLatLon(content)
        txtLatAndLon?.isVisible = content.showGoogleMap
    }

    override fun applyCommonStyle() {
        styleTextViews(txtDate, txtDay, txtAddress, txtLatAndLon, txtNameUser)
    }

    private fun formatLatLon(content: TimeMarkContent): String {
        val latitude = content.latitude ?: return ""
        val longitude = content.longitude ?: return ""
        return String.format(Locale.US, "Toạ độ: %.6f°N, %.6f°E", latitude, longitude)
    }
}
