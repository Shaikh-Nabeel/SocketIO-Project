package com.snabeel.socketio

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import io.socket.client.Socket


class MainActivity : AppCompatActivity() {

    lateinit var record: AudioRecord
    lateinit var mSocket: Socket
    var status = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val countBtn = findViewById<Button>(R.id.countBtn)
        val text = findViewById<TextView>(R.id.textView)
        val mssgAct = findViewById<Button>(R.id.messageActivity)
        val recordBtn = findViewById<Button>(R.id.recordAudio)
        val stopBtn = findViewById<Button>(R.id.stopRecording)

        SocketHandler.setSocket()
        SocketHandler.establishConnection()

        mSocket = SocketHandler.getSocket()

        countBtn.setOnClickListener {
            mSocket.emit("counter")
        }

        mSocket.on("counter"){ args ->
            if(args[0] != null){
                val counter = args[0] as Int
                runOnUiThread {
                    text.text = counter.toString()
                }
            }
        }

        mssgAct.setOnClickListener {
            startActivity(Intent(this, ChatBox::class.java))
        }

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
            && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO,Manifest.permission.WRITE_EXTERNAL_STORAGE),
                111
            )
        }

        recordBtn.setOnClickListener {
            startStreaming()
        }

        stopBtn.setOnClickListener {
            status = false
            record.stop()
            record.release()
        }

        mSocket.on("audio"){args ->
            if(args != null){
                var bytes = args[0] as ByteArray
            }
        }
//        record =  AudioRecord(MediaRecorder.AudioSource.MIC,16000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT,AudioRecord.getMinBufferSize(16000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT))

    }

    private val sampleRate = 16000 // 44100 for music
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    var minBufSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    @SuppressLint("MissingPermission")
    fun startStreaming() {
        val streamThread = Thread {
            try {

                val buffer = ByteArray(minBufSize)
                Log.d("VS", "Buffer created of size $minBufSize")
                Log.d("VS", "Address retrieved")

                record = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    minBufSize * 10
                )
                Log.d("VS", "Recorder initialized")
                record.startRecording()
                while (status) {
                    //reading data from MIC into buffer
                    minBufSize = record.read(buffer, 0, buffer.size)

                    mSocket.emit("audio",buffer)

                    //putting buffer in the packet

                    println("MinBufferSize: $minBufSize")
                }
            } catch (e: Exception) {
                Log.e("VS", "UnknownHostException")
                e.printStackTrace()
            }
        }
        streamThread.start()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 111){
            if(ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                startStreaming()
            }
        }
    }



    fun recordAudio(fileName: String) {
        val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        } else {
            MediaRecorder()
        }
        val values = ContentValues(3)
        values.put(MediaStore.MediaColumns.TITLE, fileName)
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
        recorder.setOutputFile(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).let {
            "$it/$fileName"
        })

        try {
            recorder.prepare()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val mProgressDialog = ProgressDialog(this)
        mProgressDialog.setTitle("Recording")
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        mProgressDialog.setButton(
            DialogInterface.BUTTON_POSITIVE,
            "Stop recording"
        ) { dialog, whichButton ->
            mProgressDialog.dismiss()
            recorder.stop()
            recorder.release()
        }
        mProgressDialog.setOnCancelListener {
            recorder.stop()
            recorder.release()
        }
        recorder.start()
        mProgressDialog.show()
    }
}