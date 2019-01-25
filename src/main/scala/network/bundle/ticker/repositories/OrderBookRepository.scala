package network.bundle.ticker.repositories

import com.typesafe.scalalogging.StrictLogging
import network.bundle.ticker.datasources.MysqlDataSource
import network.bundle.ticker.models.Tables
import network.bundle.ticker.models.Tables.OrderBook
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait OrderBookRepository extends StrictLogging {

  val dataSource: MysqlDataSource

  def deleteByExchange(exchange: Long)(implicit ec: ExecutionContext): Unit = {
    val q = for {
      t <- Tables.orderBooks if t.exchange === exchange
    } yield t

    dataSource.db.run(q.delete).onComplete {
      case Success(_) => logger.info("DELETED: {}", exchange)
      case Failure(e) => logger.info("DELETED Exception: {}", e)
    }
  }

  def save(orderBooks: Seq[OrderBook])(implicit ec: ExecutionContext): Unit = {
    dataSource.db.run(DBIO.seq(Tables.orderBooks ++= orderBooks)).onComplete {
      case Success(_) => logger.info("SAVED: {}", orderBooks)
      case Failure(e) => logger.info("SAVED Exception: {}", e)
    }
  }

}

object OrderBookRepository {

  def apply(ds: MysqlDataSource): OrderBookRepository = new OrderBookRepository {
    override val dataSource: MysqlDataSource = ds
  }

}
