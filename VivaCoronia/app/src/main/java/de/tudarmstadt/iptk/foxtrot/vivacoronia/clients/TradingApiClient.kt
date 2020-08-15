package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.content.Context
import android.net.Uri
import com.android.volley.*
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.RequestFuture
import com.beust.klaxon.*
import com.google.android.gms.maps.model.LatLng
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer
import org.json.JSONArray
import org.json.JSONObject
import org.threeten.bp.OffsetDateTime

object TradingApiClient : ApiBaseClient() {
    private val offerConverter : Klaxon = Klaxon()

    init {
        offerConverter.converter(LatLngConverter)
    }

    private fun getEndpoint(): String{
        return "${getBaseUrl()}/trading/"
    }

    private fun getOffersEndpoint(): String {
        return joinPaths(getEndpoint(), "offers")
    }

    fun getAllCategories(context: Context): List<String> {
        val queue = getRequestQueue(context) ?: throw VolleyError("Unable to get request queue!")
        val url = joinPaths(getEndpoint(), "categories")
        val future = RequestFuture.newFuture<JSONArray>()
        val request = JsonArrayJWT(Request.Method.GET, url, null, future, future, context)
        queue.add(request)

        val result = Klaxon().parseArray<String>(future.get().toString())
        if (result?.isNotEmpty() == true)
            return result

        throw VolleyError("Unable to get any categories!")
    }

    fun getMyOffers(context: Context): MutableList<Offer> {
        val queue = getRequestQueue(context) ?: throw VolleyError("Unable to get request queue!")
        val url = Uri.parse(getOffersEndpoint())
            .buildUpon()
            .appendQueryParameter("userId", getUserId(context).toString())
            .build()
            .toString()

        val future = RequestFuture.newFuture<JSONArray>()
        val request = JsonArrayJWT(Request.Method.GET, url, null, future, future, context)
        queue.add(request)
        val futureResult = future.get().toString()

        val result = offerConverter.parseArray<Offer>(futureResult)
        return result?.toMutableList() ?: mutableListOf()
    }

    fun deleteOffer(id: String, sold: Boolean, context: Context): Boolean {
        val queue = getRequestQueue(context) ?: return false
        val url = joinPaths(getOffersEndpoint(), id)
        val deactivatedAt = OffsetDateTime.now()
        val body = JSONObject()
        body.put("sold", sold)
        body.put("deactivatedAt", deactivatedAt)
        val future = RequestFuture.newFuture<JSONObject>()
        val request = JsonObjectJWT(Request.Method.PATCH, url, body, future, future, context)

        queue.add(request)
        val result = future.get()
        return result.has("deactivatedAt")
                && OffsetDateTime.parse(result["deactivatedAt"].toString()).isEqual(deactivatedAt)
    }

    fun putOffer(offer: Offer, context: Context): Offer? {
        val jsonString = offerConverter.toJsonString(offer)
        val jsonObject = JSONObject(jsonString)
        jsonObject.put("userId", getUserId(context)) // TODO should be done/verified @ server
        val url = joinPaths(getOffersEndpoint(), offer.id)
        val queue = getRequestQueue(context) ?: throw VolleyError("Unable to get request queue!")
        val future = RequestFuture.newFuture<JSONObject>()

        val method = if (offer.id.isEmpty()) Request.Method.POST else Request.Method.PATCH // if id is not set, this is a new offer and should be posted
        val request = JsonObjectJWT(method, url, jsonObject, future, future, context)
        queue.add(request)
        val result = future.get()
        return offerConverter.parse(result.toString())
    }
}

object LatLngConverter : Converter {
    override fun canConvert(cls: Class<*>) = cls == LatLng::class.java

    override fun fromJson(jv: JsonValue): Any? {
        if (jv.obj == null || jv.obj!!["coordinates"] !is JsonArray<*>)
            throw KlaxonException("Couldn't parse location: $jv")
        val locationJson = jv.obj!!["coordinates"] as JsonArray<*>
        return LatLng(
            (locationJson[1] as Number).toDouble(),
            (locationJson[0] as Number).toDouble()
        )
    }

    override fun toJson(value: Any): String {
        if (value !is LatLng)
            throw KlaxonException("Cannot convert class ${value::class.java} with LocationConverter")
        return """ { "coordinates": [ ${value.longitude}, ${value.latitude} ], "type": "Point" } """
    }
}