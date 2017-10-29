package network.bundle.ticker.markets.n

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods.parse

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

trait NovaExchange extends BaseMarket {

  val url = "https://novaexchange.com/remote/v2/markets/"

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      parse(res.getResponseBody)
        .values.asInstanceOf[Map[String, List[Map[String, Any]]]]("markets")
        .map { pair =>
          val assetCurrency = pair("marketname").toString.toLowerCase.split("_")
          val asset = reformatCurrencyName(assetCurrency.last)
          val currency = reformatCurrencyName(assetCurrency.head)
          val volume = pair("volume24h").toString
          val lastPrice = pair("last_price").toString

          CoinTicker("nova", CoinPair(asset, currency, pair("marketname").toString), BigDecimal(volume), BigDecimal(lastPrice))
        }
    }
  }

}

object NovaExchange {

  def apply(hc: AsyncHttpClient): NovaExchange = new NovaExchange() {

    override val id = 9L
    override val httpClient: AsyncHttpClient = hc

  }

}