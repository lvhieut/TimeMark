package com.example.timemarkbase.screen

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.text.TextPaint
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.timemarkbase.BottomSheetFragment
import com.example.timemarkbase.BuildConfig
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
                        .into(object : CustomTarget<Drawable>() {
                            override fun onResourceReady(
                                resource: Drawable,
                                transition: Transition<in Drawable>?
                            ) {
                                it1.setImageDrawable(resource)
                                it1.post {
                                    val bitmap = (binding?.imgPreview?.drawable?.toBitmap()
                                        ?: return@post)
                                    originalBitmap?.let {
                                        if (!it.isRecycled) it.recycle()
                                    }
                                    originalBitmap = bitmap
                                    currentVerifiedCode = generateSecureCode()

                                    updateWatermark()
                                }
                            }

                            override fun onLoadCleared(placeholder: Drawable?) {}

                        })
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

    private var originalBitmap: Bitmap? = null
    private var currentVerifiedCode = ""
    private var currentLat: Double = 0.0
    private var currentLon: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainFeatureBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding?.root)
        //setting default value
        featureViewModel.setEnableFullName(true)
        featureViewModel.setEnableLogo(false)
        featureViewModel.setEnableVerifiedText(true)
        featureViewModel.setEnableImageGoogleMap(false)

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
                .into(object : CustomTarget<Drawable>() {
                    override fun onResourceReady(
                        resource: Drawable,
                        transition: Transition<in Drawable>?
                    ) {
                        binding?.imgPreview?.setImageDrawable(resource)
                        binding?.imgPreview?.post {
                            val bitmap = (binding?.imgPreview?.drawable?.toBitmap()
                                ?: return@post)
                            originalBitmap?.let {
                                if (!it.isRecycled) it.recycle()
                            }

                            originalBitmap = bitmap
                            currentVerifiedCode = generateSecureCode()

                            updateWatermark()
                        }
                    }

                    override fun onLoadCleared(placeholder: Drawable?) {
                    }

                })
        }

        binding?.commonToolbarWrapper?.btnChooseImageView?.setOnClickListener {
            openGallery(SelectMode.SELECT_IMAGE.toString())
        }

        binding?.commonToolbarWrapper?.btnEditInformation?.setOnClickListener {
            BottomSheetFragment().show(supportFragmentManager, null)
            featureViewModel.setFullName(binding?.timeMarkView?.txtNameUser?.text.toString())
            featureViewModel.setDay(binding?.timeMarkView?.txtDay?.text.toString())
            featureViewModel.setAddress(binding?.timeMarkView?.txtAddress?.text.toString())
            binding?.timeMarkView?.txtTime?.let { timeView ->
                featureViewModel.setTime(timeView.getTime())
            }
            featureViewModel.setDate(binding?.timeMarkView?.txtDateFormater?.text.toString())
        }

        binding?.commonToolbarWrapper?.btnSaveImage?.setOnClickListener {
            binding?.csLayoutMainContent?.let { csLayoutMainContent ->
                val root = binding?.csLayoutMainContent ?: return@setOnClickListener
                val imageView = binding?.imgPreview ?: return@setOnClickListener
                captureAndCrop(
                    root,
                    imageView
                )
            }
        }

        binding?.commonToolbarWrapper?.iconBack?.setOnClickListener {
            finish()
        }

        binding?.imgLogoCompany?.setOnClickListener {
            openGallery(SelectMode.SELECT_LOGO.toString())
        }

        binding?.textEncrypt?.apply {
            text = generateSecureCode()
            typeface = android.graphics.Typeface.SANS_SERIF
        }
    }

    private fun captureAndCrop(rootView: View, targetView: View) {
        // 1. Capture toàn layout
        val fullBitmap = Bitmap.createBitmap(
            rootView.width,
            rootView.height,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(fullBitmap)
        rootView.draw(canvas)

        // 2. Lấy vị trí ImageView trong layout
        val location = IntArray(2)
        targetView.getLocationInWindow(location)

        val rootLocation = IntArray(2)
        rootView.getLocationInWindow(rootLocation)

        val left = location[0] - rootLocation[0]
        val top = location[1] - rootLocation[1]
        val width = targetView.width
        val height = targetView.height

        // 3. Crop đúng vùng ảnh
        val croppedBitmap = Bitmap.createBitmap(
            fullBitmap,
            left,
            top,
            width,
            height
        )

        // 4. Save bitmap đã crop
        captureImageViewAndSave(this, croppedBitmap)

        fullBitmap.recycle()
    }

    @SuppressLint("SetTextI18n")
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

        featureViewModel.latLon.observe(this) { latLon ->
            binding?.timeMarkView?.txtLatAndLon?.text = "Toạ độ: %.5f, %.5f".format(latLon.first, latLon.second)
            featureViewModel.isEnableGoogleMap.observe(this) { enable ->
                if (enable) {
                    Log.d("TAG::", "enable: $enable")
//                    binding?.imgMapGg?.loadGoongStaticMap(
//                        latLon.first,
//                        latLon.second
//                    )
                }
            }
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

        featureViewModel.isEnableVerifiedText.observe(this) { isShow ->
            updateWatermark()
        }

        featureViewModel.isEnableGoogleMap.observe(this) {
            binding?.imgMapGg?.isVisible = it
            binding?.timeMarkView?.txtLatAndLon?.isVisible = it
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
        when (from) {
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

    private fun updateWatermark() {
        val bitmap = originalBitmap ?: return
        val isShow = featureViewModel.isEnableVerifiedText.value ?: false

        val finalBitmap = addVerifiedWatermarkToBitmapV5(
            application,
            bitmap,
            currentVerifiedCode,
            isShow
        )

        binding?.imgPreview?.setImageBitmap(finalBitmap)
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

            currentLat = location.latitude
            currentLon = location.longitude
            featureViewModel.setLatLon(currentLat, currentLon)

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
        bitmap: Bitmap,
        fileName: String = "timemark_capture_${System.currentTimeMillis()}.jpg"
    ) {
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
            resolver.openOutputStream(uri).use { output ->
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

    fun addVerifiedWatermarkToBitmapV5(
        context: Context,
        original: Bitmap,
        verifiedId: String,
        isShowVerified: Boolean
    ): Bitmap {

        if (!isShowVerified) {
            return original
        }

        val result = original.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(result)
        val density = context.resources.displayMetrics.density
        val width = result.width.toFloat()
        val height = result.height.toFloat()

        val paintId = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EEEEEE")
            textSize = width * 0.024f
            typeface = Typeface.SANS_SERIF
            letterSpacing = 0.01f
            setShadowLayer(
                0.0015f * width * 0.4f,
                0.3f,
                0.3f,
                Color.parseColor("#50000000")
            )
        }

        val paintText = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EEEEEE")
            textSize = width * 0.023f
            typeface = Typeface.SANS_SERIF
            letterSpacing = 0.03f
            setShadowLayer(
                0.0015f * width * 0.4f,
                0.3f,
                0.3f,
                Color.parseColor("#50000000")
            )
        }
        //16f

        val iconSize = (width * 0.02f).toInt()   // scale theo ảnh
        val iconDrawable = context.getDrawable(R.drawable.icon_verified)!!
        val iconBitmap = Bitmap.createScaledBitmap(
            iconDrawable.toBitmap(),
            iconSize,
            iconSize,
            true
        )

//        val iconSize = (16f * density).toInt()
//        val iconDrawable = context.getDrawable(R.drawable.icon_verified)!!
//        val iconBitmap = iconDrawable.toBitmap(iconSize, iconSize)

        //val spacing = 4f * density
        val spacing = width * 0.006f
        val verifiedText = "Timemark Verified"

        val totalWidth = iconSize +
                spacing +
                paintId.measureText(verifiedId) +
                spacing +
                paintText.measureText(verifiedText)

        //14f
//        val paddingRight = 10f * density
        val paddingRight = width * 0.02f
        val translateX = width - paddingRight
        val translateY = height / 2f

        canvas.save()
        canvas.translate(translateX, translateY)
        canvas.rotate(-90f)
        canvas.translate(-totalWidth / 2f, 0f)

        canvas.drawBitmap(iconBitmap, 0f, -iconSize / 2f, null)

        val textIdX = iconSize + spacing
        val textIdY = -(paintId.descent() + paintId.ascent()) / 2
        canvas.drawText(verifiedId, textIdX, textIdY, paintId)

        val textVerifiedX =
            textIdX + paintId.measureText(verifiedId) + spacing
        val textVerifiedY =
            -(paintText.descent() + paintText.ascent()) / 2
        canvas.drawText(verifiedText, textVerifiedX, textVerifiedY, paintText)

        canvas.restore()
        return result
    }

}