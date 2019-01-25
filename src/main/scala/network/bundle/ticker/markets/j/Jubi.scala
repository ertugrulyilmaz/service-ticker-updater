package network.bundle.ticker.markets.j

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note documentation is below. assets are btc, ltc, xas
  * @see https://www.jubi.com/help/api.html
  * @see https://www.jubi.com/api/v1/allticker/
  * @see https://www.jubi.com/api/v1/ticker/?coin=btc
  */
trait Jubi extends BaseMarket {

  val url = "https://www.jubi.com/api/v1/allticker/"

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      parse(res.getResponseBody)
        .values
        .asInstanceOf[Map[String, Map[String, Any]]]
        .map { data =>
          val asset = data._1
          val volume = data._2("volume").toString
          val lastPrice = data._2("last").toString

          CoinTicker(id, CoinPair(asset, "cny"), BigDecimal(volume), BigDecimal(lastPrice))
        }.to[immutable.Seq]
    }
  }

}

object Jubi {

  def apply(hc: AsyncHttpClient): Jubi = new Jubi() {

    override val httpClient: AsyncHttpClient = hc

  }

}