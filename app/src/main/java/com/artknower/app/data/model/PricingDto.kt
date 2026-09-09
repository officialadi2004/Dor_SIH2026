package com.artknower.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductPricingRequest(
    @SerialName("category")
    val category: String? = "UNKNOWN",
    @SerialName("craft_type")
    val craftType: String? = "UNKNOWN",
    @SerialName("material")
    val material: String? = "UNKNOWN",
    @SerialName("dimensions")
    val dimensions: String? = null,
    @SerialName("state_or_location")
    val stateOrLocation: String? = "UNKNOWN",
    @SerialName("raw_material_cost")
    val rawMaterialCost: Double = 0.0,
    @SerialName("labor_cost")
    val laborCost: Double = 0.0,
    @SerialName("input_cost")
    val inputCost: Double = 0.0
)

@Serializable
data class DbPricingRequest(
    @SerialName("product_id")
    val productId: String,
    @SerialName("raw_material_cost")
    val rawMaterialCost: Double? = 0.0,
    @SerialName("labor_cost")
    val laborCost: Double? = 0.0,
    @SerialName("input_cost")
    val inputCost: Double? = 0.0
)

@Serializable
data class PricingRecommendResponse(
    @SerialName("recommended_min")
    val recommendedMin: Double? = null,
    @SerialName("recommended_max")
    val recommendedMax: Double? = null,
    @SerialName("market_predicted_min")
    val marketPredictedMin: Double? = null,
    @SerialName("market_predicted_median")
    val marketPredictedMedian: Double? = null,
    @SerialName("market_predicted_max")
    val marketPredictedMax: Double? = null,
    @SerialName("cost_floor")
    val costFloor: Double? = null,
    @SerialName("currency")
    val currency: String? = "INR",
    @SerialName("model_name")
    val modelName: String? = null,
    @SerialName("model_version")
    val modelVersion: String? = null,
    @SerialName("warning")
    val warning: String? = null
)
