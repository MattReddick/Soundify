package edu.ucsb.cs.cs184.matthewreddick.soundify.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.*
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import edu.ucsb.cs.cs184.matthewreddick.soundify.*
import edu.ucsb.cs.cs184.matthewreddick.soundify.databinding.FragmentHomeBinding
import kotlinx.android.synthetic.main.activity_main.*


private lateinit var spotifySongs : MutableList<Song>
private lateinit var soundcloudSongs : MutableList<Song>
private var con : Context ?= null

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    //lateinit var playerObject: Player
    private lateinit var accessToken : String
    //private lateinit var playerObject : Player

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        con = context

        val searchBar : EditText = root.findViewById(R.id.search_bar) as EditText
        //create 3 songs from soundcloud and spotify and put them in a list
        //then connect them to the home UI for display

        val spotifySong1 = Song("True Love",
            "Kanye West",
            "Single",
            "https://i.scdn.co/image/ab67616d0000b273f52f6a4706fea3bde44467c3",
            "spotify:album:1YA0gAfV91LkDq1DODSCbc",
            149,
            1, true)
        val spotifySong2 = Song("Homecoming",
            "Kanye West",
            "Graduation",
            "https://i.scdn.co/image/ab67616d0000b27326f7f19c7f0381e56156c94a",
            "spotify:track:4iz9lGMjU1lXS51oPmUmTe",
            203,
            2, true)
        val spotifySong3 = Song("Heartless",
            "Kanye West",
            "808's and Heartbreak",
            "https://i.scdn.co/image/ab67616d0000b273346d77e155d854735410ed18",
            "spotify:track:4EWCNWgDS8707fNSZ1oaA5",
            211,
            3, true)
        spotifySongs = mutableListOf(spotifySong1, spotifySong2, spotifySong3)

        val soundCloudSong1 = Song("Animal",
            "Miike Snow",
            "Miike Snow",
            "https://i1.sndcdn.com/artworks-000001914954-vh5avg-t500x500.jpg",
            "https://firebasestorage.googleapis.com/v0/b/cs184-soundify.appspot.com/o/Songs%2FAnimal.mp3?alt=media&token=448477d5-778f-4b44-8c2b-47aa626081ed",
            263,
            10, false)
        val soundCloudSong2 = Song("Closer",
            "ChainSmokers",
            "Single",
            "https://i1.sndcdn.com/artworks-000185496921-4ios1m-t500x500.jpg",
            "https://firebasestorage.googleapis.com/v0/b/cs184-soundify.appspot.com/o/Songs%2FCloser.mp3?alt=media&token=9ffae7c6-c766-4487-acaf-8087445ea187",
            160,
            6,false)
        val soundCloudSong3 = Song("Law Of Attraction",
            "Kanye West",
            "N/A",
            "https://i1.sndcdn.com/artworks-FGiyFZ0Q2crsCIZB-ahkovQ-t500x500.jpg",
            "https://firebasestorage.googleapis.com/v0/b/cs184-soundify.appspot.com/o/Songs%2FLawOfAttraction.mp3?alt=media&token=1a7f8dc2-660f-444c-afe0-aa184f0482af",
            207,
            4, false)
        soundcloudSongs = mutableListOf(soundCloudSong1, soundCloudSong2, soundCloudSong3)

//        if(getActivity() != null){
//            val i : Intent? = getActivity()?.getIntent()
//            if (i != null) {
//                playerObject = i.getSerializableExtra("playerObject") as Player
//            }
//            //Log.i("HomeFragment","accessToken")
//        }

        val spotifyListView = binding.spotifyList
        val soundcloudListView = binding.soundcloudList

        var soundCloud: customAdapterSoundCloud = customAdapterSoundCloud()
        var spotify: customAdapterSpotify = customAdapterSpotify()

        searchBar.setOnEditorActionListener {view, actionId, keyEvent ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE ||
            keyEvent.action == KeyEvent.ACTION_DOWN || keyEvent.action == KeyEvent.KEYCODE_ENTER) {

                val searchBarResults = searchBar.text.toString()

                soundcloudSongs.clear()

                for (i in 0..(songLib.songLib.size - 1)) {
                    if (countMatches(songLib.songLib[i].getArtist()!!.lowercase(), searchBarResults) > 0 ||
                        countMatches(songLib.songLib[i].getTitle()!!.lowercase(), searchBarResults) > 0 ||
                        countMatches(songLib.songLib[i].getAlbum()!!.lowercase(), searchBarResults) > 0) {
                        soundcloudSongs.add(songLib.songLib[i])
                    }
                }

                spotifySongs.clear()

                val temp = (activity as MainActivity).searchSpotify(searchBar.text.toString())
                Thread.sleep(1000)
                Log.i("printABLE", temp.size.toString())
                for (i in 0..(temp.size - 1)) {
                    val newSong = Song(temp[i][0],
                        temp[i][1],
                        "",
                        "",
                        temp[i][2],
                        temp[i][3].toInt(),
                        i*2, true
                    )
                    spotifySongs.add(newSong)
                }
                temp.clear()
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        spotifyListView.adapter = spotify
        soundcloudListView.adapter = soundCloud

        return root
    }

    fun countMatches(string: String, pattern: String): Int {
        var index = 0
        var count = 0

        while (true)
        {
            index = string.indexOf(pattern, index)
            index += if (index != -1)
            {
                count++
                pattern.length
            }
            else {
                return count
            }
        }
    }

    class customAdapterSpotify: BaseAdapter() {
        override fun getCount(): Int {
            return spotifySongs.size
        }

        override fun getItem(p0: Int): Any {
            if (p0 < spotifySongs.size)
                return spotifySongs[p0]
            else {
                Log.i("index bigger than spotifySongs.size, returning last element",
                    spotifySongs.size.toString())
                return spotifySongs[spotifySongs.size - 1]
            }

        }

        override fun getItemId(p0: Int): Long {
            //added getters and setters in Song class
            if (p0 < spotifySongs.size)
                return spotifySongs[p0].getId()!!.toLong()
            else {
                Log.i("index bigger than spotifySongs.size, returning last element id",
                    spotifySongs.size.toString())
                return spotifySongs[spotifySongs.size - 1].getId()!!.toLong()
            }
        }

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {

            val li = LayoutInflater.from(p2!!.context)
            val myView = li.inflate(R.layout.spotify_item, null)
            val songText: TextView = myView.findViewById(R.id.songTextName)
            songText.text = spotifySongs[p0].getTitle() + " - " + spotifySongs[p0].getArtist()
            val addToQueueBtn = myView.findViewById<ImageButton>(R.id.addToQueue)
            addToQueueBtn.setOnClickListener() {
                playerObject.addToQueue(spotifySongs[p0])
                Toast.makeText(con, "queued " + spotifySongs[p0].getTitle(), Toast.LENGTH_SHORT).show()
            }
            return myView
        }
    }

    class customAdapterSoundCloud: BaseAdapter() {
        override fun getCount(): Int {
            return soundcloudSongs.size
        }

        override fun getItem(p0: Int): Any {
            if (p0 < soundcloudSongs.size)
                return soundcloudSongs[p0]
            else {
                Log.i("index bigger than spotifySongs.size, returning last element",
                    soundcloudSongs.size.toString())
                return soundcloudSongs[soundcloudSongs.size - 1]
            }

        }

        override fun getItemId(p0: Int): Long {
            //added getters and setters in Song class
            if (p0 < soundcloudSongs.size)
                return soundcloudSongs[p0].getId()!!.toLong()
            else {
                Log.i("index bigger than spotifySongs.size, returning last element id",
                    soundcloudSongs.size.toString())
                return soundcloudSongs[soundcloudSongs.size - 1].getId()!!.toLong()
            }
        }

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {

            val li = LayoutInflater.from(p2!!.context)
            val myView = li.inflate(R.layout.soundcloud_item, null)
            val songText: TextView = myView.findViewById(R.id.songTextName)
            songText.text = soundcloudSongs[p0].getTitle() + " - " + soundcloudSongs[p0].getArtist()
            val addToQueueBtn = myView.findViewById<ImageButton>(R.id.addToQueue)
            addToQueueBtn.setOnClickListener() {
                playerObject.addToQueue(soundcloudSongs[p0])
                Toast.makeText(con, "queued " + soundcloudSongs[p0].getTitle(), Toast.LENGTH_SHORT).show()
            }
            return myView
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}