package edu.ucsb.cs.cs184.matthewreddick.soundify.ui.dashboard

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.ColorFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import edu.ucsb.cs.cs184.matthewreddick.soundify.MainActivity
import edu.ucsb.cs.cs184.matthewreddick.soundify.R
import edu.ucsb.cs.cs184.matthewreddick.soundify.Player
import edu.ucsb.cs.cs184.matthewreddick.soundify.databinding.FragmentDashboardBinding


class DashboardFragment : Fragment(){

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var playerObject : Player
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


        if(getActivity() != null){
            val i : Intent? = getActivity()?.getIntent()
            if (i != null) {
                 playerObject = i.getSerializableExtra("playerObject") as Player
            }
            //Log.i("HomeFragment","accessToken")
        }


        val playBtn : ImageButton = root.findViewById(R.id.playButton) as ImageButton
        playBtn.setOnClickListener() {
            if(playerObject != null)
                playerObject.onPlayPauseButtonClicked()
        }

        //val progressBar = binding.progressBar
        //progressBar.progressTintList= ColorStateList.valueOf(Color.GREEN)
        //can also do this to probably inverse the color of shuffle and loop
        //so a user knows when they are active


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