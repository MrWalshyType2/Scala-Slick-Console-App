package api.data

import api.data.modules.MovieModule
import slick.jdbc.JdbcProfile

class DatabaseLayer(val profile: JdbcProfile) extends Profile with MovieModule {

}
