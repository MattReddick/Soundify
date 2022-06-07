package edu.ucsb.cs.cs184.matthewreddick.soundify.ui.dashboard

import android.R.attr.button
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import edu.ucsb.cs.cs184.matthewreddick.soundify.*
import edu.ucsb.cs.cs184.matthewreddick.soundify.databinding.FragmentDashboardBinding

var playPause = false

class DashboardFragment : Fragment(){

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private val handler = Handler()
    private var progress: Int = 0
    private lateinit var progressBar: ProgressBar
    private lateinit var curTime: TextView
    private lateinit var remTime: TextView
    //DELETE BELOW LATER ON JUST FOR TEST
    private lateinit var accessToken : String
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        if(getActivity() != null){
//            val i : Intent? = getActivity()?.getIntent()
//            if (i != null) {
//                 playerObject = i.getSerializableExtra("playerObject") as Player
//            }
//            //Log.i("HomeFragment","accessToken")
//        }

        playerObject.setImageView(root.findViewById(R.id.albumCoverImage))
        val titleText : TextView = root.findViewById(R.id.songTitleText)
        val artistText : TextView = root.findViewById(R.id.artistText)

        if (started) {
            if (playerObject.queue!!.size > 0) {
                if (playerObject.getCurrentSong()!!.isSpotify()) {
                    val tmpSong : Song? = playerObject.getCurrentSong()
                    titleText.text = tmpSong?.getTitle()
                    artistText.text = tmpSong?.getArtist()
                    playerObject.reloadCoverArt()
                } else {
                    val tmpSong : Song? = playerObject.getCurrentSong()
                    titleText.text = tmpSong?.getTitle()
                    artistText.text = tmpSong?.getArtist()
                    playerObject.updateTrackCoverArtSoundCloud()
                }
            }
        }
        //val url = "https://i1.sndcdn.com/artworks-000185496921-4ios1m-t500x500.jpg"
        //Log.i("urlupdate", url)
        //Log.i("urlupdate", root.findViewById<ImageView>(R.id.albumCoverImage).toString())
        //Picasso.get().load(url).into(root.findViewById<ImageView>(R.id.albumCoverImage))

        //here changing the imageButton color so the user knows
        //when a song is on repeat or when he can skip to the next/previous songs
        val loopBtn : ImageButton = root.findViewById(R.id.loopButton) as ImageButton
        loopBtn.setOnClickListener() {
                playerObject.setLoop()
                if (playerObject.getLoop()) {
                    loopBtn.background.setColorFilter(
                        resources.getColor(R.color.green),
                        PorterDuff.Mode.MULTIPLY
                    )
                } else {
                    loopBtn.background.setColorFilter(
                        resources.getColor(R.color.purple_200),
                        PorterDuff.Mode.MULTIPLY
                    )
                }
        }

        if (playerObject.getLoop()) {
            loopBtn.background.setColorFilter(resources.getColor(R.color.green), PorterDuff.Mode.MULTIPLY)
        } else {
            loopBtn.background.setColorFilter(resources.getColor(R.color.purple_200), PorterDuff.Mode.MULTIPLY)
        }

        val playBtn : ImageButton = root.findViewById(R.id.playButton) as ImageButton
        playBtn.setOnClickListener() {
            if(playerObject != null) {
                playerObject.pausePlay()
                val tmpSong : Song? = playerObject.getCurrentSong()
                if(tmpSong != null) {
                    titleText.text = tmpSong.getTitle()
                    artistText.text = tmpSong.getArtist()
                    //val url = "https://i1.sndcdn.com/artworks-000185496921-4ios1m-t500x500.jpg"
                    //Picasso.get().load(url).into(root.findViewById<ImageView>(R.id.albumCoverImage))
                    //playerObject.updateTrackCoverArtSoundCloud(root.findViewById(R.id.albumCoverImage))
                    //Log.i("main player address", playerObject.toString())
                } else {
                    Toast.makeText(context, "No songs on queue!", Toast.LENGTH_SHORT).show()
                }
                playPause = !playPause
                if (playPause) {
                    playBtn.setImageResource(R.drawable.ic_baseline_pause_24)
                } else {
                    playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24)
                }
            }
        }

        if (playPause) {
            playBtn.setImageResource(R.drawable.ic_baseline_pause_24)
        } else {
            playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }

        val skipForwardBtn : ImageButton = root.findViewById(R.id.skipForwardButton) as ImageButton
        skipForwardBtn.setOnClickListener() {
            if(playerObject != null) {
                //if (count % 2 == 1) playerObject.currentSongIndex -= 1
                playerObject.playNext()
                val tmpSong : Song? = playerObject.getCurrentSong()
                if(tmpSong != null) {
                    titleText.text = tmpSong.getTitle()
                    artistText.text = tmpSong.getArtist()
                    //playerObject.updateTrackCoverArtSoundCloud(root.findViewById<ImageView>(R.id.albumCoverImage))
                }
                playPause = true
                playBtn.setImageResource(R.drawable.ic_baseline_pause_24)
            }
        }

        val shuffleBtn : ImageButton = root.findViewById(R.id.shuffleButton) as ImageButton
        shuffleBtn.setOnClickListener() {
            if(playerObject != null)
                playerObject.shuffleQueue()
        }

        val skipPreviousBtn : ImageButton = root.findViewById(R.id.skipBackButton) as ImageButton
        skipPreviousBtn.setOnClickListener() {
            if(playerObject != null) {
                //if (count % 2 == 1) playerObject.currentSongIndex += 1
                playerObject.playPrevious()
                val tmpSong : Song? = playerObject.getCurrentSong()
                if(tmpSong != null) {
                    titleText.text = tmpSong.getTitle()
                    artistText.text = tmpSong.getArtist()
                    //playerObject.updateTrackCoverArtSoundCloud(root.findViewById<ImageView>(R.id.albumCoverImage))
                }
                playPause = true
                playBtn.setImageResource(R.drawable.ic_baseline_pause_24)
            }
        }

        //this part below is for the progress bar, however,
        //it only works for soundcloud songs for now
        progress = binding.progressBar.progress
        progressBar = binding.progressBar
        curTime = binding.currTimeStampText
        remTime = binding.remainingTimeStampText

        curTime.text = "0:00"
        remTime.text = "-0:00"

        Thread(Runnable {
            while (progress < 100) {
                if (playerObject.getCurrentSong() != null) {
                    if (!playerObject.getCurrentSong()!!.isSpotify() && playerObject.mediaPlayer != null) {
                        progress = (playerObject.mediaPlayer!!.currentPosition) /
                                (playerObject.getCurrentSong()!!.getDuration()!! * 10)

                        val mediaPositionSeconds =
                            (playerObject.mediaPlayer!!.currentPosition / 1000).toInt()
                        val durationSeconds =
                            playerObject.getCurrentSong()!!.getDuration()!! - mediaPositionSeconds

                        val curTimeMinutes = (mediaPositionSeconds / 60).toInt() % 60
                        val curTimeSeconds = (mediaPositionSeconds % 60).toInt()

                        val remTimeMinutes = (durationSeconds / 60).toInt() % 60
                        val remTimeSeconds = (durationSeconds % 60).toInt()

                        if (curTimeSeconds < 10) curTime.text =
                            curTimeMinutes.toString() + ":0" + curTimeSeconds.toString()
                        else curTime.text =
                            curTimeMinutes.toString() + ":" + curTimeSeconds.toString()
                        if (remTimeSeconds < 10) remTime.text =
                            "-" + remTimeMinutes.toString() + ":0" + remTimeSeconds.toString()
                        else remTime.text =
                            "-" + remTimeMinutes.toString() + ":" + remTimeSeconds.toString()
                    } else {
                        progress = (playerObject.getCurrentPosition().toInt()) /
                                (playerObject.getCurrentSong()!!.getDuration()!! * 10)

                        val mediaPositionSeconds =
                            (playerObject.getCurrentPosition() / 1000).toInt()
                        val durationSeconds =
                            playerObject.getCurrentSong()!!.getDuration()!! - mediaPositionSeconds

                        val curTimeMinutes = (mediaPositionSeconds / 60).toInt() % 60
                        val curTimeSeconds = (mediaPositionSeconds % 60).toInt()

                        val remTimeMinutes = (durationSeconds / 60).toInt() % 60
                        val remTimeSeconds = (durationSeconds % 60).toInt()

                        if (curTimeSeconds < 10) curTime.text =
                            curTimeMinutes.toString() + ":0" + curTimeSeconds.toString()
                        else curTime.text =
                            curTimeMinutes.toString() + ":" + curTimeSeconds.toString()
                        if (remTimeSeconds < 10) remTime.text =
                            "-" + remTimeMinutes.toString() + ":0" + remTimeSeconds.toString()
                        else remTime.text =
                            "-" + remTimeMinutes.toString() + ":" + remTimeSeconds.toString()
                    }
                } else progress = 0

                // Update the progress bar and display the current value
                handler.post(Runnable {
                    progressBar.progress = progress
                })
                try {
                    Thread.sleep(100)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }

        }).start()
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    /*
    override fun onClick(view: View?) {
        when (requireView().id) {
            R.id.skipBackButton -> {R.id.skipBackButton.setImageResource(R.drawable.ic_baseline_skip_next_24); }
            R.id.skipForwardButton -> { }
            R.id.playButton -> { }
            R.id.shuffleButton -> { }
            R.id.loopButton -> { }
        }
    }
    */



}