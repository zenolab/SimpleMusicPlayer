package com.zeno.lbs.simplemp3player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import static com.zeno.lbs.simplemp3player.MyApplication.mp;

//http://developer.alexanderklimov.ru/android/theory/audiomanager.php
public class NoisyAudioStreamReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
            // Pause the playback

            // Получаем доступ к менеджеру звуков
            AudioManager manager = (AudioManager) new MainActivity().getSystemService(Context.AUDIO_SERVICE);

            if (manager.isBluetoothA2dpOn()) {
                // через Bluetooth
               // mp.pause();
            } else if (manager.isSpeakerphoneOn()) {
                // через динамик телефона
                mp.pause();
            } else if (manager.isWiredHeadsetOn()) {
                // Устарело в API 14
                // через проводные наушники
                mp.pause();
            } else {
                // может стоит выключить звук?
                mp.pause();
            }

        }
    }


    private IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);

    private void startPlayback() {
       // registerReceiver(myNoisyAudioStreamReceiver(), intentFilter);
    }

    private void stopPlayback() {
       // unregisterReceiver(myNoisyAudioStreamReceiver);
    }
}
