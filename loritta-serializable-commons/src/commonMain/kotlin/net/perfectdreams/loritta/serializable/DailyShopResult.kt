package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable

@Serializable
class DailyShopResult(
	val dreamStorageServiceUrl: String,
	val namespace: String,
	val backgrounds: List<DailyShopBackgroundEntry>,
	val profileDesigns: List<ProfileDesign>,
	val generatedAt: Long
)