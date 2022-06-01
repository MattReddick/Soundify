package edu.ucsb.cs.cs184.matthewreddick.soundify

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleCoroutineScope
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException
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

class SpotifyPlayer : Player,Serializable {

    private var spotifyAppRemote: SpotifyAppRemote? = null
    private val CLIENT_ID = "e01fcf6eba35472bb4aa1db36bf92863"
    /*val REDIRECT_URI = "comspotifytestsdk://callback"*/
    private val REDIRECT_URI = "edu.ucsb.cs.cs184.matthewreddick.soundify://callback"
    private var mainContext : Context ?= null
    private val TAG = "SpotifyPlayer Class"
    private val errorCallback = { throwable: Throwable -> logError(throwable) }
    constructor(context: Context, lifecycleScope : LifecycleCoroutineScope) {
        SpotifyAppRemote.setDebugMode(true)
        connect(true, context, lifecycleScope)
    }

    private fun logMessage(msg: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(mainContext, msg, duration).show()
        Log.d(TAG, msg)
    }
    override fun play() {
        var track_uri = "spotify:track:4IWZsfEkaK49itBwCTFDXQ"
        playUri(track_uri)
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
                Log.i("hereColeConnected","hereColeConnected")
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
        Log.i("hiCOLE","hi")
        assertAppRemoteConnected()
            .playerApi
            .play(uri)
            .setResultCallback { logMessage("Temp") }
            .setErrorCallback(errorCallback)
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

    fun getAccessToken() {
        val url : String = "https://accounts.spotify.com/api/token"
        /*
        val body: RequestBody = RequestBody.create(JSON, json)
        val request: Request = Request.Builder()
            .url(url)
            .post(body)
            .build()
        val response: Response = client.newCall(request).execute()
        return response.body().string()
        */
        /*
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://accounts.spotify.com/api/token")
            .header("Authorization", "Basic " + (CLIENT_ID + ':' + "5aa7b290fac74bd286c77a8b6bfdc82e"))
            .build()
        Log.i("REQUEST", request.toString())
        var response: Response
        var result : String? = null
        try {
            response = client.newCall(request).execute()
            result = response.body().string()
        } catch(e : Exception) {
            e.printStackTrace()
            Log.i("ERROR", "request failed")
        }
        if (result != null) {
            Log.i("OURTOKENINFO", result)
        }
        */
        //Log.i("OURTOKENINFO",response.body().string())
        /*
        var authOptions = {
            url: 'https://accounts.spotify.com/api/token',
            form: {
                code: code,
                redirect_uri: redirect_uri,
                grant_type: 'authorization_code'
        },
            headers: {
            'Authorization': 'Basic ' + (new Buffer(CLIENT_ID + ':' + client_secret).toString('base64'))
        },
            json: true
        };

         */
    }



    /*
    fun onConnectAndAuthorizeClicked() {
        connect(true)
    }
     */
}