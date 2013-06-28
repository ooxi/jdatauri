jDataUri [![Build Status](https://travis-ci.org/ooxi/jdatauri.png)](https://travis-ci.org/ooxi/jdatauri)
========

jDataUri is a simple and well tested Java implementation for parsing data URIs,
in a nearly self contained file (a Base64 decoder is needed).



'data' URI Syntax
=================

`"data:" + valueList + "," + data`

 * `"data:"` is case-insensitve.
 * `valueList` is a `";"`-separated list of values. The first value is a
   percent-encoded value representing the mime type. Although `"/"` and `"+"` in
   the mime type can be percent-encoded as `"%2F"` and `"%2B"` repsepectively,
   they are not required to be.
 * For the rest of the values in `valueList`, if the value does not contain a
   `"="`, then it is a content-encoding value (like `base64`). If the value does
   contain a `"="`, then the value is a name=value pair where name and value are
   percent-encoded representations of a name and value.
 * valid names in a name=value pair are `"charset"`, `"filename"` and
   `"content-disposition"`
 * The mime type of the URI is the mime type value after percent-decoding it,
   trimming leading and trailing white-space from it and converting it to
   lowercase. If the value is then empty, the mime type for the URI is
   `"text/plain"`.
 * For content-encoding values, before determining the content encoding value,
   it must be percent-decoded, stripped of leading and trailing white-space and
   converted to lowercase. Further, the value is not used if its length is 0 and
   when there are multiple content-encodings in the valueList, the first
   non-empty (if any) one that has a supported value is used and the rest are
   ignored. Supported values may differ between UAs. `"base64"` is usually the
   only supported value at the moment.
 * If a value is determined to be a name=value pair, the value must be split by
   the first `"="`. The value on the left-side of the `"="` is the name and the
   value on the right-side of the first `"="` is the value. Then, the name and
   value must each be percent-decoded, stripped of leading and trailing
   white-space and converted to lowercase. Further, if more than one value in
   the valueList contains a name=value pair with the same name, the first one
   with a non-empty value (if any) is used and the rest of the duplicates are
   ignored.
 * If a charset value is not found, then it defaults to `US-ASCII`.
 * `data` is a percent-encoded value representing the file's data. To get the
   data, it must be percent-decoded.



'data' URI Parsing Rules
========================

This document was originally hosted at [shadow2531.com](http://shadow2531.com/opera/testcases/datauri/data_uri_rules.html)


    Let URI be the string representing the data URI.

    If URI does not start with a case-insensitive "data:":
        Throw a MALFORMED_URI exception.

    If URI does not contain a ",":
        Throw a MALFORMED_URI exception.
    Let supportedContentEncodings be an array of strings representing the supported content encodings. (["base64"] for example)
    Let mimeType be a string with the value "text/plain".
    Let contentEncoding be an empy string.
    Let contentEncodingAlreadySet be a boolean with a value of false.
    Let supportedValues be a map of string:string pairs where the first string in each pair represents the name of the supported value and the second string in each pair represents an empty string or default string value. (Example: {"charset" : "", "filename" : "", "content-disposition" : ""})
    Let supportedValueSetBits be a map of string:bool pairs representing each of the names in supportedValues with each name set to false.
    Let comma be the position of the first "," found in URI.
    Let temp be the substring of URI from, and including, position 5 to, and excluding, the comma position. (between "data:" and first ",")
    Let headers be an array of strings returned by splitting temp by ";".

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

