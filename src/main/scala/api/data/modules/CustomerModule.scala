package api.data.modules

import api.data.{Profile, modules}
import slick.jdbc.JdbcProfile
import slick.migration.api._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

case class FatCustomer(forename: String, surname: String, email: String, password: String, id: Long = 0L)

trait CustomerModule { self: Profile =>

  import profile.api._

  val db = Database.forConfig("mysqlDB")

  case class Customer(forename: String, surname: String, role: Option[String] = None, id: PK[CustomerTable] = PK(0))
  case class CustomerLink(customerId: PK[CustomerTable], customerLoginId: PK[CustomerLoginTable], id: PK[CustomerLinkTable] = PK(0))
  case class CustomerLogin(email: String, password: String, id: PK[CustomerLoginTable] = PK(0))

  final class CustomerTable(tag: Tag) extends Table[Customer](tag, "customers") {

    def id = column[PK[CustomerTable]]("ID", O.PrimaryKey, O.AutoInc)
    def forename = column[String]("FORENAME")
    def surname = column[String]("SURNAME")
    def role = column[Option[String]]("ROLE")

    def * = (forename, surname, role, id).mapTo[Customer]
  }

  final class CustomerLinkTable(tag: Tag) extends Table[CustomerLink](tag, "customer_details") {

    def id = column[PK[CustomerLinkTable]]("ID", O.PrimaryKey, O.AutoInc)
    def customerId = column[PK[CustomerTable]]("CUSTOMER_ID")
    def customerLoginId = column[PK[CustomerLoginTable]]("CUSTOMER_LOGIN_ID")

    // A customer must exist to insert a record in this table
    def customer = foreignKey("CUSTOMER_FK", customerId, customers)(_.id)
    def customerLogin = foreignKey("CUSTOMER_LOGIN_FK", customerLoginId, customerLogins)(_.id)

    def * = (customerId, customerLoginId, id).mapTo[CustomerLink]
  }

  final class CustomerLoginTable(tag: Tag) extends Table[CustomerLogin](tag, "customer_login") {

    def id = column[PK[CustomerLoginTable]]("ID", O.PrimaryKey, O.AutoInc)
    def email = column[String]("EMAIL", O.Unique)
    def password = column[String]("PASSWORD")

    def * = (email, password, id).mapTo[CustomerLogin]
  }

  private object customers extends TableQuery(new CustomerTable(_)) {

    lazy val numOfCustomers = this.distinct.size
    lazy val dropCustomersTable = this.schema.dropIfExists
    lazy val createCustomersTable = this.schema.createIfNotExists

    def readAllCustomers = this.result
    def readCustomer(id: PK[CustomerTable]) = this.filter(_.id === id).result.headOption
    def createCustomer(customer: Customer) = this returning this.map(_.id) += customer
    def updateCustomer(customer: Customer) = this.filter(c => c.id === customer.id).insertOrUpdate(customer)
    def deleteCustomer(id: PK[CustomerTable]) = this.filter(_.id === id).delete
  }

  private object customerLogins extends TableQuery(new CustomerLoginTable(_)) {

    lazy val numOfCustomerLogins = this.distinct.size
    lazy val dropCustomerLoginsTable = this.schema.dropIfExists
    lazy val createCustomerLoginsTable = this.schema.createIfNotExists

    def readAllCustomerLogins = this.result
    def readCustomerLogin(id: PK[CustomerLoginTable]) = this.filter(_.id === id).result.headOption
    def createCustomerLogin(customer: CustomerLogin) = this returning this.map(_.id) += customer
    def updateCustomerLogin(customer: CustomerLogin) = this.filter(c => c.id === customer.id).insertOrUpdate(customer)
    def deleteCustomerLogin(id: PK[CustomerLoginTable]) = this.filter(_.id === id).delete
  }

  private object customerLinks extends TableQuery(new CustomerLinkTable(_)) {

    lazy val numOfCustomerLinks = this.distinct.size
    lazy val dropCustomerLinksTable = this.schema.dropIfExists
    lazy val createCustomerLinksTable = this.schema.createIfNotExists

    def readAllCustomerLinks = this.result
    def readCustomerLinks(id: PK[CustomerLinkTable]) = this.filter(_.id === id).result.headOption
    def createCustomerLinks(customer: CustomerLink) = this returning this.map(_.id) += customer
    def updateCustomerLinks(customer: CustomerLink) = this.filter(c => c.id === customer.id).insertOrUpdate(customer)
    def deleteCustomerLinks(id: PK[CustomerLinkTable]) = this.filter(_.id === id).delete
  }

  object customerInterface {

    def createDbs = {
      val seq = DBIO.seq(customers.createCustomersTable,
                         customerLogins.createCustomerLoginsTable,
                         customerLinks.createCustomerLinksTable).transactionally
      db.run(seq)
    }

    def register(c: FatCustomer) = {
      val savedCustomerPk = customers.createCustomer(Customer(c.forename, c.surname))
      val savedCustomerLoginPk = customerLogins.createCustomerLogin(CustomerLogin(c.email, c.password))

      val insertedCustomerLinksTable = savedCustomerPk.flatMap(customerPk => {
        savedCustomerLoginPk.flatMap(loginPk => {
          customerLinks.createCustomerLinks(CustomerLink(customerPk, loginPk))
        })
      })

      db.run(insertedCustomerLinksTable.transactionally)
    }

    def readAllCustomers() = {

      val query = for {
        link <- customerLinks
        customer <- link.customer
        customerLogin <- link.customerLogin
      } yield (customer.forename, customer.surname, customerLogin.email, customerLogin.password)

      val fatCustomerQuery = query.result.map(c => {
        c.map(cl => FatCustomer(cl._1, cl._2, cl._3, cl._4))
      })

      db.run(fatCustomerQuery)
    }

    def readCustomerByLinkTableId(id: Long) = {
      val links = customerLinks.filter(_.id === PK[CustomerLinkTable](id)).result.head

      val query = links.flatMap(l => {
        createFatCustomerFromLinks(l)
      })

      db.run(query.transactionally)
    }

    def readCustomerById(id: Long) = {
      val links = customerLinks.filter(_.customerId === PK[CustomerTable](id)).result.head

      val query = links.flatMap(l => {
        createFatCustomerFromLinks(l)
      })

      db.run(query.transactionally)
    }

    def readCustomerByEmail(email: String): Future[Try[FatCustomer]] = {
      val customerLoginDetails = customerLogins.filter(_.email === email)

      val cLinks = customerLoginDetails.result.head.flatMap(l => {
        customerLinks.filter(_.customerLoginId === l.id).result.head
      })

      val query = cLinks.flatMap(l => {
        createFatCustomerFromLinks(l)
      })

      db.run(query.transactionally.asTry)
    }

    private def createFatCustomerFromLinks(links: CustomerLink) = {

      val q = for {
        (c, cl) <- customers join customerLogins on { (cT, clT) =>
          (cT.id === links.customerId && clT.id === links.customerLoginId)
        }
      } yield (c.forename, c.surname, cl.email, cl.password)

      q.result.map { (c) =>
        // eww
        FatCustomer(c.head._1, c.head._2, c.head._3, c.head._4)
      }

    }

  }
}
