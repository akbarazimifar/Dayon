package mpo.dayon.common.babylon;

import java.util.Arrays;
import java.util.IllegalFormatException;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import mpo.dayon.common.log.Log;

public abstract class Babylon {
    private static final String BUNDLE = "Babylon";

    private Babylon() {
    }

    public static synchronized String translate(String tag, Object... arguments) {
        final Locale locale = Locale.getDefault();

        final ResourceBundle bundle = ResourceBundle.getBundle(BUNDLE, locale);

        String value;

        try {
            value = bundle.getString(tag);

            if (value.trim().length() == 0) {
                value = tag;
            }

            if (arguments != null && arguments.length > 0) {
                value = formatValue(locale, value, tag, arguments);
            }
        } catch (MissingResourceException ignored) {
            value = tag;

            if (arguments != null && arguments.length > 0) {
                value = formatValue(locale, value, tag, arguments);
            }
        }

        if (value != null) {
            value = value.trim();
        }

        return value;
    }

    @java.lang.SuppressWarnings("squid:S4973")
    public static String translateEnum(Enum<?> value) {
        final String tag = "enum." + value.getClass().getSimpleName() + "." + value.name();
        final String val = translate(tag);

        // OK - means not localized (!)
        if (tag == val) {
            return value.name();
        }

        return val;
    }

    /**
     * Attempt to format the tag value; if the actual tag value is missing or
     * the tag value could not be formatted for whatever reason, then the
     * <code>toString</code> of the argument array is appended to the tag
     * value...
     */
    @java.lang.SuppressWarnings("squid:S4973")
    private static String formatValue(Locale locale, String tagValue, String tag, Object... arguments) {
        String formattedTagValue;

        // The identity equality is fine; that's what I want!
        if (tagValue != tag) {
            try {
                // The locale is required for example to convert a double into
                // its string representation
                // when processing %s (of a double value) or even a %d I guess
                // (e.g., using '.' or ',' )

                formattedTagValue = String.format(locale, tagValue, arguments);
            } catch (IllegalFormatException ex) {
                Log.warn("Illegal format for tag [" + tag + "] - " + ex.getMessage(), ex);
                formattedTagValue = tagValue + " " + Arrays.toString(arguments);
                // what else can I do here?
            }
        } else {
            formattedTagValue = tagValue + " " + Arrays.toString(arguments);
            // what else can I do here?
        }

        return formattedTagValue;
    }

}
