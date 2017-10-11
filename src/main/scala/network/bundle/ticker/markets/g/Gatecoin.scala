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
  * @see https://gatecoin.com/api
  * @see https://api.gatecoin.com/Public/LiveTickers
  */
trait Gatecoin extends BaseMarket {

  val url = "https://api.gatecoin.com/Public/LiveTickers"

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      parse(res.getResponseBody)
        .values
        .asInstanceOf[Map[String, List[Map[String, Any]]]]("tickers")
        .map { ticker =>
          val coinPair = ticker("currencyPair").toString match {
            case x if x.endsWith("BTC") => CoinPair(x.replace("BTC", "").toLowerCase, "btc")
            case x if x.endsWith("USD") => CoinPair(x.replace("USD", "").toLowerCase, "usd")
            case x if x.endsWith("EUR") => CoinPair(x.replace("EUR", "").toLowerCase, "eur")
            case x if x.endsWith("ETH") => CoinPair(x.replace("ETH", "").toLowerCase, "eth")
            case x if x.endsWith("HKD") => CoinPair(x.replace("HKD", "").toLowerCase, "hkd")
          }

          val volume = ticker("volume").toString
          val lastPrice = ticker("last").toString

          CoinTicker("gatecoin", coinPair, BigDecimal(volume), BigDecimal(lastPrice))
        }
    }
  }

}

object Gatecoin {

  def apply(hc: AsyncHttpClient): Gatecoin = new Gatecoin() {

    override val httpClient: AsyncHttpClient = hc

  }

}