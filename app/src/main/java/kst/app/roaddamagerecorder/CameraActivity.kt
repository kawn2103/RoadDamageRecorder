package kst.app.roaddamagerecorder

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ExifInterface
import android.media.Image
import android.media.ImageReader
import android.media.MediaRecorder
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.Surface
import android.view.TextureView.SurfaceTextureListener
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import kst.app.roaddamagerecorder.databinding.ActivityCameraBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCameraBinding

    //파일 저장 위치
    private lateinit var basePath: String
    // 위치 정보 요청 라이브러리
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    // 위치정보 요청
    private lateinit var locationRequest: LocationRequest
    // 위치 정보 콜백
    private lateinit var locationCallback: LocationCallback

    private var cameraId: String? = null
    private var cameraDevice: CameraDevice? = null
    private var mediaRecorder: MediaRecorder? = null
    private var captureRequestBuilder: CaptureRequest.Builder? = null
    private var backgroundHandlerThread: HandlerThread? = null
    private var backgroundHandler: Handler? = null
    private var imageReader: ImageReader? = null
    private var captureTimerTask: Timer? = null

    //텍스쳐뷰의 상태를 감지하는 리스너
    private val mSurfaceTextureListener= object : SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            setupCamera()
            connectCamera()
        }

        override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
        ) {
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            return false
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    //카메라 디바이스상태 감지 및 연결
    private val cameraDeviceStateCallback= object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            mediaRecorder = MediaRecorder()
            if(binding.recordBt.tag != null){
                startRecord()
                mediaRecorder?.start()
            }else{
                startPreview()
            }

        }
        override fun onClosed(camera: CameraDevice) {
            super.onClosed(camera)
        }

        override fun onDisconnected(camera: CameraDevice) {
            camera.close()
            cameraDevice = null
        }

        override fun onError(camera: CameraDevice, error: Int) {
            camera.close()
            cameraDevice = null
        }
    }

    private inner class ImageSaver(image: Image) : Runnable {
        private val mImage: Image = image
        private val imageFilePath: String = "$basePath/${SimpleDateFormat("yyyy-MM-dd HH_mm_ss_SSS").format(Date())}.jpg"

        override fun run() {
            val byteBuffer = mImage.planes[0].buffer
            val bytes = ByteArray(byteBuffer.remaining())
            byteBuffer[bytes]
            var fileOutputStream: FileOutputStream? = null
            try {
                fileOutputStream = FileOutputStream(imageFilePath)
                fileOutputStream.write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                mImage.close()
                mediaNotification(imageFilePath)
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
            setExif(imageFilePath)
        }
    }

    private val onImageAvailableListener = ImageReader.OnImageAvailableListener {
        backgroundHandler?.post(ImageSaver(it.acquireNextImage()))
    }

    private var recordCaptureSession: CameraCaptureSession? = null
    private val recordCaptureCallback = object : CameraCaptureSession.CaptureCallback(){
        override fun onCaptureStarted(session: CameraCaptureSession, request: CaptureRequest, timestamp: Long, frameNumber: Long) {
            super.onCaptureStarted(session, request, timestamp, frameNumber)
        }

        override fun onCaptureProgressed(session: CameraCaptureSession, request: CaptureRequest, partialResult: CaptureResult) {
            super.onCaptureProgressed(session, request, partialResult)
        }

        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
            super.onCaptureCompleted(session, request, result)
            result.get(CaptureResult.SENSOR_EXPOSURE_TIME)
        }

        override fun onCaptureFailed(session: CameraCaptureSession, request: CaptureRequest, failure: CaptureFailure) {
            super.onCaptureFailed(session, request, failure)
        }

        override fun onCaptureSequenceCompleted(session: CameraCaptureSession, sequenceId: Int, frameNumber: Long) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber)
        }

        override fun onCaptureSequenceAborted(session: CameraCaptureSession, sequenceId: Int) {
            super.onCaptureSequenceAborted(session, sequenceId)
        }

        override fun onCaptureBufferLost(session: CameraCaptureSession, request: CaptureRequest, target: Surface, frameNumber: Long) {
            super.onCaptureBufferLost(session, request, target, frameNumber)
        }
    }

    private var previewCaptureSession: CameraCaptureSession? = null
    private val previewCaptureCallback = object : CameraCaptureSession.CaptureCallback(){
        override fun onCaptureStarted(session: CameraCaptureSession, request: CaptureRequest, timestamp: Long, frameNumber: Long) {
            super.onCaptureStarted(session, request, timestamp, frameNumber)
        }

        override fun onCaptureProgressed(session: CameraCaptureSession, request: CaptureRequest, partialResult: CaptureResult) {
            super.onCaptureProgressed(session, request, partialResult)
        }

        override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
            super.onCaptureCompleted(session, request, result)
        }

        override fun onCaptureFailed(session: CameraCaptureSession, request: CaptureRequest, failure: CaptureFailure) {
            super.onCaptureFailed(session, request, failure)
        }

        override fun onCaptureSequenceCompleted(session: CameraCaptureSession, sequenceId: Int, frameNumber: Long) {
            super.onCaptureSequenceCompleted(session, sequenceId, frameNumber)
        }

        override fun onCaptureSequenceAborted(session: CameraCaptureSession, sequenceId: Int) {
            super.onCaptureSequenceAborted(session, sequenceId)
        }

        override fun onCaptureBufferLost(session: CameraCaptureSession, request: CaptureRequest, target: Surface, frameNumber: Long) {
            super.onCaptureBufferLost(session, request, target, frameNumber)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        //위치 정보 요청
        getLocationUpdates()
        initViews()
    }

    private fun initViews(){
        binding.recordBt.setOnClickListener {
            var recordBt = it as ImageButton

            if (recordBt.tag != null) { //촬영 중지
                Log.d("gwan2103_recordBt",recordBt.tag.toString())
                recordBt.setImageResource(R.drawable.record_start_icon)
                recordBt.tag = null
                stopRecordRoad()
            }else{  //촬영시작
                recordBt.setImageResource(R.drawable.record_stop_icon)
                recordBt.tag = "recording"
                startRecordRoad()
            }
        }

        binding.captureBt.setOnClickListener {
            captureRoad()
        }

        binding.exitBt.setOnClickListener {
            finish()
        }
    }

    private fun getOutputDirectory(){

        Log.d("gwan2103_dir", "sdCard Storage usable ====> ${isExternalStorageWritable()}")
        Log.d("gwan2103_dir", "sdCard Storage able ====> ${isExternalStorageReadable()}")
        val externalStorageVolumes: Array<out File> =
                ContextCompat.getExternalFilesDirs(applicationContext, null)
        val primaryExternalStorage = externalStorageVolumes[1]
        Log.d("gwan2103_dir", "primaryExternalStorage ====> $primaryExternalStorage")
        val mediaDir = File(primaryExternalStorage, "${SimpleDateFormat("yyyy-MM-dd HH_mm_ss").format(Date())}")


        if (mediaDir.exists()){
            Log.d("gwan2103_dir", "${mediaDir.absolutePath}.exists ====> true")
        }else{
            Log.d("gwan2103_dir", "${mediaDir.absolutePath}.exists ====> false")
            mediaDir.mkdir()
        }

        basePath = if (mediaDir.exists())
            mediaDir.absolutePath
        else
            filesDir.absolutePath
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    private fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }

    private fun getLocationUpdates()
    {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest()
        locationRequest.interval = 0
//        locationRequest.fastestInterval = 500
//        locationRequest.smallestDisplacement = 0f
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY //set according to your app function
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                //위치정보 결과 반환
                locationResult ?: return    //위치 정보가 없으면 리턴
                if (locationResult.locations.isNotEmpty()) {    //위치 정보가 있을 때 아래 수행
                    val location = locationResult.lastLocation
                    /*binding.latTv.text = location.latitude.toString()
                    binding.longTv.text = location.longitude.toString()*/
                    Log.d("gwan2103_gps", "latitude ====> ${location.latitude}")
                    Log.d("gwan2103_gps", "longitude ====> ${location.longitude}")
                }
            }
        }
    }

    // 위치 정보 업데이트 수행 함수
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            finish()
            return
        }
        fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
        )
    }

    // 위치 정보 업데이트 정지 함수
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    //카메라 핸들러
    private fun startBackgroundThread(){
        backgroundHandlerThread = HandlerThread("Camera2VideoImage")
        backgroundHandlerThread?.start()
        backgroundHandler = Handler(backgroundHandlerThread!!.looper)
    }

    private fun stopBackgroundThread() {
        backgroundHandlerThread?.quitSafely()
        try {
            backgroundHandlerThread?.join()
            backgroundHandlerThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    private fun startRecordRoad(){
        getOutputDirectory()
        startRecord()
        mediaRecorder?.start()
        captureTimerTask = kotlin.concurrent.timer(period = 500){
            runOnUiThread {
                binding.captureBt.performClick()
            }
        }
    }

    private fun stopRecordRoad(){
        captureTimerTask?.cancel()
        startPreview()
        mediaRecorder?.stop()
        mediaRecorder?.reset()
    }

    //카메라 설정
    private fun setupCamera(){
        val cameraManager : CameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            for (cameraId in cameraManager.cameraIdList) {
                val cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId)
                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) ==
                        CameraCharacteristics.LENS_FACING_FRONT) {
                    continue
                }
                val map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                imageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 1)
                imageReader?.setOnImageAvailableListener(onImageAvailableListener, backgroundHandler)
                this.cameraId = cameraId
                return
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    //카메라 연결
    private fun connectCamera() {
        val cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    cameraId?.let {
                        cameraManager.openCamera(it, cameraDeviceStateCallback, backgroundHandler)
                    }
                } else {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                        Toast.makeText(this,
                                "Video app required access to camera", Toast.LENGTH_SHORT).show()
                    }
                    requestPermissions(arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
                    ), REQUEST_CAMERA_PERMISSION_RESULT)
                }
            } else {
                cameraId?.let {
                    cameraManager.openCamera(it, cameraDeviceStateCallback, backgroundHandler)
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun closeCamera(){
        cameraDevice?.close()
        cameraDevice = null
        mediaRecorder?.release()
        mediaRecorder = null
    }

    //동영상 녹화 미디어 레코더 세팅
    @Throws(IOException::class)
    private fun setupMediaRecorder() {
        Log.d("gwan2103_flow","setupMediaRecorder basePath ====> $basePath")
        mediaRecorder?.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setOutputFile(File(
                File(basePath),
                SimpleDateFormat(FILENAME_FORMAT, Locale.US
                ).format(System.currentTimeMillis()) + ".mp4").absolutePath)
        mediaRecorder?.setVideoEncodingBitRate(20000000)
        mediaRecorder?.setVideoFrameRate(60)
        mediaRecorder?.setVideoSize(1920, 1080)
        mediaRecorder?.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder?.prepare()
    }

    private fun startPreview(){
        val surfaceTexture = binding.cameraView.surfaceTexture
        surfaceTexture?.setDefaultBufferSize(1920,1080)
        val previewSurface = Surface(surfaceTexture)

        try {
            captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder?.addTarget(previewSurface)

            cameraDevice?.createCaptureSession(listOf(previewSurface, imageReader?.surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            previewCaptureSession = session
                            try {
                                captureRequestBuilder?.build()?.let {
                                    previewCaptureSession?.setRepeatingRequest(it,
                                            null,backgroundHandler)
                                }
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {
                        }
                    }, null)
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    private fun startRecord() {
        try {
            setupMediaRecorder()
            val surfaceTexture = binding.cameraView.surfaceTexture
            surfaceTexture?.setDefaultBufferSize(1920, 1080)
            val previewSurface = Surface(surfaceTexture)
            val recordSurface = mediaRecorder?.surface
            captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
            captureRequestBuilder?.addTarget(previewSurface)
            if (recordSurface != null) {
                captureRequestBuilder?.addTarget(recordSurface)
            }
            cameraDevice?.createCaptureSession(listOf(previewSurface, recordSurface, imageReader?.surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            recordCaptureSession = session
                            try {
                                captureRequestBuilder?.build()?.let {
                                    recordCaptureSession?.setRepeatingRequest(
                                            it, null, null
                                    )
                                }
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {
                        }
                    }, null)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mediaNotification(imageFilePath : String){
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also {
            it.data = Uri.fromFile(File(imageFilePath))
            LocalBroadcastManager.getInstance(this@CameraActivity).sendBroadcast(it)
        }
    }

    private fun captureRoad(){
        try {
            captureRequestBuilder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_VIDEO_SNAPSHOT)
            imageReader?.surface?.let { captureRequestBuilder?.addTarget(it) }
            captureRequestBuilder?.build()?.let { recordCaptureSession?.capture(it,recordCaptureCallback,backgroundHandler) }
        }catch (e : Exception){
            e.printStackTrace()
        }

    }
    //촬영 사진 메타데이터 삽입
    private fun setExif(path: String){
        try {
            val exif = ExifInterface(path)
            exif.setAttribute(ExifInterface.TAG_APERTURE_VALUE, "1")
            exif.setAttribute(ExifInterface.TAG_ARTIST, "2")
            exif.setAttribute(ExifInterface.TAG_BITS_PER_SAMPLE, "3")
            exif.setAttribute(ExifInterface.TAG_BRIGHTNESS_VALUE, "5")
            exif.setAttribute(ExifInterface.TAG_CFA_PATTERN, "7")
            exif.setAttribute(ExifInterface.TAG_COLOR_SPACE, "8")
            exif.setAttribute(ExifInterface.TAG_COMPONENTS_CONFIGURATION, "9")
            exif.setAttribute(ExifInterface.TAG_COMPRESSED_BITS_PER_PIXEL, "10")
            exif.setAttribute(ExifInterface.TAG_COMPRESSION, "11")
            exif.setAttribute(ExifInterface.TAG_CONTRAST, "12")
            exif.setAttribute(ExifInterface.TAG_COPYRIGHT, "13")
            exif.setAttribute(ExifInterface.TAG_CUSTOM_RENDERED, "14")
            exif.setAttribute(ExifInterface.TAG_DATETIME, "15")
            exif.setAttribute(ExifInterface.TAG_DATETIME_DIGITIZED, "16")
            exif.setAttribute(ExifInterface.TAG_DATETIME_ORIGINAL, "17")
            exif.setAttribute(ExifInterface.TAG_DEFAULT_CROP_SIZE, "18")
            exif.setAttribute(ExifInterface.TAG_DEVICE_SETTING_DESCRIPTION, "19")
            exif.setAttribute(ExifInterface.TAG_DIGITAL_ZOOM_RATIO, "20")
            exif.setAttribute(ExifInterface.TAG_DNG_VERSION, "21")
            exif.setAttribute(ExifInterface.TAG_EXIF_VERSION, "22")
            exif.setAttribute(ExifInterface.TAG_EXPOSURE_BIAS_VALUE, "24")
            exif.setAttribute(ExifInterface.TAG_EXPOSURE_INDEX, "25")
            exif.setAttribute(ExifInterface.TAG_EXPOSURE_MODE, "26")
            exif.setAttribute(ExifInterface.TAG_EXPOSURE_PROGRAM, "27")
            exif.setAttribute(ExifInterface.TAG_EXPOSURE_TIME, "28")
            exif.setAttribute(ExifInterface.TAG_F_NUMBER, "29")
            exif.setAttribute(ExifInterface.TAG_FILE_SOURCE, "30")
            exif.setAttribute(ExifInterface.TAG_FLASH, "31")
            exif.setAttribute(ExifInterface.TAG_FLASH_ENERGY, "32")
            exif.setAttribute(ExifInterface.TAG_FLASHPIX_VERSION, "33")
            exif.setAttribute(ExifInterface.TAG_FOCAL_LENGTH, "34")
            exif.setAttribute(ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM, "35")
            exif.setAttribute(ExifInterface.TAG_FOCAL_PLANE_RESOLUTION_UNIT, "36")
            exif.setAttribute(ExifInterface.TAG_FOCAL_PLANE_X_RESOLUTION, "37")
            exif.setAttribute(ExifInterface.TAG_FOCAL_PLANE_Y_RESOLUTION, "38")
            exif.setAttribute(ExifInterface.TAG_GAIN_CONTROL, "39")
            exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, "41")
            exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, "42")
            exif.setAttribute(ExifInterface.TAG_GPS_AREA_INFORMATION, "43")
            exif.setAttribute(ExifInterface.TAG_GPS_DATESTAMP, "44")
            exif.setAttribute(ExifInterface.TAG_GPS_DEST_BEARING, "45")
            exif.setAttribute(ExifInterface.TAG_GPS_DEST_BEARING_REF, "46")
            exif.setAttribute(ExifInterface.TAG_GPS_DEST_DISTANCE, "47")
            exif.setAttribute(ExifInterface.TAG_GPS_DEST_DISTANCE_REF, "48")
            exif.setAttribute(ExifInterface.TAG_GPS_DEST_LATITUDE, "49")
            exif.setAttribute(ExifInterface.TAG_GPS_DEST_LATITUDE_REF, "50")
            exif.setAttribute(ExifInterface.TAG_GPS_DEST_LONGITUDE, "51")
            exif.setAttribute(ExifInterface.TAG_GPS_DEST_LONGITUDE_REF, "52")
            exif.setAttribute(ExifInterface.TAG_GPS_DIFFERENTIAL, "53")
            exif.setAttribute(ExifInterface.TAG_GPS_DOP, "54")
            exif.setAttribute(ExifInterface.TAG_GPS_IMG_DIRECTION, "56")
            exif.setAttribute(ExifInterface.TAG_GPS_IMG_DIRECTION_REF, "57")
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, "58")
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "59")
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, "60")
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "61")
            exif.setAttribute(ExifInterface.TAG_GPS_MAP_DATUM, "62")
            exif.setAttribute(ExifInterface.TAG_GPS_MEASURE_MODE, "63")
            exif.setAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD, "64")
            exif.setAttribute(ExifInterface.TAG_GPS_SATELLITES, "65")
            exif.setAttribute(ExifInterface.TAG_GPS_SPEED, "66")
            exif.setAttribute(ExifInterface.TAG_GPS_SPEED_REF, "67")
            exif.setAttribute(ExifInterface.TAG_GPS_STATUS, "68")
            exif.setAttribute(ExifInterface.TAG_GPS_TIMESTAMP, "69")
            exif.setAttribute(ExifInterface.TAG_GPS_TRACK, "70")
            exif.setAttribute(ExifInterface.TAG_GPS_TRACK_REF, "71")
            exif.setAttribute(ExifInterface.TAG_GPS_VERSION_ID, "72")
            exif.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, "73")
            exif.setAttribute(ExifInterface.TAG_IMAGE_LENGTH, "74")
            exif.setAttribute(ExifInterface.TAG_IMAGE_UNIQUE_ID, "75")
            exif.setAttribute(ExifInterface.TAG_IMAGE_WIDTH, "76")
            exif.setAttribute(ExifInterface.TAG_INTEROPERABILITY_INDEX, "77")
            exif.setAttribute(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT, "81")
            exif.setAttribute(ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, "82")
            exif.setAttribute(ExifInterface.TAG_LIGHT_SOURCE, "87")
            exif.setAttribute(ExifInterface.TAG_MAKE, "88")
            exif.setAttribute(ExifInterface.TAG_MAKER_NOTE, "89")
            exif.setAttribute(ExifInterface.TAG_MAX_APERTURE_VALUE, "90")
            exif.setAttribute(ExifInterface.TAG_METERING_MODE, "91")
            exif.setAttribute(ExifInterface.TAG_MODEL, "82")
            exif.setAttribute(ExifInterface.TAG_NEW_SUBFILE_TYPE, "83")
            exif.setAttribute(ExifInterface.TAG_OECF, "84")
            exif.setAttribute(ExifInterface.TAG_ORF_ASPECT_FRAME, "85")
            exif.setAttribute(ExifInterface.TAG_ORF_PREVIEW_IMAGE_LENGTH, "86")
            exif.setAttribute(ExifInterface.TAG_ORF_PREVIEW_IMAGE_START, "87")
            exif.setAttribute(ExifInterface.TAG_ORF_THUMBNAIL_IMAGE, "88")
            exif.setAttribute(ExifInterface.TAG_ORIENTATION, "89")
            exif.setAttribute(ExifInterface.TAG_PHOTOMETRIC_INTERPRETATION, "91")
            exif.setAttribute(ExifInterface.TAG_PIXEL_X_DIMENSION, "92")
            exif.setAttribute(ExifInterface.TAG_PIXEL_Y_DIMENSION, "93")
            exif.setAttribute(ExifInterface.TAG_PLANAR_CONFIGURATION, "94")
            exif.setAttribute(ExifInterface.TAG_PRIMARY_CHROMATICITIES, "95")
            exif.setAttribute(ExifInterface.TAG_REFERENCE_BLACK_WHITE, "97")
            exif.setAttribute(ExifInterface.TAG_RELATED_SOUND_FILE, "98")
            exif.setAttribute(ExifInterface.TAG_RESOLUTION_UNIT, "99")
            exif.setAttribute(ExifInterface.TAG_ROWS_PER_STRIP, "100")
            exif.setAttribute(ExifInterface.TAG_RW2_ISO, "101")
            exif.setAttribute(ExifInterface.TAG_RW2_JPG_FROM_RAW, "102")
            exif.setAttribute(ExifInterface.TAG_RW2_SENSOR_BOTTOM_BORDER, "103")
            exif.setAttribute(ExifInterface.TAG_RW2_SENSOR_LEFT_BORDER, "104")
            exif.setAttribute(ExifInterface.TAG_RW2_SENSOR_RIGHT_BORDER, "105")
            exif.setAttribute(ExifInterface.TAG_RW2_SENSOR_TOP_BORDER, "106")
            exif.setAttribute(ExifInterface.TAG_SAMPLES_PER_PIXEL, "107")
            exif.setAttribute(ExifInterface.TAG_SATURATION, "108")
            exif.setAttribute(ExifInterface.TAG_SCENE_CAPTURE_TYPE, "109")
            exif.setAttribute(ExifInterface.TAG_SCENE_TYPE, "110")
            exif.setAttribute(ExifInterface.TAG_SENSING_METHOD, "111")
            exif.setAttribute(ExifInterface.TAG_SHARPNESS, "113")
            exif.setAttribute(ExifInterface.TAG_SHUTTER_SPEED_VALUE, "114")
            exif.setAttribute(ExifInterface.TAG_SOFTWARE, "115")
            exif.setAttribute(ExifInterface.TAG_SPATIAL_FREQUENCY_RESPONSE, "116")
            exif.setAttribute(ExifInterface.TAG_SPECTRAL_SENSITIVITY, "117")
            exif.setAttribute(ExifInterface.TAG_STRIP_BYTE_COUNTS, "119")
            exif.setAttribute(ExifInterface.TAG_STRIP_OFFSETS, "120")
            exif.setAttribute(ExifInterface.TAG_SUBFILE_TYPE, "121")
            exif.setAttribute(ExifInterface.TAG_SUBJECT_AREA, "122")
            exif.setAttribute(ExifInterface.TAG_SUBJECT_DISTANCE, "123")
            exif.setAttribute(ExifInterface.TAG_SUBJECT_DISTANCE_RANGE, "124")
            exif.setAttribute(ExifInterface.TAG_SUBJECT_LOCATION, "125")
            exif.setAttribute(ExifInterface.TAG_SUBSEC_TIME, "126")
            exif.setAttribute(ExifInterface.TAG_SUBSEC_TIME_DIGITIZED, "127")
            exif.setAttribute(ExifInterface.TAG_SUBSEC_TIME_ORIGINAL, "128")
            exif.setAttribute(ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH, "129")
            exif.setAttribute(ExifInterface.TAG_THUMBNAIL_IMAGE_WIDTH, "130")
            exif.setAttribute(ExifInterface.TAG_TRANSFER_FUNCTION, "131")
            exif.setAttribute(ExifInterface.TAG_USER_COMMENT, "132")
            exif.setAttribute(ExifInterface.TAG_WHITE_BALANCE, "133")
            exif.setAttribute(ExifInterface.TAG_WHITE_POINT, "134")
            exif.setAttribute(ExifInterface.TAG_X_RESOLUTION, "135")
            exif.setAttribute(ExifInterface.TAG_Y_CB_CR_COEFFICIENTS, "136")
            exif.setAttribute(ExifInterface.TAG_Y_CB_CR_POSITIONING, "137")
            exif.setAttribute(ExifInterface.TAG_Y_CB_CR_SUB_SAMPLING, "138")
            exif.setAttribute(ExifInterface.TAG_Y_RESOLUTION, "139")
            exif.saveAttributes()
        } catch (e: IOException) {
            // handle the error
        }
    }


    //생명주기
    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        startLocationUpdates()  //위치 정보 업데이트 시작
        startBackgroundThread()
        if (binding.cameraView.isAvailable) {
            setupCamera()
            connectCamera()
        } else {
            binding.cameraView.surfaceTextureListener = mSurfaceTextureListener
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()   //위치 정보 업데이트 종료\
        if (binding.recordBt.tag != null){
            mediaRecorder?.stop()
        }
        closeCamera()
        stopBackgroundThread()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object{
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS "
        private const val REQUEST_CAMERA_PERMISSION_RESULT = 0
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }
}