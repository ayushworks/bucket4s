package bucket4s

/**
 * @author Ayush Mittal
 */
object Bucket4s {

  def builder(): BucketBuilder = {
    new BucketBuilder
  }
}
