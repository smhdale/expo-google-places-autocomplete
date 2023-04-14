import type { CountryCode } from "./CountryCode";

/**
 * Filter your request results
 *
 */
export interface RequestConfig {
  /**
   * An array of country codes that restricts results to those countries
   * The maximum number of countries is 5
   */
  countries: CountryCode[];
  /**
   * An array of place types that restricts results to those types
   * @see https://developers.google.com/maps/documentation/places/web-service/supported_types
   */
  types: string[];
}
