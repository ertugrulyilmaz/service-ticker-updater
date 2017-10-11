package network.bundle.ticker.async

import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean

import com.ning.http.client.{AsyncHandler, HttpResponseBodyPart, HttpResponseHeaders, HttpResponseStatus}
import com.typesafe.scalalogging.StrictLogging


trait AsyncHandlerWrapper[T] extends AsyncHandler[T] with StrictLogging {

  val semaphore: Semaphore
  val asyncHandler: AsyncHandler[T]
  val politenessDelay: Long
  val completed = new AtomicBoolean(false)

  def complete(): Unit = {
    if (completed.compareAndSet(false, true)) {
      try {
        Thread.sleep(politenessDelay)
      } catch {
        case e: InterruptedException => logger.error("Interrupted in polite delay", e)
      }

      semaphore.release()
    }
  }

  override def onThrowable(t: Throwable): Unit = {
    try {
      asyncHandler.onThrowable(t)
    } finally {
      complete()
    }
  }

  override def onBodyPartReceived(bodyPart: HttpResponseBodyPart): AsyncHandler.STATE = asyncHandler.onBodyPartReceived(bodyPart)

  override def onStatusReceived(responseStatus: HttpResponseStatus): AsyncHandler.STATE = asyncHandler.onStatusReceived(responseStatus)

  override def onHeadersReceived(headers: HttpResponseHeaders): AsyncHandler.STATE = asyncHandler.onHeadersReceived(headers)

  override def onCompleted(): T = {
    try {
      asyncHandler.onCompleted()
    } finally {
      complete()
    }
  }

}

object AsyncHandlerWrapper {

  def apply[T](_semaphore: Semaphore, _asyncHandler: AsyncHandler[T], _politenessDelay: Long): AsyncHandlerWrapper[T] = new AsyncHandlerWrapper[T]() {
    override val semaphore: Semaphore = _semaphore
    override val asyncHandler: AsyncHandler[T] = _asyncHandler
    override val politenessDelay: Long = _politenessDelay
  }

}
