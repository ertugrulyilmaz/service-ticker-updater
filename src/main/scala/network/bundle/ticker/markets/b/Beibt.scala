package network.bundle.ticker.markets.b

import com.ning.http.client.AsyncHttpClient
import network.bundle.ticker.async.HttpClientFactory
import network.bundle.ticker.markets.BaseMarket
import network.bundle.ticker.models.Model.{CoinPair, CoinTicker}
import org.json4s.jackson.JsonMethods._

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

/**
  * @todo site timoute verdigi icin devam edilemedi.
  * @note links are below. Currency method should call POST. request body should contain cid. available cids are 31, 32, 33, 34, 37, 38
  * @see http://beibt.com/Index/CurrencyList.html
  * @see http://beibt.com/Api/getCurrencyBy.html
  */
trait Beibt extends BaseMarket {

  val url = "http://beibt.com/Index/CurrencyList.html"

  override def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = {
    HttpClientFactory.get(httpClient, url).map { res =>
      parse(res.getResponseBody).values.asInstanceOf[List[Map[String, Any]]].map { data =>
        val asset = if (data("currency_mark").toString.trim != "") data("currency_mark").toString.toLowerCase else data("currency_name").toString.toLowerCase
        val currency = "cny"
        val volume = data("24H_done_money").toString.replace("万", "")
        val lastPrice = data("buy_one_price").toString

        CoinTicker(id, CoinPair(asset, currency), BigDecimal(volume), BigDecimal(lastPrice))
      }
    }
  }

}

object Beibt {

  def apply(hc: AsyncHttpClient): Beibt = new Beibt() {

    override val httpClient: AsyncHttpClient = hc

  }

}
