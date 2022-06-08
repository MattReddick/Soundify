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
import edu.ucsb.cs.cs184.matthewreddick.soundify.R
import edu.ucsb.cs.cs184.matthewreddick.soundify.databinding.FragmentNotificationsBinding
import edu.ucsb.cs.cs184.matthewreddick.soundify.playerObject

lateinit var queueList: NotificationsFragment.CustomAdapterQueue

class NotificationsFragment : Fragment() {

    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val queueListView = binding.QueueList

        val shuffle = binding.shuffleQueue
        shuffle.setOnClickListener {
            playerObject.shuffleQueue()
            queueList = CustomAdapterQueue()
            queueListView.adapter = queueList
        }

        queueList = CustomAdapterQueue()
        queueListView.adapter = queueList
        return root
    }

    class CustomAdapterQueue: BaseAdapter() {
        override fun getCount(): Int {
            return playerObject.queue!!.size
        }

        override fun getItem(p0: Int): Any {
            return if (p0 < playerObject.queue!!.size)
                playerObject.queue!![p0]
            else {
                Log.i("index bigger than spotifySongs.size, returning last element",
                    playerObject.queue!!.size.toString())
                playerObject.queue!![playerObject.queue!!.size - 1]
            }

        }

        override fun getItemId(p0: Int): Long {
            return if (p0 < playerObject.queue!!.size)
                playerObject.queue!![p0].getId()!!.toLong()
            else {
                Log.i("index bigger than spotifySongs.size, returning last element id",
                    playerObject.queue!!.size.toString())
                playerObject.queue!![playerObject.queue!!.size - 1].getId()!!.toLong()
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
                removeFromQueueBtn = myViewSpotify.findViewById(R.id.removeFromQueue)
            }
            removeFromQueueBtn.setOnClickListener {
                if (playerObject.queue!!.size > 0) playerObject.queue!!.remove(playerObject.queue!![p0])
                queueList.notifyDataSetChanged()
            }

            songTextSoundCloud.setOnClickListener {
                if (playerObject.queue!!.size > 0) {
                    val tempIndex = playerObject.queue!!.indexOf(playerObject.queue!![p0])
                    playerObject.currentSongIndex = tempIndex - 1
                    playerObject.playNextFromQueue()
                }
            }

            songTextSpotify.setOnClickListener {
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