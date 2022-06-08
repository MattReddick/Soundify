package edu.ucsb.cs.cs184.matthewreddick.soundify

class Song(
    title: String,
    artist: String,
    album: String,
    imageUrl: String,
    audioUrl: String,
    duration: Int,
    id: Int,
    isSpotify: Boolean
) {
    private var title :String? = title
    private var artist: String? = artist
    private var album: String? = album
    private var imageUrl: String? = imageUrl
    private var audioUrl: String = audioUrl
    private var duration: Int? = duration
    private var id: Int? = id
    private var isSpotify: Boolean = isSpotify

    override fun toString(): String {
        var output = ""
        output += "Title: $title\n"
        output += "Artist: $artist\n"
        output += "Album: $album\n"
        output += "ImageUrl: $imageUrl\n"
        output += "AudioUrl: $audioUrl\n"
        output += "Duration: $duration\n"
        output += "ID: $id\n"
        output += "IsSpotify: $isSpotify\n"
        return output
    }

    fun getTitle() : String? {
        return title
    }

    fun getArtist() : String? {
        return artist
    }

    fun getAlbum() : String? {
        return album
    }

    fun getImageUrl() : String? {
        return imageUrl
    }

    fun getUri() : String {
        return audioUrl
    }

    fun getDuration() : Int? {
        return duration
    }

    fun getId() : Int? {
        return id
    }

    fun isSpotify() : Boolean {
        return isSpotify
    }
}