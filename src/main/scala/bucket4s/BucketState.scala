package bucket4s

/**
 * @author Ayush Mittal
 */
case class BucketState(bucketConfiguration: BucketConfiguration, currentTimeNanos: Long) {

  import BucketState._

  private var stateData: scala.collection.mutable.ArrayBuffer[Long] = scala.collection.mutable.ArrayBuffer.empty

  private def init = {
    this.stateData = scala.collection.mutable.ArrayBuffer.fill[Long](bucketConfiguration.bandwidth.length * 3)(0)
    bucketConfiguration.bandwidth.zipWithIndex.foreach {
      case (bandwidth, i) =>
        setCurrentSize(i, bandwidth.initialTokens)
        setLastRefillTime(i, currentTimeNanos)
    }
  }

  private def setCurrentSize(bandwidth: Int, currentSize: Long) = {
    stateData(bandwidth * BucketState.BANDWIDTH_SIZE + 1) = currentSize
  }

  private def setLastRefillTime(bandwidth: Int, nanos: Long) = {
    stateData(bandwidth * BucketState.BANDWIDTH_SIZE) = nanos
  }

  private def getCurrentSize(bandwidth: Int): Long = stateData(bandwidth * BANDWIDTH_SIZE + 1)

  private def getLastRefillTime(bandwidth: Int): Long = stateData(bandwidth * BANDWIDTH_SIZE)

  def refillAllBandwidth(limits: Array[Bandwidth], currentTimeNanos: Long): Unit = {
    limits.zipWithIndex.foreach {
      case (bandwidth, i) =>
        refill(i, bandwidth, currentTimeNanos)
    }
  }

  private def refill(bandwidthIndex: Int, bandwidth: Bandwidth, currentTimeNanos: Long): Unit = {

    val previousRefillNanos = getLastRefillTime(bandwidthIndex)

    if (currentTimeNanos <= previousRefillNanos) return

    val newCurrentTimeNanos = if (bandwidth.refillIntervally) {
      val incompleteIntervalCorrection = (currentTimeNanos - previousRefillNanos) % bandwidth.refillPeriodInNanos
      currentTimeNanos - incompleteIntervalCorrection
    } else {
      currentTimeNanos
    }

    if (newCurrentTimeNanos <= previousRefillNanos) return else setLastRefillTime(bandwidthIndex, newCurrentTimeNanos)

    val capacity = bandwidth.capacity
    val refillPeriodNanos = bandwidth.refillPeriodInNanos
    val refillTokens = bandwidth.refillTokens
    val currentSize = getCurrentSize(bandwidthIndex)

    var durationSinceLastRefillNanos = newCurrentTimeNanos - previousRefillNanos
    var newSize = currentSize
    if (durationSinceLastRefillNanos > refillPeriodNanos) {
      val elapsedPeriods = durationSinceLastRefillNanos / refillPeriodNanos
      val calculatedRefill = elapsedPeriods * refillTokens
      newSize += calculatedRefill
      if (newSize > capacity) {
        resetBandwidth(bandwidthIndex, capacity)
        return
      }
      if (newSize < currentSize) {
        resetBandwidth(bandwidthIndex, capacity)
        return
      }
      durationSinceLastRefillNanos %= refillPeriodNanos
    }
    var roundingError = getRoundingError(bandwidthIndex)
    val dividedWithoutError = multiplyExactOrReturnMaxValue(refillTokens, durationSinceLastRefillNanos)
    val divided = dividedWithoutError + roundingError
    if (divided < 0 || dividedWithoutError == Long.MaxValue) {
      val calculatedRefill =
        (durationSinceLastRefillNanos.toDouble / refillPeriodNanos.toDouble * refillTokens.toDouble).toLong
      newSize += calculatedRefill
      roundingError = 0
    } else {
      val calculatedRefill = divided / refillPeriodNanos
      if (calculatedRefill == 0) roundingError = divided
      else {
        newSize += calculatedRefill
        roundingError = divided % refillPeriodNanos
      }
    }
    if (newSize >= capacity) {
      resetBandwidth(bandwidthIndex, capacity)
      return
    }
    if (newSize < currentSize) { // arithmetic overflow happens. This mean that bucket reached Long.MAX_VALUE tokens.
      resetBandwidth(bandwidthIndex, capacity)
      return
    }
    setCurrentSize(bandwidthIndex, newSize)
    setRoundingError(bandwidthIndex, roundingError)
  }

  private def resetBandwidth(bandwidthIndex: Int, capacity: Long): Unit = {
    setCurrentSize(bandwidthIndex, capacity)
    setRoundingError(bandwidthIndex, 0)
  }

  private def setRoundingError(bandwidth: Int, roundingError: Long): Unit = {
    stateData(bandwidth * BANDWIDTH_SIZE + 2) = roundingError
  }

  private def getRoundingError(bandwidth: Int) = stateData(bandwidth * BANDWIDTH_SIZE + 2)

  private def consume(bandwidth: Int, tokens: Long): Unit = {
    stateData(bandwidth * BANDWIDTH_SIZE + 1) -= tokens
  }

  def getAvailableTokens(bandwidths: Array[Bandwidth]): Long = {
    var availableTokens = getCurrentSize(0)
    bandwidths.slice(1, bandwidths.length).zipWithIndex.foreach {
      case (_, i) =>
        availableTokens = Math.min(availableTokens, getCurrentSize(i))
    }
    availableTokens
  }

  def consume(bandwidths: Array[Bandwidth], toConsume: Long): Unit = {
    bandwidths.zipWithIndex.foreach {
      case (bandwidth, i) =>
        consume(i, toConsume)
    }
  }

  def copyStateFrom(sourceState: BucketState): Unit = {
    sourceState.stateData.copyToBuffer(stateData)
  }

  init
}

object BucketState {

  val BANDWIDTH_SIZE = 3

  def multiplyExactOrReturnMaxValue(x: Long, y: Long): Long = {
    val r = x * y
    val ax = Math.abs(x)
    val ay = Math.abs(y)
    val res = (ax | ay) >>> 31
    if (res != 0) {
      if (((y != 0) && (r / y != x)) || (x == Long.MinValue && y == -1)) return java.lang.Long.MAX_VALUE
    }
    r
  }

  def createInitialState(configuration: BucketConfiguration, currentTimeNanos: Long) =
    new BucketState(configuration, currentTimeNanos)
}
