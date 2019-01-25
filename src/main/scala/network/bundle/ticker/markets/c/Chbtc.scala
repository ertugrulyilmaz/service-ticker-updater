package network.bundle.ticker.markets.c

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker, Tickers}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below
  * @see https://www.chbtc.com/i/developer
  * @see http://api.chbtc.com/data/v1/ticker?currency=eth_cny
  * @see http://api.chbtc.com/data/v1/ticker?currency=eos_cny
  * @see http://api.chbtc.com/data/v1/ticker?currency=btc_cny
  * @see http://api.chbtc.com/data/v1/ticker?currency=ltc_cny
  * @see http://api.chbtc.com/data/v1/ticker?currency=etc_cny
  * @see http://api.chbtc.com/data/v1/ticker?currency=bcc_cny
  * @see http://api.chbtc.com/data/v1/ticker?currency=bts_cny
  * @see http://api.chbtc.com/data/v1/ticker?currency=qtum_cny
  *
  */
trait Chbtc extends BaseMarket {

  val tickers: immutable.Seq[Tickers] = immutable.Seq(
    Tickers(CoinPair("eth", "cny"), "http://api.chbtc.com/data/v1/ticker?currency=eth_cny"),
    Tickers(CoinPair("eos", "cny"), "http://api.chbtc.com/data/v1/ticker?currency=eos_cny"),
    Tickers(CoinPair("btc", "cny"), "http://api.chbtc.com/data/v1/ticker?currency=btc_cny"),
    Tickers(CoinPair("ltc", "cny"), "http://api.chbtc.com/data/v1/ticker?currency=ltc_cny"),
    Tickers(CoinPair("etc", "cny"), "http://api.chbtc.com/data/v1/ticker?currency=etc_cny"),
    Tickers(CoinPair("bcc", "cny"), "http://api.chbtc.com/data/v1/ticker?currency=bcc_cny"),
    Tickers(CoinPair("bts", "cny"), "http://api.chbtc.com/data/v1/ticker?currency=bts_cny"),
    Tickers(CoinPair("qtum", "cny"), "http://api.chbtc.com/data/v1/ticker?currency=qtum_cny")
  )

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    Future.traverse(tickers) { ticker =>
      HttpClientFactory.get(httpClient, ticker.url).map { res =>
        val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, Map[String, String]]]("ticker")
        val volume = data("vol")
        val lastPrice = data("last")

        CoinTicker(id, ticker.coinPair, BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

}

object Chbtc {

  def apply(hc: AsyncHttpClient): Chbtc = new Chbtc() {

    override val httpClient: AsyncHttpClient = hc

  }

}