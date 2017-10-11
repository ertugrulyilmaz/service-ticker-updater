package network.bundle.ticker.async

import com.ning.http.client.{AsyncCompletionHandler, AsyncHttpClient, AsyncHttpClientConfig, Response}

import scala.concurrent.{Future, Promise}
import scala.util.Random

object HttpClientFactory {

  val CONTENT_TYPE: String = "Content-Type"
  val APPLICATION_JSON: String = "application/json; charset=utf-8"
  val GOOGLE = "Mozilla/5.0 (compatible; googlebot2/2.0; +http://www.bing.com/bingbot2.htm)"

  def create(): AsyncHttpClient = {
    val userAgent = Random.nextInt(Int.MaxValue) + "- " + GOOGLE
    val asyncConfig: AsyncHttpClientConfig = new AsyncHttpClientConfig.Builder()
      .addRequestFilter(new CustomThrottleRequestFilter(8))
      .setMaxRedirects(2)
      .setFollowRedirect(true)
      .setAllowPoolingConnections(true)
      .setMaxRequestRetry(2)
      .setCompressionEnforced(true)
      .setMaxConnectionsPerHost(-1)
      .setMaxConnections(-1)
      .setConnectTimeout(150000)
      .setRequestTimeout(300000)
      .setUserAgent(userAgent)
      .setDisableUrlEncodingForBoundedRequests(true)
      //      .setSSLContext(CustomSSLContext.getSSLContext())
      .build()

    new AsyncHttpClient(asyncConfig)
  }

  def get(httpClient: AsyncHttpClient, url: String): Future[Response] = {
    val promise = Promise[Response]()

    httpClient.prepareGet(url).execute(new AsyncCompletionHandler[Response] {
      def onCompleted(response: Response) = {
        promise.success(response)
        response
      }

      override def onThrowable(t: Throwable): Unit = {
        promise.failure(t)
        super.onThrowable(t)
      }
    })

    promise.future
  }

  def completedGet(httpClient: AsyncHttpClient, url: String): Response = httpClient.prepareGet(url).execute().get

  def post(httpClient: AsyncHttpClient, url: String, body: String): Future[Response] = {
    val promise = Promise[Response]()

    httpClient.preparePost(url)
      .setBody(body)
      .execute(new AsyncCompletionHandler[Response] {
      def onCompleted(response: Response) = {
        promise.success(response)
        response
      }

      override def onThrowable(t: Throwable): Unit = {
        promise.failure(t)
        super.onThrowable(t)
      }
    })

    promise.future
  }


}