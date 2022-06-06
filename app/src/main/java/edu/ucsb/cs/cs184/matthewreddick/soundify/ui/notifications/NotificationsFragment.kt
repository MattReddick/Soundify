package edu.ucsb.cs.cs184.matthewreddick.soundify.ui.notifications

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import edu.ucsb.cs.cs184.matthewreddick.soundify.R
import edu.ucsb.cs.cs184.matthewreddick.soundify.databinding.FragmentNotificationsBinding
import edu.ucsb.cs.cs184.matthewreddick.soundify.playerObject


lateinit var queueList: NotificationsFragment.customAdapterQueue

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val queueListView = binding.QueueList

        val shuffle = binding.shuffleQueue
        shuffle.setOnClickListener {
            playerObject.shuffleQueue()
            queueList = customAdapterQueue()
            queueListView.adapter = queueList
        }

        queueList = customAdapterQueue()
        queueListView.adapter = queueList
        return root
    }
    class customAdapterQueue: BaseAdapter() {
        override fun getCount(): Int {
            return playerObject.queue!!.size
        }

        override fun getItem(p0: Int): Any {
            if (p0 < playerObject.queue!!.size)
                return playerObject.queue!![p0]
            else {
                Log.i("index bigger than spotifySongs.size, returning last element",
                    playerObject.queue!!.size.toString())
                return playerObject.queue!![playerObject.queue!!.size - 1]
            }

        }

        override fun getItemId(p0: Int): Long {
            //added getters and setters in Song class
            if (p0 < playerObject.queue!!.size)
                return playerObject.queue!![p0].getId()!!.toLong()
            else {
                Log.i("index bigger than spotifySongs.size, returning last element id",
                    playerObject.queue!!.size.toString())
                return playerObject.queue!![playerObject.queue!!.size - 1].getId()!!.toLong()
            }
        }

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            val li = LayoutInflater.from(p2!!.context)
            val myViewSoundCloud = li.inflate(R.layout.soundcloud_queue_item, null)
            val myViewSpotify = li.inflate(R.layout.spotify_queue_item, null)

            val songTextSoundCloud: TextView = myViewSoundCloud.findViewById(R.id.songTextName)
            val songTextSpotify: TextView = myViewSpotify.findViewById(R.id.songTextName)
            songTextSoundCloud.text = playerObject.queue!![p0].getTitle() + " - " + playerObject.queue!![p0].getArtist()
            songTextSpotify.text = playerObject.queue!![p0].getTitle() + " - " + playerObject.queue!![p0].getArtist()

            var removeFromQueueBtn = myViewSoundCloud.findViewById<ImageButton>(R.id.removeFromQueue)
            if (playerObject.queue!![p0].isSpotify()) {
                removeFromQueueBtn = myViewSpotify.findViewById<ImageButton>(R.id.removeFromQueue)
            }
            removeFromQueueBtn.setOnClickListener() {
                if (playerObject.queue!!.size > 0) playerObject.queue!!.remove(playerObject.queue!![p0])
                queueList.notifyDataSetChanged()
            }

            songTextSoundCloud.setOnClickListener() {
                //need to update image view, song, and artist part of media player, should use play next
                if (playerObject.queue!!.size > 0) {
                    val tempIndex = playerObject.queue!!.indexOf(playerObject.queue!![p0])
                    playerObject.currentSongIndex = tempIndex - 1
                    playerObject.playNextFromQueue()
                }
            }

            songTextSpotify.setOnClickListener() {
                ////need to update image view, song, and artist part of media player, use play next
                if (playerObject.queue!!.size > 0) {
                    val tempIndex = playerObject.queue!!.indexOf(playerObject.queue!![p0])
                    playerObject.currentSongIndex = tempIndex - 1
                    playerObject.playNextFromQueue()
                }
            }
            if (playerObject.queue!![p0].isSpotify()) {
                return myViewSpotify
            }
            return myViewSoundCloud
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}