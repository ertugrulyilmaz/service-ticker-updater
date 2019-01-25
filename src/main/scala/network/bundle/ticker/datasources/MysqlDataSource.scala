package network.bundle.ticker.datasources

import com.typesafe.config.Config
import com.zaxxer.hikari.HikariDataSource
import slick.jdbc.MySQLProfile.api._

trait MysqlDataSource {

  val db: Database

}

object MysqlDataSource {

  def apply(config: Config): MysqlDataSource = new MysqlDataSource() {
    val maxConnections = Some(config.getInt("data-source.mysql.maxConnections"))

    Class.forName("com.mysql.cj.jdbc.Driver")

    val ds = new HikariDataSource()
    ds.setJdbcUrl(config.getString("data-source.mysql.jdbcUrl"))
    ds.setUsername(config.getString("data-source.mysql.username"))
    ds.setPassword(config.getString("data-source.mysql.password"))
    ds.setMaximumPoolSize(150)

    override val db: Database = Database.forDataSource(ds, maxConnections, AsyncExecutor("mysql-slick", numThreads = 150, queueSize = 1000))

  }

}