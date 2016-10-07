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

import static java.lang.String.format;
import static org.apache.commons.codec.binary.Base64.encodeBase64;

class DataUriSerializerImpl implements DataUriSerializer {
    private DataUri dataUri;

    public synchronized String serialize(DataUri dataUri) {
        this.dataUri = dataUri;

        return formatHeader()
                + formatCharset()
                + formatContentDisposition()
                + formatFilename()
                + formatData();
    }

    private String formatData() {
        return format("base64,%s", encodeData());
    }

    private String encodeData() {
        return new String(encodeBase64(dataUri.getData()));
    }

    private String formatFilename() {
        if (dataUri.getFilename() == null)
            return "";

        return String.format("%s=%s;", DataUri.FILENAME_OPTION_NAME, dataUri.getFilename());
    }

    private String formatHeader() {
        return format("data:%s;", dataUri.getMime());
    }

    private String formatContentDisposition() {
        if (dataUri.getContentDisposition() == null)
            return "";

        return String.format("%s=%s;", DataUri.CONTENT_DISPOSITION_OPTION_NAME, dataUri.getContentDisposition());
    }

    private String formatCharset() {
        if (dataUri.getCharset() == null)
            return "";

        return String.format("%s=%s;", DataUri.CHARSET_OPTION_NAME, dataUri.getCharset().name());
    }
}
