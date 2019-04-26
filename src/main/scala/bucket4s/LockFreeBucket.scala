package bucket4s

import java.util.concurrent.atomic.AtomicReference

/**
 * @author Ayush Mittal
 */
class LockFreeBucket(configuration: BucketConfiguration, state: BucketState) extends Bucket {

  case class StateWithConfiguration(configuration: BucketConfiguration, state: BucketState)

  private val stateWithConfiguration: AtomicReference[StateWithConfiguration] =
    new AtomicReference[StateWithConfiguration](StateWithConfiguration(configuration, state))

  def tryConsume(numTokens: Long): Boolean = {
    val previousState = stateWithConfiguration.get()
    val newState = previousState.copy(configuration = previousState.configuration, state = previousState.state)
    val currentTimeNanos = System.nanoTime()
    while (true) {
      newState.state.refillAllBandwidth(configuration.bandwidth, currentTimeNanos)
      val availableToConsume = newState.getAvailableTokens
      val toConsume = Math.min(limit, availableToConsume)
      if (toConsume == 0) return 0
      newState.consume(toConsume)
      if (stateRef.compareAndSet(previousState, newState)) return toConsume
      else {
        previousState = stateRef.get
        newState.copyStateFrom(previousState)
      }
    }
  }

}
