package network.bundle.ticker.async

import java.util.concurrent.{Semaphore, TimeUnit}

import com.ning.http.client.filter.{FilterContext, FilterException, RequestFilter}
import com.typesafe.scalalogging.StrictLogging

class CustomThrottleRequestFilter(permits: Int) extends RequestFilter with StrictLogging {

  val maxWait = 10L
  val politeness = 1000L
  val semaphore = new Semaphore(permits)

  override def filter[T](ctx: FilterContext[T]): FilterContext[T] = {
    try {
      logger.debug("Current Throttling Status {}", semaphore.availablePermits())

      if (!semaphore.tryAcquire(maxWait, TimeUnit.SECONDS)) {
        throw new FilterException(s"No slot available for processing Request ${ctx.getRequest} with AsyncHandler ${ctx.getAsyncHandler}")
      }
    } catch {
      case _: InterruptedException => throw new FilterException(String.format("Interrupted Request %s with AsyncHandler %s", ctx.getRequest, ctx.getAsyncHandler))
    }

    logger.debug("Passed filter {}", semaphore.availablePermits())

    new FilterContext.FilterContextBuilder(ctx)
      .asyncHandler(AsyncHandlerWrapper(semaphore, ctx.getAsyncHandler, politeness)).build()
  }

}
