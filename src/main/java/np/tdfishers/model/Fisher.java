package np.tdfishers.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.function.DoubleSupplier;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import np.tdfishers.learning.Learning;
import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.portrayal.simple.OvalPortrayal2D;
import sim.util.Int2D;
import sim.util.Valuable;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toCollection;

public class Fisher implements Steppable, Valuable {

  // keep a ref to the SimState; not sure 100% legit, but makes things easier for now...
  private final TDFishers tdf;

  // the following members are for the Valuable interface, used to select portrayal:
  public static final double IS_NOT_MOVING = 0.0;
  public static final double IS_MOVING = 1.0;
  private double objectVal = IS_NOT_MOVING;
  public double doubleValue() { return objectVal; }

  private final Learning learning; // this is our TD-learning module

  private double fishCarried = 0.0;
  public double getFishCarried() { return fishCarried; }
  public void setFishCarried(double val) { if (val > 0.0) fishCarried = val; }

  public boolean isAtPort() {
    return tdf.fishersGrid.getObjectLocation(this).equals(tdf.portLocation);
  }

  public boolean isHoldFull() {
    return fishCarried >= tdf.getHoldSize();
  }

  public double getDistanceToPort() {
    return tdf.fishersGrid.getObjectLocation(this).distance(tdf.portLocation);
  }

  // What follows are the three main actions that a fisher can take:
  // trawling (`trawl()`) or moving (`move()`) when at sea, or emptying
  // the hold (`emptyHold()`) when at port.

  private double emptyHold() {
    objectVal = IS_NOT_MOVING; // for portaying purposes
    // when rewards are delayed, we get the reward for all the fish caught
    double reward = tdf.isRewardDelayed() ? fishCarried * tdf.getFishPrice() : 0.0;
    fishCarried = 0.0; // empty our hold
    return reward;
  };

  private double trawl() {
    objectVal = IS_NOT_MOVING; // for portaying purposes
    double gasCost = tdf.getGasUnitsTrawling() * tdf.getGasPrice();
    if (isHoldFull()) {
      // if our hold is full, we're just wasting gas
      return 0.0 - gasCost;
    } else {
      Int2D loc = tdf.fishersGrid.getObjectLocation(this);
      double fishHere = tdf.oceanGrid.get(loc.x, loc.y);
      // catch as much fish as we can given our space in hold
      double spaceInHold = tdf.getHoldSize() - fishCarried;
      double fishCaught = min(fishHere * tdf.getCatchability(), spaceInHold);
      // remove fish from ocean and move it to hold
      tdf.oceanGrid.set(loc.x, loc.y, fishHere - fishCaught);
      fishCarried += fishCaught;
      // unless rewards are delayed, we get a reward for fish caught
      double reward = tdf.isRewardDelayed() ? 0.0 : fishCaught * tdf.getFishPrice();
      // and we substract the cost of fuel from the reward
      return reward - gasCost;
    }
  };

  private double move(int dx, int dy) {
    objectVal = IS_MOVING; // for portaying purposes
    // we move to a new location by applying the deltas
    SparseGrid2D g = tdf.fishersGrid;
    Int2D loc = g.getObjectLocation(this);
    // we calculate the new coordinates, bounded to the grid
    int x = max(min(loc.x + dx, g.getWidth()  - 1), 0);
    int y = max(min(loc.y + dy, g.getHeight() - 1), 0);
    g.setObjectLocation(this, x, y);
    // and we get a negative reward from the cost of fuel
    // (we pay that cost even if we're bumping against the edge of the world...)
    double gasCost = tdf.getGasUnitsMoving() * tdf.getGasPrice();
    return 0.0 - gasCost;
  };

  /**
    This is a higher-order function that returns a lambda that can be used
    as an action for the learning algorithm. When a change of coordinates is
    involved, it returns a straight-up "move" action. When the coordinates don't
    change, the action is conditional on the ship being at port or not.
  */
  private DoubleSupplier makeAction(int deltaX, int deltaY) {
    // are we staying put?
    return (deltaX == 0 && deltaY == 0) ?
        // if we are, what happens depends if we're at port or not
        () -> (isAtPort() ? emptyHold() : trawl()) :
        // if we're not staying put, we're moving!
        () -> move(deltaX, deltaY);
  };

  /**
    This generates a list of actions including staying put and moving to
    each of the 8 possible neighbouring cells.
  */
  private List<DoubleSupplier> makeActions() {
    return Stream.of(
      makeAction(-1, -1), makeAction(0, -1), makeAction(1, -1),
      makeAction(-1,  0), makeAction(0,  0), makeAction(1,  0),
      makeAction(-1,  1), makeAction(0,  1), makeAction(1,  1)
    ).collect(toCollection(ArrayList::new)); // ArrayList because we need O(1) indexed access
  }

  /** Returns the current state as a new instance of the FisherState case class */
  public FisherState getFisherState() {
    Int2D loc = tdf.fishersGrid.getObjectLocation(this);
    return new FisherState(loc, isHoldFull());
  }

  public Fisher(final SimState state) {
    tdf = (TDFishers)state;
    tdf.fishersGrid.setObjectLocation(this, new Int2D(tdf.portLocation));
    learning = new Learning(
      tdf.possibleStates, makeActions(), this::getFisherState,
      tdf.getEpsilon(), tdf.getAlpha(), tdf.getGamma(), tdf.random
    );
  }

  public void step(final SimState state) {
    // let the learning algorithm handle everything...
    learning.act();
  }

}
