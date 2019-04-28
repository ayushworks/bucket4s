package bucket4s

import java.time.Duration

/**
 * @author Ayush Mittal
 */
case class Bandwidth(capacity: Long,
                     initialTokens: Long,
                     refillPeriodInNanos: Long,
                     refillTokens: Long,
                     refillIntervally: Boolean) {

  def withInitialTokens(initialTokens: Long): Bandwidth = {
    Bandwidth(capacity, initialTokens, refillPeriodInNanos, refillTokens, refillIntervally)
  }
}

object Bandwidth {

  def simple(capacity: Long, duration: Duration): Bandwidth = {
    Bandwidth.classic(capacity, Refill.greedy(capacity, duration))
  }

  def classic(capacity: Long, refill: Refill): Bandwidth = {
    Bandwidth(capacity, capacity, refill.periodNanos, refill.tokens, refill.refillIntervally)
  }
}
