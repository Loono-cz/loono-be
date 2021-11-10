package cz.loono.backend.db.model

import java.io.Serializable

data class HealthcareProviderId(
    val locationId: Long? = null,
    val institutionId: Long? = null,
) : Serializable
