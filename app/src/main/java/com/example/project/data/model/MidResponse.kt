package com.example.project.data.model

data class MidLandResponse(
    val response: MidLandKmaResponse
)

data class MidLandKmaResponse(
    val header: KmaHeader,
    val body: MidLandBody?
)

data class MidLandBody(
    val dataType: String?,
    val items: MidLandItems?
)

data class MidLandItems(
    val item: List<MidLandItem> = emptyList()
)

data class MidLandItem(
    val regId: String?,

    val rnSt4Am: String?,
    val rnSt4Pm: String?,
    val rnSt5Am: String?,
    val rnSt5Pm: String?,
    val rnSt6Am: String?,
    val rnSt6Pm: String?,

    val wf4Am: String?,
    val wf4Pm: String?,
    val wf5Am: String?,
    val wf5Pm: String?,
    val wf6Am: String?,
    val wf6Pm: String?
)

data class MidTaResponse(
    val response: MidTaKmaResponse
)

data class MidTaKmaResponse(
    val header: KmaHeader,
    val body: MidTaBody?
)

data class MidTaBody(
    val dataType: String?,
    val items: MidTaItems?
)

data class MidTaItems(
    val item: List<MidTaItem> = emptyList()
)

data class MidTaItem(
    val regId: String?,

    val taMin4: String?,
    val taMax4: String?,
    val taMin5: String?,
    val taMax5: String?,
    val taMin6: String?,
    val taMax6: String?
)