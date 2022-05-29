package edu.ucsb.cs.cs184.matthewreddick.soundify

import android.content.ContentValues
import android.media.Image
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.database.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import edu.ucsb.cs.cs184.matthewreddick.soundify.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var player: Player
    private lateinit var songLibrary: MutableList<Song>

    private lateinit var firebase: FirebaseDatabase
    private lateinit var databaseRef: DatabaseReference
    private lateinit var fbStorage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        player = Player()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        // Lines below initialize real-time database and firebase storage
        firebase = Firebase.database
        databaseRef = firebase.getReference("Songs")
        fbStorage = Firebase.storage
        storageRef = fbStorage.reference
//        readData()
        songLibrary = mutableListOf()
        Log.i("songLibrary1", songLibrary.toString())
        getSongs()
        Log.i("songLibrary2", songLibrary.toString())

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
    }


    fun readData() {
        // Read from the database
        databaseRef.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
//                val value = snapshot.value
                val niceSwing = snapshot.child("NiceSwing")
//                Log.v("fb", snapshot.key + ": " + snapshot.value)
                Log.v("fb", niceSwing.key + ": " + niceSwing.child("Artist").value)

            }

            override fun onCancelled(error: DatabaseError) {
                Log.w(ContentValues.TAG, "Failed to read value.", error.toException())
            }
        })

        // read from storage
        val songRef: StorageReference = storageRef.child("Songs/NiceSwing.mp3")
        songRef.downloadUrl.addOnSuccessListener {
            // Got the download URL for 'users/me/profile.png'
            Log.v("fb", "URI: $it")
        }.addOnFailureListener {
            // Handle any errors
            Log.e("firebase", "Error getting data", it)
        }

    }
}