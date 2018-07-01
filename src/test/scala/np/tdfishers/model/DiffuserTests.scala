package np.tdfishers.model

import org.scalatest.FlatSpec

class DiffuserSpec extends FlatSpec {

  "Diffuser" should "not move fish around when there aren't any" in {
    val a = Array.fill(3, 3)(0.0)
    Diffuser.diffuseAll(0.5, a)
    assert(a === Array.fill(3, 3)(0.0))
  }

  it should "move fish in a small matrix" in {
    val a = Array(
      Array(1.0, 0.0),
      Array(0.0, 0.0)
    )
    Diffuser.diffuseAll(0.5, a)
    assert(a === Array(
      Array(0.2500, 0.1250),
      Array(0.3125, 0.3125)
    ))
  }

}
