package edu.cnm.deepdive.crapssimulator.service;

import edu.cnm.deepdive.crapssimulator.model.Round;
import edu.cnm.deepdive.crapssimulator.model.Snapshot;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;
import java.util.Random;
import java.util.concurrent.Executors;
import org.apache.commons.rng.simple.JDKRandomBridge;
import org.apache.commons.rng.simple.RandomSource;

public class CrapsRepository {

  private static final int DEFAULT_ROUNDS_PER_SNAPSHOT = 1_000;

  private final Scheduler scheduler;
  private final Round round;

  private long wins;
  private long losses;
  private int roundsPerSnapshot;
  private boolean runningFast;
  private boolean runningOnce;

  public CrapsRepository() {
    Random rng = new JDKRandomBridge(RandomSource.XO_RO_SHI_RO_128_PP, null);
    scheduler = Schedulers.from(Executors.newSingleThreadExecutor());
    round = new Round(rng);
    roundsPerSnapshot = DEFAULT_ROUNDS_PER_SNAPSHOT;
  }

  public void reset() {
    runningFast = false;
    runningOnce = false;
    wins = 0;
    losses = 0;
  }

  public Flowable<Snapshot> snapshots() {
    return Flowable
        .create((FlowableEmitter<Snapshot> emitter) -> {
          while (!emitter.isCancelled()) {
            while (runningFast) {
              play(roundsPerSnapshot);
              emitter.onNext(new Snapshot(round, wins, losses));
            }
            if (runningOnce) {
              runningOnce = false;
              play(roundsPerSnapshot);
              emitter.onNext(new Snapshot(round, wins, losses));
            }
          }
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
