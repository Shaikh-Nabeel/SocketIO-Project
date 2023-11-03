package com.snabeel.socketio

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class ChatBox : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_box)

        val sendBtn = findViewById<Button>(R.id.sendMssg)
        val messageET = findViewById<EditText>(R.id.messageEt)
        val textView = findViewById<TextView>(R.id.chatTV)
        val nameET = findViewById<EditText>(R.id.name)


        val mSocket = SocketHandler.getSocket()

        sendBtn.setOnClickListener {
            val message = messageET.text.toString()
            val name = nameET.text.toString()
            if(message.isNotEmpty()){
                mSocket.emit("message", "$name : $message")
            }
        }

        mSocket.on("message"){
            if(it[0] != null){
                val message = it[0] as String
                runOnUiThread {
                    val prev = textView.text
                    textView.text = "$prev \n\n$message"
                }
            }
        }

    }
}