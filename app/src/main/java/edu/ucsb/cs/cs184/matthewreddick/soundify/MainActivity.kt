package edu.ucsb.cs.cs184.matthewreddick.soundify

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import edu.ucsb.cs.cs184.matthewreddick.soundify.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var firebase: FirebaseDatabase
    private lateinit var databaseRef: DatabaseReference
    private lateinit var fbStorage: FirebaseStorage
    private lateinit var storageRef: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        // Lines below initialize real-time database and firebase storage
        firebase = Firebase.database
        databaseRef = firebase.getReference("Songs")
        fbStorage = Firebase.storage
        storageRef = fbStorage.reference
        readData()

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

    fun readData() {
        // Read from the database
        databaseRef.addValueEventListener(object: ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
//                val value = snapshot.value
                Log.v("fb", snapshot.key + ": " + snapshot.value)

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