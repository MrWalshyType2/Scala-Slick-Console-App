package api.data.service

import api.data.{CustomerLayer, modules}
import api.data.modules.{CustomerModule, FatCustomer, PK}
import slick.jdbc.JdbcProfile

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CustomerService(profile: JdbcProfile) {

  val customerDbLayer = new CustomerLayer(profile)

  def readAll(): Future[Option[Seq[FatCustomer]]] = ???

  def update(element: FatCustomer): Future[Int] = ???

  def create(element: FatCustomer): Future[FatCustomer] = {
    val customerLinkPK = customerDbLayer.customerInterface.register(element)
    customerLinkPK.flatMap(links => {
      customerDbLayer.customerInterface.readCustomerByLinkTableId(links.value)
    })
  }

  def read(id: Long): Future[FatCustomer] = {
    customerDbLayer.customerInterface.readCustomerById(id)
  }
}
