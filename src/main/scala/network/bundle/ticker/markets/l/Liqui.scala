package network.bundle.ticker.markets.l

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below. available markets are btc_usdt, eth_usdt, dnt_usdt, pay_usdt, stx_usdt, bcc_usdt, omg_usdt, snt_usdt, icn_usdt, bat_usdt, dnt_eth, stx_eth, bat_eth, pay_eth, mgo_eth, icn_eth, edg_eth, omg_eth, bcc_eth, snm_eth, dnt_btc, eth_btc, bat_btc, gnt_btc, mgo_btc, stx_btc, round_btc, bcc_btc, pay_btc, omg_btc
  * @see https://liqui.io/api
  * @see https://api.liqui.io/api/3/ticker/eth_btc
  */
trait Liqui extends BaseMarket {

  val pairsUrl = "https://api.liqui.io/api/3/info"
  val tickerUrl = "https://api.liqui.io/api/3/ticker"

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    val res = HttpClientFactory.completedGet(httpClient, pairsUrl)
    val pairs = parse(res.getResponseBody)
      .values
      .asInstanceOf[Map[String, Map[String, Any]]]("pairs")
      .keys
      .filter(_.endsWith("_btc"))
      .toSeq
      .mkString("-")

    HttpClientFactory.get(httpClient, s"$tickerUrl/$pairs").map { res =>
      parse(res.getResponseBody)
        .values.asInstanceOf[Map[String, Map[String, Double]]]
        .map { data =>
          val pair = data._1
          val assetCurrency = pair.split("_")
          val asset = reformatCurrencyName(assetCurrency.head)
          val currency = reformatCurrencyName(assetCurrency.last)
          val volume = data._2.getOrElse("vol", 0.0)
          val lastPrice = data._2.getOrElse("last", 0.0)

          CoinTicker("liqui", CoinPair(asset, currency, pair), BigDecimal(volume), BigDecimal(lastPrice))
        }.toList
    }
  }

}

object Liqui {

  def apply(hc: AsyncHttpClient): Liqui = new Liqui() {

    override val id = 8L
    override val httpClient: AsyncHttpClient = hc

  }

}