package dem.vaccae.mediaprojection.floatwindow

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import java.lang.Exception

/**
 * 作者：Vaccae
 * 邮箱：3657447@qq.com
 * 创建时间： 00:32
 * 功能模块说明：
 */
class FloatWindowJobService : JobService() {


    override fun onStartJob(p0: JobParameters?): Boolean {
        Log.i("job", "StartJob")
        val resScope = CoroutineScope(Job())
        resScope.launch {
            try {
                Log.i("job", "StartJob1")
                // 当前界面没有悬浮窗显示，则创建悬浮窗。
                if (!MyWindowManager.isWindowShowing()) {
                    Log.i("job", "StartJob2")
                    withContext(Dispatchers.Main) {
                        MyWindowManager.createSmallWindow(mContext)
                    }
                }
                startScheduler(mContext)
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, e.message.toString())
            }
        }
        return false
    }


    override fun onStopJob(p0: JobParameters?): Boolean = false

    companion object {
        private lateinit var mContext:Context
        private var TAG: String = "floatjob"
        private var JOBID: Int = 999
        private var InterValTime: Long = 1000
        private var jobScheduler: JobScheduler? = null
        private var jobInfo: JobInfo? = null

        fun setJOBID(id: Int) {
            JOBID = id
        }

        fun setInterValTime(time: Long) {
            InterValTime = time
        }

        fun setTag(tag: String) {
            TAG = tag
        }


        fun startScheduler(context: Context) {
            mContext = context
            jobScheduler = context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
            cancelScheduler()
            if (jobInfo == null) {
                jobInfo =
                    JobInfo.Builder(
                        JOBID,
                        ComponentName(context, FloatWindowJobService::class.java)
                    )
                        .setMinimumLatency(InterValTime)
                        .build()
            }
            val result = jobScheduler?.schedule(jobInfo!!)
        }

        fun cancelScheduler() {
            //jobScheduler?.cancelAll()
            jobScheduler?.cancel(JOBID)
        }
    }
}