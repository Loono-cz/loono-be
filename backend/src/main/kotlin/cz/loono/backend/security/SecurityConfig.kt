package cz.loono.backend.security

import cz.loono.backend.db.repository.ServerPropertiesRepository
import cz.loono.backend.security.basic.CustomBasicAuthenticationEntryPoint
import cz.loono.backend.security.basic.SuperUserDetailsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

@Configuration
@EnableWebSecurity
class SecurityConfig @Autowired constructor(
    private val authenticationEntryPoint: CustomBasicAuthenticationEntryPoint
) : WebSecurityConfigurerAdapter() {

    @Autowired
    private lateinit var serverPropertiesRepository: ServerPropertiesRepository

    @Bean
    override fun userDetailsService(): UserDetailsService {
        return SuperUserDetailsService(serverPropertiesRepository)
    }

    @Bean
    fun authProvider(): DaoAuthenticationProvider {
        val authProvider = DaoAuthenticationProvider()
        authProvider.setUserDetailsService(userDetailsService())
        authProvider.setPasswordEncoder(encoder())
        return authProvider
    }

    @Autowired
    fun configureGlobal(authentication: AuthenticationManagerBuilder) {
        authentication.userDetailsService(userDetailsService())
    }

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.authenticationProvider(authProvider())
    }

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
            .antMatchers("/providers/update").hasRole("ADMIN")
            .and()
            .httpBasic()
            .authenticationEntryPoint(authenticationEntryPoint)
            .and()
            .csrf().disable()
    }

    @Bean
    fun encoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}
