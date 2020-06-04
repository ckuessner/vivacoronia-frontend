package de.tudarmstadt.iptk.foxtrot.vivacoronia.periodicLocationUpload

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.DataStorage.AppDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UploadService : Service() {
    private val TAG = "UploadService"

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "entered onCreate")

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "started upload Service")

        // TODO call LocationUploadCommunicator and check for network connection
        val db = AppDatabase.getDatabase(applicationContext)
        // do uploading unblocking
        GlobalScope.launch {
            var locList = db.coronaDao().getLocations()
            Log.i(TAG, "locList: " + locList.toString())

            LocationServerCommunicator.sendPositionsToServer(applicationContext, Constants().USER_ID, locList)

            // TODO delete db.coronaDao().deleteLocations()    // since the data got uploaded we can delete it
        }

        // stop service after uploading
        stopSelf()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        Log.i(TAG, "upload service destroyed")
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

}