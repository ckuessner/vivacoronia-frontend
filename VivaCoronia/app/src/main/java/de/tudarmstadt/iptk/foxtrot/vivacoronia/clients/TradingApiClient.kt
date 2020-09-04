package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.content.Context
import android.net.Uri
import com.android.volley.*
import com.android.volley.toolbox.RequestFuture
import com.beust.klaxon.*
import com.google.android.gms.maps.model.LatLng
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.BaseProduct
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Need
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.ProductSearchQuery
import org.json.JSONArray
import org.json.JSONObject
import org.threeten.bp.OffsetDateTime

object TradingApiClient : ApiBaseClient() {
    private val TAG = "TradingApiClient"
    private val productConverter : Klaxon = Klaxon()

    init {
        productConverter.converter(LatLngConverter)
    }

    private fun getEndpoint(): String{
        return "${getBaseUrl()}/trading/"
    }

    private fun getOffersEndpoint(): String {
        return joinPaths(getEndpoint(), "offers")
    }

    private fun getNeedsEndpoint(): String {
        return joinPaths(getEndpoint(), "needs")
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
        val query = ProductSearchQuery()
        query.userId = TradingApiClient.getUserId(context)
        return getOffers(context, query)
    }

    fun getOffers(context: Context, query: ProductSearchQuery): MutableList<Offer> {
        val queue = getRequestQueue(context) ?: throw VolleyError("Unable to get request queue!")
        val url = Uri.parse(getOffersEndpoint())
            .buildUpon()
            .encodedQuery(query.toString())
            .build()
            .toString()

        val future = RequestFuture.newFuture<JSONArray>()
        val request = JsonArrayJWT(Request.Method.GET, url, null, future, future, context)
        queue.add(request)
        val futureResult = future.get().toString()

        val result = productConverter.parseArray<Offer>(futureResult)
        return result?.toMutableList() ?: mutableListOf()
    }

    fun getMyNeeds(context: Context): MutableList<Need> {
        val queue = getRequestQueue(context) ?: throw VolleyError("Unable to get request queue!")
        val url = Uri.parse(getNeedsEndpoint())
            .buildUpon()
            .appendQueryParameter("userId", getUserId(context).toString())
            .build()
            .toString()

        val future = RequestFuture.newFuture<JSONArray>()
        val request = JsonArrayJWT(Request.Method.GET, url, null, future, future)
        queue.add(request)
        val futureResult = future.get().toString()

        val result = productConverter.parseArray<Need>(futureResult)
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

    fun deleteNeed(id: String, fulfilled: Boolean, context: Context): Boolean {
        val queue = getRequestQueue(context) ?: return false
        val url = joinPaths(getNeedsEndpoint(), id)
        val deactivatedAt = OffsetDateTime.now()
        val body = JSONObject()
        body.put("fulfilled", fulfilled)
        body.put("deactivatedAt", deactivatedAt)
        val future = RequestFuture.newFuture<JSONObject>()
        val request = JsonObjectJWT(Request.Method.DELETE, url, body, future, future)

        queue.add(request)
        val result = future.get()
        return result.has("deactivatedAt")
                && OffsetDateTime.parse(result["deactivatedAt"].toString()).isEqual(deactivatedAt)
    }

    fun putOffer(offer: Offer, context: Context): Offer? {
        val jsonString = productConverter.toJsonString(offer)
        val jsonObject = JSONObject(jsonString)
        jsonObject.put("userId", getUserId(context)) // TODO should be done/verified @ server
        val url = joinPaths(getOffersEndpoint(), offer.id)
        val queue = getRequestQueue(context) ?: throw VolleyError("Unable to get request queue!")
        val future = RequestFuture.newFuture<JSONObject>()

        val method = if (offer.id.isEmpty()) Request.Method.POST else Request.Method.PATCH // if id is not set, this is a new offer and should be posted
        val request = JsonObjectJWT(method, url, jsonObject, future, future)
        queue.add(request)
        val result = future.get()
        return productConverter.parse(result.toString())
    }

    fun putNeed(need: Need, context: Context): Need?{
        val jsonString = productConverter.toJsonString(need)
        val jsonObject = JSONObject(jsonString)
        jsonObject.put("userId", getUserId()) // TODO should be done/verified @ server
        val url = joinPaths(getNeedsEndpoint(), need.id)
        val queue = getRequestQueue(context) ?: throw VolleyError("Unable to get request queue!")
        val future = RequestFuture.newFuture<JSONObject>()

        val method = Request.Method.POST // no distinction between POST and PATCH like with offers, because needs cannot be edited
        val request = JsonObjectRequest(method, url, jsonObject, future, future)
        queue.add(request)
        val result = future.get()
        return productConverter.parse(result.toString())
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
