/*
  Copyright (c) 2013 ooxi
      https://github.com/ooxi/jdatauri

  This software is provided 'as-is', without any express or implied warranty.
  In no event will the authors be held liable for any damages arising from the
  use of this software.

  Permission is granted to anyone to use this software for any purpose,
  including commercial applications, and to alter it and redistribute it
  freely, subject to the following restrictions:

   1. The origin of this software must not be misrepresented; you must not
      claim that you wrote the original software. If you use this software in a
      product, an acknowledgment in the product documentation would be
      appreciated but is not required.

   2. Altered source versions must be plainly marked as such, and must not be
      misrepresented as being the original software.

   3. This notice may not be removed or altered from any source distribution.
 */
package com.github.ooxi.jdatauri;

import org.apache.commons.codec.binary.Base64;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.lang.String.format;
import static java.net.URLDecoder.decode;
import static java.util.Collections.singletonList;

class DataUri {
    static final String CHARSET_OPTION_NAME = "charset";
    static final String FILENAME_OPTION_NAME = "filename";
    static final String CONTENT_DISPOSITION_OPTION_NAME = "content-disposition";

    private final String mime;
    private final Charset charset;
    private final String filename;
    private final String contentDisposition;
    private final byte[] data;
    private DataUriSerializerImpl dataUriFormatter = new DataUriSerializerImpl();

    DataUri(String mime, Charset charset, byte[] data) {
        this(mime, charset, null, null, data);
    }

    DataUri(String mime, Charset charset, String filename, String contentDisposition, byte[] data) {
        this.mime = mime;
        this.charset = charset;
        this.filename = filename;
        this.contentDisposition = contentDisposition;
        this.data = data;

        if (null == mime) {
            throw new IllegalArgumentException("`mime' must not be null");
        }
        if (null == data) {
            throw new IllegalArgumentException("`data' must not be null");
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static DataUri parse(String uri, Charset charset) {
        validateDataUri(uri);

        Collection<String> supportedContentEncodings = singletonList("base64");

        String mimeType = "text/plain";
        String contentEncoding = "";

        boolean contentEncodingAlreadySet = false;
        final Map<String, String> supportedValues = new HashMap<String, String>() {{
            put(CHARSET_OPTION_NAME, "");
            put(FILENAME_OPTION_NAME, "");
            put(CONTENT_DISPOSITION_OPTION_NAME, "");
        }};

        final Map<String, Boolean> supportedValueSetBits = new HashMap<String, Boolean>() {{
            for (String key : supportedValues.keySet()) {
                put(key, false);
            }
        }};

        int comma = uri.indexOf(',');
        String temp = uri.substring("data:".length(), comma);
        String[] headers = temp.split(";");

        for (int header = 0; header < headers.length; ++header) {
            String s = headers[header].toLowerCase();

            String name, value;
            if (!s.contains("=")) {
                name = percentDecode(s, charset);
                value = "";
            } else {
                name = percentDecode(s.substring(0, s.indexOf('=')), charset);
                value = percentDecode(s.substring(s.indexOf('=') + 1), charset);
            }

            name = name.trim();
            value = value.trim();

            if ((0 == header) && (-1 == s.indexOf('=')) && !name.isEmpty()) {
                mimeType = name;
            } else {
                if (-1 == s.indexOf('=')) {
                    final String nameCaseInsensitive = name.toLowerCase();

                    if (supportedContentEncodings.contains(nameCaseInsensitive)) {
                        if (!contentEncodingAlreadySet) {
                            contentEncoding = name;
                            contentEncodingAlreadySet = true;
                        }
                    }
                } else {
                    final String nameCaseInsensitive = name.toLowerCase();

                    if (!value.isEmpty() && supportedValues.containsKey(nameCaseInsensitive)) {
                        boolean valueSet = supportedValueSetBits.get(nameCaseInsensitive);

                        if (!valueSet) {
                            supportedValues.put(nameCaseInsensitive, value);
                            supportedValueSetBits.put(nameCaseInsensitive, true);
                        }
                    }
                }
            }

        }

        String data = percentDecode(uri.substring(comma + 1), charset);

        final String finalMimeType = mimeType;
        final Charset finalCharset = supportedValues.get(CHARSET_OPTION_NAME).equals("")
                ? null : Charset.forName(supportedValues.get(CHARSET_OPTION_NAME));
        final String finalFilename = supportedValues.get(FILENAME_OPTION_NAME).equals("")
                ? null : supportedValues.get(FILENAME_OPTION_NAME);
        final String finalContentDisposition = supportedValues.get(CONTENT_DISPOSITION_OPTION_NAME).equals("")
                ? null : supportedValues.get(CONTENT_DISPOSITION_OPTION_NAME);
        final byte[] finalData = "base64".equalsIgnoreCase(contentEncoding)
                ? Base64.decodeBase64(data) : data.getBytes(charset);

        return new DataUri(
                finalMimeType,
                finalCharset,
                finalFilename,
                finalContentDisposition,
                finalData
        );
    }

    private static void validateDataUri(String uri) {
        if (!uri.toLowerCase().startsWith("data:")) {
            throw new IllegalArgumentException("URI must start with a case-insensitive `data:'");
        }

        if (!uri.contains(",")) {
            throw new IllegalArgumentException("URI must contain a `,'");
        }
    }

    private static String percentDecode(String s, Charset cs) {
        try {
            return decode(s, cs.name()).replace(' ', '+');
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(format("Charset `%s' not supported", cs.name()), e);
        }
    }

    public String getMime() {
        return mime;
    }

    public Charset getCharset() {
        return charset;
    }

    public String getFilename() {
        return filename;
    }

    public byte[] getData() {
        return data;
    }

    public String getContentDisposition() {
        return contentDisposition;
    }

    public String toString() {
        return dataUriFormatter.serialize(this);
    }

    void setDataUriFormatter(DataUriSerializerImpl dataUriFormatter) {
        this.dataUriFormatter = dataUriFormatter;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 23 * hash + getHashCodeOrDefault(this.mime, 0);
        hash = 23 * hash + getHashCodeOrDefault(this.charset, 0);
        hash = 23 * hash + getHashCodeOrDefault(this.filename, 0);
        hash = 23 * hash + getHashCodeOrDefault(this.contentDisposition, 0);
        hash = 23 * hash + Arrays.hashCode(this.data);
        return hash;
    }

    private int getHashCodeOrDefault(Object subject, int defaultValue) {
        return subject != null ? subject.hashCode() : defaultValue;
    }

    public boolean equals(Object other) {
        return equals((DataUri) other);
    }

    public boolean equals(DataUri other) {
        return other != null
                && getClass() == other.getClass()
                && !isMimeDifferent(other)
                && !isCharsetDifferent(other)
                && !isFilenameDifferent(other)
                && !isContentDispositionDifferent(other)
                && Arrays.equals(data, other.data);
    }

    private boolean isContentDispositionDifferent(DataUri other) {
        return contentDisposition == null
                ? other.contentDisposition != null
                : !contentDisposition.equals(other.contentDisposition);
    }

    private boolean isFilenameDifferent(DataUri other) {
        return filename == null
                ? (other.filename != null)
                : !filename.equals(other.filename);
    }

    private boolean isCharsetDifferent(DataUri other) {
        return !(charset == other.charset
                || charset != null
                && charset.equals(other.charset)
        );
    }

    private boolean isMimeDifferent(DataUri other) {
        return mime == null
                || other.mime == null
                || !mime.equals(other.mime);
    }
}
