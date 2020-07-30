package de.tudarmstadt.iptk.foxtrot.vivacoronia.pushNotificaitons

import android.app.Service
import android.content.Intent
import android.os.IBinder

class WebSocketReconnectService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val websocketIntent = Intent(this, WebSocketService::class.java)
        startService(websocketIntent)

        stopSelf()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }
}
