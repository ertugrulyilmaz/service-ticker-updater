package network.bundle.ticker.markets.v

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker, Tickers}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below. available markets are BTCCNY, BCCCNY, BCCBTC, LTCCNY, ETHCNY, ZECCNY
  * @see https://github.com/viabtc/viabtc_exchange_cn_api_en/wiki
  * @see https://www.viabtc.com/api/v1/market/ticker?market=btccny
  */
trait Viabtc extends BaseMarket {

  val tickers: immutable.Seq[Tickers] = immutable.Seq(
    Tickers(CoinPair("btc", "cny"), "https://www.viabtc.com/api/v1/market/ticker?market=BTCCNY"),
    Tickers(CoinPair("bcc", "cny"), "https://www.viabtc.com/api/v1/market/ticker?market=BCCCNY"),
    Tickers(CoinPair("bcc", "btc"), "https://www.viabtc.com/api/v1/market/ticker?market=BCCBTC"),
    Tickers(CoinPair("ltc", "cny"), "https://www.viabtc.com/api/v1/market/ticker?market=LTCCNY"),
    Tickers(CoinPair("eth", "cny"), "https://www.viabtc.com/api/v1/market/ticker?market=ETHCNY"),
    Tickers(CoinPair("zec", "cny"), "https://www.viabtc.com/api/v1/market/ticker?market=ZECCNY")
  )

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    Future.traverse(tickers) { ticker =>
      HttpClientFactory.get(httpClient, ticker.url).map { res =>
        val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, Map[String, Map[String, String]]]]("data")("ticker")
        val volume = data("vol")
        val lastPrice = data("last")

        CoinTicker("viabtc", ticker.coinPair, BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

}

object Viabtc {

  def apply(hc: AsyncHttpClient): Viabtc = new Viabtc() {

    override val httpClient: AsyncHttpClient = hc

  }

}