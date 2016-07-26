package io.github.mkjung.iviplayer.gui.tv;

import android.app.Activity;
import android.os.Bundle;

import io.github.mkjung.iviplayer.R;
import io.github.mkjung.ivi.gui.helpers.UiTools;

public class AboutActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_main);
//        UiTools.fillAboutView(getWindow().getDecorView().getRootView());
        TvUtil.applyOverscanMargin(this);
    }
}
