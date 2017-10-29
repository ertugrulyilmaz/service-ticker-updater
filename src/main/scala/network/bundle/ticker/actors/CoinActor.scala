package network.bundle.ticker.actors

import akka.actor.{Actor, Props}
import com.typesafe.scalalogging.StrictLogging
import network.bundle.ticker.models.Model.{CoinTicker, Decrease, Increase}
import network.bundle.ticker.models.Tables.Coin
import network.bundle.ticker.services.CoinService


object CoinActor {

  def props(service: CoinService) = Props(new CoinActor(service))

}

class CoinActor(service: CoinService) extends Actor with StrictLogging {

  import context.dispatcher

  var counter = 0L

  override def receive = {
    case coinTicker: CoinTicker => service.saveExchange(coinTicker)
    case coin: Coin => service.saveCoin(self, coin)
    case Increase =>
      counter += 1
      logger.info("COUNTER = {}", counter)
    case Decrease =>
      counter -= 1
      logger.info("COUNTER = {}", counter)

      if (counter == 0) {
        context.system.terminate().onComplete {
          case _ =>
            logger.info("SHUTDOWN COUNTER = {}", counter)
            System.exit(0)
        }
      }
  }

}
