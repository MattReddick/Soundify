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


}