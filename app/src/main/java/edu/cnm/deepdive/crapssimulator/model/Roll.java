package edu.cnm.deepdive.crapssimulator.model;

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public class Roll {

  private final int[] dice;
  private final int value;

  public Roll(Random rng, int numDice, int numSides) {
    dice = IntStream
        .generate(() -> 1 + rng.nextInt(numSides))
        .limit(numDice)
        .toArray();
    value = IntStream
        .of(dice)
        .sum();
  }

  public int[] getDice() {
    return Arrays.copyOf(dice, dice.length);
  }

  public int getValue() {
    return value;
  }

}
