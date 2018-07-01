package np.tdfishers.model;

import sim.engine.Steppable;
import sim.engine.SimState;

public class Biology implements Steppable {

  public static double newBiomass(double oldBiomass, double r, double k) {
    return oldBiomass + r * (1 - (oldBiomass / k)) * oldBiomass;
  }

  public void step(SimState state) {
    TDFishers tdf = (TDFishers)state;
    double r = tdf.getR();
    double k = tdf.getK();
    double[][] a = tdf.oceanGrid.field;
    for (int i = 0; i < a.length; i++)
      for (int j=0; j < a[i].length; j++)
        a[i][j] = newBiomass(a[i][j], r, k);
  }

}
