package network.bundle.ticker.markets.b

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below
  * @see https://www.btcc.com/apidocs/usd-spot-exchange-market-data-rest-api
  * @see https://spotusd-data.btcc.com/data/pro/ticker?symbol=BTCUSD
  */
trait Btcc extends BaseMarket {

  val url = "https://spotusd-data.btcc.com/data/pro/ticker?symbol=BTCUSD"

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, Map[String, Any]]]("ticker")
      val volume = data.getOrElse("Volume24H", "0.0").toString
      val lastPrice = data.getOrElse("Last", "0.0").toString

      immutable.Seq(CoinTicker("btcc", CoinPair("btc", "usd"), BigDecimal(volume), BigDecimal(lastPrice)))
    }
  }

}

object Btcc {

  def apply(hc: AsyncHttpClient): Btcc = new Btcc() {

    override val httpClient: AsyncHttpClient = hc

  }

}