package np.tdfishers.experiments

import np.tdfishers.model.TDFishers
import np.tdfishers.model.Fisher
import np.tdfishers.model.Fisher.IS_NOT_MOVING

import java.io.File
import java.io.PrintWriter

import scala.collection.Iterator
import scala.util.control.Breaks._
import scala.collection.JavaConverters._


object FishingDistanceExperiment extends App {
  val writer = new PrintWriter(new File("experiments/port_distances.csv"))
  writer.println(s"reward_delayed,step,distance")
  for (rewardDelayed <- List(true, false)) {
    val init = { tdf: TDFishers => tdf.setRewardDelayed(rewardDelayed) }
    Runner(100, 300000, init) { (tdf, run) =>
      val steps = tdf.schedule.getSteps
      if (steps % (24 * 14) == 0) { // only sample periodically
        val dists = tdf.fishersGrid.getAllObjects.iterator.asScala // grab the bag's iterator
          .collect { case fisher: Fisher => fisher } // cast every object to Fisher
          .filter { fisher => fisher.doubleValue == IS_NOT_MOVING && !fisher.isAtPort } // keep only those trawling
          .map { fisher => fisher.getDistanceToPort } // get the distances
          .toVector // and store those in a vector
        val meanDist = dists.sum / dists.size
        if (!dists.isEmpty) writer.println(s"$rewardDelayed,$steps,$meanDist")
      }
    }
  }
  writer.close()
  System.exit(0)
}
