package cz.loono.backend.db.repository

import cz.loono.backend.db.model.ServerProperties
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ServerPropertiesRepository : CrudRepository<ServerProperties, Long> {

    override fun findAll(): List<ServerProperties>

    @Query("SELECT sp.superUserName as superUserName, sp.superUserPassword as superUserPassword FROM ServerProperties AS sp")
    fun getSuperUserNameAndPassword(): List<SuperUser>

    @Query("SELECT sp.updateInterval as updateInterval FROM ServerProperties AS sp")
    fun getUpdateInterval(): List<OpenDataProperties>
}


interface SuperUser {
    var superUserName: String
    var superUserPassword: String
}

interface OpenDataProperties {
    var updateInterval: String
}
