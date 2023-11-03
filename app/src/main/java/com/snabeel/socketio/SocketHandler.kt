package com.snabeel.socketio

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException


object SocketHandler {

    lateinit var mSocket: Socket

    @Synchronized
    fun setSocket() {
        try {
    // "http://10.0.2.2:3000" is the network your Android emulator must use to join the localhost network on your computer

    //IO.socket("http://100.126.212.179:3000")
            mSocket = IO.socket("http://192.168.43.89:3000")
            Log.d("LLLLLL","success")
        } catch (e: URISyntaxException) {
            Log.d("LLLLLL","failure")
            e.printStackTrace()
        }
    }

    @Synchronized
    fun getSocket(): Socket {
        return mSocket
    }

    @Synchronized
    fun establishConnection() {
        mSocket.connect()
    }

    @Synchronized
    fun closeConnection() {
        mSocket.disconnect()
    }
}