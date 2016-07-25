/*
 * *************************************************************************
 *  PreferencesPerformances.java
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

package io.github.mkjung.iviplayer.gui.preferences;

import android.content.SharedPreferences;

import io.github.mkjung.iviplayer.R;
import io.github.mkjung.ivi.util.VLCInstance;

public class PreferencesPerformances extends BasePreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
    @Override
    protected int getXml() {
        return R.xml.preferences_perf;
    }

    @Override
    protected int getTitleId() {
        return R.string.performance_prefs_category;
    }

    @Override
    public void onStart() {
        super.onStart();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key){
            case "chroma_format":
            case "deblocking":
            case "enable_frame_skip":
            case "enable_time_stretching_audio":
                VLCInstance.restart();
                if (getActivity() != null )
                    ((PreferencesActivity)getActivity()).restartMediaPlayer();
        }
    }
}
