package network.bundle.ticker.markets.h

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker, Tickers}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below
  * @see https://github.com/huobiapi/API_Docs/wiki
  * @see https://github.com/huobiapi/API_Docs_en/wiki
  * @see https://be.huobi.com/v1/common/symbols
  * @see https://be.huobi.com/v1/common/currencys
  * @see https://be.huobi.com/market/detail?symbol=ethcny
  * @see http://api.huobi.pro/v1/common/symbols
  * @see http://api.huobi.pro/v1/common/currencys
  * @see http://api.huobi.pro/market/detail?symbol=ethbtc
  */
trait Huobi extends BaseMarket {

  val tickers: immutable.Seq[Tickers] = immutable.Seq(
    Tickers(CoinPair("etc", "cny"), "https://be.huobi.com/market/detail?symbol=etccny"),
    Tickers(CoinPair("eth", "cny"), "https://be.huobi.com/market/detail?symbol=ethcny"),
    Tickers(CoinPair("ltc", "btc"), "http://api.huobi.pro/market/detail?symbol=ltcbtc"),
    Tickers(CoinPair("eth", "btc"), "http://api.huobi.pro/market/detail?symbol=ethbtc"),
    Tickers(CoinPair("etc", "btc"), "http://api.huobi.pro/market/detail?symbol=etcbtc")
  )

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    Future.traverse(tickers) { ticker =>
      HttpClientFactory.get(httpClient, ticker.url).map { res =>
        val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, Map[String, Double]]]("tick")
        val volume = data("vol")
        val lastPrice = data("open")

        CoinTicker("huobi", ticker.coinPair, BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

}

object Huobi {

  def apply(hc: AsyncHttpClient): Huobi = new Huobi() {

    override val httpClient: AsyncHttpClient = hc

  }

}