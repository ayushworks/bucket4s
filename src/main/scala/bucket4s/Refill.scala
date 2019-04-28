package bucket4s

import java.time.Duration

/**
 * @author Ayush Mittal
 */
case class Refill(tokens: Long, periodNanos: Long, refillIntervally: Boolean)

object Refill {

  def greedy(tokens: Long, period: Duration): Refill =
    Refill(tokens, period.toNanos, false)

  def intervally(tokens: Long, period: Duration): Refill =
    Refill(tokens, period.toNanos, true)
}
