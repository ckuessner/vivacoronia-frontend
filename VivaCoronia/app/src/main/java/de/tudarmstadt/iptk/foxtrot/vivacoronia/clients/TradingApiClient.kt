package de.tudarmstadt.iptk.foxtrot.vivacoronia.clients

import android.content.Context
import android.net.Uri
import com.android.volley.*
import com.android.volley.toolbox.RequestFuture
import com.android.volley.toolbox.Volley
import com.beust.klaxon.*
import com.google.android.gms.maps.model.LatLng
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Need
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.InventoryItem
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Offer
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.ProductSearchQuery
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.models.Supermarket
import de.tudarmstadt.iptk.foxtrot.vivacoronia.trading.supermarketInventory.PlacesApiResult
import org.json.JSONArray
import org.json.JSONObject
import org.threeten.bp.OffsetDateTime


object TradingApiClient : ApiBaseClient() {
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

    private fun getSupermarketEndpoint(): String {
        return joinPaths(getEndpoint(), "supermarket")
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

    fun getSupermarkets(context: Context, location: LatLng, radius: Double): ArrayList<PlacesApiResult> {
        val queue: RequestQueue = Volley.newRequestQueue(context)

        val future = RequestFuture.newFuture<JSONObject>()
        val jsObjRequest = JsonObjectRequest(
            Request.Method.GET,
            "https://maps.googleapis.com/maps/api/place/search/json?location=${location.latitude},${location.longitude}&sensor=true&rankby=distance&type=grocery_or_supermarket&key=AIzaSyCExEI8en2xFz8pQSIGavdl50U06PIA4Qk",
            null,
            future,
            future)
        queue.add(jsObjRequest)
        val response = future.get()
        return parseSupermarkets(response)
    }

    private fun parseSupermarkets(json: JSONObject): ArrayList<PlacesApiResult> {
        val supermarketPairs: ArrayList<PlacesApiResult> = arrayListOf()
        val resultArray: JSONArray = json.get("results") as JSONArray
        for(i in 0 until resultArray.length()){
            val currentResult: JSONObject = resultArray[i] as JSONObject
            val id: String = currentResult.get("place_id") as String
            val name: String = currentResult.get("name") as String
            val location: JSONObject = (currentResult.get("geometry") as JSONObject).get("location") as JSONObject
            supermarketPairs.add(
                PlacesApiResult(id, name,
                    LatLng(
                        location.get("lat") as Double,
                        location.get("lng") as Double
                ))
            )
        }
        return supermarketPairs
    }

    fun getSupermarketInventoryForID(context: Context, supermarket: PlacesApiResult, onRequestFailed: (error: VolleyError, supermarket: PlacesApiResult) -> Unit): Supermarket {
        val queue = getRequestQueue(context) ?: throw VolleyError("Unable to get request queue")
        val url = joinPaths(getSupermarketEndpoint(), supermarket.supermarketPlaceId)
        val future = RequestFuture.newFuture<JSONObject>()
        val request = JsonObjectRequest(Request.Method.GET, url, null, future, Response.ErrorListener { error ->
            onRequestFailed(error, supermarket)
        })
        queue.add(request)
        val result = future.get()

        return parseSupermarket(result)
    }

    private fun parseSupermarket(supermarket: JSONObject): Supermarket {
        val locationJson = (supermarket["location"] as JSONObject )["coordinates"] as JSONArray
        val location = LatLng(locationJson[0] as Double, locationJson[1] as Double)
        val inventory = supermarket["inventory"] as JSONArray
        val supermarketId = supermarket["supermarketId"] as String
        val supermarketName = supermarket["name"] as String
        return Supermarket(
            supermarketId,
            supermarketName,
            location,
            parseInventory(inventory, supermarketId, supermarketName, location)
        )
    }

    private fun parseInventory(inventoryJson: JSONArray, supermarketId: String, supermarketName: String, supermarketLocation: LatLng): List<InventoryItem> {
        val inventory = arrayListOf<InventoryItem>()
        for(i in 0 until inventoryJson.length()){
            val inventoryItemJson = inventoryJson[i] as JSONObject
            val id = inventoryItemJson["_id"] as String
            val name = inventoryItemJson["product"] as String
            val productCategory = inventoryItemJson["productCategory"] as String
            val availability = inventoryItemJson["availabilityLevel"] as Int
            inventory.add(InventoryItem(id, name, productCategory, availability, supermarketId, supermarketName, supermarketLocation))
        }
        return inventory
    }

    fun putInventoryItem(item: InventoryItem, newItem:Boolean, newSupermarket: Boolean, availability: Int, context: Context): Boolean? {
        val queue = getRequestQueue(context) ?: throw VolleyError("Unable to get request queue!")
        val future = RequestFuture.newFuture<JSONObject>()
        val request = when {
            newSupermarket -> {
                createPostSupermarketRequest(item, future)
            }
            newItem -> {
                createNewItemRequest(item, future)
            }
            else -> {
                createEditItemRequest(item, availability, future)
            }
        }
        queue.add(request)
        return true
    }

    private fun createPostSupermarketRequest(
        item: InventoryItem,
        future: RequestFuture<JSONObject>?
    ): JsonObjectRequest {
        val url = getSupermarketEndpoint()
        val method = Request.Method.POST
        val jsonInventoryItemObject = JSONObject()
        jsonInventoryItemObject
            .put("product", item.itemName)
            .put("productCategory", item.productCategory)
            .put("availabilityLevel", item.availability)
        val jsonInventoryArray = JSONArray()
        jsonInventoryArray.put(jsonInventoryItemObject)
        val jsonLocationObject = JSONObject()
        val location = item.supermarket!!.supermarketLocation
        val coordinatesJSONArray = JSONArray().put(location.latitude).put(location.longitude)
        jsonLocationObject
            .put("type", "Point")
            .put("coordinates", coordinatesJSONArray)
        val jsonSupermarketPostObject = JSONObject()
        jsonSupermarketPostObject
            .put("supermarketId", item.supermarket!!.supermarketId)
            .put("name", item.supermarket!!.supermarketName)
            .put("location", jsonLocationObject)
            .put("inventory", jsonInventoryArray)
        return JsonObjectRequest(method, url, jsonSupermarketPostObject, future, future)
    }

    private fun createEditItemRequest(
        item: InventoryItem,
        availability: Int,
        future: RequestFuture<JSONObject>?
    ): JsonObjectRequest {
        val url = joinPaths(getSupermarketEndpoint(), item.supermarket!!.supermarketId, item.id)
        val jsonPatchObject = JSONObject()
        jsonPatchObject.put("availabilityLevel", availability)
        val method = Request.Method.PATCH
        return JsonObjectRequest(method, url, jsonPatchObject, future, future)
    }

    private fun createNewItemRequest(
        item: InventoryItem,
        future: RequestFuture<JSONObject>?
    ): JsonObjectRequest {
        val url = joinPaths(getSupermarketEndpoint(), item.supermarket!!.supermarketId)
        val jsonPostObject = JSONObject()
        jsonPostObject
            .put("product", item.itemName)
            .put("availabilityLevel", item.availability)
            .put("productCategory", item.productCategory)
        val method = Request.Method.POST
        return JsonObjectRequest(method, url, jsonPostObject, future, future)
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
            .appendQueryParameter("userId", getUserId(context))
            .build()
            .toString()

        val future = RequestFuture.newFuture<JSONArray>()
        val request = JsonArrayJWT(Request.Method.GET, url, null, future, future, context)
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
        val future = RequestFuture.newFuture<JSONObject>()
        val request = JsonObjectJWT(Request.Method.PATCH, url, body, future, future, context)

        queue.add(request)
        val result = future.get()
        return result.has("deactivatedAt")
                && OffsetDateTime.parse(result["deactivatedAt"].toString()) >= deactivatedAt
    }

    fun putOffer(offer: Offer, context: Context): Offer? {
        val jsonString = productConverter.toJsonString(offer)
        val jsonObject = JSONObject(jsonString)
        jsonObject.put("userId", getUserId(context))
        val url = joinPaths(getOffersEndpoint(), offer.id)
        val queue = getRequestQueue(context) ?: throw VolleyError("Unable to get request queue!")
        val future = RequestFuture.newFuture<JSONObject>()

        val method = if (offer.id.isEmpty()) Request.Method.POST else Request.Method.PATCH // if id is not set, this is a new offer and should be posted
        val request = JsonObjectJWT(method, url, jsonObject, future, future,context)
        queue.add(request)
        val result = future.get()
        return productConverter.parse(result.toString())
    }

    fun postNeed(need: Need, context: Context): Need?{
        val jsonString = productConverter.toJsonString(need)
        val jsonObject = JSONObject(jsonString)
        jsonObject.put("userId", getUserId(context))
        val url = joinPaths(getNeedsEndpoint(), need.id)
        val queue = getRequestQueue(context) ?: throw VolleyError("Unable to get request queue!")
        val future = RequestFuture.newFuture<JSONObject>()

        val method = Request.Method.POST
        val request = JsonObjectJWT(method, url, jsonObject, future, future, context)
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
