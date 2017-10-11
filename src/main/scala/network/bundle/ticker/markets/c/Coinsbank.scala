package network.bundle.ticker.markets.c

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker, Tickers}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below. Markets are BTCRUB, LTCRUB, BTCUSD, BTCGBP, BTCEUR, LTCEUR, LTCUSD, LTCBTC, LTCGBP
  * @see https://coinsbank.com/api/public/ticker?pair=BTCRUB
  */
trait Coinsbank extends BaseMarket {

  val tickers: immutable.Seq[Tickers] = immutable.Seq(
    Tickers(CoinPair("btc", "rub"), "https://coinsbank.com/api/public/ticker?pair=BTCRUB"),
    Tickers(CoinPair("btc", "usd"), "https://coinsbank.com/api/public/ticker?pair=BTCUSD"),
    Tickers(CoinPair("btc", "eur"), "https://coinsbank.com/api/public/ticker?pair=BTCEUR"),
    Tickers(CoinPair("btc", "gbp"), "https://coinsbank.com/api/public/ticker?pair=BTCGBP"),
    Tickers(CoinPair("ltc", "btc"), "https://coinsbank.com/api/public/ticker?pair=LTCBTC"),
    Tickers(CoinPair("ltc", "rub"), "https://coinsbank.com/api/public/ticker?pair=LTCRUB"),
    Tickers(CoinPair("ltc", "usd"), "https://coinsbank.com/api/public/ticker?pair=LTCUSD"),
    Tickers(CoinPair("ltc", "eur"), "https://coinsbank.com/api/public/ticker?pair=LTCEUR"),
    Tickers(CoinPair("ltc", "gbp"), "https://coinsbank.com/api/public/ticker?pair=LTCGBP")
  )

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    Future.traverse(tickers) { ticker =>
      HttpClientFactory.get(httpClient, ticker.url).map { res =>
        val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, Map[String, Any]]]("data")
        val volume = data.getOrElse("volume", "0.0").toString
        val lastPrice = data.getOrElse("last", "0.0").toString

        CoinTicker("coinsbank", ticker.coinPair, BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

}

object Coinsbank {

  def apply(hc: AsyncHttpClient): Coinsbank = new Coinsbank() {

    override val httpClient: AsyncHttpClient = hc

  }

}