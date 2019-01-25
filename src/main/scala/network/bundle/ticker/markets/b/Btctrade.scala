package network.bundle.ticker.markets.b

import java.util.Calendar

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker, Tickers}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below. assets are eth, btc, etc, ltc, ybc, doge
  * @see https://www.btctrade.com/api.help.html
  * @see http://api.btctrade.com/api/ticker/
  * @see curl -H "X-Requested-With: XMLHttpRequest" https://www.btctrade.com/coin/rmb/btc/order.js\?t\=122103990
  * @see https://www.btctrade.com/coin/rmb/eth/order.js?t={epoxtime}
  */
trait Btctrade extends BaseMarket {

  val tickers: immutable.Seq[Tickers] = immutable.Seq(
    Tickers(CoinPair("eth", "btc"), "https://m.btctrade.com/coin/rmb/eth/order.js"),
    Tickers(CoinPair("btc", "btc"), "https://m.btctrade.com/coin/rmb/btc/order.js"),
    Tickers(CoinPair("etc", "btc"), "https://m.btctrade.com/coin/rmb/etc/order.js"),
    Tickers(CoinPair("ltc", "btc"), "https://m.btctrade.com/coin/rmb/ltc/order.js"),
    Tickers(CoinPair("doge", "btc"), "https://m.btctrade.com/coin/rmb/doge/order.js"),
    Tickers(CoinPair("ybc", "btc"), "https://m.btctrade.com/coin/rmb/ybc/order.js")
  )

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    val epoxtime = Calendar.getInstance().getTimeInMillis / 1000

    Future.traverse(tickers) { ticker =>
      HttpClientFactory.get(httpClient, s"${ticker.url}?t=${epoxtime}").map { res =>
        val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, Any]]
        val volume = data("sum").toString
        val lastPrice = data("d").asInstanceOf[List[Map[String, Any]]].last("p").toString

        CoinTicker(id, ticker.coinPair, BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

}

object Btctrade {

  def apply(hc: AsyncHttpClient): Btctrade = new Btctrade() {

    override val httpClient: AsyncHttpClient = hc

  }

}