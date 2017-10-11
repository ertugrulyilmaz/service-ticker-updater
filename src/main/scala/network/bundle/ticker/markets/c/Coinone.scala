package network.bundle.ticker.markets.c

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below
  * @see http://doc.coinone.co.kr
  * @see https://api.coinone.co.kr/currency/
  * @see https://api.coinone.co.kr/ticker/?currency=all
  */
trait Coinone extends BaseMarket {

  val url = "https://api.coinone.co.kr/ticker/?currency=all"

  override def values()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      parse(res.getResponseBody)
        .values
        .asInstanceOf[Map[String, Map[String, String]]]
        .filter(!_._1.contains("timestamp"))
        .filter(!_._1.contains("errorCode"))
        .filter(!_._1.contains("result"))
        .map { pair =>
          val asset = pair._1
          val volume = pair._2("volume")
          val lastPrice = pair._2("last")

          CoinTicker("coinone", CoinPair(asset, "btc"), BigDecimal(volume), BigDecimal(lastPrice))
        }.to[immutable.Seq]
    }
  }

}

object Coinone {

  def apply(hc: AsyncHttpClient): Coinone = new Coinone() {

    override val httpClient: AsyncHttpClient = hc

  }

}