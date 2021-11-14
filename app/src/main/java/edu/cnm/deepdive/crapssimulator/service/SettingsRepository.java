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
  private final String batchSizePrefKey;
  private final int batchSizePrefDefault;
  @SuppressWarnings("FieldCanBeLocal")
  private final OnSharedPreferenceChangeListener listener;

  private ObservableEmitter<Integer> batchSizePrefEmitter;

  public SettingsRepository(Context context) {
    Resources resources = context.getResources();
    batchSizePrefKey = resources.getString(R.string.batch_size_pref_key);
    batchSizePrefDefault = resources.getInteger(R.integer.batch_size_pref_default);
    listener = this::emitChangedPreference;
    preferences = PreferenceManager.getDefaultSharedPreferences(context);
    preferences.registerOnSharedPreferenceChangeListener(listener);
  }

  public Observable<Integer> getBatchSizePreference() {
    return Observable.create((emitter) -> {
      batchSizePrefEmitter = emitter;
      emitChangedPreference(preferences, batchSizePrefKey);
    });
  }

  private void emitChangedPreference(SharedPreferences prefs, String key) {
    if (key.equals(batchSizePrefKey)) {
      if (batchSizePrefEmitter != null && !batchSizePrefEmitter.isDisposed()) {
        int count =
            (int) Math.round(Math.pow(10, prefs.getInt(batchSizePrefKey, batchSizePrefDefault)));
        batchSizePrefEmitter.onNext(count);
      }
    }
  }

}
