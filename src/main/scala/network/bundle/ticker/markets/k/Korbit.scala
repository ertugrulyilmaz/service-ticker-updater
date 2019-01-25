package network.bundle.ticker.markets.k

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker, Tickers}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below
  * @see https://apidocs.korbit.co.kr/#introduction2
  * @see https://api.korbit.co.kr/v1/ticker/detailed?currency_pair=btc_krw
  * @see https://api.korbit.co.kr/v1/ticker/detailed?currency_pair=bch_krw
  * @see https://api.korbit.co.kr/v1/ticker/detailed?currency_pair=eth_krw
  * @see https://api.korbit.co.kr/v1/ticker/detailed?currency_pair=etc_krw
  * @see https://api.korbit.co.kr/v1/ticker/detailed?currency_pair=xrp_krw
  */
trait Korbit extends BaseMarket {

  val tickers: immutable.Seq[Tickers] = immutable.Seq(
    Tickers(CoinPair("btc", "krw"), "https://api.korbit.co.kr/v1/ticker/detailed?currency_pair=btc_krw"),
    Tickers(CoinPair("bch", "krw"), "https://api.korbit.co.kr/v1/ticker/detailed?currency_pair=bch_krw"),
    Tickers(CoinPair("eth", "krw"), "https://api.korbit.co.kr/v1/ticker/detailed?currency_pair=eth_krw"),
    Tickers(CoinPair("etc", "krw"), "https://api.korbit.co.kr/v1/ticker/detailed?currency_pair=etc_krw"),
    Tickers(CoinPair("xrp", "krw"), "https://api.korbit.co.kr/v1/ticker/detailed?currency_pair=xrp_krw")
  )

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    Future.traverse(tickers) { ticker =>
      HttpClientFactory.get(httpClient, ticker.url).map { res =>
        val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, String]]
        val volume = data.getOrElse("volume", "0.0")
        val lastPrice = data.getOrElse("last", "0.0")

        CoinTicker(id, ticker.coinPair, BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

}

object Korbit {

  def apply(hc: AsyncHttpClient): Korbit = new Korbit() {

    override val httpClient: AsyncHttpClient = hc

  }

}
