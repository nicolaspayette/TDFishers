package np.tdfishers.experiments

import np.tdfishers.model.TDFishers

import scala.collection.Iterator
import scala.util.control.Breaks._

/**
  This is just a convenience object for running experiments. In addition
  to the number of runs and steps in each run, it takes a lambda for
  initialising the simulation and another one for performing side-effects
  at each time step (presumably for writing to a file).
*/
object Runner {
  def apply(runs: Int, steps: Int, init: TDFishers => Unit)(f: (TDFishers, Int) => Unit): Unit = {
    val sims = Iterator.continually(new TDFishers(System.currentTimeMillis()))
    for ((tdf, run) <- sims.zipWithIndex.take(runs)) {
      println(s"Run $run - $tdf")
      tdf.start()
      init(tdf)
      breakable {
        do {
          if (!tdf.schedule.step(tdf)) break
          f(tdf, run)
        } while (tdf.schedule.getSteps < steps)
      }
      tdf.finish()
    }
  }
}
