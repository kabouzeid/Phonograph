package com.kabouzeid.gramophone.service.notification;

import com.kabouzeid.gramophone.service.MusicService;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */

public interface PlayingNotification {
    void init(MusicService service);

    void update();

    void stop();
}
