package expo.community.modules.googleplacesautocomplete

import android.content.Context
import android.os.Bundle
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.Exceptions
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class ExpoGooglePlacesAutocompleteModule : Module() {
    private val context: Context
        get() = appContext.reactContext ?: throw Exceptions.ReactContextLost()
    private lateinit var placesClient: PlacesClient
    private var token = AutocompleteSessionToken.newInstance()
    private var request =
        FindAutocompletePredictionsRequest.builder()
            .setSessionToken(token)

    override fun definition() = ModuleDefinition {
        Name("ExpoGooglePlacesAutocomplete")

        Function("initPlaces") { apiKey: String ->
            Places.initialize(context, apiKey)
            placesClient = Places.createClient(context)
        }

        AsyncFunction("findPlaces") { query: String, config: RequestConfig?, promise: Promise ->
            findPlaces(query, config, promise)
        }

        AsyncFunction("placeDetails") { placeId: String, promise: Promise ->
            placeDetails(placeId, promise)
        }
    }

    private fun newAutocompleteRequest() {
        token = AutocompleteSessionToken.newInstance()
        request.setSessionToken(token)
    }

    private fun findPlaces(query: String, config: RequestConfig?, promise: Promise) {
        request.setQuery(query)

        if (config != null) {
            if (config.types.orEmpty().isNotEmpty()) {
                request.setTypesFilter(config.types)
            }
            if (config.countries.orEmpty().isNotEmpty()) {
                request.setCountries(config.countries)
            }
        }

        placesClient.findAutocompletePredictions(request.build())
            .addOnSuccessListener { response ->
                val places =
                    response.autocompletePredictions.map { mapFromPrediction(it) }
                val result = PredictionResult(places = places)
                promise.resolve(result)
            }
            .addOnFailureListener {
                promise.reject(
                    FailedToFetchPlace(it.message)
                )
            }
    }

    private fun placeDetails(placeId: String, promise: Promise) {
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.NAME,
            Place.Field.LAT_LNG,
            Place.Field.ADDRESS,
            Place.Field.ADDRESS_COMPONENTS
        )

        val request =
            FetchPlaceRequest.builder(placeId, placeFields)
                .setSessionToken(token)
                .build()

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                newAutocompleteRequest()
                val place = mapFromPlace(response.place)
                promise.resolve(place)
            }
            .addOnFailureListener {
                promise.reject(
                    FailedToFetchDetails(it.message)
                )
            }
    }
}
