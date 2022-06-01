package edu.ucsb.cs.cs184.matthewreddick.soundify

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.*
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import kotlinx.coroutines.launch
import java.io.Serializable
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


class Player : Serializable {

    private var currentSong: Song? = null
    private var queue: ArrayList<Song>? = null
    private var mediaPlayer: MediaPlayer? = null

    private var spotifyAppRemote: SpotifyAppRemote? = null
    private val CLIENT_ID = "e01fcf6eba35472bb4aa1db36bf92863"
    /*val REDIRECT_URI = "comspotifytestsdk://callback"*/
    private val REDIRECT_URI = "edu.ucsb.cs.cs184.matthewreddick.soundify://callback"
    private var mainContext : Context ?= null
    private val TAG = "SpotifyPlayer Class"

    private var trackWasStartedSpotify = false


    private fun playSoundCloud(track_uri : String) {

    }

    private val errorCallback = { throwable: Throwable -> logError(throwable) }
    constructor(context: Context, lifecycleScope : LifecycleCoroutineScope) {
        SpotifyAppRemote.setDebugMode(true)
        connect(true, context, lifecycleScope)
    }

    private fun logMessage(msg: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(mainContext, msg, duration).show()
        Log.d(TAG, msg)
    }

    fun playSpotify(track_uri : String) {
        playUri(track_uri)
        //trackWasStarted = true
    }

    private fun logError(throwable: Throwable) {

        Toast.makeText(mainContext, "test string", Toast.LENGTH_SHORT).show()
        Log.e(TAG, "", throwable)
    }

    private fun connect(showAuthView: Boolean, context: Context, lifecycleScope : LifecycleCoroutineScope) {

        SpotifyAppRemote.disconnect(spotifyAppRemote)
        mainContext = context
        lifecycleScope.launch {
            try {
                spotifyAppRemote = connectToAppRemote(showAuthView)
                onConnected()
                spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback {
                    val hasEnded : Boolean = handleTrackEnded(it)

                }
                Log.i("PlayerClass","Connected")
            } catch (error: Throwable) {
                disconnect()
                //logError(error)
            }
        }
    }
    fun onPlayPauseButtonClicked() {
        assertAppRemoteConnected().let {
            it.playerApi
                .playerState
                .setResultCallback { playerState ->
                    if (playerState.isPaused) {
                        it.playerApi
                            .resume()
                            .setResultCallback { logMessage("tmp") }
                            .setErrorCallback(errorCallback)
                    } else {
                        it.playerApi
                            .pause()
                            .setResultCallback { logMessage("tmp") }
                            .setErrorCallback(errorCallback)
                    }
                }
        }

    }
    private fun disconnect() {
        SpotifyAppRemote.disconnect(spotifyAppRemote)
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
            .setResultCallback { logMessage("Temp") }
            .setErrorCallback(errorCallback)
    }

    private fun handleTrackEnded (playerState: PlayerState) : Boolean {
        setTrackWasStarted(playerState)

        val position = playerState.playbackPosition

        val isPaused = playerState.isPaused

        val hasEnded = trackWasStartedSpotify && isPaused && position == 0L

        if (hasEnded) {

            trackWasStartedSpotify = false

        }
        return hasEnded
    }
    /*
    fun onSubscribedToPlayerStateButtonClicked(notUsed: View) {
        playerStateSubscription = cancelAndResetSubscription(playerStateSubscription)

        binding.currentTrackLabel.visibility = View.VISIBLE
        binding.subscribeToPlayerStateButton.visibility = View.INVISIBLE

        playerStateSubscription = assertAppRemoteConnected()
            .playerApi
            .subscribeToPlayerState()
            .setEventCallback(playerStateEventCallback)
            .setLifecycleCallback(
                object : Subscription.LifecycleCallback {
                    override fun onStart() {
                        logMessage("Event: start")
                    }

                    override fun onStop() {
                        logMessage("Event: end")
                    }
                })
            .setErrorCallback {
                binding.currentTrackLabel.visibility = View.INVISIBLE
                binding.subscribeToPlayerStateButton.visibility = View.VISIBLE
            } as Subscription<PlayerState>
    }

    private fun <T : Any?> cancelAndResetSubscription(subscription: Subscription<T>?): Subscription<T>? {
        return subscription?.let {
            if (!it.isCanceled) {
                it.cancel()
            }
            null
        }
    }

     */

    private fun setTrackWasStarted(playerState: PlayerState) {
        val duration = playerState.track.duration
        val position = playerState.playbackPosition
        val isPlaying = !playerState.isPaused

        if (!trackWasStartedSpotify && position > 0 && duration > 0 && isPlaying) {

            trackWasStartedSpotify = true

        }
    }

    //Be sure to pass context

    private suspend fun connectToAppRemote(showAuthView: Boolean): SpotifyAppRemote? =
        suspendCoroutine { cont: Continuation<SpotifyAppRemote> ->
            SpotifyAppRemote.connect(
                mainContext?.applicationContext,
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
    fun onConnected() {

    }

}
