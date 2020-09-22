package de.tudarmstadt.iptk.foxtrot.vivacoronia.pushNotificaitons

import android.util.Log
import com.beust.klaxon.*
import com.google.android.gms.maps.model.LatLng
import de.tudarmstadt.iptk.foxtrot.vivacoronia.Constants
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.ProductSearchQuery
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

class PushNotificationListener : WebSocketListener(){
    private val tag = "PushListener"

    private val productSearchConverter : Klaxon = Klaxon()

    //gets set in the init method of websocketservice
    lateinit var socketService : WebSocketService

    var webSocket: WebSocket? = null

    init {
        productSearchConverter.converter(ProductSearchConverter)
    }

    override fun onOpen(webSocket: WebSocket, response: Response) {
        super.onOpen(webSocket, response)
        Log.i(tag, "onOpen: $response")
        this.webSocket = webSocket
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.i(tag, "received $text")
        // tell service to make a notification
        if(text == "you had contact with an infected person") {
            socketService.makeContactNotification()
            return
        }

        if (text.startsWith("Congratulations!!! You achieved")) {
            socketService.makeAchievementNotification(text)
            return
        }

        if (text.startsWith("QUIZ")) {
            val splitted = text.split('|')
            socketService.makeQuizNotification(splitted[0], JSONObject(splitted[1]))
            return
        }
        // we only get contact or product notifications so if we are here it can only be a product notification
        try {
            val obj = productSearchConverter.parse<ProductSearchQuery>(text)
            if (obj != null ) socketService.makeProductMatchNotification(obj)
        } catch (e: KlaxonException) {
            Log.e(tag, "Couldnt parse ProductSearchQuery from notification message or making a notification from it", e)
        }
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.i(tag, "close listener")
        socketService.reconnect()
    }

    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        webSocket.close(code, reason)
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e(tag, "websocket failure " + t.cause.toString())
        Log.i(tag, socketService.toString())
        socketService.reconnect()
    }


}

object ProductSearchConverter : Converter {
    override fun canConvert(cls: Class<*>) = cls == ProductSearchQuery::class.java

    override fun fromJson(jv: JsonValue): Any? {
        if (!(jv.obj != null
            && jv.obj!!["location"] is JsonArray<*>
            && jv.obj!!["product"] is String
            && jv.obj!!["productCategory"] is String
            && jv.obj!!["minAmount"] is Int
            && jv.obj!!["perimeter"] is Int))
            throw KlaxonException("Couldn't parse product query: $jv")
        val locationJson = jv.obj!!["location"] as JsonArray<*>
        var latlng : LatLng? = null
        if ((locationJson[1] as Number).toDouble() != 0.0 && (locationJson[0] as Number).toDouble() != 0.0) {
            latlng = LatLng(
                (locationJson[1] as Number).toDouble(),
                (locationJson[0] as Number).toDouble()
            )
        }
        return ProductSearchQuery(
            jv.obj!!["product"] as String,
            jv.obj!!["productCategory"] as String,
            jv.obj!!["minAmount"].toString(),
            latlng,
            // kotlin round function lets websocket fail
            if (latlng != null) Math.round(jv.obj!!["perimeter"] as Int / 1000f) else 0 // convert from m to km
        )
    }

    override fun toJson(value: Any): String {
        throw KlaxonException("Creating JSON is not supported!")
    }
}
