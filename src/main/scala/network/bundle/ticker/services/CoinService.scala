package network.bundle.ticker.services

import java.sql.Date
import java.util.Calendar

import com.typesafe.scalalogging.StrictLogging
import network.bundle.ticker.datasources.MysqlDataSource
import network.bundle.ticker.models.Model.CoinTicker
import network.bundle.ticker.models.Tables.{Coin, ExchangeCoin}
import network.bundle.ticker.repositories.{CoinRepository, ExchangeRepository}

import scala.concurrent.ExecutionContext
import scala.math.BigDecimal.RoundingMode

trait CoinService extends StrictLogging {

  val exchangeRepository: ExchangeRepository
  val coinRepository: CoinRepository

  def saveExchange(ct: CoinTicker)(implicit executionContext: ExecutionContext): Unit = {
    val ec = ExchangeCoin(ct.exchange, ct.pair.asset, ct.pair.currency, ct.lastPrice.setScale(8, RoundingMode.HALF_UP), ct.volume.setScale(8, RoundingMode.HALF_UP), new Date(Calendar.getInstance().getTimeInMillis))
    exchangeRepository.save(ec)
  }

  def saveCoin(coin: Coin)(implicit ec: ExecutionContext): Unit = {
    coinRepository.save(coin.copy(price = coin.price.setScale(8, RoundingMode.HALF_UP)))
  }

}

object CoinService {

  def apply(dataSource: MysqlDataSource): CoinService = new CoinService() {
    override val exchangeRepository: ExchangeRepository = ExchangeRepository(dataSource)
    override val coinRepository: CoinRepository = CoinRepository(dataSource)
  }

}
