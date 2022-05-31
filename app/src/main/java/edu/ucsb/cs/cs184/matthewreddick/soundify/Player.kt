package edu.ucsb.cs.cs184.matthewreddick.soundify

import android.content.Context
import android.media.MediaPlayer
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.*
import com.spotify.android.appremote.api.ContentApi
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException
import kotlinx.coroutines.launch
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


open class Player {

    private var currentSong: Song? = null
    private var queue: ArrayList<Song>? = null
    private var mediaPlayer: MediaPlayer? = null

    open fun play(){

    }

    fun pause(){

    }

    fun skip(){

    }

    fun deleteSong(index:Int){

    }

    fun moveSongUp(){

    }

    fun moveSongDown(){

    }
}

class SpotifyPlayer : Player {

    private var spotifyAppRemote: SpotifyAppRemote? = null
    private val CLIENT_ID = "e01fcf6eba35472bb4aa1db36bf92863"
    /*val REDIRECT_URI = "comspotifytestsdk://callback"*/
    private val REDIRECT_URI = "edu.ucsb.cs.cs184.matthewreddick.soundify://callback"
    constructor(context: Context, lifecycleScope : LifecycleCoroutineScope) {
        connect(true, context, lifecycleScope)
    }
    override fun play() {
        var track_uri = "spotify:track:4IWZsfEkaK49itBwCTFDXQ"
        playUri(track_uri)
    }

    private fun connect(showAuthView: Boolean, context: Context, lifecycleScope : LifecycleCoroutineScope) {

        SpotifyAppRemote.disconnect(spotifyAppRemote)
        lifecycleScope.launch {
            try {
                spotifyAppRemote = connectToAppRemote(showAuthView, context)
            } catch (error: Throwable) {
                disconnect()
                //logError(error)
            }
        }
    }
    private fun disconnect() {
        SpotifyAppRemote.disconnect(spotifyAppRemote)
    }
    private fun onConnecting() {
        /*
        binding.connectButton.apply {
            //isEnabled = false
            text = getString(R.string.connecting)
        }
        binding.connectAuthorizeButton.apply {
            //isEnabled = false
            text = getString(R.string.connecting)
        }
        */
    }

    private fun assertAppRemoteConnected(): SpotifyAppRemote {
        spotifyAppRemote?.let {
            if (it.isConnected) {
                return it
            }
        }
        //Log.e(TAG, getString(R.string.err_spotify_disconnected))
        throw SpotifyDisconnectedException()
    }

    private fun playUri(uri: String) {
        assertAppRemoteConnected()
            .playerApi
            .play(uri)
    }
    //Be sure to pass context
    private suspend fun connectToAppRemote(showAuthView: Boolean, context: Context): SpotifyAppRemote? =
        suspendCoroutine { cont: Continuation<SpotifyAppRemote> ->
            SpotifyAppRemote.connect(
                context?.applicationContext,
                ConnectionParams.Builder(CLIENT_ID)
                    .setRedirectUri(REDIRECT_URI)
                    .showAuthView(showAuthView)
                    .build(),
                object : Connector.ConnectionListener {
                    override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                        cont.resume(spotifyAppRemote)
                    }

                    override fun onFailure(error: Throwable) {
                        cont.resumeWithException(error)
                    }
                })
        }
    /*
    fun onConnectAndAuthorizeClicked() {
        connect(true)
    }
     */
}