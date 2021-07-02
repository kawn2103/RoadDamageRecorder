package kst.app.roaddamagerecorder

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kst.app.roaddamagerecorder.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.versionTv.text = BuildConfig.VERSION_NAME

        TedPermission.with(this)
            .setPermissionListener(permissionListener)
            .setDeniedMessage("권한을 허용하지 않으면 앱의 기능을 사용 하실 수 없습니다.\n\n[설정] > [권한] 페이지에서 권한을 수락해 주세요")
            .setPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.RECORD_AUDIO
            )
            .check()
    }

    private var permissionListener: PermissionListener = object : PermissionListener {
        override fun onPermissionGranted() {
            //모든 권한이 정상적으로 요청 성공 했을 때
            Log.d("gwan_2103","테드 퍼미션 권한 획득 완료")
            var value = 0
            val add = 1

            Thread(Runnable {
                while (true) {
                    value += add    //프로그래스바 value 변경
                    runOnUiThread {
                        binding.introProgress.setProgress(value)
                    }
                    try {
                        Thread.sleep(10)    //시간지연
                    } catch (e: InterruptedException) {

                    }
                    if (value == 100) {
                        startActivity(Intent(this@IntroActivity, LoginActivity::class.java))
                        finish()
                        break
                    }
                }
            }).start()
        }
        override fun onPermissionDenied(deniedPermissions: List<String>) {
            //권한이 정상적으로 요청 실패 했을 때
            Log.d("gwan_2103","테드 퍼미션 권한 획득 실패")
        }
    }
}