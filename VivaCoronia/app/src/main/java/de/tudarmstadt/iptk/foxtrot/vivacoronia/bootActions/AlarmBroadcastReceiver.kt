package de.tudarmstadt.iptk.foxtrot.vivacoronia.bootActions

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import de.tudarmstadt.iptk.foxtrot.vivacoronia.locationTracking.checkPermissionsAndStartTracking
import de.tudarmstadt.iptk.foxtrot.vivacoronia.periodicLocationUpload.setupUploadAlarm
import de.tudarmstadt.iptk.foxtrot.vivacoronia.pushNotificaitons.WebSocketService

class AlarmBroadcastReceiver : BroadcastReceiver() {
    private val tag = "MyBroadcastReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.i(tag, "Broadcast received")
        // register the alarm after the phone has booted
        if (intent?.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            // TODO
            Log.i(tag, "Boot completed")
            if (context != null) {
                setupUploadAlarm(context)
                WebSocketService.tryStartWebSocketService(context)
                checkPermissionsAndStartTracking(context, false)
                Log.i(tag, "alarm registered")
            }
            else {
                Log.e(tag, "couldnt register alarm because context was null")
            }
        }
    }
}