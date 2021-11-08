package edu.cnm.deepdive.crapssimulator.model;

import edu.cnm.deepdive.crapssimulator.model.Round.State;
import java.util.Collections;
import java.util.List;

public class Snapshot {

  private final List<Roll> rolls;
  private final long wins;
  private final long losses;
  private final State state;
  private final boolean win;

  public Snapshot() {
    rolls = Collections.emptyList();
    wins = 0;
    losses = 0;
    state = Round.State.initial();
    win = false;
  }

  public Snapshot(Round round, long wins, long losses) {
    rolls = round.getRolls();
    this.wins = wins;
    this.losses = losses;
    state = round.getState();
    win = round.isWin();
  }

  public List<Roll> getRolls() {
    return rolls;
  }

  public long getWins() {
    return wins;
  }

  public long getLosses() {
    return losses;
  }

  public long getRounds() {
    return wins + losses;
  }

  public State getState() {
    return state;
  }

  public boolean isWin() {
    return win;
  }

}
