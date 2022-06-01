package edu.ucsb.cs.cs184.matthewreddick.soundify.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import edu.ucsb.cs.cs184.matthewreddick.soundify.MainActivity
import edu.ucsb.cs.cs184.matthewreddick.soundify.R
import edu.ucsb.cs.cs184.matthewreddick.soundify.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var accessToken : String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        val searchBar : EditText = root.findViewById(R.id.search_bar) as EditText
        searchBar.setOnEditorActionListener(OnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Log.i("SEARCHING", searchBar.text.toString())
                (activity as MainActivity).searchSpotify(searchBar.text.toString())
                return@OnEditorActionListener true
            }
            false
        })
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}