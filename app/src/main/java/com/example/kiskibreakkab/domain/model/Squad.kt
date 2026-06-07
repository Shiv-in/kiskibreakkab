package com.example.kiskibreakkab.domain.model

data class Squad(
    val squadId: String = "",
    val squadName: String = "",
    val memberIds: List<String> = emptyList()
)
