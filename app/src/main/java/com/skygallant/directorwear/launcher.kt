package com.skygallant.directorwear

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class WebActivity : Activity() {

    data class PlusCode(
        val compound_code: String,
        val global_code: String
    )

    data class Photo(
        val height: Int,
        val html_attributions: List<String>,
        val photo_reference: String,
        val width: Int
    )

    data class OpeningHours(
        val open_now: Boolean
    )

    data class Viewport(
        val northeast: Location,
        val southwest: Location
    )

    data class Location(
        val lat: Double,
        val lng: Double
    )

    data class Geometry(
        val location: Location,
        val viewport: Viewport
    )

    data class Result(
        val business_status: String,
        val geometry: Geometry,
        val icon: String,
        val name: String,
        val opening_hours: OpeningHours,
        val photos: List<Photo>,
        val place_id: String,
        val plus_code: PlusCode,
        val rating: Double,
        val reference: String,
        val scope: String,
        val types: List<String>,
        val user_ratings_total: Int,
        val vicinity: String
    )

    data class TopLevel(
        val html_attributions: List<String>,
        val next_page_token: String,
        val results: List<Result>,
        val status: String
    )

    private var lat: Double = 0.0
    private var lng: Double = 0.0

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var thisLocation: android.location.Location = android.location.Location("Google Maps API")

    private fun queryAPI() {

        Log.d(TAG, "Location?")

        Log.d(TAG, thisLocation.latitude.toString())
        Log.d(TAG, thisLocation.longitude.toString())

        val url = URL(
            """
            |https://maps.googleapis.com/maps/api/place/nearbysearch/json
            |?opennow
            |&location=${thisLocation.latitude}%2C${thisLocation.longitude}
            |&radius=1000
            |&key=${BuildConfig.MAPS_API_KEY}
            """.trimMargin()
        )
        Log.d(TAG, "URL")
        val reader = InputStreamReader(url.openStream())
        Log.d(TAG, "Reader")
        val mapsXML = Gson().fromJson(reader, TopLevel::class.java)
        Log.d(TAG, "mapsXML")
        val thisDestiny = mapsXML.results.random()

        lat = thisDestiny.geometry.location.lat
        lng = thisDestiny.geometry.location.lng
        Log.d(TAG, "Location!")
    }

    private fun checkPerms(gotCon: Context): Boolean {
        return ActivityCompat.checkSelfPermission(
            gotCon,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    suspend fun getLastLocation(): android.location.Location = withContext(Dispatchers.IO) {
        // Use suspendCoroutine to bridge between coroutine and callback
        suspendCoroutine { continuation ->
            try {
                val lastLocationTask = fusedLocationProviderClient.lastLocation
                val location = Tasks.await(lastLocationTask) // Await the result without blocking the thread
                continuation.resume(location)
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }


    @SuppressLint("WearRecents")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "SUCCESS?")
        if (checkPerms(this)) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            CoroutineScope(Dispatchers.IO).launch {
                // Your I/O operation goes here
                thisLocation = getLastLocation()
                queryAPI()

                // Switch to the Main thread if you need to update any UI components
                withContext(Dispatchers.Main) {
                    // Update UI or perform further actions on the main thread
                    val mapsIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:$lat,$lng")).apply {
                        setPackage("com.google.android.apps.maps")
                    }
                    Log.d(TAG, "SUCCESS!")
                    this@WebActivity.startActivity(mapsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
        } else {
            val text = "No Perms"
            val duration = Toast.LENGTH_SHORT
            val toast = Toast.makeText(this, text, duration)
            toast.show()
        }
    }
}