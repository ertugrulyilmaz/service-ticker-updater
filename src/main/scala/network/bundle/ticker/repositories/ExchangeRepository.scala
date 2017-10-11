package network.bundle.ticker.repositories

import com.typesafe.scalalogging.StrictLogging
import network.bundle.ticker.datasources.MysqlDataSource
import network.bundle.ticker.models.Tables.ExchangeCoin
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait ExchangeRepository extends StrictLogging {

  val dataSource: MysqlDataSource

  def save(exchangeCoin: ExchangeCoin)(implicit ec: ExecutionContext): Unit = {
    val insertAction =
      sqlu"""
         INSERT INTO exchange_coin
             (exchange, asset, currency, price, volume, updated_at)
         VALUES
             (
              ${exchangeCoin.exchange},
              ${exchangeCoin.asset},
              ${exchangeCoin.currency},
              ${exchangeCoin.price},
              ${exchangeCoin.volume},
              ${exchangeCoin.updatedAt}
          )
          ON DUPLICATE KEY UPDATE
             price = ${exchangeCoin.price},
             volume = ${exchangeCoin.volume},
             updated_at = ${exchangeCoin.updatedAt}
             ;
          """

    dataSource.db.run(insertAction).onComplete {
      case Success(_) => logger.info("{}", exchangeCoin)
      case Failure(ex) => logger.error("{} {}", exchangeCoin, ex)
    }
  }

}

object ExchangeRepository {

  def apply(_dataSource: MysqlDataSource): ExchangeRepository = new ExchangeRepository() {
    override val dataSource: MysqlDataSource = _dataSource
  }

}
