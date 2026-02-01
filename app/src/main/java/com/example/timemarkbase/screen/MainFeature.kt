package com.example.timemarkbase.screen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.example.timemarkbase.BottomSheetFragment
import com.example.timemarkbase.R
import com.example.timemarkbase.databinding.ActivityMainFeatureBinding
import com.example.timemarkbase.utils.SelectMode
import com.example.timemarkbase.view_model.MainFeatureViewModel
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainFeature : AppCompatActivity() {

    private val pickMediaLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                binding?.imgPreview?.let { it1 ->
                    Glide.with(it1.context)
                        .load(it)
                        .dontTransform()
                        .into(it1)

                }
            }
        }

    private val pickMediaLogoLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
            uri?.let {
                binding?.imgLogoCompany?.let { img ->
                    Glide.with(img.context)
                        .load(it)
                        .dontTransform()
                        .into(img)

                }
            }
        }
    private var binding: ActivityMainFeatureBinding? = null

    private val featureViewModel: MainFeatureViewModel by viewModels()

    private val secureRandom = SecureRandom()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainFeatureBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding?.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initObserve()

        getDateFormater()
        getTimeFormatter()
        checkLocationPermissionAndGetAddress()

        val photoPath = intent.getStringExtra("photo_path")
        photoPath?.let {
            Glide.with(this)
                .load(it)
                .into(binding?.imgPreview!!)
        }

        binding?.commonToolbarWrapper?.btnChooseImageView?.setOnClickListener {
            openGallery(SelectMode.SELECT_IMAGE.toString())
        }

        binding?.timeMarkView?.root?.setOnClickListener {
            featureViewModel.setFullName(binding?.timeMarkView?.txtNameUser?.text.toString())
            featureViewModel.setDay(binding?.timeMarkView?.txtDay?.text.toString())
            featureViewModel.setAddress(binding?.timeMarkView?.txtAddress?.text.toString())
            binding?.timeMarkView?.txtTime?.let { timeView ->
                featureViewModel.setTime(timeView.getTime())
            }
            featureViewModel.setDate(binding?.timeMarkView?.txtDateFormater?.text.toString())
            featureViewModel.setEnableFullName(true)
            featureViewModel.setEnableLogo(true)
            BottomSheetFragment().show(supportFragmentManager, null)
        }

        binding?.commonToolbarWrapper?.btnSaveImage?.setOnClickListener {
            binding?.csLayoutMainContent?.let { csLayoutMainContent ->
                captureImageViewAndSave(this@MainFeature,
                    csLayoutMainContent
                )
            }
        }

        binding?.commonToolbarWrapper?.iconBack?.setOnClickListener {
            finish()
        }

        binding?.imgLogoCompany?.setOnClickListener {
            openGallery(SelectMode.SELECT_LOGO.toString())
        }


        binding?.textEncrypt?.text = generateSecureCode()
    }

    private fun initObserve() {
        featureViewModel.time.observe(this) {
            binding?.timeMarkView?.txtTime?.setTime(it)
        }

        featureViewModel.day.observe(this) {
            binding?.timeMarkView?.txtDay?.text = it
        }

        featureViewModel.date.observe(this) {
            binding?.timeMarkView?.txtDateFormater?.text = it
        }

        featureViewModel.address.observe(this) {
            binding?.timeMarkView?.txtAddress?.text = it
        }

        featureViewModel.fullName.observe(this) {
            binding?.timeMarkView?.txtNameUser?.text = it
        }

        featureViewModel.isEnableFullName.observe(this) {
            binding?.timeMarkView?.txtNameUser?.isVisible = it
        }

        featureViewModel.isEnableLogo.observe(this) {
            binding?.imgLogoCompany?.isVisible = it
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            checkLocationPermissionAndGetAddress()
        } else {
            binding?.timeMarkView?.txtAddress?.text = "Không có quyền vị trí"
        }
    }

    private fun checkLocationPermissionAndGetAddress() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            getCurrentAddress(this) { result ->
                binding?.timeMarkView?.txtAddress?.text = result ?: "Không xác định địa chỉ"
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1001
            )
        }
    }

    override fun onResume() {
        super.onResume()

    }

    private fun getDateFormater() {
        val currentDate = Date(System.currentTimeMillis())
        val patternDate = getString(R.string.text_time_formater)
        val patternDay = getString(R.string.text_day_demo)
        val formatter = SimpleDateFormat(patternDate, Locale("vi"))
        val dayFormatter = SimpleDateFormat(patternDay, Locale("vi"))
        val formattedDate = formatter.format(currentDate)
        val formattedDay = dayFormatter.format(currentDate)
        binding?.timeMarkView?.txtDateFormater?.text = formattedDate
        binding?.timeMarkView?.txtDay?.text = formattedDay
    }

    private fun openGallery(from: String) {
        when(from) {
            SelectMode.SELECT_IMAGE.toString() -> {
                pickMediaLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
            SelectMode.SELECT_LOGO.toString() -> {
                pickMediaLogoLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly
                    )
                )
            }
        }
    }

    private fun getTimeFormatter() {
        val currentDate = Date()
        val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime = timeFormatter.format(currentDate)
        binding?.timeMarkView?.txtTime?.setTime(formattedTime)
    }

    fun getCurrentAddress(
        activity: Activity,
        onResult: (String?) -> Unit
    ) {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onResult(null)
            return
        }

        getCurrentLocation(activity) { location ->
            if (location == null) {
                onResult(null)
                return@getCurrentLocation
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val geocoder = Geocoder(activity, Locale("vi", "VN"))
                    val list = geocoder.getFromLocation(
                        location.latitude,
                        location.longitude,
                        1
                    )
                    val address = list?.firstOrNull()?.getAddressLine(0)

                    withContext(Dispatchers.Main) {
                        onResult(address)
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        onResult(null)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(
        activity: Activity,
        onResult: (Location?) -> Unit
    ) {
        val fusedClient =
            LocationServices.getFusedLocationProviderClient(activity)

        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            1000
        )
            .setMaxUpdates(1)
            .build()

        fusedClient.requestLocationUpdates(
            request,
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    fusedClient.removeLocationUpdates(this)
                    onResult(result.lastLocation)
                }
            },
            Looper.getMainLooper()
        )
    }

    fun generateSecureCode(): String {
        val totalLength = 14
        val letterCount = secureRandom.nextInt(4) + 9
        val numberCount = totalLength - letterCount

        val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val numbers = "0123456789"

        val result = mutableListOf<Char>()

        repeat(letterCount) {
            result.add(letters[secureRandom.nextInt(letters.length)])
        }

        repeat(numberCount) {
            result.add(numbers[secureRandom.nextInt(numbers.length)])
        }

        result.shuffle(secureRandom)
        return result.joinToString("")
    }

    private fun captureImageViewAndSave(
        activity: Activity,
        view: View,
        fileName: String = "timemark_capture_${System.currentTimeMillis()}.jpg"
    ) {
        if (view.width == 0 || view.height == 0) {
            view.post { captureImageViewAndSave(activity, view, fileName) }
            return
        }

        val bitmap = Bitmap.createBitmap(
            view.width,
            view.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = android.graphics.Canvas(bitmap)
        view.draw(canvas)

        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/TimeMark")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val resolver = activity.contentResolver
        val imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let { uri ->
            val outputStream = resolver.openOutputStream(uri)
            outputStream.use { output ->
                if (output != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, output)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            showGrayToast(activity, "Đã lưu ảnh vào thư viện")
        } ?: run {
            showGrayToast(activity, "Lưu ảnh thất bại")
        }
    }

    private fun showGrayToast(context: Context, message: String) {
        val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
        val view = toast.view

        view?.let {
            it.setBackgroundResource(android.R.drawable.toast_frame)
            it.background?.setTint(0xCC444444.toInt())
            val text = it.findViewById<TextView>(android.R.id.message)
            text.setTextColor(Color.WHITE)
            text.textSize = 13f
        }

        toast.show()
    }
}