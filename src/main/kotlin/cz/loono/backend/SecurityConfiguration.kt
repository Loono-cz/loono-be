package cz.loono.backend

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter

@Configuration
@EnableWebSecurity
class SecurityConfiguration : WebSecurityConfigurerAdapter() {

    public override fun configure(http: HttpSecurity) {
        http.authorizeRequests()
            .antMatchers("/actuator/**").hasRole("ACTUATOR")
            .anyRequest().permitAll()
            .and()
            .httpBasic()
    }
}
