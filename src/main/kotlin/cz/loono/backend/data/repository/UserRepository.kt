package cz.loono.backend.data.repository

import cz.loono.backend.data.model.User
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : CrudRepository<User, Long> {

    fun existsByUid(uid: String): Boolean
}
