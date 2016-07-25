package io.github.mkjung.iviplayer.gui.video;

import android.os.Message;

import io.github.mkjung.ivi.media.MediaLibrary;
import io.github.mkjung.ivi.media.MediaWrapper;
import io.github.mkjung.ivi.interfaces.IVideoBrowser;
import io.github.mkjung.ivi.util.WeakHandler;

public class VideoListHandler extends WeakHandler<IVideoBrowser> {

    public VideoListHandler(IVideoBrowser owner) {
        super(owner);
    }

    @Override
    public void handleMessage(Message msg) {
        IVideoBrowser owner = getOwner();
        if(owner == null) return;

        switch (msg.what) {
            case MediaLibrary.UPDATE_ITEM:
                owner.updateItem((MediaWrapper)msg.obj);
                break;
            case MediaLibrary.MEDIA_ITEMS_UPDATED:
                owner.updateList();
                break;
        }
    }
};