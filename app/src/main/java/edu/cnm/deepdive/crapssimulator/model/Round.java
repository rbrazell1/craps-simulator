package edu.cnm.deepdive.crapssimulator.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Round {

  private static final int NUM_DICE = 2;
  private static final int NUM_SIDES = 6;

  private final Random rng;
  private final List<Roll> rolls;

  private State state;
  private boolean win;

  public Round(Random rng) {
    this.rng = rng;
    rolls = new LinkedList<>();
  }

  public boolean play() {
    state = State.initial();
    win = false;
    int point = 0;
    boolean firstRoll = true;
    do {
      Roll roll = new Roll(rng, NUM_DICE, NUM_SIDES);
      state = state.next(roll, point);
      if (firstRoll) {
        if (state == State.POINT) {
          point = roll.getValue();
        }
        firstRoll = false;
      }
    } while (!state.isTerminal());
    win = (state == State.WIN);
    return win;
  }

  public List<Roll> getRolls() {
    return Collections.unmodifiableList(rolls);
  }

  public State getState() {
    return state;
  }

  public boolean isWin() {
    return win;
  }

  public enum State {
    COME_OUT {

      @Override
      protected boolean isTerminal() {
        return false;
      }

      @Override
      protected State next(Roll roll, int ignoredPoint) {
        State state;
        switch (roll.getValue()) {
          case 2:
          case 3:
          case 12:
            state = LOSS;
            break;
          case 7:
          case 11:
            state = WIN;
            break;
          default:
            state = POINT;
            break;
        }
        return state;
      }

    },
    POINT {

      @Override
      protected boolean isTerminal() {
        return false;
      }

      @Override
      protected State next(Roll roll, int point) {
        State state;
        int value = roll.getValue();
        if (value == point) {
          state = WIN;
        } else if (value == 7) {
          state = LOSS;
        } else {
          state = this;
        }
        return state;
      }

    },
    WIN,
    LOSS;

    static State initial() {
      return COME_OUT;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    protected boolean isTerminal() {
      return true;
    }

    protected State next(Roll roll, int point) {
      throw new IllegalStateException();
    }

  }

}
