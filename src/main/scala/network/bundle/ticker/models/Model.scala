package network.bundle.ticker.models

import java.util.concurrent.Semaphore

import network.bundle.ticker.markets.BaseMarket


object Model {

  case class Market(exchange: BaseMarket)

  case class CoinPair(asset: String, currency: String, market: String = "") {
    def name: String = s"$asset-$currency"
  }

  case class CoinTicker(exchangeId: Long, pair: CoinPair, volume: BigDecimal, lastPrice: BigDecimal)
  case class Tickers(coinPair: CoinPair, url: String)

  case class Order(price: BigDecimal, amount: BigDecimal)
  case class CoinOrder(pair: CoinPair, orders: Seq[Order])
  case class ExchangeCoinOrders(exchange: Long, bids: CoinOrder, asks: CoinOrder)

  case object Increase
  case object Decrease

  case class ThrottleRequestFilter(maxWait: Long, politeness: Long, semaphore: Semaphore)

}
