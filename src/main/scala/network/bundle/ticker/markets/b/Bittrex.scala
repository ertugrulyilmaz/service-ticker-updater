package network.bundle.ticker.markets.b

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods.parse

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below
  * @see https://bittrex.com/Home/Api
  * @see https://bittrex.com/api/v1.1/public/getmarketsummaries
  */
trait Bittrex extends BaseMarket {

  val url = "https://bittrex.com/api/v1.1/public/getmarketsummaries"

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      parse(res.getResponseBody)
        .values
        .asInstanceOf[Map[String, List[Map[String, Any]]]]("result")
        .map { pair =>
          val marketName = pair("MarketName").toString.toLowerCase.split("-")
          val asset = reformatCurrencyName(marketName.last.toLowerCase)
          val currency = reformatCurrencyName(marketName.head.toLowerCase)

          val volume = pair("Volume").toString
          val lastPrice = pair("Last").toString

          CoinTicker("bittrex", CoinPair(asset, currency, pair("MarketName").toString), BigDecimal(volume), BigDecimal(lastPrice))
        }
    }
  }

}

object Bittrex {

  def apply(hc: AsyncHttpClient): Bittrex = new Bittrex() {

    override val id = 3L
    override val httpClient: AsyncHttpClient = hc

  }

}