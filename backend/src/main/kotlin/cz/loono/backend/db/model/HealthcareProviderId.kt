package cz.loono.backend.db.model

import java.io.Serializable

data class HealthcareProviderId(
    val locationId: Long = 0,
    val institutionId: Long = 0,
) : Serializable
