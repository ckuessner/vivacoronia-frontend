package de.tudarmstadt.iptk.foxtrot.vivacoronia.pushNotificaitons

import android.util.Log
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class PushNotificationListener : WebSocketListener(){
    private val TAG = "PushListener"

    lateinit var socketService : WebSocketService

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        Log.i(TAG, "onOpen: " + response)
        webSocket.send(Constants.USER_ID.toString())
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        // tell service to make a notification
        if(text == "you are infected") {
            socketService.makeNotification()
        }
        Log.i(TAG, "received " + text)
        this.onClosing(webSocket, 100, "fun")
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosing(webSocket, code, reason)
        Log.i(TAG, "closing connection")
    }

    // TODO maybe reconnect if server is down
    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        super.onFailure(webSocket, t, response)
        Log.e(TAG, "websocket failure: " + response.toString())
        Log.e(TAG, "websocket failure " + t.cause.toString())
    }

}
