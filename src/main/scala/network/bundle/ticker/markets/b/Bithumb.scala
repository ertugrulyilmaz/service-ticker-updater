package network.bundle.ticker.markets.b

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note price currency is KRW - XE.
  * @note avabilable assets are BTC, ETH, DASH, LTC, ETC, XRP, BCH (default value: BTC), ALL(Entire)
  * @see https://www.bithumb.com/u1/US127
  * @see https://api.bithumb.com/public/ticker/
  * @see https://api.bithumb.com/public/ticker/:currency
  */
trait Bithumb extends BaseMarket {

  val url = "https://api.bithumb.com/public/ticker/all"

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      parse(res.getResponseBody).values
        .asInstanceOf[Map[String, Any]]("data")
        .asInstanceOf[Map[String, Map[String, String]]]
        .filter(_._1 != "date")
        .map { m =>
          val asset = m._1.toLowerCase
          val currency = "krw"
          val volume = m._2("volume_1day")
          val lastPrice = m._2("buy_price")

          CoinTicker("bithumb", CoinPair(asset, currency), BigDecimal(volume), BigDecimal(lastPrice))
        }.to[immutable.Seq]
    }
  }

}

object Bithumb {

  def apply(hc: AsyncHttpClient): Bithumb = new Bithumb() {

    override val httpClient: AsyncHttpClient = hc

  }

}
