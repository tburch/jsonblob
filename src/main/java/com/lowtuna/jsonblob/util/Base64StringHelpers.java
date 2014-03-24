package com.lowtuna.jsonblob.util;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;

import static org.apache.commons.lang3.Validate.notNull;

public enum Base64StringHelpers implements Helper<String> {
    base64Encode {
        @Override
        protected CharSequence safeApply(final String value, final Options options) {
            String urlSafeOption = options.get("urlSafe");
            return Boolean.valueOf(urlSafeOption) ? Base64.encodeBase64URLSafeString(value.getBytes()) : Base64.encodeBase64String(value.getBytes());
        }

    },

    base64Decode {
        @Override
        protected CharSequence safeApply(final String value, final Options options) {
            String urlSafeOption = options.get("urlSafe");
            return new String(Boolean.valueOf(urlSafeOption) ? Base64.encodeBase64URLSafe(value.getBytes()) : Base64.decodeBase64(value.getBytes()));
        }
    };

    @Override
    public CharSequence apply(final String context, final Options options) throws IOException {
        if (options.isFalsy(context)) {
            Object param = options.param(0, null);
            return param == null ? null : param.toString();
        }
        return safeApply(context, options);
    }

    protected abstract CharSequence safeApply(final String context, final Options options);

    public void registerHelper(final Handlebars handlebars) {
        notNull(handlebars, "The handlebars is required.");
        handlebars.registerHelper(name(), this);
    }

    public static void register(final Handlebars handlebars) {
        notNull(handlebars, "A handlebars object is required.");
        Base64StringHelpers[] helpers = values();
        for (Base64StringHelpers helper : helpers) {
            helper.registerHelper(handlebars);
        }
    }
}
