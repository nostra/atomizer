package no.api.atomizer.cachechannel;

import java.util.List;

public final class CacheChannelHelper {

    public static final String KEY_MUST_REVALIDATE = "must-revalidate";

    public static final String KEY_NO_CACHE = "no-cache";

    public static final String KEY_GROUP = "group";

    public static final String KEY_CHANNEL = "channel";

    /**
     * Explicitly named frontend in order to avoid confusion.
     */
    public static final String KEY_MAX_AGE_FRONT_END = "max-age";

    /**
     * Channel-maxage for server storage
     */
    public static final String KEY_CHANNEL_MAX_AGE = "channel-maxage";
    
    public static final String KEY_PRE_CHECK = "pre-check";
    
    public static final String KEY_POST_CHECK = "post-check";

    public static final String GROUP_KEY_VALUE_DELIMETER = "=";

    public static final String GROUP_DELIMETER = ",";


    private CacheChannelHelper() {
        // Intentional
    }

    public static boolean isChannelKey(String key) {
        return KEY_CHANNEL.equals(key);
    }

    public static boolean isChannelMaxAgeKey(String key) {
        return KEY_CHANNEL_MAX_AGE.equals(key);
    }

    public static boolean isMaxAgeKey(String key) {
        return KEY_MAX_AGE_FRONT_END.equals(key);
    }

    public static boolean isGroupKey(String key) {
        return KEY_GROUP.equals(key);
    }

    public static CacheChannelElement createCacheChannelElementFromToken(final String token) {

        if (token == null) {
            return null;
        }
        String trimmed = token.trim();
        if (trimmed.contains(GROUP_KEY_VALUE_DELIMETER)) {
            String key = trimmed.substring(0, trimmed.indexOf(GROUP_KEY_VALUE_DELIMETER)).trim();
            String value = trimmed.substring(trimmed.indexOf(GROUP_KEY_VALUE_DELIMETER) + 1).trim();
            if (value.startsWith("\"")) {
                value = value.substring(1);
            }
            if (value.endsWith("\"")) {
                value = value.substring(0, value.length() - 1);
            }
            if (key != null && !"".equals(key)) {
                return new CacheChannelElement(key, value.trim());
            }
            return null;
        } else {
            return new CacheChannelElement(trimmed.trim(), null);
        }
    }

    public static boolean isLegalCacheChannelElement(CacheChannelElement element) {
        if (element == null) {
            return false;
        }
        return element.isLegal();
    }

    public static Integer stripId(String value, String prefix) {
          if (value == null || prefix == null || !value.startsWith(prefix)) {
              return null;
          }
          String strip = value.substring(prefix.length());
        try {
            return Integer.valueOf(strip);
        } catch ( NumberFormatException ignored ) {
            return null;
        }
      }



    /**
     * @deprecated  TODO remove
     */
    public static boolean isArticleGroupElement(String value) {
        if (value == null) {
            return false;
        }
        return value.startsWith(PredefinedGroups.ART);
    }

    /**
     * @deprecated  TODO remove
     */
    public static boolean isSectionGroupElement(String value) {
        if (value == null) {
            return false;
        }
        return value.startsWith(PredefinedGroups.SEC);
    }

    /**
     * @deprecated  TODO remove
     */
    public static String createCacheChannelString(int maxAge, String channel, List<String> otherGroups,
            List<Integer> articleGroups, List<Integer> sectionGroups,
            int channelMaxAge) {

        StringBuilder builder = new StringBuilder();

        String delim = CacheChannelHelper.GROUP_DELIMETER + " ";

        // max-age
        if ( maxAge > 0 ) {
            builder.append(CacheChannelHelper.KEY_MAX_AGE_FRONT_END).append(CacheChannelHelper.GROUP_KEY_VALUE_DELIMETER).append(maxAge);
        } else {
            builder.append("must-revalidate");
        }

        // channel
        builder.append(delim).append(CacheChannelHelper.KEY_CHANNEL).append(CacheChannelHelper.GROUP_KEY_VALUE_DELIMETER).append("\"")
                .append(channel).append("\"");

        // channel-maxage
        builder.append(delim).append(CacheChannelHelper.KEY_CHANNEL_MAX_AGE).append(CacheChannelHelper.GROUP_KEY_VALUE_DELIMETER).append(channelMaxAge);

        // other groups
        for (String group : otherGroups) {
            builder.append(delim).append(CacheChannelHelper.KEY_GROUP).append(CacheChannelHelper.GROUP_KEY_VALUE_DELIMETER).append("\"")
                    .append(group).append("\"");
        }

        // section groups
        for (Integer group : sectionGroups) {
            builder.append(delim).append(CacheChannelHelper.KEY_GROUP).append(CacheChannelHelper.GROUP_KEY_VALUE_DELIMETER).append("\"")
                    .append(PredefinedGroups.SEC).append(group).append("\"");
        }

        // article groups
        for (Integer group : articleGroups) {
            builder.append(delim).
                    append(CacheChannelHelper.KEY_GROUP).append(CacheChannelHelper.GROUP_KEY_VALUE_DELIMETER).append("\"")
                    .append(PredefinedGroups.ART).append(group).append("\"");
        }

        return builder.toString();
    }

}
