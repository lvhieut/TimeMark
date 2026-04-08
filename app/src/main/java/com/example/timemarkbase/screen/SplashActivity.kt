package com.example.timemarkbase.screen

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.timemarkbase.R
import com.example.timemarkbase.config.fb_ext.parseUserConfig
import com.example.timemarkbase.utils.ExpiryWarningDialog
import com.example.timemarkbase.utils.UserPrefs
import com.example.timemarkbase.utils.getAndroidID
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var remoteConfig: FirebaseRemoteConfig
    private var daysLeft: Int = -1
    //build without fetconfig
    companion object {
        //test mode
        const val modeBuild: Boolean = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initRemoteConfig()
        if (modeBuild) {
            goNextSelect()
        } else {
            fetchConfigAndNavigate()
        }
    }

    private fun fetchConfigAndNavigate() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    handleRemoteConfig(task.isSuccessful)
                }
            }
    }

    private fun goNext() {
        if (isFinishing) return
        val intent = Intent(this, ActiveActivity::class.java)
        intent.putExtra("expiry_warning", "Tài khoản sắp hết hạn sau $daysLeft ngày")
        startActivity(intent)
        finish()
    }

    private fun goNextSelect() {
        if (isFinishing) return
        val intent = Intent(this, SelectModeEditImageActivity::class.java)
        if (!modeBuild) {
            intent.putExtra("expiry_warning", "Tài khoản sắp hết hạn sau $daysLeft ngày")
        }
        startActivity(intent)
        finish()
    }

    private fun initRemoteConfig() {
        remoteConfig = Firebase.remoteConfig

        val settings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 600
        }
        remoteConfig.setConfigSettingsAsync(settings)
    }

    private fun handleRemoteConfig(success: Boolean) {
        if (!success) {
            goNext()
            return
        }

        val json = remoteConfig.getString("user_config")
        if (json.isBlank()) {
            UserPrefs.clearUser(this)
            goNext()
            return
        }

        val config = parseUserConfig(json)
        val androidId = getAndroidID()

        val remoteUser = config.listUserId
            .firstOrNull { it.userId == androidId }

        if (remoteUser == null) {
            UserPrefs.clearUser(this)
            goNext()
            return
        }
        val valid = isValidDueDate(remoteUser.dueDate)
        if (!valid) {
            return
        }

        val remotePassword = remoteUser.activeKey
        val localPassword = UserPrefs.getPassword(this)

        if (localPassword == null) {
            UserPrefs.saveUser(this, androidId, remotePassword)
            goNext()
            return
        }

        if (localPassword != remotePassword) {
            UserPrefs.clearUser(this)
            goNext()
            return
        }

        goNextSelect()
    }

    private fun isValidDueDate(dueDate: String): Boolean {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            formatter.isLenient = false

            val due = formatter.parse(dueDate) ?: return false

            val todayStr = formatter.format(Date())
            val today = formatter.parse(todayStr)!!

            val diffMillis = due.time - today.time
            daysLeft = (diffMillis / (24 * 60 * 60 * 1000)).toInt()

            return if (today.after(due)) {
                ExpiryWarningDialog("Người dùng này đã sử dụng quá hạn").show(
                    supportFragmentManager,
                    "expiry_dialog"
                )
                false
            } else {
                val twoDaysMillis = 2 * 24 * 60 * 60 * 1000
                if (diffMillis <= twoDaysMillis) {
                    //Todo
                    Log.d("TAG::", "daysLeft: $daysLeft")
                }
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}