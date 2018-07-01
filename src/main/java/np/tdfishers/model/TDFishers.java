package np.tdfishers.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.IntStream;
import sim.engine.SimState;
import sim.field.grid.DoubleGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;
import sim.util.MutableInt2D;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.function.Function.identity;

public class TDFishers extends SimState {

  // Map parameters

  private int gridHeight = 25;
  public int getGridHeight() { return gridHeight; }
  public void setGridHeight(int val) { if (val > 0) gridHeight = val; }

  private int gridWidth = 25;
  public int getGridWidth() { return gridWidth; }
  public void setGridWidth(int val) { if (val > 0) gridWidth = val; }

  public final MutableInt2D portLocation = new MutableInt2D(gridWidth - 1, gridHeight / 2);

  // Biology parameters

  private double k = 5000; // a cell's carrying capacity
  public double getK() { return k; }
  public void setK(double val) { if (val > 0.0) k = val; }

  private double r = 0.7; // Malthusian growth parameter
  public double getR() { return r; }
  public void setR(double val) { if (val > 0.0) r = val; }

  private double m = 0.001; // fish spreading speed
  public double getM() { return m; }
  public void setM(double val) { if (val > 0.0) m = val; }

  // Fisher parameters

  private int numFishers = 200;
  public int getNumFishers() { return numFishers; }
  public void setNumFishers(int val) { if (val > 0) numFishers = val; }

  private double holdSize = 100.0; // max units of fish storable in boat
  public double getHoldSize() { return holdSize; }
  public void setHoldSize(double val) { if (val > 0) holdSize = val; }

  private double catchability = 0.01;  // % of the biomass caught per tow hour
  public double getCatchability() { return catchability; }
  public void setCatchability(double val) { if (val >= 0) catchability = val; }

  // Learning parameters

  private double epsilon = 0.1; // exploration rate
  public double getEpsilon() { return epsilon; }
  public void setEpsilon(double val) { if (val >= 0) epsilon = val; }

  private double alpha = 0.1; // learning rate
  public double getAlpha() { return alpha; }
  public void setAlpha(double val) { if (val >= 0) alpha = val; }

  private double gamma = 1.0; // discount factor
  public double getGamma() { return gamma; }
  public void setGamma(double val) { if (val >= 0) gamma = val; }

  private boolean rewardDelayed = true;
  public boolean isRewardDelayed() { return rewardDelayed; }
  public void setRewardDelayed(boolean val) { rewardDelayed = val; }

  // Economic parameters

  private double fishPrice = 10.0; // fish price per unit
  public double getFishPrice() { return fishPrice; }
  public void setFishPrice(double val) { if (val > 0) fishPrice = val; }

  private double gasPrice = 0.01; // the cost per unit of gas
  public double getGasPrice() { return gasPrice; }
  public void setGasPrice(double val) { if (val > 0) gasPrice = val; }

  private double gasUnitsMoving = 10.0; // gas consumed when travelling one cell
  public double getGasUnitsMoving() { return gasUnitsMoving; }
  public void setGasUnitsMoving(double val) { if (val > 0) gasUnitsMoving = val; }

  private double gasUnitsTrawling = 5.0; // gas consumed when trawling
  public double getGasUnitsTrawling() { return gasUnitsTrawling; }
  public void setGasUnitsTrawling(double val) { if (val > 0) gasUnitsTrawling = val; }

  // ---------------------------------------------------------------------------

  public DoubleGrid2D oceanGrid;
  public SparseGrid2D fishersGrid;
  public Set<FisherState> possibleStates;

  public TDFishers(long seed) {
    super(seed);
  }

  public void start() {

    super.start(); // clear out the schedule

    // create an ocean, with each cell at 1% of full carrying capacity
    oceanGrid = new DoubleGrid2D(gridWidth, gridHeight, k);
    oceanGrid.set(portLocation.x, portLocation.y, 0.0); // no fish at port!


    // create a grid to put our fishers on
    fishersGrid = new SparseGrid2D(gridWidth, gridHeight);

    // build a set of all the possible states that will be used by the fishers'
    // learning algorithm, which is just where they are on the map and whether
    // or not their hold is full.
    possibleStates =
      IntStream.range(0, fishersGrid.getWidth()).mapToObj(x ->
        IntStream.range(0, fishersGrid.getHeight()).mapToObj(y ->
          Stream.of(true, false).map(holdFull ->
            new FisherState(new Int2D(x, y), holdFull)
          )
        ).flatMap(identity()) // woohoo! flatMap in Java! It's awkward but it works...
      ).flatMap(identity()).collect(toSet());

    // Build a list of fishers and add them to the schedule
    List<Fisher> fishers = Stream
      .generate(() -> new Fisher(this))
      .limit(numFishers)
      .collect(toList());
    fishers.forEach(schedule::scheduleRepeating);

    // Also schedule a Biology object to handle fish regrowth every year
    schedule.scheduleRepeating(schedule.EPOCH, 1, new Biology(), 24.0 * 365.0);

    // And a diffuser object to move fish between cells every day
    schedule.scheduleRepeating(schedule.EPOCH, 1, new Diffuser(), 24.0);

  }

  public static void main(String[] args) {
    doLoop(TDFishers.class, args);
    System.exit(0);
  }

}
