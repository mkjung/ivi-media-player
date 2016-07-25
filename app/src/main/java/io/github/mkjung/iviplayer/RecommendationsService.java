/*****************************************************************************
 * RecommendationsService.java
 *****************************************************************************
 * Copyright © 2014-2015 VLC authors, VideoLAN and VideoLabs
 * Author: Geoffrey Métais
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *****************************************************************************/
package io.github.mkjung.iviplayer;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import io.github.mkjung.ivi.media.MediaDatabase;
import io.github.mkjung.ivi.media.MediaLibrary;
import io.github.mkjung.ivi.media.MediaWrapper;
import io.github.mkjung.iviplayer.gui.preferences.PreferencesActivity;
import io.github.mkjung.iviplayer.gui.video.VideoPlayerActivity;
import io.github.mkjung.iviplayer.gui.helpers.BitmapUtil;
import io.github.mkjung.ivi.util.Util;
import io.github.mkjung.ivi.util.WeakHandler;

import java.util.ArrayList;
import java.util.Collections;

public class RecommendationsService extends IntentService {

    private static final String TAG = "VLC/RecommendationsService";
    private static final int MAX_RECOMMENDATIONS = 3;

    private NotificationManager mNotificationManager;
    private MediaDatabase mMediaDatabase = MediaDatabase.getInstance();
    private Context mContext;

    public RecommendationsService() {
        super("RecommendationsService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager)
                    VLCApplication.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null && !doRecommendations()) {
            MediaLibrary.getInstance().addUpdateHandler(mHandler);
            MediaLibrary.getInstance().scanMediaItems();
        }
    }
    private void buildRecommendation(MediaWrapper movie, int id, int priority) {
        if (movie == null)
            return;

        //TODO
//        if (mBackgroundUri != movie.getBackgroundUri()) {
//            extras.putString(EXTRA_BACKGROUND_IMAGE_URL, movie.getBackgroundUri());
//        }

        // build the recommendation as a Notification object
        Notification notification = new NotificationCompat.BigPictureStyle(
                new NotificationCompat.Builder(mContext)
                        .setContentTitle(movie.getTitle())
                        .setContentText(movie.getDescription())
                        .setContentInfo(getString(R.string.app_name))
                        .setPriority(priority)
                        .setLocalOnly(true)
                        .setOngoing(true)
                        .setColor(mContext.getResources().getColor(R.color.orange800))
                        .setCategory("recommendation")
                        .setLargeIcon(BitmapUtil.getPicture(movie))
                        .setSmallIcon(R.drawable.icon)
                        .setContentIntent(buildPendingIntent(movie, id))
        ).build();

        // post the recommendation to the NotificationManager
        mNotificationManager.notify(id, notification);
    }

    private PendingIntent buildPendingIntent(MediaWrapper mediaWrapper, int id) {
        Intent intent = new Intent(mContext, VideoPlayerActivity.class);
        intent.setAction(VideoPlayerActivity.PLAY_FROM_VIDEOGRID);
        intent.putExtra(VideoPlayerActivity.PLAY_EXTRA_ITEM_LOCATION, mediaWrapper.getUri());
        intent.putExtra(VideoPlayerActivity.PLAY_EXTRA_ITEM_TITLE, mediaWrapper.getTitle());
        intent.putExtra(VideoPlayerActivity.PLAY_EXTRA_FROM_START, false);

        PendingIntent pi = PendingIntent.getActivity(this, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pi;
    }

    RecommendationsHandler mHandler = new RecommendationsHandler(this);

    private class RecommendationsHandler extends WeakHandler<RecommendationsService> {
        public RecommendationsHandler(RecommendationsService owner) {
            super(owner);
        }

        @Override
        public void handleMessage(Message msg) {
            doRecommendations();
        }
    }

    private boolean doRecommendations() {
        mNotificationManager.cancelAll();
        int id = 0;
        ArrayList<MediaWrapper> videoList = MediaLibrary.getInstance().getVideoItems();
        if (Util.isListEmpty(videoList))
            return false;
        Bitmap pic;
        Collections.shuffle(videoList);
        for (MediaWrapper mediaWrapper : videoList){
            pic = mMediaDatabase.getPicture(mediaWrapper.getUri());
            if (pic != null && pic.getByteCount() > 4 && mediaWrapper.getTime() == 0) {
                buildRecommendation(mediaWrapper, ++id, Notification.PRIORITY_DEFAULT);
            }
            if (id == MAX_RECOMMENDATIONS)
                break;
        }
        return true;
    }
}
