package com.example.internet_speed_testing

import android.app.Activity
import android.os.AsyncTask
import android.util.Log
import fr.bmartel.speedtest.SpeedTestReport
import fr.bmartel.speedtest.SpeedTestSocket
import fr.bmartel.speedtest.inter.ISpeedTestListener
import fr.bmartel.speedtest.model.SpeedTestError
import java.math.BigDecimal

class InternetSpeedBuilder(var activity: Activity) {

    private var countTestSpeed = 0
    private var LIMIT = 3
    lateinit var url: String
    lateinit var javaListener: OnEventInternetSpeedListener
    lateinit var onDownloadProgressListener: ()->Unit
    lateinit var onUploadProgressListener: ()->Unit
    lateinit var onTotalProgressListener: ()->Unit
    lateinit var downloadTestSocket: SpeedTestSocket
    lateinit var uploadTestSocket: SpeedTestSocket
    private lateinit var progressModel: ProgressionModel

    fun start(url: String, limitCount: Int) {
        this.url = url
        this.LIMIT = limitCount
        startTestDownload()
    }

    // TODO[Restart properly]: Find a proper way to stop both socket
    fun stop() {
        this.LIMIT = 0
        downloadTestSocket.forceStopTask()
        uploadTestSocket.forceStopTask()
    }

    fun setOnEventInternetSpeedListener(javaListener: OnEventInternetSpeedListener) {
        this.javaListener = javaListener
    }

    fun setOnEventInternetSpeedListener(onDownloadProgress: ()->Unit, onUploadProgress: ()->Unit, onTotalProgress: ()->Unit) {
        this.onDownloadProgressListener = onDownloadProgress
        this.onUploadProgressListener = onUploadProgress
        this.onTotalProgressListener = onTotalProgress
    }


    private fun startTestDownload() {
        progressModel = ProgressionModel()
        SpeedDownloadTestTask().execute()
    }

    private fun startTestUpload() {
        SpeedUploadTestTask().execute()

    }

    interface OnEventInternetSpeedListener {
        fun onDownloadProgress(count: Int, progressModel: ProgressionModel)
        fun onUploadProgress(count: Int, progressModel: ProgressionModel)
        fun onTotalProgress(count: Int, progressModel: ProgressionModel)
    }

    inner class SpeedDownloadTestTask : AsyncTask<Void, Void, String>() {

        override fun onPreExecute() {
            super.onPreExecute()
        }

        override fun doInBackground(vararg params: Void): String? {

            downloadTestSocket = SpeedTestSocket()

            // add a listener to wait for speedtest completion and progress
            downloadTestSocket.addSpeedTestListener(object : ISpeedTestListener {

                override fun onCompletion(report: SpeedTestReport) {
                    // called when download/upload is finished
                    Log.v("speedtest Download" + countTestSpeed, "[COMPLETED] rate in octet/s : " + report.transferRateOctet)
                    Log.v("speedtest Download" + countTestSpeed, "[COMPLETED] rate in bit/s   : " + report.transferRateBit)

                    /*downloadProgressModel.progressTotal = 50f
                    downloadProgressModel.progressDownload = 100f
                    downloadProgressModel.downloadSpeed = report.transferRateBit

                    totalProgressModel.progressTotal = 50f
                    totalProgressModel.progressDownload = 100f
                    totalProgressModel.downloadSpeed = report.transferRateBit

                    activity.runOnUiThread {
                        javaListener.onDownloadProgress(countTestSpeed, downloadProgressModel)
                        javaListener.onTotalProgress(countTestSpeed, totalProgressModel)

                    }*/

                    startTestUpload()

                }

                override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
                    // called when a download/upload error occur
                }

                override fun onProgress(percent: Float, report: SpeedTestReport) {
                    // called to notify download/upload progress
                    Log.v("speedtest Download" + countTestSpeed, "[PROGRESS] progress : $percent%")
                    Log.v("speedtest Download" + countTestSpeed, "[PROGRESS] rate in octet/s : " + report.transferRateOctet)
                    Log.v("speedtest Download" + countTestSpeed, "[PROGRESS] rate in bit/s   : " + report.transferRateBit)

                    progressModel.progressTotal = percent / 2
                    progressModel.progressDownload = percent
                    progressModel.downloadSpeed = report.transferRateBit

                    activity.runOnUiThread {
                        javaListener.onDownloadProgress(countTestSpeed, progressModel)
                        javaListener.onTotalProgress(countTestSpeed, progressModel)
                    }

                }
            })

            downloadTestSocket.startDownload(url)

            return null
        }
    }

    inner class SpeedUploadTestTask : AsyncTask<Void, Void, Void>() {

        override fun doInBackground(vararg params: Void): Void? {

            uploadTestSocket = SpeedTestSocket()

            // add a listener to wait for speedtest completion and progress
            uploadTestSocket.addSpeedTestListener(object : ISpeedTestListener {

                override fun onCompletion(report: SpeedTestReport) {
                    // called when download/upload is finished
                    Log.v("speedtest Upload" + countTestSpeed, "[COMPLETED] rate in octet/s : " + report.transferRateOctet)
                    Log.v("speedtest Upload" + countTestSpeed, "[COMPLETED] rate in bit/s   : " + report.transferRateBit)

                    /*progressModel.progressTotal = 100f
                    progressModel.progressUpload= 100f
                    progressModel.uploadSpeed = report.transferRateBit

                    activity.runOnUiThread {
                        javaListener.onUploadProgress(countTestSpeed, progressModel)
                        javaListener.onTotalProgress(countTestSpeed, progressModel)
                    }*/


                    countTestSpeed++
                    if (countTestSpeed < LIMIT) {
                        startTestDownload()
                    }
                }

                override fun onError(speedTestError: SpeedTestError, errorMessage: String) {
                    // called when a download/upload error occur
                }

                override fun onProgress(percent: Float, report: SpeedTestReport) {
                    // called to notify download/upload progress
                    Log.v("speedtest Upload" + countTestSpeed, "[PROGRESS] progress : $percent%")
                    Log.v("speedtest Upload" + countTestSpeed, "[PROGRESS] rate in octet/s : " + report.transferRateOctet)
                    Log.v("speedtest Upload" + countTestSpeed, "[PROGRESS] rate in bit/s   : " + report.transferRateBit)

                    progressModel.progressTotal = percent / 2 + 50
                    progressModel.progressUpload = percent
                    progressModel.uploadSpeed= report.transferRateBit

                    activity.runOnUiThread {

                        if (countTestSpeed < LIMIT) {
                            javaListener.onUploadProgress(countTestSpeed, progressModel)
                            javaListener.onTotalProgress(countTestSpeed, progressModel)

                        }
                    }

                }
            })

            uploadTestSocket.startDownload(url)

            return null
        }
    }

}