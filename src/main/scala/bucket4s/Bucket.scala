package bucket4s

/**
 * Performs rate limiting using algorithm based on top of ideas of <a href="https://en.wikipedia.org/wiki/Token_bucket">Token Bucket</a>.
 */
trait Bucket {

  /**
   * Tries to consume a specified number of tokens from this bucket.
   *
   * @param numTokens The number of tokens to consume from the bucket, must be a positive number.
   * @return { @code true} if the tokens were consumed, { @code false} otherwise.
   */
  def tryConsume(numTokens: Long): Boolean
}
