package cz.loono.backend.data.constants

object Constants {

    const val OPEN_DATA_URL =
        "https://opendata.mzcr.cz/data/nrpzs/narodni-registr-poskytovatelu-zdravotnich-sluzeb.csv"

    const val CORRECTED_DATA_URL =
        "https://docs.google.com/spreadsheets/d/e/2PACX-1vQOpp7Y7eMlDgP-avxqxLHzFvigIoiqXVIYWCTe4UIT5IemWKr2lu6FCiwVqpNhSrLAXm5FV0XOMRkt/pub?gid=486149684&single=true&output=xlsx"

    val healthcareProvidersCSVHeader = listOf(
        // IDs - location ID, institution ID
        "MistoPoskytovaniId", "ZdravotnickeZarizeniId",
        "Kod", // Code
        "NazevZarizeni", // Title of institution
        "DruhZarizeni", // Type of institution
        // Address
        "Obec", "Psc", "Ulice", "CisloDomovniOrientacni", "Kraj", "KrajCode", "Okres", "OkresCode", "SpravniObvod",
        // Contacts
        "PoskytovatelTelefon", "PoskytovatelFax", "PoskytovatelEmail", "PoskytovatelWeb",
        "Ico", // ICO
        "TypOsoby", // Personal type
        "PravniFormaKod", // Code of lawyer form
        // Institution HQ
        "KrajCodeSidlo", "OkresCodeSidlo", "ObecSidlo", "PscSidlo", "UliceSidlo", "CisloDomovniOrientacniSidlo",
        // Healthcare types
        "OborPece", "FormaPece", "DruhPece",
        "OdbornyZastupce", // Substitute
        "Lat", "Lng" // Geographical coords
    )

    const val MAXIMUM_CUSTOM_EXAMS = 10
}
