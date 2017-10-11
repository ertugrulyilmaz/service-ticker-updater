package network.bundle.ticker.repositories

import com.typesafe.scalalogging.StrictLogging
import network.bundle.ticker.datasources.MysqlDataSource
import network.bundle.ticker.models.Tables
import network.bundle.ticker.models.Tables.Coin
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait CoinRepository extends StrictLogging {

  val dataSource: MysqlDataSource

  def save(coin: Coin)(implicit ec: ExecutionContext): Unit = {
    val q = for {
      t <- Tables.coins if t.symbol === coin.symbol
    } yield t.price

    dataSource.db.run(q.update(coin.price)).onComplete {
      case Success(_) => logger.info("{}", coin)
      case Failure(ex) => logger.error("{} {}", coin, ex)
    }

    logger.debug("{}", coin)
  }

}

object CoinRepository {

  def apply(_dataSource: MysqlDataSource): CoinRepository = new CoinRepository() {
    override val dataSource: MysqlDataSource = _dataSource
  }

}
