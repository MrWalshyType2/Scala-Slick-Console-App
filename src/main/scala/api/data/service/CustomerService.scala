package api.data.service

import api.data.modules
import api.data.modules.{CustomerModule, FatCustomer, PK}
import api.data.service.layer.CustomerLayer
import slick.jdbc.JdbcProfile

import scala.collection.immutable.{AbstractSeq, LinearSeq}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class CustomerService(profile: JdbcProfile) {

  val customerDbLayer = new CustomerLayer(profile)

  def login(email: String, password: String): Future[Option[FatCustomer]] = {
    read(email).map(c => {
      c match {
        case Failure(exception) => None
        case Success(value) => {
          if (value.email == email && value.password == password) Some(value) else None
        }
      }
    })
  }

  def readAll(): Future[Seq[FatCustomer]] = {
    customerDbLayer.customerInterface.readAllCustomers()
  }

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

  private def read(email: String): Future[Try[FatCustomer]] = {
    customerDbLayer.customerInterface.readCustomerByEmail(email)
  }
}
