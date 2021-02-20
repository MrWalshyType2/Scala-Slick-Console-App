package api.data.modules

import api.data.{Profile, modules}

import scala.concurrent.Future
import scala.util.Try

trait MovieModule { self: Profile =>

  import profile.api._

  implicit val ageRatingType = {
    MappedColumnType.base[AgeRating, String](
      rating => rating match {
        case AGE_U => "U"
        case AGE_PG => "PG"
        case AGE_12A => "12A"
        case AGE_15 => "15"
        case AGE_18 => "18"
      },
      code => code match {
        case "U" => AGE_U
        case "PG" => AGE_PG
        case "12A" => AGE_12A
        case "15" => AGE_15
        case "18" => AGE_18
      }
    )
  }

  implicit class MovieQueryOps(movie: MovieModule#MovieTable) {

    def isUniversalRated = movie.ageRating === (AGE_U: AgeRating)
    def isPgRated = movie.ageRating === (AGE_PG: AgeRating)
    def isTwelveRated = movie.ageRating === (AGE_12A: AgeRating)
    def isFifteenRated = movie.ageRating === (AGE_15: AgeRating)
    def isEighteenRated = movie.ageRating === (AGE_18: AgeRating)

  }

  // TODO: Price needs its own Table, adults, seniors, students, children
  case class Movie(title: String, description: String, price: Int, ageRating: AgeRating, id: PK[MovieTable])

  final class MovieTable(tag: Tag) extends Table[Movie](tag, "movies") {

    // Columns & (Name, Constraints*)
    // def name = column[DATA-TYPE]("COLUMN_NAME", column_settings*)
    def id = column[PK[MovieTable]]("ID", O.PrimaryKey, O.AutoInc)
    def title = column[String]("TITLE", O.Length(32, true), O.Unique) // O.Length(maxLength, varchar = true | char = false)
    def description = column[String]("DESCRIPTION", O.Length(256, true))
    def price = column[Int]("PRICE")
    def ageRating  = column[AgeRating]("AGE_RATING")

    // 'default projection' for mapping between columns in the table and
    // instances of the case class.
    //  - mapTo creates a two-way mapping between the fields in User and the database columns in UserTable
    //  def * = (id, fName, lName, age) <>(User.tupled, User.unapply)
    def * = (title, description, price, ageRating, id).mapTo[Movie]
  }

  object movies extends TableQuery(new MovieTable(_)) {

    val db = Database.forConfig("mysqlDB")

    lazy val numOfMovies = this.distinct.size
    lazy val dropMoviesTable = this.schema.dropIfExists
    lazy val createMoviesTable = this.schema.createIfNotExists
//    lazy val initialDataInsert = this ++= initialData
//
//    def initialData = Seq(
//      Movie("Fake", "Fake movie", 1500, AGE_PG, PK[MovieTable](0)),
//      Movie("Fake Four", "Fake movie 4", 1100, AGE_18, PK[MovieTable](0)),
//      Movie("Fake Three", "Fake movie 3", 1300, AGE_PG, PK[MovieTable](0))
//    )

    def readAllMovies(): Future[Try[Seq[Movie]]] = {
      db.run {
        this.result.asTry
      }
    }

    def readAllMovies(ageRating: AgeRating): Future[Try[Seq[Movie]]] = {
      db.run {
        ageRating match {
          case modules.AGE_U => this.filter(_.isUniversalRated).result.asTry
          case modules.AGE_PG => this.filter(_.isPgRated).result.asTry
          case modules.AGE_12A => this.filter(_.isTwelveRated).result.asTry
          case modules.AGE_15 => this.filter(_.isFifteenRated).result.asTry
          case modules.AGE_18 => this.filter(_.isEighteenRated).result.asTry
        }
      }
    }

    // id is type safe
    def readMovie(id: PK[MovieTable]): Future[Option[Movie]] = {
      // PK is a value type of `MovieTable` that passes its 'value' of type Long when used
      db.run {
        this.filter(_.id === id).result.headOption
      }
    }

    def createMovie(movie: Movie): Future[PK[MovieTable]] = {
      val insertOfMoviePk: DBIO[PK[MovieTable]] = this returning this.map(_.id) += movie
      db.run(insertOfMoviePk)
    }

    def updateMovie(movie: Movie): Future[Int] = {
      // Nice action, no multi-future hell
      val updateQuery = this.filter(m => m.id === movie.id)
                            .insertOrUpdate(movie)
      db.run(updateQuery)
    }
  }
}
