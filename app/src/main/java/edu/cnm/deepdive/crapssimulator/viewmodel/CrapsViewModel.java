package edu.cnm.deepdive.crapssimulator.viewmodel;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import edu.cnm.deepdive.crapssimulator.R;
import edu.cnm.deepdive.crapssimulator.model.Snapshot;
import edu.cnm.deepdive.crapssimulator.service.CrapsRepository;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class CrapsViewModel extends AndroidViewModel implements DefaultLifecycleObserver {

  private final CrapsRepository crapsRepository;
  private final MutableLiveData<Snapshot> snapshot;
  private final MutableLiveData<Boolean> running;
  private final MutableLiveData<Boolean> finishing;
  private final MutableLiveData<Throwable> throwable;
  private final CompositeDisposable pending;
  private final SharedPreferences preferences;
  private final String batchSizePrefKey;
  private final int batchSizePrefDefault;

  public CrapsViewModel(@NonNull Application application) {
    super(application);
    crapsRepository = new CrapsRepository();
    snapshot = new MutableLiveData<>(new Snapshot());
    running = new MutableLiveData<>(false);
    finishing = new MutableLiveData<>(false);
    throwable = new MutableLiveData<>();
    pending = new CompositeDisposable();
    preferences = PreferenceManager.getDefaultSharedPreferences(application);
    Resources resources = application.getResources();
    batchSizePrefKey = resources.getString(R.string.batch_size_pref_key);
    batchSizePrefDefault = resources.getInteger(R.integer.batch_size_pref_default);
  }

  public LiveData<Snapshot> getSnapshot() {
    return snapshot;
  }

  public LiveData<Boolean> getRunning() {
    return running;
  }

  public LiveData<Boolean> getFinishing() {
    return finishing;
  }

  public LiveData<Throwable> getThrowable() {
    return throwable;
  }

  public void runFast() {
    running.setValue(true);
    crapsRepository.runFast(getBatchSizePreference());
  }

  public void runOnce() {
    crapsRepository.runOnce(getBatchSizePreference());
  }

  public void stop() {
    crapsRepository.stop();
    running.postValue(false);
  }

  public void reset() {
    crapsRepository.reset();
    snapshot.setValue(new Snapshot());
  }

  @Override
  public void onStart(@NonNull LifecycleOwner owner) {
    DefaultLifecycleObserver.super.onStart(owner);
    subscribeToSnapshots();
  }

  @Override
  public void onStop(@NonNull LifecycleOwner owner) {
    DefaultLifecycleObserver.super.onStop(owner);
    pending.clear();
  }

  private void subscribeToSnapshots() {
    pending.add(
        crapsRepository
            .getSnapshots()
            .subscribe(
                snapshot::postValue,
                this::postThrowable
            )
    );
  }

  private void postThrowable(Throwable throwable) {
    Log.e(getClass().getSimpleName(), throwable.getMessage(), throwable);
    this.throwable.postValue(throwable);
  }

  private int getBatchSizePreference() {
    return (int) Math.pow(10, preferences.getInt(batchSizePrefKey, batchSizePrefDefault));
  }

}
