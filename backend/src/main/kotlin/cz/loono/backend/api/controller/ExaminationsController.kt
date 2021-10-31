package cz.loono.backend.api.controller

import cz.loono.backend.api.Attributes
import cz.loono.backend.api.BasicUser
import cz.loono.backend.api.dto.ExaminationCompletionDto
import cz.loono.backend.api.dto.ExaminationRecordDto
import cz.loono.backend.api.dto.ExaminationTypeEnumDto
import cz.loono.backend.api.exception.LoonoBackendException
import cz.loono.backend.api.service.ExaminationRecordService
import cz.loono.backend.db.model.ExaminationRecord
import cz.loono.backend.let3
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import javax.validation.Valid

@RestController
@RequestMapping("/examinations", produces = [MediaType.APPLICATION_JSON_VALUE])
class ExaminationsController @Autowired constructor(
    private val recordService: ExaminationRecordService
) {

    @GetMapping
    fun getExaminationRecords(
        @RequestAttribute(Attributes.ATTR_BASIC_USER)
        basicUser: BasicUser
    ): List<ExaminationRecordDto> {
        return recordService.getOrCreateRecords(basicUser.uid).map { it.toDto() }
    }

    private fun ExaminationRecord.toDto(): ExaminationRecordDto {
        return ExaminationRecordDto(
            type = ExaminationTypeEnumDto.valueOf(type),
            worth = examinationWorth(ExaminationTypeEnumDto.valueOf(type)),
            lastVisitMonth = lastVisit?.monthValue,
            lastVisitYear = lastVisit?.year,
        )
    }

    @PostMapping("/{type}/complete")
    fun complete(
        @RequestAttribute(Attributes.ATTR_BASIC_USER)
        basicUser: BasicUser,

        @PathVariable(name = "type")
        type: String,

        @Valid
        @RequestBody
        completionDto: ExaminationCompletionDto
    ): List<ExaminationRecordDto> {
        if (type !in ExaminationTypeEnumDto.values().map { it.name }) {
            throw LoonoBackendException(HttpStatus.NOT_FOUND)
        }

        recordService.completeExamination(
            basicUser.uid,
            type,
            let3(completionDto.year, completionDto.month, 1, LocalDate::of)
        )

        return getExaminationRecords(basicUser)
    }
}

// Purposefully implemented as an expression to leverage the exhaustiveness check performed by the compiler
// TODO update the types with 0 points
fun examinationWorth(type: ExaminationTypeEnumDto): Int =
    when (type) {
        ExaminationTypeEnumDto.BREAST_SELF -> 0
        ExaminationTypeEnumDto.COLONOSCOPY -> 0
        ExaminationTypeEnumDto.DENTIST -> 300
        ExaminationTypeEnumDto.DERMATOLOGIST -> 100
        ExaminationTypeEnumDto.GENERAL_PRACTITIONER -> 200
        ExaminationTypeEnumDto.GYNECOLOGIST -> 200
        ExaminationTypeEnumDto.MAMMOGRAM -> 500
        ExaminationTypeEnumDto.OPHTHALMOLOGIST -> 100
        ExaminationTypeEnumDto.TESTICULAR_SELF -> 0
        ExaminationTypeEnumDto.TOKS -> 500
        ExaminationTypeEnumDto.ULTRASOUND_BREAST -> 100
        ExaminationTypeEnumDto.UROLOGIST -> 300
        ExaminationTypeEnumDto.VENEREAL_DISEASES -> 0
    }
