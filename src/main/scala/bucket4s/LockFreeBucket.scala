package bucket4s

import java.util.concurrent.atomic.AtomicReference

/**
 * @author Ayush Mittal
 */
class LockFreeBucket(configuration: BucketConfiguration, state: BucketState) extends Bucket {

  case class StateWithConfiguration(configuration: BucketConfiguration, state: BucketState) {

    def copyStateFrom(other: StateWithConfiguration): StateWithConfiguration = {
      state.copyStateFrom(other.state)
      StateWithConfiguration(other.configuration, state)
    }

  }

  private val stateWithConfiguration: AtomicReference[StateWithConfiguration] =
    new AtomicReference[StateWithConfiguration](StateWithConfiguration(configuration, state))

  def tryConsume(numTokens: Long): Boolean = {
    var previousState = stateWithConfiguration.get()
    var newState = previousState.copy(configuration = previousState.configuration, state = previousState.state)
    val currentTimeNanos = System.nanoTime()
    while (true) {
      newState.state.refillAllBandwidth(configuration.bandwidth, currentTimeNanos)
      val availableToConsume = newState.state.getAvailableTokens(configuration.bandwidth)
      if (numTokens > availableToConsume) {
        return false
      }
      newState.state.consume(newState.configuration.bandwidth, numTokens)
      if (stateWithConfiguration.compareAndSet(previousState, newState)) {
        return true
      } else {
        previousState = stateWithConfiguration.get()
        newState = newState.copyStateFrom(previousState)
      }
    }
    return true
  }

}
