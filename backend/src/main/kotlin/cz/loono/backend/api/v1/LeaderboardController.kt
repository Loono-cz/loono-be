package cz.loono.backend.api.v1

import cz.loono.backend.api.Attributes
import cz.loono.backend.api.BasicUser
import cz.loono.backend.api.dto.LeaderboardDto
import cz.loono.backend.api.service.LeaderboardService
import org.springframework.http.MediaType
import org.springframework.jmx.export.annotation.ManagedOperation
import org.springframework.jmx.export.annotation.ManagedResource
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/leaderboard", produces = [MediaType.APPLICATION_JSON_VALUE], headers = ["app-version"])
@ManagedResource(objectName = "LoonoMBean:category=MBeans,name=leaderboardBean")
class LeaderboardController(
    private val leaderboardService: LeaderboardService
) {

    @GetMapping
    @ManagedOperation
    fun getLeaderboard(
        @RequestAttribute(name = Attributes.ATTR_BASIC_USER)
        basicUser: BasicUser,
        @RequestParam(name = "leaderboard_size")
        leaderboardSize: Int = 100
    ): LeaderboardDto = leaderboardService.getLeaderboard(basicUser.uid)
}
