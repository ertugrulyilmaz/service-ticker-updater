package network.bundle.ticker.async

import java.util.concurrent.TimeUnit

import com.ning.http.client.filter.{FilterContext, FilterException, RequestFilter}
import com.typesafe.scalalogging.StrictLogging
import network.bundle.ticker.models.Model.ThrottleRequestFilter

class CustomThrottleRequestFilter(throttleRequestFilterMap: Map[String, ThrottleRequestFilter]) extends RequestFilter with StrictLogging {

  override def filter[T](ctx: FilterContext[T]): FilterContext[T] = {
    val throttleRequestFilter = throttleRequestFilterMap(ctx.getRequest.getUri.getHost)

    try {
      logger.debug("Current Throttling Status {}", throttleRequestFilter.semaphore.availablePermits())

      if (!throttleRequestFilter.semaphore.tryAcquire(throttleRequestFilter.maxWait, TimeUnit.SECONDS)) {
        throw new FilterException(s"No slot available for processing Request ${ctx.getRequest} with AsyncHandler ${ctx.getAsyncHandler}")
      }
    } catch {
      case _: InterruptedException => throw new FilterException(String.format("Interrupted Request %s with AsyncHandler %s", ctx.getRequest, ctx.getAsyncHandler))
    }

    logger.debug("Passed filter {}", throttleRequestFilter.semaphore.availablePermits())

    new FilterContext.FilterContextBuilder(ctx)
      .asyncHandler(AsyncHandlerWrapper(throttleRequestFilter.semaphore, ctx.getAsyncHandler, throttleRequestFilter.politeness)).build()
  }

}
