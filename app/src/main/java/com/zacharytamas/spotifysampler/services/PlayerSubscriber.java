package com.zacharytamas.spotifysampler.services;

/**
 * Created by zacharytamas on 6/7/15.
 */
public interface PlayerSubscriber {
    void onPlayerEvent(String event, PlayerService service);

}
