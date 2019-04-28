package bucket4s

import scala.collection.mutable.ListBuffer

/**
 * @author Ayush Mittal
 */
case class ConfigurationBuilder() {

  private var bandwidths: scala.collection.mutable.ListBuffer[Bandwidth] = ListBuffer.empty

  def addLimit(bandwidth: Bandwidth): Unit = {
    bandwidths += bandwidth
  }

  def build(): BucketConfiguration = {
    BucketConfiguration(bandwidths.toArray)
  }
}
