package io.github.mkjung.iviplayer.extensions;

import android.net.Uri;

import io.github.mkjung.ivi.media.MediaWrapper;
import org.videolan.vlc.extensions.api.VLCExtensionItem;

public class Utils {

    public static MediaWrapper mediawrapperFromExtension(VLCExtensionItem vlcItem) {
                MediaWrapper media = new MediaWrapper(Uri.parse(vlcItem.link));
                media.setDisplayTitle(vlcItem.title);
                if (vlcItem.type != VLCExtensionItem.TYPE_OTHER_FILE)
                    media.setType(vlcItem.type);
        return media;

    }
}
