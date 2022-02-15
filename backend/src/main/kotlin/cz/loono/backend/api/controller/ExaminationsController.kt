package cz.loono.backend.api.controller

import cz.loono.backend.api.Attributes
import cz.loono.backend.api.BasicUser
import cz.loono.backend.api.dto.ExaminationIdDto
import cz.loono.backend.api.dto.ExaminationRecordDto
import cz.loono.backend.api.dto.ExaminationTypeDto
import cz.loono.backend.api.dto.PreventionStatusDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.api.service.ExaminationRecordService
import cz.loono.backend.api.service.PreventionService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/examinations", produces = [MediaType.APPLICATION_JSON_VALUE])
class ExaminationsController(
    private val recordService: ExaminationRecordService,
    private val preventionService: PreventionService
) {
    @PostMapping("/{type}/confirm")
    fun confirm(
        @RequestAttribute(Attributes.ATTR_BASIC_USER)
        basicUser: BasicUser,

        @PathVariable(name = "type")
        type: String,

        @Valid
        @RequestBody
        examinationIdDto: ExaminationIdDto
    ): ExaminationRecordDto =
        if (type !in getAvailableExaminations()) {
            throw LoonoBackendException(HttpStatus.NOT_FOUND)
        } else {
            recordService.confirmExam(examinationIdDto.uuid, basicUser.uid)
        }

    @PostMapping("/{type}/cancel")
    fun cancel(
        @RequestAttribute(Attributes.ATTR_BASIC_USER)
        basicUser: BasicUser,

        @PathVariable(name = "type")
        type: String,

        @Valid
        @RequestBody
        examinationIdDto: ExaminationIdDto
    ): ExaminationRecordDto =
        if (type !in getAvailableExaminations()) {
            throw LoonoBackendException(HttpStatus.NOT_FOUND)
        } else {
            recordService.cancelExam(examinationIdDto.uuid, basicUser.uid)
        }

    @PostMapping
    fun updateOrCreate(
        @RequestAttribute(Attributes.ATTR_BASIC_USER)
        basicUser: BasicUser,

        @Valid
        @RequestBody
        examinationRecordDto: ExaminationRecordDto
    ): ExaminationRecordDto = recordService.createOrUpdateExam(examinationRecordDto, basicUser.uid)

    @GetMapping
    fun getPreventionStatus(
        @RequestAttribute(Attributes.ATTR_BASIC_USER)
        basicUser: BasicUser
    ): PreventionStatusDto = preventionService.getPreventionStatus(basicUser.uid)

    private fun getAvailableExaminations() = ExaminationTypeDto.values().map(ExaminationTypeDto::name)
}
