package de.tudarmstadt.iptk.foxtrot.vivacoronia.periodicLocationUpload

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import de.tudarmstadt.iptk.foxtrot.vivacoronia.clients.LocationApiClient
import de.tudarmstadt.iptk.foxtrot.vivacoronia.dataStorage.AppDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UploadService : Service() {
    private val tag = "UploadService"

    override fun onCreate() {
        super.onCreate()
        Log.i(tag, "entered onCreate")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(tag, "started upload Service")

        // TODO call LocationUploadCommunicator and check for network connection
        val db = AppDatabase.getDatabase(applicationContext)
        // do uploading unblocking
        GlobalScope.launch {
            val locList = db.coronaDao().getLocations()
            Log.i(tag, "locList: $locList")
            Log.i(tag, "locList Length: " + locList.size.toString())

            // if location array is empty, no upload is needed
            if (locList.isNotEmpty()) {
                // uploads the data and deletes it if upload was successful
                Log.i(tag, "upload")
                LocationApiClient.sendPositionsToServer(applicationContext, locList)
            } else {
                Log.i(tag, "no upload")
            }

        }

        // stop service after uploading
        stopSelf()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.i(tag, "upload service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}