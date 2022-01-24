package cz.loono.backend.security.basic

import cz.loono.backend.db.repository.ServerPropertiesRepository
import cz.loono.backend.db.repository.SuperUser
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.stereotype.Service

@Service
class SuperUserDetailsService(
    private val serverPropertiesRepository: ServerPropertiesRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val superUser: SuperUser = serverPropertiesRepository.getSuperUserNameAndPassword().firstOrNull()
            ?: throw NullPointerException("Superuser $username not found.")
        return SuperUserDetails(
            username = superUser.superUserName,
            password = superUser.superUserPassword
        )
    }
}

data class SuperUserDetails(
    private val username: String,
    private val password: String,
    private val isEnabled: Boolean = true, // Disabled account can not log in
    private val isCredentialsNonExpired: Boolean = true, // credential can be expired,eg. Change the password every three months
    private val isAccountNonExpired: Boolean = true, // eg. Demo account（guest） can only be online  24 hours
    private val isAccountNonLocked: Boolean = true, // eg. Users who malicious attack system,lock their account for one year
    private val authorities: Set<GrantedAuthority> = setOf(SimpleGrantedAuthority("ROLE_ADMIN")),
) : UserDetails {
    override fun getUsername(): String = username
    override fun getPassword(): String = password
    override fun isEnabled(): Boolean = isEnabled
    override fun isCredentialsNonExpired(): Boolean = isCredentialsNonExpired
    override fun isAccountNonExpired(): Boolean = isAccountNonExpired
    override fun isAccountNonLocked(): Boolean = isAccountNonLocked
    override fun getAuthorities(): Set<GrantedAuthority> = authorities
}
