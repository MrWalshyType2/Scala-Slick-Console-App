package api.data.service.layer

import api.data.Profile
import api.data.modules.CustomerModule
import slick.jdbc.JdbcProfile

class CustomerLayer(val profile: JdbcProfile) extends Profile with CustomerModule {

}
