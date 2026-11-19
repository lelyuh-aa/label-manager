package com.leliukh.labelmanager.contract.dto.reference

/**
 * Countries accepted by the API for labels and artists, sorted by name.
 *
 * The server rejects any other code with
 * [com.leliukh.labelmanager.contract.dto.error.ErrorCodeDto.VALIDATION_FAILED].
 */
public object Countries {

    public val all: List<CountryDto> = listOf(
        CountryDto("AL", "Albania"),
        CountryDto("DZ", "Algeria"),
        CountryDto("AR", "Argentina"),
        CountryDto("AM", "Armenia"),
        CountryDto("AU", "Australia"),
        CountryDto("AT", "Austria"),
        CountryDto("AZ", "Azerbaijan"),
        CountryDto("BE", "Belgium"),
        CountryDto("BO", "Bolivia"),
        CountryDto("BA", "Bosnia and Herzegovina"),
        CountryDto("BR", "Brazil"),
        CountryDto("BG", "Bulgaria"),
        CountryDto("CM", "Cameroon"),
        CountryDto("CA", "Canada"),
        CountryDto("CL", "Chile"),
        CountryDto("CN", "China"),
        CountryDto("CO", "Colombia"),
        CountryDto("CR", "Costa Rica"),
        CountryDto("HR", "Croatia"),
        CountryDto("CY", "Cyprus"),
        CountryDto("CZ", "Czechia"),
        CountryDto("CI", "Côte d’Ivoire"),
        CountryDto("DK", "Denmark"),
        CountryDto("EC", "Ecuador"),
        CountryDto("EG", "Egypt"),
        CountryDto("EE", "Estonia"),
        CountryDto("FI", "Finland"),
        CountryDto("FR", "France"),
        CountryDto("GE", "Georgia"),
        CountryDto("DE", "Germany"),
        CountryDto("GH", "Ghana"),
        CountryDto("GR", "Greece"),
        CountryDto("HU", "Hungary"),
        CountryDto("IS", "Iceland"),
        CountryDto("IN", "India"),
        CountryDto("IR", "Iran"),
        CountryDto("IE", "Ireland"),
        CountryDto("IL", "Israel"),
        CountryDto("IT", "Italy"),
        CountryDto("JM", "Jamaica"),
        CountryDto("JP", "Japan"),
        CountryDto("KZ", "Kazakhstan"),
        CountryDto("KR", "Korea, Republic of"),
        CountryDto("LV", "Latvia"),
        CountryDto("LT", "Lithuania"),
        CountryDto("LU", "Luxembourg"),
        CountryDto("ML", "Mali"),
        CountryDto("MX", "Mexico"),
        CountryDto("MD", "Moldova"),
        CountryDto("ME", "Montenegro"),
        CountryDto("MA", "Morocco"),
        CountryDto("NL", "Netherlands"),
        CountryDto("NZ", "New Zealand"),
        CountryDto("NG", "Nigeria"),
        CountryDto("MK", "North Macedonia"),
        CountryDto("NO", "Norway"),
        CountryDto("PA", "Panama"),
        CountryDto("PY", "Paraguay"),
        CountryDto("PE", "Peru"),
        CountryDto("PL", "Poland"),
        CountryDto("PT", "Portugal"),
        CountryDto("QA", "Qatar"),
        CountryDto("RO", "Romania"),
        CountryDto("SA", "Saudi Arabia"),
        CountryDto("SN", "Senegal"),
        CountryDto("RS", "Serbia"),
        CountryDto("SK", "Slovakia"),
        CountryDto("SI", "Slovenia"),
        CountryDto("ZA", "South Africa"),
        CountryDto("ES", "Spain"),
        CountryDto("SE", "Sweden"),
        CountryDto("CH", "Switzerland"),
        CountryDto("TN", "Tunisia"),
        CountryDto("TR", "Türkiye"),
        CountryDto("UA", "Ukraine"),
        CountryDto("AE", "United Arab Emirates"),
        CountryDto("GB", "United Kingdom"),
        CountryDto("US", "United States"),
        CountryDto("UY", "Uruguay"),
        CountryDto("UZ", "Uzbekistan"),
        CountryDto("VE", "Venezuela"),
    )

    private val byCode: Map<String, CountryDto> = all.associateBy(CountryDto::code)

    /**
     * @param code ISO 3166-1 alpha-2 code, case-sensitive
     * @return the country with this code, or null when the API does not accept it
     */
    public fun find(code: String): CountryDto? = byCode[code]
}
