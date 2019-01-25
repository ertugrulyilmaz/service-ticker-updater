package network.bundle.ticker.services

import com.typesafe.scalalogging.StrictLogging
import network.bundle.ticker.datasources.MysqlDataSource
import network.bundle.ticker.models.Model.ExchangeCoinOrders
import network.bundle.ticker.models.Tables.OrderBook
import network.bundle.ticker.repositories.OrderBookRepository

import scala.concurrent.ExecutionContext

trait OrderBookService extends StrictLogging {

  val orderBoookRepository: OrderBookRepository

  def filterOrders(exchangeCoinOrders: ExchangeCoinOrders): Boolean = {
    (exchangeCoinOrders.asks.orders.nonEmpty
      && exchangeCoinOrders.bids.orders.nonEmpty
      && exchangeCoinOrders.bids.pair.currency == "btc"
      && exchangeCoinOrders.asks.pair.currency == "btc")
  }

  def deleteByExchange(exchange: Long)(implicit ec: ExecutionContext): Unit = {
    orderBoookRepository.deleteByExchange(exchange)
  }

  def saveOrderBook(exchangeCoinOrders: Seq[ExchangeCoinOrders])(implicit ec: ExecutionContext): Unit = {
    val rows = exchangeCoinOrders.foldLeft[Seq[OrderBook]](Seq.empty[OrderBook]) { (acc: Seq[OrderBook], curr: ExchangeCoinOrders) =>
      val exchange = curr.exchange
      val asset = curr.asks.pair.asset

      val bids = curr.bids.orders
        .map { order =>
          OrderBook(asset, "sell", exchange, order.price, order.amount)
        }
        .sortWith(_.price > _.price)

      val asks = curr.asks.orders
        .map { order =>
          OrderBook(asset, "buy", exchange, order.price, order.amount)
        }
        .sortWith(_.price < _.price)

      acc ++ bids ++ asks
    }

    orderBoookRepository.save(rows)
  }

}

object OrderBookService {

  def apply(dataSource: MysqlDataSource): OrderBookService = new OrderBookService {
    override val orderBoookRepository = OrderBookRepository(dataSource)
  }

}
