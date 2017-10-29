package network.bundle.ticker.repositories

import akka.actor.ActorRef
import com.typesafe.scalalogging.StrictLogging
import network.bundle.ticker.datasources.MysqlDataSource
import network.bundle.ticker.models.Model.Decrease
import network.bundle.ticker.models.Tables
import network.bundle.ticker.models.Tables.Coin
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait CoinRepository extends StrictLogging {

  val dataSource: MysqlDataSource

  def save(coinActor: ActorRef, coin: Coin)(implicit ec: ExecutionContext): Unit = {
    val q = for {
      t <- Tables.coins if t.symbol === coin.symbol
    } yield t.price

    dataSource.db.run(q.update(coin.price)).onComplete {
      case Success(_) =>
        logger.debug("{}", coin)
        coinActor ! Decrease
      case Failure(ex) =>
        logger.error("{} {}", coin, ex)
        coinActor ! Decrease
    }

    logger.debug("{}", coin)
  }

}

object CoinRepository {

  def apply(_dataSource: MysqlDataSource): CoinRepository = new CoinRepository() {
    override val dataSource: MysqlDataSource = _dataSource
  }

}
