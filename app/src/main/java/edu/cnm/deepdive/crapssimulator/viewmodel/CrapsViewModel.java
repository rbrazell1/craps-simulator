package edu.cnm.deepdive.crapssimulator.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import edu.cnm.deepdive.crapssimulator.model.Snapshot;
import edu.cnm.deepdive.crapssimulator.service.CrapsRepository;
import edu.cnm.deepdive.crapssimulator.service.SettingsRepository;
import io.reactivex.disposables.Disposable;

public class CrapsViewModel extends AndroidViewModel implements DefaultLifecycleObserver {

  private final CrapsRepository crapsRepository;
  private final SettingsRepository settingsRepository;
  private final MutableLiveData<Snapshot> snapshot;
  private final MutableLiveData<Boolean> running;
  private final MutableLiveData<Boolean> finishing;
  private final MutableLiveData<Throwable> throwable;

  private int batchSize;
  private int updateSize;
  private Disposable simulation;

  public CrapsViewModel(@NonNull Application application) {
    super(application);
    crapsRepository = new CrapsRepository();
    settingsRepository = new SettingsRepository(application);
    snapshot = new MutableLiveData<>(new Snapshot());
    running = new MutableLiveData<>(false);
    finishing = new MutableLiveData<>(false);
    throwable = new MutableLiveData<>();
    subscribeToPreferences();
  }

  public LiveData<Snapshot> getSnapshot() {
    return snapshot;
  }

  public MutableLiveData<Boolean> getRunning() {
    return running;
  }

  public LiveData<Boolean> getFinishing() {
    return finishing;
  }

  public LiveData<Throwable> getThrowable() {
    return throwable;
  }

  public void simulateFast() {
    running.postValue(true);
    simulation = crapsRepository
        .simulateFast(updateSize)
        .subscribe(
            this.snapshot::postValue,
            this::postThrowable
        );
  }

  public void simulateBatch() {
    simulation = crapsRepository
        .simulateBatch(batchSize)
        .subscribe(
            this.snapshot::postValue,
            this::postThrowable
        );
  }

  @SuppressLint("CheckResult")
  public void stop() {
    finishing.postValue(true);
    if (simulation != null) {
      simulation.dispose();
      simulation = null;
    }
    //noinspection ResultOfMethodCallIgnored
    crapsRepository
        .getSnapshot()
        .subscribe(this::finishSimulation);
  }

  public void reset() {
    stop();
    crapsRepository.reset();
    snapshot.setValue(new Snapshot());
  }

  @Override
  public void onStop(@NonNull LifecycleOwner owner) {
    DefaultLifecycleObserver.super.onStop(owner);
    stop();
  }

  @SuppressLint("CheckResult")
  private void subscribeToPreferences() {
    //noinspection ResultOfMethodCallIgnored
    settingsRepository
        .getPlayOncePreference()
        .subscribe((batchSize) -> this.batchSize = batchSize);
    //noinspection ResultOfMethodCallIgnored
    settingsRepository
        .getPlayFastPreference()
        .subscribe((updateSize) -> this.updateSize = updateSize);
  }

  private void finishSimulation(Snapshot snapshot) {
    this.snapshot.postValue(snapshot);
    //noinspection ConstantConditions
    if (running.getValue()) {
      running.postValue(false);
    }
    finishing.postValue(false);
  }

  private void postThrowable(Throwable throwable) {
    Log.e(getClass().getSimpleName(), throwable.getMessage(), throwable);
    this.throwable.postValue(throwable);
  }

}
