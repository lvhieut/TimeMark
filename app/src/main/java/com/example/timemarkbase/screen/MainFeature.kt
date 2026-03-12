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
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import androidx.core.graphics.drawable.toDrawable
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.timemarkbase.BottomSheetFragment
import com.example.timemarkbase.R
import com.example.timemarkbase.databinding.ActivityMainFeatureBinding
import com.example.timemarkbase.utils.CompassManager
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

class MainFeature : AppCompatActivity(), SensorEventListener {

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

                                    val formattedCode = getString(R.string.image_verified_code, currentVerifiedCode)
                                    binding?.timeMarkView?.txtImageVerified?.text = formattedCode

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
    private lateinit var sensorManager: SensorManager
    private var rotationSensor: Sensor? = null
    private val commonTypeface: Typeface by lazy {
        Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
    }

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
        applyCommonFont()
        initObserve()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
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
                            val formattedCode = getString(R.string.image_verified_code, currentVerifiedCode)
                            binding?.timeMarkView?.txtImageVerified?.text = formattedCode

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
            featureViewModel.setTime(binding?.timeMarkView?.txtTime?.text.toString())
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
            binding?.timeMarkView?.txtTime?.text = it
        }

        featureViewModel.isEnableVerifiedText.observe(this) {
            binding?.timeMarkView?.txtImageVerified?.isVisible = it
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
            binding?.txtLatAndLon?.text = "%.6f°N, %.6f°E".format(latLon.first, latLon.second)
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
//            binding?.imgMapGg?.isVisible = it
//            binding?.timeMarkView?.txtLatAndLon?.isVisible = it
            binding?.csLayoutMap?.isVisible = it
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
        rotationSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        } ?: run {
            Log.e("SensorError", "Thiết bị không hỗ trợ Rotation Vector Sensor")
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    private fun getDateFormater() {
        val currentDate = Date(System.currentTimeMillis())
        val patternDate = getString(R.string.text_time_formater_v2)
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
        binding?.timeMarkView?.txtTime?.text = formattedTime
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
    private fun getCurrentLocation(
        activity: Activity,
        onResult: (Location?) -> Unit
    ) {

        val fusedClient =
            LocationServices.getFusedLocationProviderClient(activity)

        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            onResult(null)
            return
        }

        fusedClient.lastLocation
            .addOnSuccessListener { location ->

                if (location != null) {
                    onResult(location)
                    return@addOnSuccessListener
                }

                // 3️⃣ Nếu null thì request GPS update
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

                            val newLocation = result.lastLocation
                            onResult(newLocation)
                        }
                    },
                    Looper.getMainLooper()
                )
            }
            .addOnFailureListener {
                onResult(null)
            }
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

    override fun onSensorChanged(event: SensorEvent?) {
        Log.d("TAG::", "event: $event")
        Log.d("TAG::", "event: ${event?.sensor?.type}")
        if (event?.sensor?.type == Sensor.TYPE_ROTATION_VECTOR) {
            val rotationMatrix = FloatArray(9)
            val orientationValues = FloatArray(3)
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationValues)
            Log.d("TAG::", "event: quay")

            // 1. Tính góc (0 - 360 độ)
            var bearing = Math.toDegrees(orientationValues[0].toDouble()).toFloat()
            if (bearing < 0) bearing += 360f // Đưa về khoảng 0-360

            // 2. Lấy hướng chữ (N, S, E, W...)
            val directionStr = getDirectionName(bearing)

            binding?.iconCompass?.rotation = -bearing

            // 4. Cập nhật TextView tổng hợp
            val displayStr = "%.1f°%s".format(
                bearing,
                directionStr
            )

            binding?.tvDirection?.text = displayStr
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    private fun getDirectionName(bearing: Float): String {
        return when (bearing) {
            in 0f..22.5f, in 337.5f..360f -> "N"
            in 22.5f..67.5f -> "NE"
            in 67.5f..112.5f -> "E"
            in 112.5f..157.5f -> "SE"
            in 157.5f..202.5f -> "S"
            in 202.5f..247.5f -> "SW"
            in 247.5f..292.5f -> "W"
            in 292.5f..337.5f -> "NW"
            else -> "N"
        }
    }

    fun calculateFinalBearing(
        currentLoc: Location,
        destLoc: Location,
        azimuth: Float
    ): Float {
        // 1. Tính toán Declination (Độ lệch từ trường)
        val geoField = GeomagneticField(
            currentLoc.latitude.toFloat(),
            currentLoc.longitude.toFloat(),
            currentLoc.altitude.toFloat(),
            System.currentTimeMillis()
        )
        val declination = geoField.declination

        // 2. Điều chỉnh azimuth từ Magnetic North sang True North
        // Sensor trả về âm nếu lệch trái, dương nếu lệch phải
        val trueHeading = azimuth + declination

        // 3. Tính bearing từ mình đến đích (độ Đông của True North)
        val bearingToDest = currentLoc.bearingTo(destLoc)

        // 4. Tính góc quay tương đối của mũi tên
        // Công thức: Góc cần quay = (Góc tới đích) - (Hướng máy đang nhìn)
        var finalRotation = bearingToDest - trueHeading

        finalRotation = (finalRotation + 360) % 360

        return finalRotation
    }

    private fun applyCommonFont() {
        val textViews = listOf(
            binding?.timeMarkView?.txtTime,
            binding?.timeMarkView?.txtDay,
            binding?.timeMarkView?.txtDateFormater,
            binding?.timeMarkView?.txtAddress,
            binding?.timeMarkView?.txtNameUser,
            binding?.timeMarkView?.txtImageVerified
        )

        textViews.forEach { it?.typeface = commonTypeface }
    }

}