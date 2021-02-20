package api.data.service

import api.data.modules.PK

import scala.concurrent.Future

trait Service[A] {

  def readAll(): Future[Option[Seq[A]]]

  def readAll[B](search: B): Future[Option[Seq[A]]]

  def read(id: PK[A]): Future[Option[A]]

  def create(element: A): Future[PK[A]]

  def update(element: A): Future[Int]

}
