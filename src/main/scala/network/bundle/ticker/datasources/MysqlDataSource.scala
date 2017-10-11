package network.bundle.ticker.datasources

import com.typesafe.config.ConfigFactory
import com.zaxxer.hikari.HikariDataSource
import slick.jdbc.MySQLProfile.api._

trait MysqlDataSource {

  val db: Database

}

object MysqlDataSource {

  def apply(): MysqlDataSource = new MysqlDataSource() {
    val config = ConfigFactory.load()

    val maxConnections = Some(config.getInt("data-source.mysql.maxConnections"))

    val ds = new HikariDataSource()
    ds.setJdbcUrl(config.getString("data-source.mysql.jdbcUrl"))
    ds.setUsername(config.getString("data-source.mysql.username"))
    ds.setPassword(config.getString("data-source.mysql.password"))

    override val db = Database.forDataSource(ds, maxConnections, AsyncExecutor.default())

  }

}