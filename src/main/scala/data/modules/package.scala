package data

import slick.lifted.{MappedTo, Query, Rep}
import slick.memory.MemoryProfile.MappedColumnType

package object modules {

  sealed trait AgeRating
  case object AGE_U extends AgeRating
  case object AGE_PG extends AgeRating
  case object AGE_12A extends AgeRating
  case object AGE_15 extends AgeRating
  case object AGE_18 extends AgeRating

  case class PK[A](value: Long) extends AnyVal with MappedTo[Long]

//  object AgeRating {
//
//    val universal: AgeRating = AGE_U
//    val parentalGuidance: AgeRating = AGE_PG
//    val twelveAndOver: AgeRating = AGE_12A
//    val fifteenAndOver: AgeRating = AGE_15
//    val eighteenAndOver: AgeRating = AGE_18
//  }

}
