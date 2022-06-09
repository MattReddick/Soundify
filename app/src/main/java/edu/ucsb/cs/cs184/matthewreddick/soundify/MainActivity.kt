package edu.ucsb.cs.cs184.matthewreddick.soundify

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import edu.ucsb.cs.cs184.matthewreddick.soundify.databinding.ActivityMainBinding
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

lateinit var playerObject: Player
var started = false

const val AUTH_TOKEN_REQUEST_CODE = 0x10
const val AUTH_CODE_REQUEST_CODE = 0x11

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var songLibrary: MutableList<Song>
    private var spotLibrary = mutableListOf<List<String>>()
    private lateinit var firebase: FirebaseDatabase
    private lateinit var databaseRef: DatabaseReference
    private lateinit var fbStorage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private val clientId = "e01fcf6eba35472bb4aa1db36bf92863"
    private val mOkHttpClient = OkHttpClient()
    private var mAccessToken: String? = null
    private var mAccessCode: String? = null
    private val mCall: Call? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerObject = Player(applicationContext, lifecycleScope)
        Log.i("main player address", playerObject.toString())
        Log.i("MainActivity","SpotifyPlayer Made")
        val navView: BottomNavigationView = binding.navView

        // Lines below initialize real-time database and firebase storage
        firebase = Firebase.database
        databaseRef = firebase.getReference("Songs")
        fbStorage = Firebase.storage
        storageRef = fbStorage.reference

        onRequestTokenClicked(binding.root)
        songLibrary = mutableListOf()
        Log.i("songLibrary1", songLibrary.toString())
        getSongs()
        Log.i("songLibrary2", songLibrary.toString())

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)
    }

    private fun getRedirectUri(): Uri? {
        return Uri.Builder()
            .scheme("edu.ucsb.cs.cs184.matthewreddick.soundify")
            .authority("auth")
            .build()
    }

    private fun onRequestTokenClicked(view: View?) {
        val request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN)
        AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request)
    }

    private fun cancelCall() {
        mCall?.cancel()
    }

    private fun getAuthenticationRequest(type: AuthorizationResponse.Type): AuthorizationRequest? {
        return AuthorizationRequest.Builder(clientId, type, getRedirectUri().toString())
            .setShowDialog(false)
            .setScopes(arrayOf("user-read-email"))
            .setCampaign("your-campaign-token")
            .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val response = AuthorizationClient.getResponse(resultCode, data)

        if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
            mAccessToken = response.accessToken
            Log.i("TOKEN",mAccessToken.toString())
            getIntent().putExtra("tokenObject", mAccessToken)
        } else if (requestCode == AUTH_CODE_REQUEST_CODE) {
            mAccessCode = response.code
            Log.i("CODE",mAccessCode.toString())
        }
    }

    fun searchSpotify(input : String): MutableList<List<String>> {
        val inputEncoded : String = java.net.URLEncoder.encode(input, "utf-8")
        val myURL : String =
            "https://api.spotify.com:443/v1/search?q=$inputEncoded&limit=10&market=SE&offset=0&type=track"
        val request = Request.Builder()
            .url(myURL)
            .header(
                "Authorization",
                "Bearer $mAccessToken"
            )
            .build()
        cancelCall()
        val mCall = mOkHttpClient.newCall(request)
        mCall.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("MainSearch","Failed to fetch data")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                try {
                    val jsonObject = JSONObject(response.body!!.string())
                    val jsonSongs: JSONObject = jsonObject.getJSONObject("tracks")
                    val items: JSONArray = jsonSongs.getJSONArray(("items"))

                    if(items.length() <= 0) {
                        return
                    }

                    for (i in 1..items.length()) {
                        val song : JSONObject = items[i-1] as JSONObject
                        val songInfo = listOf(song.getString("name"),
                            (song.getJSONArray("artists")[0] as JSONObject).getString("name"),
                            song.getString("uri"),
                            (song.getString("duration_ms").toInt()/1000).toString())
                        spotLibrary.add(songInfo)
                    }
                } catch (e: JSONException) {
                    Log.i("MainSearch","Failed to parse data")
                }
            }
        })
        return spotLibrary
    }

    private fun getSongs(){
        databaseRef.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                val children = snapshot.children
                children.forEach {
                    var title = ""
                    var artist = ""
                    var album = ""
                    var imageUrl = ""
                    var audioUrl =  ""
                    var duration = 0
                    var id = 0

                    val values = it.children
                    Log.i("key", it.key.toString())
                    if (it.key.toString() != "Example") {
                        values.forEach {
                            when (it.key.toString()) {
                                "Artist" ->
                                    artist = it.value as String
                                "Length" ->
                                    duration = it.value.toString().toInt()
                                "Id" ->
                                    id = it.value.toString().toInt()
                                "Title" ->
                                    title = it.value as String
                                "Audio" ->
                                    audioUrl = it.value as String
                                "Image" ->
                                    imageUrl = it.value as String
                                "Album" ->
                                    album = it.value as String
                                else -> {
                                    print("x is neither 1 nor 2")
                                }
                            }
                        }
                        val song = Song(title, artist, album, imageUrl, audioUrl, duration, id, false)
                        songLibrary.add(song)
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })
    }
}