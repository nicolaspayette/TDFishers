package np.tdfishers.model

import org.scalatest.FlatSpec

class BiologySpec extends FlatSpec {

  "Biology" should "be able to properly compute logistic growth" in {
    assert(Biology.newBiomass(   0, 0.7, 5000.0) ===    0.0)
    assert(Biology.newBiomass(4000, 0.7, 5000.0) === 4560.0)
    assert(Biology.newBiomass(5000, 0.7, 5000.0) === 5000.0)
    assert(Biology.newBiomass(6000, 0.7, 5000.0) === 5160.0)
  }

}
