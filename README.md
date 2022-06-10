# Soundify

CS184 Final Project <br />

Team Members: Erwan Fraisse, Cole McKim, Alon Katz, Zachary Friedland, Matthew Reddick, Katelyn Zhang <br />

Soundify is a cross platform application between Soundcloud and Spotify on Android so that people 
can queue up music from both platforms without having to alternate between both. 

# Quick Note
->In order to run the app, a user must have Spotify and their user email must be added to a group access or else the SDK will refuse to connect
->Current Songs in the Firebase (only songs that can be looked up from SoundCloud from our application): 
  Animal, Closer, Law Of Attraction, Love Letter, Nice Swing, Ninjas, Pepas, So Far Above, The Calling, The Dream

# UI Design
### Player Page (Home)
Design is supposed to resemble that of a typical music player application. The page includes: Album Art, Artist Name, Song name, Progress Bar, Play Button, PlayNext Button, PlayPrevious Button, Shuffle, and Repeat Song Button. On this page, users have the functionality associated with the listed buttons and UI mention in the previous sentence.


### Search Page
Design is supposed to resemble that of a typical search page. The search bar is at the top. We decided to use two listviews to display song results. One listview is for Spotify songs and the second is for Soundcloud Firebase songs. On this page, users are able to search for songs and add songs to the queue.  


### Queue Page
Design is supposed to resemble that of a typical queue page on music player application. On this page, users have the ability to delete songs and shuffle the order of the queue around. Users can also delete and play songs directly from the queue.

# Code Structure

We ultimately decided to use 3 main fragments to draw out the different pages of our app. The MainActivity contains the navigation view that displays the fragments. We also implemented a Song class that represents a song and a Player class that handles the playing of music and queueing of songs. 

### Song Class
This class contains the relevant information that a song should have.

- Song Title (String)
- Artist (String)
- Album Name (String)
- Audio URL (String)
- Image URL (String)
- isSpotify (Boolean)
- Duration (Int)
- ID (Int)

The isSpotify variable allows us to see if a song in the queue came from the Spotify or if it came from the Firebase Library.

### Player Class

Contains functions and variable that handle playing the music.

#### Variables:

- Current Song (Song)
- Queue (List of Songs)
- Progress Bar
- Album Art (ImageView)
- SpotifyPlayer
- FirebasePlayer (MediaPlayer)

#### Functions:

- General Play/Pause() functions
- PlayNext()
- PlayPrevious()
- Shuffle()
- Loop functionality
- Spotify SDK/Firebase functions
  - Handle Data Retrieving/error handling

Since Spotify uses an SDK to play its music and Firebase uses the android studio native MediaPlayer class, we had to create two different ways to play music and figure out a way to smoothly transition between the Spotify SDK and MediaPlayer.

To play the music from all fragments continuously we had to make the player class global in MainActivity. This allows the player to continuously run in the background even when displaying all of the fragments and switching between them. We decided to create the player object from the MainActivity so if changes need to be made to the player, each fragment is able to access the player by accessing MainActivity. 

# Data
### Spotify

We use authentication tokens to communicate with Spotify's servers. When searching for a song in Spotify's library, we make an HTTP request and we recieve a JSON formatted text that contains information about songs that are related to our search. Ex. If we make an HTTP request using the string "Justin Bieber", the JSON will contain the top 10 results relating to Justin Bieber. Once we parse the song data, we can use the Spotify SDK to play the song.

### Firebase

Due to issues with the Soundcloud’s API not being public, we had to create our own Music Library so we decided to use Firebase. In Firebase, we stored several songs which each stored the basic values of a Song Class. The links that each song has are connected to files that are saved within the Firebase storage. MediaPlayer has the option of playing songs using URLs, so when we feed the MediaPlayer a song URL, it fetches it from the Firebase storage and can then play the song! 



