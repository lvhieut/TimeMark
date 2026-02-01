package com.example.timemarkbase.screen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.timemarkbase.R
import com.example.timemarkbase.config.fb_ext.parseUserConfig
import com.example.timemarkbase.config.model.UserConfig
import com.example.timemarkbase.databinding.ActivityActiveBinding
import com.example.timemarkbase.utils.UserPrefs
import com.example.timemarkbase.utils.getAndroidID
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings

class ActiveActivity : AppCompatActivity() {

    private lateinit var binding: ActivityActiveBinding
    private lateinit var remoteConfig: FirebaseRemoteConfig

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityActiveBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Firebase Remote Config
        remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 300
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    override fun onResume() {
        super.onResume()
        val androidId = getAndroidID()
        binding.androidId.text = androidId
        Log.d("TAG::", "androidId: $androidId")
        binding.btnActive.setOnClickListener {
            val inputKey = binding.textActive.text.toString().trim()
            binding.androidId.text = androidId
            fetchRemoteConfigAndValidate(androidId, inputKey)
        }
    }

    private fun fetchRemoteConfigAndValidate(androidId: String, inputKey: String) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val json = remoteConfig.getString("user_config")
                    if (json.isNotBlank()) {
                        val config = parseUserConfig(json)
                        val listDeviceAccepted = config.listUserId

                        // tìm user hợp lệ
                        val matchedUser = listDeviceAccepted.firstOrNull {
                            it.userId == androidId && it.activeKey == inputKey
                        }

                        if (matchedUser != null) {
                            // Hợp lệ → lưu SharedPreferences + chuyển màn
                            UserPrefs.saveUser(this, androidId, inputKey)
                            goNext()
                        } else {
                            // Không hợp lệ → show thông báo
                            Toast.makeText(this, "Active key không hợp lệ hoặc thiết bị chưa được phép", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(this, "Không có dữ liệu config từ server", Toast.LENGTH_LONG).show()
                    }
                } else {
                    Toast.makeText(this, "Lỗi kết nối server, thử lại", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun goNext() {
        val intent = Intent(this, SelectModeEditImageActivity::class.java)
        startActivity(intent)
        finish()
    }
}