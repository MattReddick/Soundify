# Soundify

CS184 Final Project <br />

Team Members: Erwan Fraisse, Cole McKim, Alon Katz, Zachary Friedland, Matthew Reddick, Katelyn Zhang <br />

Soundify is a cross platform application between Soundcloud and Spotify on Android so that people 
can queue up music from both platforms without having to alternate between both. 



# UI Design
## Player (Home Page)
- Design is supposed to resemble that
- -  

From a UI design perspective, we ultimately decided to make the UI look like a typical music player, like one that you would find on either Apple Music or Spotify. That way, new users could easily pick up on how to use the app. Even our search and queue pages are easy to learn how to use since their designs are familiar to that of other applications users likely used in the past.

From a code design perspective. We ultimately decided to use 3 main fragments to draw out the different pages of our app. The Main Activity contains the navigation view that displays the fragments. We also implemented a Song class which contains the necessary values that a song would need to have such as its title, artist name, duration, url string linked to audio data, as well as a variable telling us if the song came from Spotify or our Soundcloud Firebase Library.

The Player class is the class that actually plays the music. The Player has a variable to keep track of the current song playing and a list of the current queue. Since Spotify uses an SDK to play its music and Firebase uses the android studio native MediaPlayer class, we had to create two different ways to play music and figure out a way to smoothly transition between the Spotify SDK and MediaPlayer. That is why the song class contains the isSpotify variable, to help us identify which player to use. 

Due to issues with Soundcloud’s API not being public, we had to create our own Music Library so we decided to do that using Firebase. In Firebase, we stored several songs which each stored the basic values of a Song Class. The links that each song has are connected to files that are saved within the Firebase storage. MediaPlayer has the option of playing songs using URLs, so when we feed MediaPlayer the URL, it fetches it from the Firebase storage. 

In order to be able to play the music from all fragments continuously we had to make it global. The most global class in the application is MainActivity. It is continuously running in the background since it is the class that is displaying the fragments. So we decided to create the player object from the MainActivity, that way it will also run continuously in the background. If changes need to be made to the player, each fragment is able to access the player by accessing MainActivity. 

