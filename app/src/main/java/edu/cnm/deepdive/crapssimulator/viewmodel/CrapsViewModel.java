package edu.cnm.deepdive.crapssimulator.viewmodel;

import android.annotation.SuppressLint;
import android.app.Application;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import edu.cnm.deepdive.crapssimulator.model.Snapshot;
import edu.cnm.deepdive.crapssimulator.service.CrapsRepository;
import edu.cnm.deepdive.crapssimulator.service.SettingsRepository;
import io.reactivex.disposables.CompositeDisposable;

public class CrapsViewModel extends AndroidViewModel implements DefaultLifecycleObserver {

  private final CrapsRepository crapsRepository;
  private final SettingsRepository settingsRepository;
  private final MutableLiveData<Snapshot> snapshot;
  private final MutableLiveData<Boolean> running;
  private final MutableLiveData<Boolean> finishing;
  private final MutableLiveData<Throwable> throwable;
  private final CompositeDisposable pending;

  private int batchSize;

  public CrapsViewModel(@NonNull Application application) {
    super(application);
    crapsRepository = new CrapsRepository();
    settingsRepository = new SettingsRepository(application);
    snapshot = new MutableLiveData<>(new Snapshot());
    running = new MutableLiveData<>(false);
    finishing = new MutableLiveData<>(false);
    throwable = new MutableLiveData<>();
    pending = new CompositeDisposable();
    subscribeToPreferences();
    subscribeToSnapshots();
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
    crapsRepository.runFast(batchSize);
  }

  public void runOnce() {
    crapsRepository.runOnce(batchSize);
  }

  public void stop() {
    crapsRepository.stop();
    running.postValue(false);
  }

  public void reset() {
    crapsRepository.reset();
    snapshot.setValue(new Snapshot());
  }

  @SuppressLint("CheckResult")
  private void subscribeToPreferences() {
    //noinspection ResultOfMethodCallIgnored
    settingsRepository
        .getBatchSizePreference()
        .subscribe(
            (batchSize) -> this.batchSize = batchSize,
            this::postThrowable
        );
  }

  @SuppressLint("CheckResult")
  private void subscribeToSnapshots() {
    //noinspection ResultOfMethodCallIgnored
    crapsRepository
        .getSnapshots()
        .subscribe(
            snapshot::postValue,
            this::postThrowable
        );
  }

  private void postThrowable(Throwable throwable) {
    Log.e(getClass().getSimpleName(), throwable.getMessage(), throwable);
    this.throwable.postValue(throwable);
  }

}
