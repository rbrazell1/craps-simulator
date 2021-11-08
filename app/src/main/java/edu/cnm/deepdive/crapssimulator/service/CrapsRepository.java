package edu.cnm.deepdive.crapssimulator.service;

import edu.cnm.deepdive.crapssimulator.model.Round;
import edu.cnm.deepdive.crapssimulator.model.Snapshot;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.Scheduler;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.schedulers.Schedulers;
import java.util.Random;
import java.util.concurrent.Executors;
import org.apache.commons.rng.simple.JDKRandomBridge;
import org.apache.commons.rng.simple.RandomSource;

public class CrapsRepository {

  private static final int ROUNDS_PER_SNAPSHOT = 50_000;

  private final Scheduler scheduler;
  private final Round round;

  private int wins;
  private int losses;

  public CrapsRepository() {
    Random rng = new JDKRandomBridge(RandomSource.XO_RO_SHI_RO_128_PP, null);
    scheduler = Schedulers.from(Executors.newSingleThreadExecutor());
    round = new Round(rng);
  }

  public void reset() {
    wins = 0;
    losses = 0;
  }

  public Flowable<Snapshot> simulateFast(int count) {
    return Flowable
        .create((FlowableEmitter<Snapshot> emitter) -> {
          do {
            play(count);
            if (!emitter.isCancelled()) {
              emitter.onNext(new Snapshot(round, wins, losses));
            } else {
              break;
            }
          } while (true);
        }, BackpressureStrategy.LATEST)
        .subscribeOn(scheduler);
  }

  public Single<Snapshot> simulateBatch(int count) {
    return Single
        .create((SingleEmitter<Snapshot> emitter) -> {
          play(count);
          if (!emitter.isDisposed()) {
            emitter.onSuccess(new Snapshot(round, wins, losses));
          }
        })
        .subscribeOn(scheduler);
  }

  public synchronized Single<Snapshot> getSnapshot() {
    return Single
        .just(new Snapshot(round, wins, losses))
        .subscribeOn(scheduler);
  }

  private synchronized void play(int count) {
    for (int i = 0; i < count; i++) {
      if (round.play()) {
        wins++;
      } else {
        losses++;
      }
    }
  }

}
