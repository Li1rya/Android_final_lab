package com.example.animalwiki.data.model

import com.google.gson.annotations.SerializedName

data class JsonAnimal(
    @SerializedName("id") val id: String,
    @SerializedName("cnname") val cnname: List<String>,
    @SerializedName("latinName") val latinName: String,
    @SerializedName("classification") val classification: JsonClassification,
    @SerializedName("appearance") val appearance: String,
    @SerializedName("habits") val habits: String,
    @SerializedName("habitat") val habitat: String,
    @SerializedName("diet") val diet: String
)

data class JsonClassification(
    @SerializedName("kingdom") val kingdom: String,
    @SerializedName("phylum") val phylum: String,
    @SerializedName("class") val className: String,
    @SerializedName("order") val order: String,
    @SerializedName("family") val family: String,
    @SerializedName("genus") val genus: String,
    @SerializedName("species") val species: String
)