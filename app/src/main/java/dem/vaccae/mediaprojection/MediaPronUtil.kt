package dem.vaccae.mediaprojection

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.util.Log
import android.view.Surface
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间： 21:52
 * 功能模块说明：
 */
class MediaPronUtil {
    companion object {
        val RECORD_REQUEST_CODE = 999;

        private var mMediaPronUtil: MediaPronUtil? = null

        fun getInstance(): MediaPronUtil {
            mMediaPronUtil ?: run {
                synchronized(MediaPronUtil::class.java) {
                    mMediaPronUtil = MediaPronUtil()
                }
            }
            return mMediaPronUtil!!
        }
    }

    private var mActivity: Activity? = null
    private lateinit var mediaProMng: MediaProjectionManager
    private var mVirtualDisplay: VirtualDisplay? = null
    private var mMediaPron: MediaProjection? = null
    private var mSurface: Surface? = null
    private var mMediaCodec: MediaCodec? = null
    private var mMuxer: MediaMuxer? = null
    private var mVideoTrackIndex = -1;

    //是否保存录制文件
    private var isSaveFile = true

    //是否开始录制
    private var isRecord = false
    private var frameSPSFPS: ByteArray = ByteArray(0)

    private var mBufferInfo: MediaCodec.BufferInfo = MediaCodec.BufferInfo()


    /**
     * 请求录屏
     */
    fun requestRecording(activity: Activity) {
        mActivity = activity;
        mActivity?.let {
            mediaProMng =
                it.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

            var captureIntent: Intent? = null
            if (mediaProMng != null) {
                captureIntent = mediaProMng.createScreenCaptureIntent()
            }
            it.startActivityForResult(captureIntent, RECORD_REQUEST_CODE)
        }
    }

    /**
     * 开始录屏
     */
    fun startRecording(data: Intent?, issavefile: Boolean = true) {
        isSaveFile = issavefile
        data?.let {
            mMediaPron = mediaProMng.getMediaProjection(RESULT_OK, it);
            setconfigMedia()
        }
    }


    /**
     * 关闭录屏
     */
    fun stopRecording() {
        release()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentTime(): String {
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val formatted = current.format(formatter)
        return formatted.toString()
    }


    private fun setconfigMedia() {
        mActivity?.let {
            val resScope = CoroutineScope(Job())
            resScope.launch {
                try {
                    //隐藏本Activity
                    it.moveTaskToBack(true)
                    //获取windowManager
                    val windowManager =
                        it.getSystemService(AppCompatActivity.WINDOW_SERVICE) as WindowManager
                    //获取屏幕对象
                    val defaultDisplay = windowManager.defaultDisplay
                    //获取屏幕的宽、高，单位是像素
                    val width = defaultDisplay.width
                    val height = defaultDisplay.height

                    //录屏存放目录
                    val fname = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        getCurrentTime() + ".mp4"
                    } else {
                        "screen.mp4"
                    }
                    val filename = it.externalMediaDirs[0].absolutePath + "/" + fname
                    Log.i("video", filename)
                    if (isSaveFile) {
                        mMuxer = MediaMuxer(filename, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
                    } else {
                        mMuxer = null
                    }


                    mMediaCodec = getVideoMediaCodec(width, height)
                    mMediaCodec?.let { mit ->
                        mSurface = mit.createInputSurface()
                        /**
                         * 创建投影
                         * name 本次虚拟显示的名称
                         * width 录制后视频的宽
                         * height 录制后视频的高
                         * dpi 显示屏像素
                         * flags VIRTUAL_DISPLAY_FLAG_PUBLIC 通用显示屏
                         * Surface 输出的Surface
                         */
                        mVirtualDisplay = mMediaPron?.createVirtualDisplay(
                            "ScreenRecord", width, height, 1,
                            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mSurface, null, null
                        )

                        isRecord = true;
                        mit.start();

                        recordVirtualDisplay()
                    }
                } catch (e: Exception) {
                    Log.e("video", e.message.toString())
                }
            }
        }
    }

    private fun getVideoMediaCodec(width: Int, height: Int): MediaCodec? {
        val format = MediaFormat.createVideoFormat("video/avc", width, height)
        //设置颜色格式
        format.setInteger(
            MediaFormat.KEY_COLOR_FORMAT,
            MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface
        )
        //设置比特率(设置码率，通常码率越高，视频越清晰)
        format.setInteger(MediaFormat.KEY_BIT_RATE, 1000 * 1024)
        //设置帧率
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 20)
        //关键帧间隔时间，通常情况下，你设置成多少问题都不大。
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        // 当画面静止时,重复最后一帧，不影响界面显示
        format.setLong(MediaFormat.KEY_REPEAT_PREVIOUS_FRAME_AFTER, (1000000 / 45).toLong())
        format.setInteger(
            MediaFormat.KEY_BITRATE_MODE,
            MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_VBR
        )
        //设置复用模式
        format.setInteger(
            MediaFormat.KEY_COMPLEXITY,
            MediaCodecInfo.EncoderCapabilities.BITRATE_MODE_CBR
        )
        var mediaCodec: MediaCodec? = null
        try {
//            MediaRecorder mediaRecorder = new MediaRecorder();
            mediaCodec = MediaCodec.createEncoderByType("video/avc")
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        } catch (e: Exception) {
            e.printStackTrace()
            if (mediaCodec != null) {
                mediaCodec.reset()
                mediaCodec.stop()
                mediaCodec.release()
                mediaCodec = null
            }
        }
        return mediaCodec
    }


    private fun recordVirtualDisplay() {
        while (isRecord) {
            val index = mMediaCodec!!.dequeueOutputBuffer(mBufferInfo, 10000)
            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) { //后续输出格式变化
                resetOutputFormat()
            } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) { //请求超时
                try {
                    // wait 10ms
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                }
            } else if (index >= 0) { //有效输出
                encodeToVideoTrack(index)
                mMediaCodec!!.releaseOutputBuffer(index, false)
            }
        }
    }

    private fun resetOutputFormat() {
        Log.i("video", "Reqoutputformat")
        val newFormat: MediaFormat = mMediaCodec!!.getOutputFormat()
        mMuxer?.let {
            mVideoTrackIndex = it.addTrack(newFormat)
            it.start()
        }
    }

    private fun encodeToVideoTrack(index: Int) {
        try {
            var encodedData = mMediaCodec!!.getOutputBuffer(index)
            //是编码需要的特定数据，不是媒体数据
            if (mBufferInfo.flags and MediaCodec.BUFFER_FLAG_CODEC_CONFIG != 0) {
                mBufferInfo.size = 0
            }
            if (mBufferInfo.size == 0) {
                Log.d("video", "info.size == 0, drop it.")
                encodedData = null
            } else {
                Log.d(
                    "video", "got buffer, info: size=" + mBufferInfo.size
                            + ", presentationTimeUs=" + mBufferInfo.presentationTimeUs
                            + ", offset=" + mBufferInfo.offset
                )
            }
            if (encodedData != null) {

                Log.d("video", "outdata size:" + mBufferInfo.size)
                encodedData.position(mBufferInfo.offset)
                encodedData.limit(mBufferInfo.offset + mBufferInfo.size)

                mMuxer?.let {
                    it.writeSampleData(mVideoTrackIndex, encodedData, mBufferInfo);
                }
                val outData: ByteArray = ByteArray(mBufferInfo.size)
                encodedData.get(outData);
    //
    //            var h264RawFrame: ByteArray? = null
    //            if (mBufferInfo.flags == MediaCodec.BUFFER_FLAG_KEY_FRAME) {
    //                //h264RawFrame 每一帧的视频数据
    //                h264RawFrame = ByteArray(frameSPSFPS.size + outData.size);
    //                System.arraycopy(frameSPSFPS, 0, h264RawFrame, 0, frameSPSFPS.size);
    //                System.arraycopy(outData, 0, h264RawFrame, frameSPSFPS.size, outData.size);
    //            } else {
    //                h264RawFrame = outData;
    //            }
            }
        } catch (e: Exception) {
            Log.e("video", e.message.toString())
        }
    }


    private fun release() {
        mMuxer?.let {
            if (isRecord) {
                it.stop()
                it.release()
            }
        }
        mMediaCodec?.let {
            if (isRecord) {
                try {
                    it.stop()
                    it.release()
                } catch (e: Exception) {
                    mMediaCodec = null;
                    mMediaCodec = MediaCodec.createByCodecName("")
                    mMediaCodec?.stop();
                    mMediaCodec?.release();
                }
            }
            null
        }
        mVirtualDisplay?.let {
            if (isRecord) {
                it.release()
                null
            }
        }
        isRecord = false
    }
}
