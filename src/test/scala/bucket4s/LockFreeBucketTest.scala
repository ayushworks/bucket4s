package bucket4s

import java.time.Duration

import org.scalatest.{FlatSpec, Matchers}

/**
 * @author Ayush Mittal
 */
class LockFreeBucketTest extends FlatSpec with Matchers {

  val bandwidth = Bandwidth.simple(1, Duration.ofSeconds(10)).withInitialTokens(0)

  val bucket = Bucket4s.builder().addLimit(bandwidth).build()

  //there should be a better way of testing this
  it should "produce required tokens in time period" in {
    Thread.sleep(10001)
    bucket.tryConsume(1) shouldBe true
    bucket.tryConsume(1) shouldBe false
    Thread.sleep(10000)
    bucket.tryConsume(1) shouldBe true
  }
}
