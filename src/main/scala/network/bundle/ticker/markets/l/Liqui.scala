package network.bundle.ticker.markets.l

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model._
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}
import scala.math.BigDecimal.RoundingMode

/**
  * @note documentation is below. available markets are btc_usdt, eth_usdt, dnt_usdt, pay_usdt, stx_usdt, bcc_usdt, omg_usdt, snt_usdt, icn_usdt, bat_usdt, dnt_eth, stx_eth, bat_eth, pay_eth, mgo_eth, icn_eth, edg_eth, omg_eth, bcc_eth, snm_eth, dnt_btc, eth_btc, bat_btc, gnt_btc, mgo_btc, stx_btc, round_btc, bcc_btc, pay_btc, omg_btc
  * @see https://liqui.io/api
  * @see https://api.liqui.io/api/3/ticker/eth_btc
  */
trait Liqui extends BaseMarket {

  val PAIRS_URL = "https://api.liqui.io/api/3/info"
  val TICKER_URL = "https://api.liqui.io/api/3/ticker"
  val ORDER_URL = "https://api.liqui.io/api/3/depth"

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    val res = HttpClientFactory.completedGet(httpClient, PAIRS_URL)
    val pairs = parse(res.getResponseBody)
      .values
      .asInstanceOf[Map[String, Map[String, Any]]]("pairs")
      .keys
      .filter(_.endsWith("_btc"))
      .toSeq
      .mkString("-")

    HttpClientFactory.get(httpClient, s"$TICKER_URL/$pairs").map { res =>
      logger.info("{}, {}", s"$TICKER_URL/$pairs", res.getResponseBody)

      parse(res.getResponseBody)
        .values.asInstanceOf[Map[String, Map[String, Double]]]
        .map { data =>
          val pair = data._1
          val assetCurrency = pair.split("_")
          val asset = reformatCurrencyName(assetCurrency.head)
          val currency = reformatCurrencyName(assetCurrency.last)
          val volume = data._2.getOrElse("vol", 0.0)
          val lastPrice = data._2.getOrElse("last", 0.0)

          CoinTicker(id, CoinPair(asset, currency, pair), BigDecimal(volume), BigDecimal(lastPrice))
        }.toList
    }
  }

  override def orders()(implicit ec: ExecutionContext): Future[immutable.Seq[ExchangeCoinOrders]] = {
    val coinPairs = parse(HttpClientFactory.completedGet(httpClient, PAIRS_URL).getResponseBody)
      .values
      .asInstanceOf[Map[String, Map[String, Any]]]("pairs")
      .map { symbol =>
        val assetCurrency = symbol._1.split("_")
        val asset = assetCurrency.head
        val currency = assetCurrency.last

        CoinPair(asset, currency, symbol._1)
      }
      .filter(_.currency == "btc")
      .to[immutable.Seq]

    Future.traverse(coinPairs) { coinPair =>
      HttpClientFactory.get(httpClient, s"$ORDER_URL/${coinPair.market}").map { res =>
        val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, Map[String, Seq[Seq[Double]]]]](coinPair.market)

        val bids = data.getOrElse("bids", Seq.empty[Seq[Double]])
          .map { bid =>
            val price = bid.head
            val amount = bid.last

            Order(BigDecimal(price).setScale(8, RoundingMode.HALF_UP), BigDecimal(amount).setScale(8, RoundingMode.HALF_UP))
          }
          .filter(_.amount > 0)
          .take(ORDER_LIMIT)

        val asks = data.getOrElse("asks", Seq.empty[Seq[Double]])
          .map { ask =>
            val price = ask.head
            val amount = ask.last

            Order(BigDecimal(price).setScale(8, RoundingMode.HALF_UP), BigDecimal(amount).setScale(8, RoundingMode.HALF_UP))
          }
          .filter(_.amount > 0)
          .take(ORDER_LIMIT)

        ExchangeCoinOrders(id, CoinOrder(coinPair, bids), CoinOrder(coinPair, asks))
      }
    }
  }

}

object Liqui {

  def apply(hc: AsyncHttpClient): Liqui = new Liqui() {

    override val id = 8L
    override val httpClient: AsyncHttpClient = hc

  }

}