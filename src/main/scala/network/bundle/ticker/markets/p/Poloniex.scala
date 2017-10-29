package network.bundle.ticker.markets.p

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note Market Api Informations
  * @see https://poloniex.com/support/api/
  * @see https://poloniex.com/public?command=returnTicker
  * @see https://poloniex.com/public?command=return24hVolume
  *
  **/
trait Poloniex extends BaseMarket {

  val url: String = "https://poloniex.com/public?command=returnTicker"

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      parse(res.getResponseBody).values.asInstanceOf[Map[String, Map[String, String]]].map { data =>
        val pair = data._1.toLowerCase.split("_")
        val asset = reformatCurrencyName(pair.last)
        val currency = reformatCurrencyName(pair.head)
        val volume = data._2("quoteVolume")
        val lastPrice = data._2("last")

        CoinTicker("poloniex", CoinPair(asset, currency, data._1), BigDecimal(volume), BigDecimal(lastPrice))
      }.to[immutable.Seq]
    }
  }

}

object Poloniex {

  def apply(hc: AsyncHttpClient): Poloniex = new Poloniex() {

    override val id = 10L
    override val httpClient: AsyncHttpClient = hc

  }

}
