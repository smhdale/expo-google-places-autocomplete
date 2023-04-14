import ExpoModulesCore
import GooglePlaces

internal struct RequestConfig: Record {
  @Field var countries: [String]
  @Field var types: [String]
}
