package dem.vaccae.mediaprojection

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.jeremyliao.liveeventbus.LiveEventBus
import dem.vaccae.mediaprojection.floatwindow.FloatWindowJobService

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //申请权限
        allPermissionsGranted()
        //请求上层悬浮框权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && !Settings.canDrawOverlays(this)
        ) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
            startActivity(intent);
            finish();
        }

        //监听事件
        LiveEventBus.get<Int>("MediaPron").observe(
            this,
            {
                if (it == 1) {
                    MediaPronUtil.getInstance().requestRecording(this)
                } else {
                    MediaPronUtil.getInstance().stopRecording()
                }
            }
        )

        //请求录屏
        MediaPronUtil.getInstance().requestRecording(this)

    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                //请求录屏
                MediaPronUtil.getInstance()
                    .requestRecording(this)
            } else {
                Toast.makeText(this, "未开启权限.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    //开启服务
    fun startjobservice() {
        FloatWindowJobService.setJOBID(1)
        FloatWindowJobService.setInterValTime(1000)
        FloatWindowJobService.setTag("floatjob")
        FloatWindowJobService.startScheduler(this)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i("video", "Activity:$requestCode $resultCode")
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == MediaPronUtil.RECORD_REQUEST_CODE) {
                if (resultCode == RESULT_OK) {
                    //开启悬浮框
                    startjobservice()
                    //开始录制
                    MediaPronUtil.getInstance()
                        .startRecording(data)

                } else {
                    Toast.makeText(
                        this, "用戶拒绝录制屏幕", Toast.LENGTH_SHORT
                    ).show();
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                this, e.message.toString(), Toast.LENGTH_SHORT
            ).show();
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //停止录制
        MediaPronUtil.getInstance().stopRecording()
    }

}