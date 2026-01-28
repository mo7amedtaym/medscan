package com.albarmajy.medscan.ui.navigation
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed interface Routes: NavKey {

    @Serializable data object Dashboard : NavKey

    @Serializable data object Calendar : NavKey

    @Serializable data object Medicines : NavKey

    @Serializable data object Settings : NavKey

    @Serializable data object Scanner : NavKey

    @Serializable data class MedicationPlan(val medId: Long) : NavKey

    @Serializable data class MedicationDetails(val medId: Long) : NavKey

}