import * as React from "react";
import { ScrollView, View } from "react-native";
import { SearchInput, ResultItem, ListFooter } from "./components";

import { ExpoGooglePlacesAutocompleteViewProps } from "./ExpoGooglePlacesAutocomplete.types";
import PlacesAutocomplete from "./ExpoGooglePlacesAutocompleteModule";
import { Place, PlacesError } from "./types";

export default function ExpoGooglePlacesAutocompleteView({
  apiKey,
  placeholder,
  requestConfig,
  onPlaceSelected,
  onSearchError,
  inputRef,
  resultsContainerStyle,
  resultItemStyle,
  ...props
}: ExpoGooglePlacesAutocompleteViewProps) {
  const [inputValue, setInputValue] = React.useState("");
  const [results, setResults] = React.useState<Place[]>([]);

  React.useEffect(() => {
    PlacesAutocomplete.initPlaces(apiKey);
  }, [apiKey]);

  const onSelectPlace = React.useCallback(
    async (placeId: string, fullText: string) => {
      try {
        const details = await PlacesAutocomplete.placeDetails(placeId);
        setInputValue(fullText);
        setResults([]);
        onPlaceSelected(details);
      } catch (e) {
        const error = e as PlacesError;
        onSearchError?.(error);
      }
    },
    [onPlaceSelected, onSearchError],
  );

  const onChangeText = React.useCallback(
    async (text: string) => {
      let result = await PlacesAutocomplete.findPlaces(text, requestConfig);
      setResults(result.items);
      setInputValue(text);
    },
    [requestConfig],
  );

  return (
    <ScrollView {...props}>
      <SearchInput
        ref={inputRef}
        inputValue={inputValue}
        onChangeText={onChangeText}
        placeholder={placeholder || "Search for your address"}
        clearButtonMode="while-editing"
      />
      {results.length > 0 ? (
        <View style={resultsContainerStyle}>
          {results.map(place => (
            <ResultItem
              key={place.placeId}
              place={place}
              style={resultItemStyle}
              onSelectPlace={onSelectPlace}
            />
          ))}
          <ListFooter />
        </View>
      ) : null}
    </ScrollView>
  );
}
