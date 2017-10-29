package network.bundle.ticker.repositories

import com.typesafe.scalalogging.StrictLogging
import network.bundle.ticker.datasources.MysqlDataSource
import network.bundle.ticker.models.Tables.ExchangeCoin
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait ExchangeCoinRepository extends StrictLogging {

  val dataSource: MysqlDataSource

  def save(exchangeCoin: ExchangeCoin)(implicit ec: ExecutionContext): Unit = {
    val insertAction =
      sqlu"""
         INSERT INTO exchange_coin
             (exchange, market, asset, currency, price, volume, updated_at)
         VALUES
             (
              ${exchangeCoin.exchange},
              ${exchangeCoin.market},
              ${exchangeCoin.asset},
              ${exchangeCoin.currency},
              ${exchangeCoin.price},
              ${exchangeCoin.volume},
              ${exchangeCoin.updatedAt}
          )
          ON DUPLICATE KEY UPDATE
             market = ${exchangeCoin.market},
             price = ${exchangeCoin.price},
             volume = ${exchangeCoin.volume},
             updated_at = ${exchangeCoin.updatedAt}
             ;
          """

    dataSource.db.run(insertAction).onComplete {
      case Success(_) => logger.debug("{}", exchangeCoin)
      case Failure(ex) => logger.error("{} {}", exchangeCoin, ex)
    }
  }

}

object ExchangeCoinRepository {

  def apply(_dataSource: MysqlDataSource): ExchangeCoinRepository = new ExchangeCoinRepository() {
    override val dataSource: MysqlDataSource = _dataSource
  }

}
