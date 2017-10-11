package network.bundle.ticker.markets.c

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below
  * @see https://coincheck.com/documents/exchange/api#public
  * @see https://coincheck.com/api/ticker
  */
trait Coincheck extends BaseMarket {

  val url = "https://coincheck.com/api/ticker"

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, Double]]
      val volume = data("volume")
      val lastPrice = data("last")

      immutable.Seq(CoinTicker("coincheck", CoinPair("btc", "btc"), BigDecimal(volume), BigDecimal(lastPrice)))
    }
  }
}

object Coincheck {

  def apply(hc: AsyncHttpClient): Coincheck = new Coincheck() {

    override val httpClient: AsyncHttpClient = hc

  }

}