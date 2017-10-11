package network.bundle.ticker.markets.b

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below. btc currency results is returned empty
  * @see http://www.btc38.com/api_detailed.html
  * @see http://api.btc38.com/v1/ticker.php?c=all&mk_type=cny
  * @see http://api.btc38.com/v1/ticker.php?c=all&mk_type=btc
  */
trait Btc38 extends BaseMarket {

  val url = "http://api.btc38.com/v1/ticker.php?c=all&mk_type=cny"

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      parse(res.getResponseBody)
        .values
        .asInstanceOf[Map[String, Map[String, Any]]]
        .filter(_._2("ticker") != "")
        .map { row =>
          val data = row._2.asInstanceOf[Map[String, Map[String, Double]]] getOrElse("ticker", Map.empty[String, Double])
          val asset = row._1
          val currency = "cny"
          val volume = data.getOrElse("vol", 0.0)
          val lastPrice = data.getOrElse("last", 0.0)

          CoinTicker("btc38", CoinPair(asset, currency), BigDecimal(volume), BigDecimal(lastPrice))
        }.to[immutable.Seq]
    }
  }

}

object Btc38 {

  def apply(hc: AsyncHttpClient): Btc38 = new Btc38() {

    override val httpClient: AsyncHttpClient = hc

  }

}
