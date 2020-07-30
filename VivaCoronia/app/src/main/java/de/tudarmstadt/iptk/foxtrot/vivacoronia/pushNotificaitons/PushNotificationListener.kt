package de.tudarmstadt.iptk.foxtrot.vivacoronia.pushNotificaitons

import android.util.Log
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class PushNotificationListener : WebSocketListener(){
    private val TAG = "PushListener"

    //gets set in the init method of websocketservice
    lateinit var socketService : WebSocketService

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        Log.i(TAG, "onOpen: " + response)
        webSocket.send(Constants.USER_ID.toString())
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        // tell service to make a notification
        if(text == "you had contact with an infected person") {
            socketService.makeNotification()
        }
        Log.i(TAG, "received " + text)
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        socketService.reconnect()
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e(TAG, "websocket failure: " + response.toString())
        Log.e(TAG, "websocket failure " + t.cause.toString())
        socketService.reconnect()
    }

}
