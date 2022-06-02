package edu.ucsb.cs.cs184.matthewreddick.soundify

import android.media.Image
import android.util.Log
import kotlin.math.log


class Song(
    title: String,
    artist: String,
    album: String,
    imageUrl: String,
    audioUrl: String,
    duration: Int,
    id: Int
) {
    private var title :String? = title
    private var artist: String? = artist
    private var album: String? = album
    private var imageUrl: String? = imageUrl
    private var audioUrl: String? = audioUrl
    private var duration: Int? = duration
    private var id: Int? = id

    override fun toString(): String {
        var output = ""
        output += "Title: $title\n"
        output += "Artist: $artist\n"
        output += "Album: $album\n"
        output += "ImageUrl: $imageUrl\n"
        output += "AudioUrl: $audioUrl\n"
        output += "Duration: $duration\n"
        output += "ID: $id\n"
        return output
    }

    fun getTitle() : String? {
        return title
    }

    fun setTitle(input: String?) {
        title = input
    }

    fun getArtist() : String? {
        return artist
    }

    fun setArtist(input: String?) {
        artist = input
    }

    fun getAlbum() : String? {
        return album
    }

    fun setAlbum(input: String?) {
        album = input
    }

    fun getImageUrl() : String? {
        return imageUrl
    }

    fun setImageUrl(input: String?) {
        imageUrl = input
    }

    fun getAudioUrl() : String? {
        return audioUrl
    }

    fun setAudioUrl(input: String?) {
        audioUrl = input
    }

    fun getDuration() : Int? {
        return duration
    }

    fun setDuration(input: Int?) {
        duration = input
    }

    fun getId() : Int? {
        return id
    }

    fun setId(input: Int?) {
        id = input
    }

}