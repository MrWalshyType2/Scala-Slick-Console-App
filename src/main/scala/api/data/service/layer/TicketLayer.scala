package api.data.service.layer

import api.data.Profile
import api.data.modules.TicketModule
import slick.jdbc.JdbcProfile

class TicketLayer(val profile: JdbcProfile) extends Profile with TicketModule {

}
