package app

import api.data.modules.FatCustomer
import api.data.service.CustomerService
import app.Runner.customerService

import scala.annotation.tailrec
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, MILLISECONDS}
import scala.concurrent.{Await, Future}
import scala.io.StdIn.readLine
import scala.util.{Failure, Success}

object Runner extends App {

  val customerService = new CustomerService(slick.jdbc.MySQLProfile)

  val menu: String =
    s"MENU\n${"=" * 10}\nREGISTER: R\nLOGIN: L\n"

  runnerLoop()

  @tailrec
  def runnerLoop(): Unit = {
    println(s"WELCOME\n")

    println(menu)
    val in = readLine()

    in.toUpperCase match {
      case "R" => {
        register()
      }
      case "L" => {
        println("EMAIL: ")
        val email = readLine()
        println("PASSWORD: ")
        val password = readLine()

        val user = Await.result(customerService.login(email, password), Duration(1000, MILLISECONDS))

        user match {
          case Some(value) => loggedInLoop(value)
          case None => println("LOGIN UNSUCCESSFUL")
        }
      }
      case _ => println("Invalid input")
    }

    if (!in.equalsIgnoreCase("Q")) runnerLoop()
  }

  def loggedInLoop(user: FatCustomer): Unit = {
    println(s"${user.forename} ${user.surname} LOGGED IN SUCCESSFULLY!\n\n")
    do {
      println("CURRENT MOVIES - M\nVIEW AVAILABLE BOOKINGS - V\nBOOK TICKETS - B\nLOG OUT - L")
      val in = readLine()


    } while(true)
  }


  def register(): Unit = {
    println("FORENAME: ")
    val forename = readLine()
    println("SURNAME: ")
    val surname = readLine()
    println("EMAIL: ")
    val email = readLine()
    println("PASSWORD: ")
    val password = readLine()

    val newCustomer = customerService.create(FatCustomer(forename, surname, email, password))
    newCustomer.onComplete { c =>
      c match {
        case Failure(exception) => println("Something went wrong creating your account...")
        case Success(value) => {
          println(s"SUCCESS\nCUSTOMER ACCOUNT FOR ${value.email} CREATED SUCCESSFULLY...")
          println(s"PLEASE LOGIN ${value.forename} TO ACCESS THE SYSTEM\n\n")
        }
      }
    }
  }
}
