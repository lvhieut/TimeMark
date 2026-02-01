package com.example.timemarkbase.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.timemarkbase.R
import com.example.timemarkbase.config.fb_ext.parseUserConfig
import com.example.timemarkbase.utils.UserPrefs
import com.example.timemarkbase.utils.getAndroidID
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var remoteConfig: FirebaseRemoteConfig

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
        fetchConfigAndNavigate()
    }

    private fun fetchConfigAndNavigate() {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                Log.d("TAG::", "task: ${task.isSuccessful} + ${task.isComplete}")
                if (task.isSuccessful) {
                    handleRemoteConfig(task.isSuccessful)
                }
            }
    }

    private fun goNext() {
        if (isFinishing) return
        val intent = Intent(this, ActiveActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun goNextSelect() {
        if (isFinishing) return
        val intent = Intent(this, SelectModeEditImageActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun initRemoteConfig() {
        remoteConfig = Firebase.remoteConfig

        val settings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 300
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

        // 🔴 1. ƯU TIÊN check Android ID
        val remoteUser = config.listUserId
            .firstOrNull { it.userId == androidId }
        Log.d("TAG::", "remoteUser: $remoteUser")

        // ❌ Android ID KHÔNG TỒN TẠI / BỊ XOÁ
        if (remoteUser == null) {
            UserPrefs.clearUser(this)
            Log.d("TAG::", "clearUser (remoteUser == null)")
            goNext()
            return
        }

        val remotePassword = remoteUser.activeKey
        val localPassword = UserPrefs.getPassword(this)
        Log.d("TAG::", "remotePassword: $remotePassword + localPassword: $localPassword")

        // 🟡 2. Lần đầu / clear app
        if (localPassword == null) {
            UserPrefs.saveUser(this, androidId, remotePassword)
            goNext()
            return
        }

        // 🔁 3. Android ID đúng nhưng password ĐÃ ĐỔI
        if (localPassword != remotePassword) {
            UserPrefs.clearUser(this)
            goNext()
            return
        }

        // ✅ 4. Android ID + password đều hợp lệ
        goNextSelect()
    }

    override fun onResume() {
        super.onResume()
    }
}