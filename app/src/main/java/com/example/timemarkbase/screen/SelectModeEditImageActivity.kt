package com.example.timemarkbase.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.timemarkbase.R
import com.example.timemarkbase.utils.ExpiryWarningDialog

class SelectModeEditImageActivity : AppCompatActivity() {

    private var btnChooseCamera: Button? = null
    private var btnChooseImage: Button? = null

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_select_mode_edit_image)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        if (!SplashActivity.modeBuild) {
            val message = intent.getStringExtra("expiry_warning")
            if (!message.isNullOrEmpty()) {
                ExpiryWarningDialog(message).show(
                    supportFragmentManager,
                    "expiry_dialog"
                )
            }
        }

        btnChooseCamera = findViewById(R.id.chooseCamera)
        btnChooseImage = findViewById(R.id.chooseImage)

        btnChooseImage?.setOnClickListener {
            val intent = Intent(this, MainFeature::class.java)
            startActivity(intent)
        }

        btnChooseCamera?.setOnClickListener {
            val intent = Intent(this, CameraActivity::class.java)
            startActivity(intent)
        }

        requestCameraPermissionIfNeeded()
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermissionIfNeeded() {
        if (!hasCameraPermission()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST
            )
        }
    }
}