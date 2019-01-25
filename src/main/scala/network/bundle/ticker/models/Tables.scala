package network.bundle.ticker.models

import slick.jdbc.MySQLProfile.api._

object Tables {

  case class Coin(symbol: String, price: BigDecimal, updatedAt: Long)
  case class ExchangeCoin(exchangeId: Long, market: String, asset: String, currency: String, price: BigDecimal, volume: BigDecimal, updatedAt: Long)

  case class OrderBook(asset: String, side: String, exchange: Long, price: BigDecimal, amount: BigDecimal)

  class CoinTable(_tag: Tag) extends Table[Coin](_tag, "coin") {
    def symbol = column[String]("symbol", O.PrimaryKey)
    def price = column[BigDecimal]("price")
    def updatedAt = column[Long]("updated_at")

    def * = (symbol, price, updatedAt) <> (Coin.tupled, Coin.unapply)
  }

  class ExchangeCoinTable(_tag: Tag) extends Table[ExchangeCoin](_tag, "exchange_coin") {
    def exchangeId = column[Long]("exchange_id")
    def market = column[String]("market")
    def asset = column[String]("asset")
    def currency = column[String]("currency")
    def price = column[BigDecimal]("price")
    def volume = column[BigDecimal]("volume")
    def updatedAt = column[Long]("updated_at")

    def * = (exchangeId, market, asset, currency, price, volume, updatedAt) <> (ExchangeCoin.tupled, ExchangeCoin.unapply)
  }

  class OrderBookTable(_tag: Tag) extends Table[OrderBook](_tag, "order_book") {
    def asset = column[String]("asset")
    def side = column[String]("side")
    def exchange = column[Long]("exchange")
    def price = column[BigDecimal]("price")
    def amount = column[BigDecimal]("amount")

    def * = (asset, side, exchange, price, amount) <> (OrderBook.tupled, OrderBook.unapply)
  }

  val coins = TableQuery[CoinTable]
  val exchangeCoins = TableQuery[ExchangeCoinTable]
  val orderBooks = TableQuery[OrderBookTable]

}
