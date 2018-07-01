package np.tdfishers.model;

import sim.engine.Steppable;
import sim.engine.SimState;
import static sim.field.grid.Grid2D.BOUNDED;

public class Diffuser implements Steppable {

  /** run diffusion between two cells in a matrix */
  public static void diffuse(double m, double[][] a, int x1, int y1, int x2, int y2) {
    double transfer = m * (a[x1][y1] - a[x2][y2]);
    a[x1][y1] -= transfer;
    a[x2][y2] += transfer;
  }

  /**
    Run diffusion for all cells in a matrix.
    Each cell is paired with the cell on the right and the cell below
    (with boundary checks). This should give us each pair of von Neumann
    neighbours exactly once.

    The order in which we do this (left to right, top to bottom) introduces
    some bias. I should probably use some kind of synchronous updating
    but I'm going quick and dirty here...
  */
  public static void diffuseAll(double m, double[][] a) {
    for (int x = 0; x < a.length; x++)
      for (int y = 0; y < a[x].length; y++) {
        if (x + 1 < a.length   ) diffuse(m, a, x, y, x + 1, y);
        if (y + 1 < a[x].length) diffuse(m, a, x, y, x, y + 1);
      }
  }

  public void step(SimState state) {
    TDFishers tdf = (TDFishers)state;
    diffuseAll(tdf.getM(), tdf.oceanGrid.field);
  }

}
