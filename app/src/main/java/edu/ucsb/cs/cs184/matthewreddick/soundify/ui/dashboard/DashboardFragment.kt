package edu.ucsb.cs.cs184.matthewreddick.soundify.ui.dashboard

import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
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
    private val binding get() = _binding!!
    private val handler = Handler()
    private var progress: Int = 0
    private lateinit var progressBar: ProgressBar
    private lateinit var curTime: TextView
    private lateinit var remTime: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(DashboardViewModel::class.java)

        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        playerObject.setImageView(root.findViewById(R.id.albumCoverImage))
        val titleText : TextView = root.findViewById(R.id.songTitleText)
        val artistText : TextView = root.findViewById(R.id.artistText)

        if (started) {
            if (playerObject.queue!!.size > 0) {
                val tmpSong : Song? = playerObject.getCurrentSong()
                titleText.text = tmpSong?.getTitle()
                artistText.text = tmpSong?.getArtist()
                if (playerObject.getCurrentSong()!!.isSpotify()) {
                    playerObject.reloadCoverArt()
                } else {
                    playerObject.updateTrackCoverArtSoundCloud()
                }
            }
        }

        //here changing the imageButton color so the user knows
        //when a song is on repeat or when he can skip to the next/previous songs
        val loopBtn : ImageButton = root.findViewById(R.id.loopButton) as ImageButton
        loopBtn.setOnClickListener {
            playerObject.setLoop()
            changeColorLoopBtn(loopBtn)
        }
        changeColorLoopBtn(loopBtn)

        val playBtn : ImageButton = root.findViewById(R.id.playButton) as ImageButton
        playBtn.setOnClickListener {
            playerObject.pausePlay()
            updateTitleArtistTextView(titleText, artistText)
            playPause = !playPause
            changeImagePausePlay(playBtn)
        }
        changeImagePausePlay(playBtn)

        val skipForwardBtn : ImageButton = root.findViewById(R.id.skipForwardButton) as ImageButton
        skipForwardBtn.setOnClickListener {
            playerObject.playNext()
            updateTitleArtistTextView(titleText, artistText)
            playPause = true
            playBtn.setImageResource(R.drawable.ic_baseline_pause_24)
        }

        val shuffleBtn : ImageButton = root.findViewById(R.id.shuffleButton) as ImageButton
        shuffleBtn.setOnClickListener {
            playerObject.shuffleQueue()
        }

        val skipPreviousBtn : ImageButton = root.findViewById(R.id.skipBackButton) as ImageButton
        skipPreviousBtn.setOnClickListener {
            playerObject.playPrevious()
            updateTitleArtistTextView(titleText, artistText)
            playPause = true
            playBtn.setImageResource(R.drawable.ic_baseline_pause_24)
        }

        progress = binding.progressBar.progress
        progressBar = binding.progressBar
        curTime = binding.currTimeStampText
        remTime = binding.remainingTimeStampText

        curTime.text = "0:00"
        remTime.text = "-0:00"

        Thread(Runnable {
            var mediaOrSpotify = 0
            while (progress < 100) {
                if (playerObject.getCurrentSong() != null) {
                    mediaOrSpotify = if (!playerObject.getCurrentSong()!!.isSpotify() && playerObject.mediaPlayer != null) {
                        playerObject.mediaPlayer!!.currentPosition

                    } else {
                        playerObject.getCurrentPosition()
                    }
                    progress = (mediaOrSpotify) /
                            (playerObject.getCurrentSong()!!.getDuration()!! * 10)

                    val mediaPositionSeconds =
                        (mediaOrSpotify / 1000)
                    val durationSeconds =
                        playerObject.getCurrentSong()!!.getDuration()!! - mediaPositionSeconds

                    val curTimeMinutes = (mediaPositionSeconds / 60) % 60
                    val curTimeSeconds = (mediaPositionSeconds % 60)

                    val remTimeMinutes = (durationSeconds / 60) % 60
                    val remTimeSeconds = (durationSeconds % 60)

                    if (curTimeSeconds < 10) curTime.text =
                        curTimeMinutes.toString() + ":0" + curTimeSeconds.toString()
                    else curTime.text =
                        curTimeMinutes.toString() + ":" + curTimeSeconds.toString()
                    if (remTimeSeconds < 10) remTime.text =
                        "-" + remTimeMinutes.toString() + ":0" + remTimeSeconds.toString()
                    else remTime.text =
                        "-" + remTimeMinutes.toString() + ":" + remTimeSeconds.toString()
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

    private fun changeImagePausePlay(playBtn: ImageButton) {
        if (playPause) {
            playBtn.setImageResource(R.drawable.ic_baseline_pause_24)
        } else {
            playBtn.setImageResource(R.drawable.ic_baseline_play_arrow_24)
        }
    }

    private fun changeColorLoopBtn(loopBtn: ImageButton) {
        if (playerObject.getLoop()) {
            loopBtn.background.setColorFilter(resources.getColor(R.color.green), PorterDuff.Mode.MULTIPLY)
        } else {
            loopBtn.background.setColorFilter(resources.getColor(R.color.purple_200), PorterDuff.Mode.MULTIPLY)
        }
    }

    private fun updateTitleArtistTextView(titleText: TextView, artistText: TextView) {
        val tmpSong : Song? = playerObject.getCurrentSong()
        if(tmpSong != null) {
            titleText.text = tmpSong.getTitle()
            artistText.text = tmpSong.getArtist()
        } else {
            Toast.makeText(context, "No songs on queue!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}