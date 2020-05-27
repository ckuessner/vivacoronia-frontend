package de.tudarmstadt.iptk.foxtrot.vivacoronia.periodicLocationUpload

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class MyBroadcastReceiver : BroadcastReceiver() {
    private val TAG = "MyBroadcastReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        // register the alarm after the phone has booted
        if (intent?.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            // TODO
            Log.i(TAG, "Boot completed")
            if (context != null) {
                setupUploadAlarm(context)
                Log.i(TAG, "alarm registered")
            }
            else {
                Log.e(TAG, "couldnt register alarm because context was null")
            }
        }

        // TODO if upload wasnt possible due to no internet connection upload again if internet connection is finally available
    }
}