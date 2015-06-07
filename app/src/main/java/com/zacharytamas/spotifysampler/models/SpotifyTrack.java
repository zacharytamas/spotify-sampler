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

    }

    public SpotifyTrack(Parcel parcel) {
        this.name = parcel.readString();
        this.albumName = parcel.readString();
        this.albumImageUrl = parcel.readString();
        this.albumImageThumbUrl = parcel.readString();
    }

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
    }
}
