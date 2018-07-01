package np.tdfishers.model

import sim.util.Int2D

// I'm trying not to rely on Scala too much for this project, but getting
// `hashCode` and `equals` for free is too tempting in this case...
case class FisherState(location: Int2D, holdFull: Boolean)
