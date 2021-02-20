package api.data.service

import api.data.service.layer.TicketLayer
import slick.jdbc.JdbcProfile

class TicketService(profile: JdbcProfile) {

  val ticketLayer = new TicketLayer(profile)
}
