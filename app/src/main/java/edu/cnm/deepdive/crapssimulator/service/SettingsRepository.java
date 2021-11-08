package edu.cnm.deepdive.crapssimulator.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Resources;
import androidx.preference.PreferenceManager;
import edu.cnm.deepdive.crapssimulator.R;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;

public class SettingsRepository {

  private final SharedPreferences preferences;
  private final String playOncePrefKey;
  private final String playFastPrefKey;
  private final int playOncePrefDefault;
  private final int playFastPrefDefault;
  @SuppressWarnings("FieldCanBeLocal")
  private final OnSharedPreferenceChangeListener listener;

  private ObservableEmitter<Integer> playOncePrefEmitter;
  private ObservableEmitter<Integer> playFastPrefEmitter;

  public SettingsRepository(Context context) {
    Resources resources = context.getResources();
    playOncePrefKey = resources.getString(R.string.play_once_pref_key);
    playFastPrefKey = resources.getString(R.string.play_fast_pref_key);
    playOncePrefDefault = resources.getInteger(R.integer.play_once_pref_default);
    playFastPrefDefault = resources.getInteger(R.integer.play_fast_pref_default);
    listener = this::emitChangedPreference;
    preferences = PreferenceManager.getDefaultSharedPreferences(context);
    preferences.registerOnSharedPreferenceChangeListener(listener);
  }

  public Observable<Integer> getPlayOncePreference() {
    return Observable.create((emitter) -> {
      playOncePrefEmitter = emitter;
      emitChangedPreference(preferences, playOncePrefKey);
    });
  }

  public Observable<Integer> getPlayFastPreference() {
    return Observable.create((emitter) -> {
      playFastPrefEmitter = emitter;
      emitChangedPreference(preferences, playFastPrefKey);
    });
  }

  private void emitChangedPreference(SharedPreferences prefs, String key) {
    if (key.equals(playOncePrefKey)) {
      if (playOncePrefEmitter != null && !playOncePrefEmitter.isDisposed()) {
        int count =
            (int) Math.round(Math.pow(10, prefs.getInt(playOncePrefKey, playOncePrefDefault)));
        playOncePrefEmitter.onNext(count);
      }
    } else if (key.equals(playFastPrefKey)) {
      if (playFastPrefEmitter != null && !playFastPrefEmitter.isDisposed()) {
        int count =
            (int) Math.round(Math.pow(10, prefs.getInt(playFastPrefKey, playFastPrefDefault)));
        playFastPrefEmitter.onNext(count);
      }
    }
  }

}
