package cz.loono.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RestController
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class SpringfoxConfiguration {

    @Bean
    fun api(): Docket? {
        return Docket(DocumentationType.SWAGGER_2)
            .apiInfo(
                ApiInfo(
                    "The Loono API",
                    "The Loono API for the mobile application.",
                    "1.0",
                    null,
                    Contact("The Loono", "https://www.loono.cz/", null),
                    "MIT license",
                    "http://www.opensource.org/licenses/mit-license.html",
                    emptyList()
                )
            )
            .select()
            .apis(RequestHandlerSelectors.withClassAnnotation(RestController::class.java))
            .paths(PathSelectors.any())
            .build()
    }
}
