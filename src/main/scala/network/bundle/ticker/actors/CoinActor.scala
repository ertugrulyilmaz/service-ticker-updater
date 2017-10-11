package network.bundle.ticker.actors

import akka.actor.{Actor, Props}
import com.typesafe.scalalogging.StrictLogging
import network.bundle.ticker.models.Model.CoinTicker
import network.bundle.ticker.models.Tables.Coin
import network.bundle.ticker.services.CoinService


object CoinActor {

  def props(service: CoinService) = Props(new CoinActor(service))

}

class CoinActor(service: CoinService) extends Actor with StrictLogging {

  import context.dispatcher

  override def receive = {
    case coinTicker: CoinTicker => service.saveExchange(coinTicker)
    case coin: Coin => service.saveCoin(coin)
  }

}
