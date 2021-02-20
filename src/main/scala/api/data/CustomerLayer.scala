package api.data

import api.data.modules.CustomerModule
import slick.jdbc.JdbcProfile

class CustomerLayer(val profile: JdbcProfile) extends Profile with CustomerModule {

}
