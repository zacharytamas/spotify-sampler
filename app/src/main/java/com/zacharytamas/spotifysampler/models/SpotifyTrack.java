package com.zacharytamas.spotifysampler.models;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Track;


/**
 * Created by zacharytamas on 6/6/15.
 */
public class SpotifyTrack implements Parcelable {

    public String name;
    public String albumName;
    public String albumImageUrl;
    public String albumImageThumbUrl;
    public String previewUrl;
    public String artistName;

    public SpotifyTrack(Track track) {
        this.name = track.name;
        this.albumName = track.album.name;

        if (track.album.images.size() > 0) {
            // assuming the first one is always the largest
            albumImageUrl = track.album.images.get(0).url;
            // also get smallest to save bandwidth
            albumImageThumbUrl = track.album.images.get(track.album.images.size() - 1).url;
            // There should be first() and last(). Oh, Java. This will fallback to grabbing
            // the largest if there only happens to be one (1 - 1 is 0 index)
        }

        this.previewUrl = track.preview_url;
        // TODO Should probably format this artist array as a comma-separated string
        this.artistName = track.artists.get(0).name;
    }

    public SpotifyTrack(Parcel parcel) {
        this.name = parcel.readString();
        this.albumName = parcel.readString();
        this.albumImageUrl = parcel.readString();
        this.albumImageThumbUrl = parcel.readString();
        this.previewUrl = parcel.readString();
        this.artistName = parcel.readString();
    }

    // I really don't like how Java does this. :(
    // Took a while to get this to work but the Android docs on this were helpful
    // http://developer.android.com/reference/android/os/Parcelable.html
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public SpotifyTrack createFromParcel(Parcel parcel) {
            return new SpotifyTrack(parcel);
        }

        public SpotifyTrack[] newArray(int size) {
            return new SpotifyTrack[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.name);
        parcel.writeString(this.albumName);
        parcel.writeString(this.albumImageUrl);
        parcel.writeString(this.albumImageThumbUrl);
        parcel.writeString(this.previewUrl);
        parcel.writeString(this.artistName);
    }
}
