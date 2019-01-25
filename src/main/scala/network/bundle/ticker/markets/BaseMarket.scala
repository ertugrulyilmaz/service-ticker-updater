package network.bundle.ticker.markets

import com.ning.http.client.AsyncHttpClient
import com.typesafe.scalalogging.StrictLogging
import network.bundle.ticker.models.Model.{CoinTicker, ExchangeCoinOrders}

import scala.collection.immutable
import scala.concurrent.{ExecutionContext, Future}

trait BaseMarket extends StrictLogging {

  def id: Long = 0L

  val ORDER_LIMIT = 25
  val zero = BigDecimal(0.0)

  val httpClient: AsyncHttpClient

  def tickers()(implicit ec: ExecutionContext): Future[immutable.Seq[CoinTicker]] = Future(immutable.Seq.empty)

  def orders()(implicit ec: ExecutionContext): Future[immutable.Seq[ExchangeCoinOrders]] = Future(immutable.Seq.empty)

  def reformatCurrencyName(currency: String): String = currency match {
    case c if c == "usdt" => "usd"
    case c if c == "xbt" => "btc"
    case c => c
  }

}