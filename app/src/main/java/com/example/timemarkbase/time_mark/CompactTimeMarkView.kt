package com.example.timemarkbase.time_mark

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.TextView
import androidx.core.view.isVisible
import com.example.timemarkbase.R
import com.example.timemarkbase.custom_view.TimeDigitView
import com.example.timemarkbase.model.TimeMarkContent
import java.util.Locale

class CompactTimeMarkView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : BaseTimeMarkView(context, attrs) {

    private val txtTime: TimeDigitView?
    private val txtDate: TextView?
    private val txtDay: TextView?
    private val txtAddress: TextView?
    private val txtLatAndLon: TextView?
    private val txtNameUser: TextView?
    private val txtCompany: TextView?

    init {
        LayoutInflater.from(context).inflate(R.layout.view_time_mark_compact, this, true)
        txtTime = findViewById(R.id.txtTime)
        txtDate = findViewById(R.id.txtDateFormater)
        txtDay = findViewById(R.id.txtDay)
        txtAddress = findViewById(R.id.txtAddress)
        txtLatAndLon = findViewById(R.id.txtLatAndLon)
        txtNameUser = findViewById(R.id.txtNameUser)
        txtCompany = findViewById(R.id.txtCompany)
        applyCommonStyle()
    }

    override fun bind(content: TimeMarkContent) {
        txtTime?.setTime(content.time)
        txtDate?.text = formatDateTime(content)
        txtDay?.text = content.day
        txtAddress?.text = content.address
        txtNameUser?.text = formatFullName(content.fullName)
        txtNameUser?.isVisible = content.showFullName
        txtCompany?.text = content.companyName
        txtCompany?.isVisible = content.showCompanyName
        txtLatAndLon?.text = formatLatLon(content)
        txtLatAndLon?.isVisible = content.showGoogleMap
    }

    override fun applyCommonStyle() {
        styleTextViews(txtDate, txtDay, txtAddress, txtLatAndLon)
        txtNameUser?.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
        txtCompany?.typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
    }

    private fun formatDateTime(content: TimeMarkContent): String {
        return listOfNotNull(
            content.day.takeIf { it.isNotBlank() },
            content.date.takeIf { it.isNotBlank() }
        ).joinToString(", ")
            .let { dateText ->
                if (content.time.isBlank()) dateText else "$dateText ${content.time}"
            }
    }

    private fun formatFullName(fullName: String): String {
        if (fullName.isBlank()) return ""
        return if (fullName.contains(":")) fullName else fullName
    }

    private fun formatLatLon(content: TimeMarkContent): String {
        val latitude = content.latitude ?: return ""
        val longitude = content.longitude ?: return ""
        return String.format(Locale.US, "%.6f, %.6f", latitude, longitude)
    }
}
