package bucket4s

/**
 * @author Ayush Mittal
 */
class BucketBuilder {

  val configurationBuilder: ConfigurationBuilder = new ConfigurationBuilder()

  def addLimit(bandwidth: Bandwidth): BucketBuilder = {
    configurationBuilder.addLimit(bandwidth)
    this
  }

  def build(): LockFreeBucket = {
    val bucketConfiguration = configurationBuilder.build()
    new LockFreeBucket(bucketConfiguration, BucketState.createInitialState(bucketConfiguration, System.nanoTime()))
  }
}
