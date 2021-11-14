package edu.cnm.deepdive.crapssimulator.controller;

import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SeekBarPreference;
import edu.cnm.deepdive.crapssimulator.R;

public class SettingsFragment extends PreferenceFragmentCompat {

  @Override
  public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
    setPreferencesFromResource(R.xml.preferences, rootKey);
    setupSeekBar(R.string.batch_size_pref_key, R.string.batch_size_pref_summary);
  }

  private void setupSeekBar(int keyResId, int summaryResId) {
    SeekBarPreference pref = findPreference(getString(keyResId));
    //noinspection ConstantConditions
    pref.setOnPreferenceChangeListener((preference, newValue) ->
        updateSummary(preference, (Integer) newValue, summaryResId));
    updateSummary(pref, pref.getValue(), summaryResId);
  }

  private boolean updateSummary(Preference preference, Integer newValue, int resId) {
    int quantity = (int) Math.round(Math.pow(10, newValue));
    String quantityString = getResources().getQuantityString(R.plurals.round_quantity, quantity);
    preference.setSummary(getString(resId, quantity, quantityString));
    return true;
  }

}
