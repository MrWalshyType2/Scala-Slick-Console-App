package api.data.service.layer

import api.data.Profile
import api.data.modules.{CustomerModule, MovieModule}
import slick.jdbc.JdbcProfile

class DatabaseLayer(val profile: JdbcProfile) extends Profile with MovieModule with CustomerModule {

}
