package de.tudarmstadt.iptk.foxtrot.vivacoronia.pushNotificaitons

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.LocationApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.AppDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class WebsocketService : Service() {
    private val TAG = "WebSocketService"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "started upload Service")



        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}
