package network.bundle.ticker.markets.i

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker, Tickers}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below. Markets are XBTUSD XBTSGD XBTEUR.
  * @see https://api.itbit.com/docs
  * @see https://api.itbit.com/v1/markets/XBTUSD/ticker
  */
trait Itbit extends BaseMarket {

  val tickers: immutable.Seq[Tickers] = immutable.Seq(
    Tickers(CoinPair("xbt", "usd"), "https://api.itbit.com/v1/markets/XBTUSD/ticker"),
    Tickers(CoinPair("xbt", "sgd"), "https://api.itbit.com/v1/markets/XBTSGD/ticker"),
    Tickers(CoinPair("xbt", "eur"), "https://api.itbit.com/v1/markets/XBTEUR/ticker")
  )

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    Future.traverse(tickers) { ticker =>
      HttpClientFactory.get(httpClient, ticker.url).map { res =>
        val data = parse(res.getResponseBody).values.asInstanceOf[Map[String, String]]
        val volume = data.getOrElse("volume24h", "0.0")
        val lastPrice = data.getOrElse("lastPrice", "0.0")

        CoinTicker(id, ticker.coinPair, BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

}

object Itbit {

  def apply(hc: AsyncHttpClient): Itbit = new Itbit() {

    override val httpClient: AsyncHttpClient = hc

  }

}