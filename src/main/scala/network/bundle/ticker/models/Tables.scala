package network.bundle.ticker.models

import slick.jdbc.MySQLProfile.api._

object Tables {

  case class Coin(symbol: String, price: BigDecimal, updatedAt: Long)
  case class ExchangeCoin(exchange: String, market: String, asset: String, currency: String, price: BigDecimal, volume: BigDecimal, updatedAt: Long)

  class CoinTable(_tag: Tag) extends Table[Coin](_tag, "coin") {
    def symbol = column[String]("symbol", O.PrimaryKey)
    def price = column[BigDecimal]("price")
    def updatedAt = column[Long]("updated_at")

    def * = (symbol, price, updatedAt) <> (Coin.tupled, Coin.unapply)
  }

  class ExchangeCoinTable(_tag: Tag) extends Table[ExchangeCoin](_tag, "exchange_coin") {
    def exchange = column[String]("exchange")
    def market = column[String]("market")
    def asset = column[String]("asset")
    def currency = column[String]("currency")
    def price = column[BigDecimal]("price")
    def volume = column[BigDecimal]("volume")
    def updatedAt = column[Long]("updated_at")

    def * = (exchange, market, asset, currency, price, volume, updatedAt) <> (ExchangeCoin.tupled, ExchangeCoin.unapply)
  }

  val coins = TableQuery[CoinTable]
  val exchangeCoins = TableQuery[ExchangeCoinTable]

}
