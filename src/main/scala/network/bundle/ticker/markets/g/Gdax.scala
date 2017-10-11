package network.bundle.ticker.markets.g

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below
  * @see https://docs.gdax.com
  * @see https://api-public.sandbox.gdax.com/products
  * @see https://api-public.sandbox.gdax.com/products/<product-id>/stats
  */
trait Gdax extends BaseMarket {

  val url = "https://api-public.sandbox.gdax.com/products"

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    val resProducts = HttpClientFactory.completedGet(httpClient, url)

    val products = parse(resProducts.getResponseBody).values.asInstanceOf[immutable.Seq[Map[String, String]]]

    Future.traverse(products) { pair =>
      val productId = pair("id")
      val asset = pair("base_currency").toLowerCase
      val currency = pair("quote_currency").toLowerCase

      HttpClientFactory.get(httpClient, s"$url/$productId/ticker").map { resTicker =>
        val data = parse(resTicker.getResponseBody).values.asInstanceOf[Map[String, Any]]
        val volume = data.getOrElse("volume", 0.0).toString
        val lastPrice = data.getOrElse("price", 0.0).toString

        CoinTicker("gdax", CoinPair(asset, currency), BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

}

object Gdax {

  def apply(hc: AsyncHttpClient): Gdax = new Gdax() {

    override val httpClient: AsyncHttpClient = hc

  }

}