package np.tdfishers.gui;

import java.awt.Color;
import javax.swing.*;
import np.tdfishers.model.TDFishers;
import sim.display.*;
import sim.engine.*;
import sim.field.grid.SparseGrid2D;
import sim.portrayal.*;
import sim.portrayal.grid.*;
import sim.portrayal.simple.*;
import sim.util.gui.SimpleColorMap;
import sim.util.Int2D;

public class TDFishersGUI extends GUIState {

  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch (Exception e) {
      // handle exception
      System.err.println("Couldn't load the system look and feel.");
    }
    TDFishersGUI gui = new TDFishersGUI();
    Console c = new Console(gui);
    c.setVisible(true);
  }
  public TDFishersGUI() { super(new TDFishers(System.currentTimeMillis())); }
  public TDFishersGUI(SimState state) { super(state); }
  public static String getName() { return "Temporal-difference-learning fishers"; }
  public Object getSimulationInspectedObject() { return state; }

  public Display2D display;
  public JFrame displayFrame;
  FastValueGridPortrayal2D oceanPortrayal = new FastValueGridPortrayal2D();
  SparseGridPortrayal2D portPortrayal = new SparseGridPortrayal2D();
  SparseGridPortrayal2D fishersPortrayal = new SparseGridPortrayal2D();

  public void start() {
    super.start();
    setupPortrayals();
  }

  public void load(SimState state) {
    super.load(state);
    setupPortrayals();
  }

  public void setupPortrayals() {
    TDFishers tdf = (TDFishers) state;
    // tell the portrayals what to portray and how to portray them
    oceanPortrayal.setField(tdf.oceanGrid);
    oceanPortrayal.setMap(new SimpleColorMap(0.0, tdf.getK(), Color.white, new Color(200, 0, 0)));
    fishersPortrayal.setField(tdf.fishersGrid);
    Color fishersColor = new Color(0, 0, 0, 50); // half-transparent black
    fishersPortrayal.setPortrayalForAll(
      new FacetedPortrayal2D(new SimplePortrayal2D[] {
        new OvalPortrayal2D(fishersColor, 0.8), // fishers are big when not moving
        new OvalPortrayal2D(fishersColor, 0.2)  // and much smaller when moving
      })
    );

    // Place a green rectangle at the port's location
    SparseGrid2D portGrid = new SparseGrid2D(tdf.getGridWidth(), tdf.getGridHeight());
    portGrid.setObjectLocation(new RectanglePortrayal2D(Color.green), new Int2D(tdf.portLocation));
    portPortrayal.setField(portGrid);

    // reschedule the displayer
    display.reset();
    display.setBackdrop(Color.white);
    // redraw the display
    display.repaint();
  }

  public void init(Controller c) {
    super.init(c);
    display = new Display2D(600,600,this);
    display.setClipping(false);
    displayFrame = display.createFrame();
    displayFrame.setTitle("TDFishers Display");
    c.registerFrame(displayFrame);
    // so the frame appears in the "Display" list
    displayFrame.setVisible(true);
    display.attach(oceanPortrayal, "Ocean");
    display.attach(portPortrayal, "Port");
    display.attach(fishersPortrayal, "Fishers");
  }

  public void quit() {
    super.quit();
    if (displayFrame != null) displayFrame.dispose();
    displayFrame = null;
    display = null;
  }

}
