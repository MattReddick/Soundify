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
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
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


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var playerObject: Player
    private lateinit var songLibrary: MutableList<Song>

    private lateinit var firebase: FirebaseDatabase
    private lateinit var databaseRef: DatabaseReference
    private lateinit var fbStorage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    val CLIENT_ID = "e01fcf6eba35472bb4aa1db36bf92863"
    val AUTH_TOKEN_REQUEST_CODE = 0x10
    val AUTH_CODE_REQUEST_CODE = 0x11

    private val mOkHttpClient = OkHttpClient()
    private var mAccessToken: String? = null
    private var mAccessCode: String? = null
    private val mCall: Call? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerObject = Player(applicationContext, lifecycleScope)
        Log.i("MainActivity","SpotifyPlayer Made")
        val navView: BottomNavigationView = binding.navView
        // Lines below initialize real-time database and firebase storage
        firebase = Firebase.database
        databaseRef = firebase.getReference("Songs")
        fbStorage = Firebase.storage
        storageRef = fbStorage.reference
//        readData()
        //spotifyPlayer.getAccessToken()
        onRequestTokenClicked(binding.root)
        songLibrary = mutableListOf()
        Log.i("songLibrary1", songLibrary.toString())
        val tmpSong : Song = Song(title = "Dear Maria, Count Me In", artist = "All Time Low", "", "", audioUrl = "spotify:track:0JJP0IS4w0fJx01EcrfkDe", 0, 0, true)
        val tmpSoundcloud : Song = Song("Closer", "Chainsmokers", "https://i1.sndcdn.com/artworks-3nETEFJoML7B-0-t500x500.jpg", "", "https://firebasestorage.googleapis.com/v0/b/cs184-soundify.appspot.com/o/Songs%2FCloser.mp3?alt=media&token=9ffae7c6-c766-4487-acaf-8087445ea187", 287,7, false)
        val tmpSong2 : Song = Song(title = "Heading South", artist = "Zach Bryan", "", "", audioUrl = "spotify:track:2Dct3GykKZ58hpWRFfe2Qd", 0, 0, true)
        playerObject.addToQueue(tmpSong)
        playerObject.addToQueue(tmpSoundcloud)
        playerObject.addToQueue(tmpSong2)
        getSongs()
        Log.i("songLibrary2", songLibrary.toString())
        getIntent().putExtra("playerObject", playerObject)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
    private fun getRedirectUri(): Uri? {
        return Uri.Builder()
            .scheme("edu.ucsb.cs.cs184.matthewreddick.soundify")
            .authority("auth")
            .build()
    }
    fun onRequestCodeClicked(view: View?) {
        val request = getAuthenticationRequest(AuthorizationResponse.Type.CODE)
        AuthorizationClient.openLoginActivity(this, AUTH_CODE_REQUEST_CODE, request)
    }

    fun onRequestTokenClicked(view: View?) {
        val request = getAuthenticationRequest(AuthorizationResponse.Type.TOKEN)
        AuthorizationClient.openLoginActivity(this, AUTH_TOKEN_REQUEST_CODE, request)
    }

    private fun cancelCall() {
        mCall?.cancel()
    }

    private fun getAuthenticationRequest(type: AuthorizationResponse.Type): AuthorizationRequest? {
        return AuthorizationRequest.Builder(CLIENT_ID, type, getRedirectUri().toString())
            .setShowDialog(false)
            .setScopes(arrayOf("user-read-email"))
            .setCampaign("your-campaign-token")
            .build()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val response = AuthorizationClient.getResponse(resultCode, data)
        /*
        if (response.error != null && !response.error.isEmpty()) {
            setResponse(response.error)
        }
         */
        if (requestCode == AUTH_TOKEN_REQUEST_CODE) {
            mAccessToken = response.accessToken
            Log.i("TOKEN",mAccessToken.toString())
            getIntent().putExtra("tokenObject", mAccessToken)
            //updateTokenView()
        } else if (requestCode == AUTH_CODE_REQUEST_CODE) {
            mAccessCode = response.code
            //updateCodeView()
            Log.i("CODE",mAccessCode.toString())
        }
    }

    fun searchSpotify(input : String) {
        val inputEncoded : String = java.net.URLEncoder.encode(input, "utf-8")
        val myURL : String = "https://api.spotify.com:443/v1/search?q=" + inputEncoded + "&limit=3&market=SE&offset=0&type=track"
        val request = Request.Builder()
            .url(myURL)
            .header(
                "Authorization",
                "Bearer " + mAccessToken
            )
            .build()
        cancelCall()
        val mCall = mOkHttpClient.newCall(request)
        mCall!!.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.i("MainSearch","Failed to fetch data")
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                try {
                    val jsonObject = JSONObject(response.body!!.string())
                    //Log.i("Full Search Details", jsonObject.toString(3))
                    val jsonSongs: JSONObject = jsonObject.getJSONObject("tracks")
                    val items: JSONArray = jsonSongs.getJSONArray(("items"))
                    //I realize this should be a for loop
                    val printABLE = mutableListOf<List<String>>()

                    if(items.length() <= 0) {
                        return
                    }

                    for (i in 1..items.length()) {
                        val song : JSONObject = items[i-1] as JSONObject
                        var songInfo = listOf(song.getString("uri"), song.getString("name"), (song.getJSONArray("artists")[0] as JSONObject).getString("name"), ((song.getInt("duration_ms"))/1000).toString())
                        printABLE.add(songInfo)
                    }
                    Log.i("Spotify Search Result", printABLE.toString())

                } catch (e: JSONException) {
                    Log.i("MainSearch","Failed to parse data")
                }
            }
        })
    }

    fun getSongs(){
        databaseRef.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
//                val value = snapshot.value
                val children = snapshot.children
                children.forEach {
                    var title = ""
                    var artist = ""
                    var album = ""
                    var imageUrl = ""
                    var audioUrl =  ""
                    var duration = 0
                    var id = 0

//                    title = it.value.toString()

                    val values = it.children
                    Log.i("key", it.key.toString())
                    if (it.key.toString() != "Example") {
                        values.forEach {
                            when (it.key.toString()) {
                                "Artist" ->
                                    artist = it.value as String
                                "Length" ->
//                                Log.i("duration", it.value.toString())
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
                        playerObject.addToQueue(song)
//                        Log.i("Song", song.toString())
                    }
                }

                Log.i("songLibrary", songLibrary.toString())
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }

        })

//        val songRef: StorageReference = storageRef.child("Songs/NiceSwing.mp3")
//        songRef.downloadUrl.addOnSuccessListener {
//            // Got the download URL for 'users/me/profile.png'
//            Log.v("fb", "URI: $it")
//        }.addOnFailureListener {
//            // Handle any errors
//            Log.e("firebase", "Error getting data", it)
//        }
    }
}