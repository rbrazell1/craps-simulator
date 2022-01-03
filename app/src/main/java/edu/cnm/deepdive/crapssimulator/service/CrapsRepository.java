package edu.cnm.deepdive.crapssimulator.service;

import edu.cnm.deepdive.crapssimulator.model.Round;
import edu.cnm.deepdive.crapssimulator.model.Snapshot;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.apache.commons.rng.simple.JDKRandomBridge;
import org.apache.commons.rng.simple.RandomSource;

public class CrapsRepository {

  private static final int DEFAULT_ROUNDS_PER_SNAPSHOT = 1_000;
  private static final long SLEEP_INTERVAL = 100;

  private final Scheduler scheduler;
  private final Round round;

  private ScheduledExecutorService executor;
  private ScheduledFuture<?> future;
  private long wins;
  private long losses;
  private int roundsPerSnapshot;
  private boolean runningFast;
  private boolean runningOnce;

  public CrapsRepository() {
    Random rng = new JDKRandomBridge(RandomSource.XO_RO_SHI_RO_128_PP, null);
    scheduler = Schedulers.single();
    round = new Round(rng);
    roundsPerSnapshot = DEFAULT_ROUNDS_PER_SNAPSHOT;
  }

  public void reset() {
    runningFast = false;
    runningOnce = false;
    wins = 0;
    losses = 0;
  }

  public Flowable<Snapshot> getSnapshots() {
    return Flowable
        .create((FlowableEmitter<Snapshot> emitter) -> {
          executor = Executors.newSingleThreadScheduledExecutor();
          future = executor.scheduleWithFixedDelay(() -> {
            if (!emitter.isCancelled()) {
              while (runningFast) {
                play(roundsPerSnapshot);
                emitter.onNext(new Snapshot(round, wins, losses));
              }
              if (runningOnce) {
                runningOnce = false;
                play(roundsPerSnapshot);
                emitter.onNext(new Snapshot(round, wins, losses));
              }
            } else {
              future.cancel(true);
            }
          }, 0, SLEEP_INTERVAL, TimeUnit.MILLISECONDS);
        }, BackpressureStrategy.LATEST)
        .subscribeOn(scheduler);
  }

  public void runFast(int roundsPerSnapshot) {
    this.roundsPerSnapshot = roundsPerSnapshot;
    runningFast = true;
  }

  public void runOnce(int rounds) {
    roundsPerSnapshot = rounds;
    runningOnce = true;
  }

  public void stop() {
    runningFast = false;
  }

  public Single<Snapshot> getSnapshot() {
    return Single
        .just(new Snapshot(round, wins, losses))
        .subscribeOn(scheduler);
  }

  private void play(int count) {
    long wins = 0;
    long losses = 0;
    for (int i = 0; i < count; i++) {
      if (round.play()) {
        wins++;
      } else {
        losses++;
      }
    }
    this.wins += wins;
    this.losses += losses;
  }

}
