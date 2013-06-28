'data' URI Parsing Rules
========================

 * Let `URI` be the string representing the data URI.

 * If `URI` does not start with a case-insensitive `data:`:
    * Throw a MALFORMED_URI exception.

 * If `URI` does not contain a `,`:
    * Throw a MALFORMED_URI exception.

 * Let `supportedContentEncodings` be an array of strings representing the supported content encodings. (`["base64"]` for example)
 * Let `mimeType` be a string with the value `text/plain`.
 * Let `contentEncoding` be an empy string.
 * Let `contentEncodingAlreadySet` be a boolean with a value of `false`.
 * Let `supportedValues` be a map of string:string pairs where the first string in each pair represents the name of the supported value and the second string in each pair represents an empty string or default string value. (Example: `{"charset" : "", "filename" : "", "content-disposition" : ""}`)
 * Let `supportedValueSetBits` be a map of string:bool pairs representing each of the names in supportedValues with each name set to `false`.
 * Let `comma` be the position of the first `,` found in `URI`.
 * Let `temp` be the substring of `URI` from, and including, position `5` to, and excluding, the `comma` position. (between `data:` and first `,`)
 * Let `headers` be an array of strings returned by splitting temp by `;`.

    For each string s in headers:
        Let s equal the lowercase version of s
        Let eq be the position result of searching for "=" in s.
        Let name and value be empty strings.

        If eq is not a valid position in s:
            Let name equal the result of percent-decoding s.
            Let name equal the result of trimming leading and trailing white-space from name.

        Else:
            Let name equal the substring of s from position 0 to, but not including, position eq.
            Let name equal the result of percent-decoding name.
            Let name equal the result of trimmnig leading and trailing white-space from name.
            Let value equal the substring of s from position eq + 1 to the end of s.
            Let value equal the result of precent-decoding value.
            Let value equal the result of trimming leading and trailing white-space from value.

        If s is the first element in headers and eq is not a valid position in s and the length of name is greater than 0:
            Let mimeType equal name.

        Else:

            If eq is not a valid position in s:

                If name is found case-insensitively in supportedContentEncodings:

                    If contentEncodingAlreadySet is false:
                        Let contentEncoding equal name.
                        Let ContentEncodingAlreadySet equal true.

            Else:

                If the length of value is greater than 0 and name is found case-insensitively in supportedValues:

                    If the corresponding value for name found (case-insensitivley) in supportedValueSetBits is false:
                        Let the corresponding value for name found (case-insensitively) in supportedValues equal value.
                        Let the corresponding value for name found (case-insensitively) in supportedValueSetBits equal true.
    Let data be the substring of URI from position comma + 1 to the end of URI.
    Let data be the result of percent-decoding data.
    Let dataURIObject be an object consisting of the mimeType, contentEncoding, data and supportedValues objects.
    return dataURIObject.

