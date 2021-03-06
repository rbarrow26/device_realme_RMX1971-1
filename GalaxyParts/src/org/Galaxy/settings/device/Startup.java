/*
* Copyright (C) 2013 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/
package org.Galaxy.settings.device;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.text.TextUtils;
import androidx.preference.PreferenceManager;
import org.Galaxy.settings.device.ModeSwitch.GameModeSwitch;
import org.Galaxy.settings.device.kcal.DisplayCalibration;
import org.Galaxy.settings.device.DiracUtils;

public class Startup extends BroadcastReceiver {

    private static final String TAG = "BootReceiver";
    private static final String ONE_TIME_TUNABLE_RESTORE = "hardware_tunable_restored";

    private final String HEADPHONE_GAIN_PATH = "/sys/kernel/sound_control/headphone_gain";
    private final String MICROPHONE_GAIN_PATH = "/sys/kernel/sound_control/mic_gain";

    private void restore(String file, boolean enabled) {
        if (file == null) {
            return;
        }
        if (enabled) {
            Utils.writeValue(file, "1");
        }
    }

    private void restore(String file, String value) {
        if (file == null) {
            return;
        }
        Utils.writeValue(file, value);
    }

    @Override
    public void onReceive(final Context context, final Intent bootintent) {
        boolean enabled = false;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences (context);
        enabled = sharedPrefs.getBoolean (GalaxyParts.KEY_GAME_SWITCH, false);
        restore (GameModeSwitch.getFile ( ), enabled);
        org.Galaxy.settings.device.doze.Utils.checkDozeService (context);
        context.startService (new Intent (context, DisplayCalibration.class));

        int gain = Settings.Secure.getInt (context.getContentResolver ( ),
                GalaxyParts.PREF_HEADPHONE_GAIN, 4);
        FileUtils.setValue (HEADPHONE_GAIN_PATH, gain + " " + gain);
        FileUtils.setValue (MICROPHONE_GAIN_PATH, Settings.Secure.getInt (context.getContentResolver ( ),
                GalaxyParts.PREF_MICROPHONE_GAIN, 0));
        FileUtils.setValue (GalaxyParts.EARPIECE_GAIN_PATH, Settings.Secure.getInt (context.getContentResolver ( ),
                GalaxyParts.PREF_EARPIECE_GAIN, 0));
        context.startService (new Intent (context, DiracService.class));

        enabled = sharedPrefs.getBoolean (GalaxyParts.PREF_KEY_FPS_INFO, false);
        if (enabled) {
            context.startService (new Intent (context, FPSInfoService.class));
            enabled = sharedPrefs.getBoolean (GalaxyParts.KEY_CHARGING_SWITCH, false);
            if (enabled) {
                context.startService (new Intent (context, SmartChargingService.class));
            }
        }

    }    
    private boolean hasRestoredTunable(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getBoolean(ONE_TIME_TUNABLE_RESTORE, false);
    }

    private void setRestoredTunable(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putBoolean(ONE_TIME_TUNABLE_RESTORE, true).apply();
    }
}
