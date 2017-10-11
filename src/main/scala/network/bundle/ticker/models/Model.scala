package network.bundle.ticker.models

import network.bundle.ticker.markets.BaseMarket


object Model {

  case class Market(market: BaseMarket)

  case class CoinPair(asset: String, currency: String) {
    def name: String = s"$asset-$currency"
  }

  case class CoinTicker(exchange: String, pair: CoinPair, volume: BigDecimal, lastPrice: BigDecimal)

  case class Tickers(coinPair: CoinPair, url: String)

}
