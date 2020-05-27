package de.tudarmstadt.iptk.foxtrot.vivacoronia.periodicLocationUpload

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
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
        var db = AppDatabase.getDatabase(applicationContext)
        GlobalScope.launch {
            var locList = db.coronaDao().getLocations()
            Log.i(TAG, "locList: " + locList.toString())


            // TODO wait with that until after the upload to verify everything worked and no data got lost
            // TODO if upload doesnt open a new thread or does it in a coroutine do it here
            db.coronaDao().deleteLocations()    // since the data gets uploaded we can delete them
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