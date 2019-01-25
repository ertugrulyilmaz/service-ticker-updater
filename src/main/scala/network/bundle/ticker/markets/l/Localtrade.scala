package network.bundle.ticker.markets.l

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.jsoup.Jsoup

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @note this site has no api. market links are below.
  * @see https://localtrade.pro/trade
  * @see https://localtrade.pro/trade/9/
  * @see markets ids are 2, 3, 4, 6, 7, 8, 9, 10, 11, 14, 15, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27
  */
trait Localtrade extends BaseMarket {

  val url = "https://localtrade.pro/trade"

  val marketIds = immutable.Seq(2, 3, 4, 6, 7, 8, 9, 10, 11, 14, 15, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27)

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    Future.traverse(marketIds) { marketId =>
      HttpClientFactory.get(httpClient, s"$url/$marketId/").map { res =>
        val doc = Jsoup.parse(res.getResponseBodyAsStream, "utf-8", "https://localtrade.pro")
        val pair = doc.select("div.mtrade__tradepair > a.active").text().toLowerCase.split(" / ")
        val asset = pair.head
        val currency = pair.last
        val volume = doc.select(".b_graph__vars-item_val > span").text()
        val lastPrice = doc.select(".b_graph__vars-item_last > span").text()

        CoinTicker(id, CoinPair(asset, currency), BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

}

object Localtrade {

  def apply(hc: AsyncHttpClient): Localtrade = new Localtrade() {

    override val httpClient: AsyncHttpClient = hc

  }

}
