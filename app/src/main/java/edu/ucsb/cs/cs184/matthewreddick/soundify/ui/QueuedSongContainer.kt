package edu.ucsb.cs.cs184.matthewreddick.soundify.ui

import android.widget.Button
import android.widget.TextView
import edu.ucsb.cs.cs184.matthewreddick.soundify.Song
import edu.ucsb.cs.cs184.matthewreddick.soundify.MainActivity

class QueuedSongContainer {
    private var song: Song? = null
    private var btnUp: Button? = null
    private var btnDown: Button? = null
    private var label: TextView? = null

    constructor(){
        btnUp!!.setOnClickListener {
            // TODO:
            //      Move song up
        }
        btnDown!!.setOnClickListener {
            // TODO:
            //      Move song down
        }




    }

}