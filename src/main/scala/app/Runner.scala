package app

import api.data.modules.FatCustomer
import api.data.service.CustomerService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.StdIn.readLine

object Runner extends App {

  val customerService = new CustomerService(slick.jdbc.MySQLProfile)

  val menu: String =
    s"MENU\n${"=" * 10}\nREGISTER: R\nLOGIN: L\n"

  println(s"WELCOME\n")

  do {

    println(menu)
    val in = readLine()

    in match {
      case "R" => {
        println("FORENAME: ")
        val forename = readLine()
        println("SURNAME: ")
        val surname = readLine()
        println("EMAIL: ")
        val email = readLine()
        println("PASSWORD: ")
        val password = readLine()

        val newCustomer = customerService.create(FatCustomer(forename, surname, email, password))
        newCustomer.onComplete(println)
      }
      case _ => println("Invalid input")
    }

  } while (true)


}
