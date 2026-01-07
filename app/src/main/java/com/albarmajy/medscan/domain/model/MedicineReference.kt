package com.albarmajy.medscan.domain.model

data class MedicineReference(
    val id: Int,
    val nameEn: String,
    val nameAr: String,
    val strength: String,
    val activeIngredient: String,
    val form: String,

)
