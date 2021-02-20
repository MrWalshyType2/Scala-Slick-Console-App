package api.data.modules

import api.data.Profile

trait TicketModule { self: Profile =>

  import profile.api._

  case class Ticket(id: PK[TicketTable] = PK(0))
  case class TicketItem(id: PK[TicketTable], movieId: PK[MovieModule#MovieTable])

  final class TicketTable(tag: Tag) extends Table[Ticket](tag, "tickets") {

    def id = column[PK[TicketTable]]("ID", O.PrimaryKey, O.AutoInc)

    def * = (id).mapTo[Ticket]
  }
}
