/*
 * *************************************************************************
 *  StartActivity.java
 * **************************************************************************
 *  Copyright © 2015 VLC authors and VideoLAN
 *  Author: Geoffrey Métais
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston MA 02110-1301, USA.
 *  ***************************************************************************
 */

package io.github.mkjung.iviplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import org.videolan.libvlc.util.AndroidUtil;

import io.github.mkjung.iviplayer.PlaybackService;
import io.github.mkjung.ivi.media.MediaWrapper;
import io.github.mkjung.iviplayer.gui.AudioPlayerContainerActivity;
import io.github.mkjung.iviplayer.gui.MainActivity;
import io.github.mkjung.iviplayer.gui.tv.MainTvActivity;
import io.github.mkjung.iviplayer.gui.tv.audioplayer.AudioPlayerActivity;
import io.github.mkjung.iviplayer.gui.video.VideoPlayerActivity;
import io.github.mkjung.iviplayer.media.MediaUtils;
import io.github.mkjung.ivi.util.AndroidDevices;
import io.github.mkjung.ivi.util.Util;
import io.github.mkjung.ivi.VLCApplication;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

public class StartActivity extends Activity {

    public final static String TAG = "VLC/StartActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        if (intent != null && TextUtils.equals(intent.getAction(), Intent.ACTION_VIEW) && intent.getData() != null) {
            intent.setDataAndType(getUri(intent), intent.getType());
            if (intent.getType() != null && intent.getType().startsWith("video")) {
                startActivity(intent.setClass(this, VideoPlayerActivity.class));
            } else {
                final Context context = VLCApplication.getAppContext();
                final MediaWrapper media = new MediaWrapper(intent.getData());

                if (media == null)
                    return;

                if (media.getType() == MediaWrapper.TYPE_VIDEO) {
                    VideoPlayerActivity.start(context, media.getUri(), media.getTitle());
                } else {
                    new BaseCallBack(context) {
                        @Override
                        public void onConnected(PlaybackService service) {
                            service.load(media);
                            mClient.disconnect();
                        }
                    };
                }
            }
        } else if (intent != null && TextUtils.equals(intent.getAction(), AudioPlayerContainerActivity.ACTION_SHOW_PLAYER)) {
            startActivity(new Intent(this, VLCApplication.showTvUi() ? AudioPlayerActivity.class : MainActivity.class));
        } else {
            startActivity(new Intent(this, VLCApplication.showTvUi() ? MainTvActivity.class : MainActivity.class));
        }

        finish();
    }

    private Uri getUri(Intent intent) {
        Uri mUri = null, data = intent.getData();
        if (data != null && TextUtils.equals(data.getScheme(), "content")) {
            // Mail-based apps - download the stream to a temporary file and play it
            if(data.getHost().equals("com.fsck.k9.attachmentprovider")
                    || data.getHost().equals("gmail-ls")) {
                InputStream is = null;
                OutputStream os = null;
                try {
                    Cursor cursor = getContentResolver().query(data,
                            new String[]{MediaStore.MediaColumns.DISPLAY_NAME}, null, null, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        String filename = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
                        cursor.close();
                        Log.i(TAG, "Getting file " + filename + " from content:// URI");

                        is = getContentResolver().openInputStream(data);
                        os = new FileOutputStream(AndroidDevices.EXTERNAL_PUBLIC_DIRECTORY + "/Download/" + filename);
                        byte[] buffer = new byte[1024];
                        int bytesRead = 0;
                        while((bytesRead = is.read(buffer)) >= 0) {
                            os.write(buffer, 0, bytesRead);
                        }
                        mUri = AndroidUtil.PathToUri(AndroidDevices.EXTERNAL_PUBLIC_DIRECTORY + "/Download/" + filename);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Couldn't download file from mail URI");
                    return data;
                } finally {
                    Util.close(is);
                    Util.close(os);
                }
            }
            // Media or MMS URI
            else if (TextUtils.equals(data.getAuthority(), "media")){
                mUri = MediaUtils.getContentMediaUri(data);
            } else {
                ParcelFileDescriptor inputPFD;
                try {
                    inputPFD = getContentResolver().openFileDescriptor(data, "r");
                    if (AndroidUtil.isHoneycombMr1OrLater())
                        mUri = AndroidUtil.LocationToUri("fd://" + inputPFD.getFd());
                    else {
                        String fdString = inputPFD.getFileDescriptor().toString();
                        mUri = AndroidUtil.LocationToUri("fd://" + fdString.substring(15, fdString.length() - 1));
                    }
//                    Cursor returnCursor =
//                            getContentResolver().query(data, null, null, null, null);
//                    if (returnCursor != null) {
//                        if (returnCursor.getCount() > 0) {
//                            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
//                            if (nameIndex > -1) {
//                                returnCursor.moveToFirst();
//                                title = returnCursor.getString(nameIndex);
//                            }
//                        }
//                        returnCursor.close();
//                    }
                } catch (FileNotFoundException e) {
                    Log.e(TAG, "Couldn't understand the intent");
                    return data;
                }
            }
        } else
            mUri = data;
        return mUri;
    }

    private static abstract class BaseCallBack implements PlaybackService.Client.Callback {
        protected PlaybackService.Client mClient;

        private BaseCallBack(Context context) {
            mClient = new PlaybackService.Client(context, this);
            mClient.connect();
        }

        protected BaseCallBack() {}

        @Override
        public void onDisconnected() {}
    }
}
