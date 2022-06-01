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
import okhttp3.Call
import okhttp3.OkHttpClient


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var player: Player
    private lateinit var spotifyPlayer: SpotifyPlayer
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

        player = Player()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        spotifyPlayer = SpotifyPlayer(applicationContext, lifecycleScope)
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
        getSongs()
        Log.i("songLibrary2", songLibrary.toString())

        getIntent().putExtra("spotifyPlayerObject", spotifyPlayer)

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
            //updateTokenView()
        } else if (requestCode == AUTH_CODE_REQUEST_CODE) {
            mAccessCode = response.code
            //updateCodeView()
            Log.i("CODE",mAccessCode.toString())
        }
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
                        val song = Song(title, artist, album, imageUrl, audioUrl, duration, id)
                        songLibrary.add(song)
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