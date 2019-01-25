package network.bundle.ticker.markets.y

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below
  * @see https://yunbi.com/documents/api/guide
  * @see https://yunbi.com/api/v2/markets
  * @see https://yunbi.com/api/v2/tickers.json
  * @see https://yunbi.com/api/v2/tickers/btscny.json
  */
trait Yunbi extends BaseMarket {

  val url = "https://yunbi.com/api/v2/tickers.json"

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      parse(res.getResponseBody).values.asInstanceOf[Map[String, Map[String, Map[String, String]]]]
        .map { pair =>
          val data = pair._2("ticker")
          val volume = data("vol")
          val lastPrice = data("last")

          CoinTicker(id, CoinPair(pair._1.replace("cny", ""), "cny"), BigDecimal(volume), BigDecimal(lastPrice))
        }.to[immutable.Seq]
    }
  }

}

object Yunbi {

  def apply(hc: AsyncHttpClient): Yunbi = new Yunbi() {

    override val httpClient: AsyncHttpClient = hc

  }

}