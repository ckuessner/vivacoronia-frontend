package de.tudarmstadt.iptk.foxtrot.vivacoronia.periodicLocationUpload

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants

fun setupUploadAlarm(context: Context){
    Log.i("setupUploadAlarm", "entered method setupUploadAlarm")
    // https://developer.android.com/training/scheduling/alarms
    // get alarmManager
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    // setup the upload intent
    val uploadIntent = Intent(context, UploadService::class.java)
    val upload = PendingIntent.getService(context, Constants.LOCATION_UPLOAD_REQUEST_CODE, uploadIntent, PendingIntent.FLAG_UPDATE_CURRENT)

    // set the upload interval
    if (upload != null){

        alarmManager?.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime()+Constants.LOCATION_UPLOAD_INTERVAL, Constants.LOCATION_UPLOAD_INTERVAL, upload)
        Log.i("setupUploadAlarm", "upload service registerd")
    }
    else {
        Log.e("setupUploadAlarm", "upload service not registered")
    }
}