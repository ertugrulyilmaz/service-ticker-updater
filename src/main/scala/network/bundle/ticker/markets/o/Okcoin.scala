package network.bundle.ticker.markets.o

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker, Tickers}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation link is below
  * @see https://www.okcoin.cn/rest_getStarted.html
  * @see https://www.okcoin.cn/api/v1/ticker.do?symbol=ltc_cny
  * @see https://www.okcoin.cn/api/v1/ticker.do?symbol=btc_cny
  * @see https://www.okcoin.cn/api/v1/ticker.do?symbol=eth_cny
  **/
trait Okcoin extends BaseMarket {

  val tickers: immutable.Seq[Tickers] = immutable.Seq(
    Tickers(CoinPair("ltc", "cny"), "https://www.okcoin.cn/api/v1/ticker.do?symbol=ltc_cny"),
    Tickers(CoinPair("btc", "cny"), "https://www.okcoin.cn/api/v1/ticker.do?symbol=btc_cny"),
    Tickers(CoinPair("eth", "cny"), "https://www.okcoin.cn/api/v1/ticker.do?symbol=eth_cny")
  )

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    Future.traverse(tickers) { ticker =>
      HttpClientFactory.get(httpClient, ticker.url).map { res =>
        val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, Any]]("ticker").asInstanceOf[Map[String, String]]
        val volume = data("vol")
        val lastPrice = data("last")

        CoinTicker(id, ticker.coinPair, BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

}

object Okcoin {

  def apply(hc: AsyncHttpClient): Okcoin = new Okcoin() {

    override val httpClient: AsyncHttpClient = hc

  }

}