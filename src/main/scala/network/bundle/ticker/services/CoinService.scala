package network.bundle.ticker.services

import java.util.Calendar

import akka.actor.ActorRef
import com.typesafe.scalalogging.StrictLogging
import network.bundle.ticker.datasources.MysqlDataSource
import network.bundle.ticker.models.Model.CoinTicker
import network.bundle.ticker.models.Tables.{Coin, ExchangeCoin}
import network.bundle.ticker.repositories.{CoinRepository, ExchangeCoinRepository}

import scala.concurrent.ExecutionContext
import scala.math.BigDecimal.RoundingMode

trait CoinService extends StrictLogging {

  val exchangeCoinRepository: ExchangeCoinRepository
  val coinRepository: CoinRepository

  def saveExchange(ct: CoinTicker)(implicit executionContext: ExecutionContext): Unit = {
    val ec = ExchangeCoin(ct.exchangeId, ct.pair.market, ct.pair.asset, ct.pair.currency, ct.lastPrice.setScale(8, RoundingMode.HALF_DOWN), ct.volume.setScale(8, RoundingMode.HALF_DOWN), Calendar.getInstance().getTimeInMillis)
    exchangeCoinRepository.save(ec)
  }

  def saveCoin(coinActor: ActorRef, coin: Coin)(implicit ec: ExecutionContext): Unit = {
    coinRepository.save(coinActor, coin.copy(price = coin.price.setScale(8, RoundingMode.HALF_DOWN)))
  }

}

object CoinService {

  def apply(dataSource: MysqlDataSource): CoinService = new CoinService() {
    override val exchangeCoinRepository: ExchangeCoinRepository = ExchangeCoinRepository(dataSource)
    override val coinRepository: CoinRepository = CoinRepository(dataSource)
  }

}
