package edu.ucsb.cs.cs184.matthewreddick.soundify

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException
import com.spotify.protocol.types.*
import com.squareup.picasso.Picasso
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.Serializable
import java.util.*
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class Player : Serializable {
    var inQueueFragment = false
    private var loop = false
    private var currentSong: Song? = null
    var currentSongIndex: Int = -1
    var queue: ArrayList<Song>? = null
    var mediaPlayer: MediaPlayer? = null
    private var length:Int = 0
    private var imageView : ImageView? = null
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private val CLIENT_ID = "e01fcf6eba35472bb4aa1db36bf92863"
    private val REDIRECT_URI = "edu.ucsb.cs.cs184.matthewreddick.soundify://callback"
    private var mainContext : Context ?= null
    private val TAG = "SpotifyPlayer Class"
    private var trackWasStartedSpotify = false

    fun getLoop(): Boolean {
        return loop
    }

    fun setLoop() {
        loop = !loop
    }

    private val errorCallback = { throwable: Throwable -> logError(throwable) }
    constructor(context: Context, lifecycleScope : LifecycleCoroutineScope) {
        SpotifyAppRemote.setDebugMode(true)
        connect(true, context, lifecycleScope)
        queue = ArrayList<Song>()
        mediaPlayer = MediaPlayer()
        mediaPlayer!!.setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
    }

    private fun playSpotify(track_uri : String) {
        playUri(track_uri)
        started = true
    }

    fun shuffleQueue() {
        if(currentSongIndex < ((queue?.size) as Int) - 1) {
            Collections.shuffle(queue?.subList(currentSongIndex + 1, (queue?.size!!)))
            Log.i("QUEUE", queue.toString())
        }
        else {
            Toast.makeText(mainContext, "Add songs to queue", Toast.LENGTH_SHORT).show()
        }
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
                spotifyAppRemote?.playerApi?.subscribeToPlayerState()?.setEventCallback {
                    val hasEnded : Boolean = handleTrackEnded(it)
                    Log.i("Has Ended", hasEnded.toString())
                    if(hasEnded) {
                        assertAppRemoteConnected().let {
                            it.playerApi
                                .playerState
                                .setResultCallback { _ ->
                                    it.playerApi
                                        .pause()
                                        .setErrorCallback(errorCallback)

                                }
                        }
                        playNext()
                    }
                    if (currentSong?.isSpotify() == true) updateTrackCoverArtSpotify(it)
                }
                Log.i("PlayerClass","Connected")
            } catch (error: Throwable) {
                disconnect()
            }
        }
    }
    private fun onPlayPauseButtonClicked() {
        Log.i("PAUSING1","here")
        assertAppRemoteConnected().let {
            it.playerApi
                .playerState
                .setResultCallback { playerState ->
                    if (playerState.isPaused) {
                        it.playerApi
                            .resume()
                            .setErrorCallback(errorCallback)
                    } else {
                        Log.i("PAUSING","here")
                        it.playerApi
                            .pause()
                            .setErrorCallback(errorCallback)
                    }
                }
        }
    }

    fun getCurrentPosition(): Int {
        var result = 0
        assertAppRemoteConnected().let {
            it.playerApi
                .playerState
                .setResultCallback { playerState ->
                    result = playerState.playbackPosition.toInt()
                }
        }
        Thread.sleep(100)
        return result
    }

    fun reloadCoverArt() {
        Log.i("PAUSING1","here")
        assertAppRemoteConnected().let {
            it.playerApi
                .playerState
                .setResultCallback { playerState ->
                    updateTrackCoverArtSpotify(playerState)
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
        throw SpotifyDisconnectedException()
    }

    fun addToQueue(songToAdd: Song) {
        queue?.add(songToAdd)
    }

    private fun playUri(uri: String) {
        Log.i("Here",uri)
        assertAppRemoteConnected()
            .playerApi
            .play(uri)
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

    private fun setTrackWasStarted(playerState: PlayerState) {
        val duration = playerState.track.duration
        val position = playerState.playbackPosition
        val isPlaying = !playerState.isPaused
        if (!trackWasStartedSpotify && position > 0 && duration > 0 && isPlaying) {
            trackWasStartedSpotify = true
        }
    }

    private suspend fun connectToAppRemote(showAuthView: Boolean): SpotifyAppRemote =
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

    fun getCurrentSong() : Song? {
        return currentSong
    }

    fun pausePlay() {
        Log.i("QUEUE",queue.toString())
        if (currentSongIndex == -1) {
            playNext()
        }
        else {
            if (currentSong?.isSpotify() == true) {
                onPlayPauseButtonClicked()
                return
            }
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                length = mediaPlayer?.currentPosition!!
            } else {
                mediaPlayer?.seekTo(length)
                mediaPlayer?.start()
            }
        }
    }

    fun playNext() {
        if (currentSongIndex == -1 || !loop) currentSongIndex += 1
        if(currentSongIndex >= queue?.size!!) {
            currentSongIndex -= 1
            Toast.makeText(mainContext, "Queue a song!", Toast.LENGTH_SHORT).show()
            assertAppRemoteConnected().let {
                it.playerApi
                    .playerState
                    .setResultCallback { playerState ->
                        if(!playerState.isPaused) {
                            it.playerApi
                                .pause()
                                .setErrorCallback(errorCallback)
                        }
                    }
            }
            if (mediaPlayer!!.isPlaying){
                mediaPlayer!!.stop()
            }
            return
        }
        currentSong = queue?.get(currentSongIndex)
        if (currentSong != null) {
            assertAppRemoteConnected().let {
                it.playerApi
                    .playerState
                    .setResultCallback { playerState ->
                        if(!playerState.isPaused) {
                            it.playerApi
                                .pause()
                                .setErrorCallback(errorCallback)
                        }
                    }
            }
            if (mediaPlayer!!.isPlaying) {
                mediaPlayer!!.stop()
                Thread.sleep(100)
            }
            if (currentSong!!.isSpotify()) {
                if (currentSong != null) {
                    playSpotify(currentSong!!.getUri())
                }
                else {
                    Toast.makeText(mainContext, "Queue a song!", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                if (currentSong != null) {
                    playSoundCloud(currentSong!!.getUri())
                    if (!inQueueFragment) updateTrackCoverArtSoundCloud()
                }
                else {
                    Toast.makeText(mainContext, "Queue a song!", Toast.LENGTH_SHORT).show()
                }
            }
        }
        length = 0
    }

    private fun playSoundCloud(track_uri : String) {
        try {
            mediaPlayer!!.reset()
            mediaPlayer!!.setDataSource(track_uri)
            mediaPlayer!!.prepare()
            mediaPlayer!!.start()
            mediaPlayer!!.setOnCompletionListener {
                playNext()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        started = true
    }

    fun setImageView(newImageView : ImageView) {
        imageView = newImageView
    }

    private fun updateTrackCoverArtSpotify(playerState: PlayerState) {
        if(imageView != null) {
            assertAppRemoteConnected()
                .imagesApi
                .getImage(playerState.track.imageUri, Image.Dimension.LARGE)
                .setResultCallback { bitmap ->
                    imageView!!.setImageBitmap(bitmap)
                }
        }
    }

    fun playPrevious() {
        if(currentSongIndex <= 0){
            if(currentSongIndex == -1) {
                Toast.makeText(mainContext, "Queue a song!", Toast.LENGTH_SHORT).show()
            }
            else {
                currentSongIndex += -1
                playNext()
            }
        }
        else {
            if (!loop) currentSongIndex += -2
            playNext()
        }

    }

    fun updateTrackCoverArtSoundCloud() {
        if(currentSong?.getImageUrl()?.length == 0){
            imageView!!.setImageBitmap(null)
            imageView!!.destroyDrawingCache()
        }
        else {
            val url = ((currentSong?.getImageUrl()))
            Picasso.get().load(url).into(imageView)
        }
    }
}

