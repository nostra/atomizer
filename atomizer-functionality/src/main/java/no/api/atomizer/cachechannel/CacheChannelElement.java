package no.api.atomizer.cachechannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static no.api.atomizer.cachechannel.CacheChannelHelper.KEY_CHANNEL_MAX_AGE;
import static no.api.atomizer.cachechannel.CacheChannelHelper.KEY_GROUP;
import static no.api.atomizer.cachechannel.CacheChannelHelper.KEY_MUST_REVALIDATE;
import static no.api.atomizer.cachechannel.CacheChannelHelper.KEY_NO_CACHE;
import static no.api.atomizer.cachechannel.CacheChannelHelper.KEY_PRE_CHECK;
import static no.api.atomizer.cachechannel.CacheChannelHelper.KEY_CHANNEL;
import static no.api.atomizer.cachechannel.CacheChannelHelper.KEY_MAX_AGE_FRONT_END;
import static no.api.atomizer.cachechannel.CacheChannelHelper.KEY_POST_CHECK;

public class CacheChannelElement  {

    private static Logger log = LoggerFactory.getLogger(CacheChannelElement.class);

    private final String key;

    private final String value;


    /**
     * Construct a new CacheElement with a given key and value.
     *
     * @param key   The cache element key
     * @param value The cache element value
     */
    public CacheChannelElement(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public boolean hasKey() {
        return getKey() != null;
    }

    public boolean hasValue() {
        return getValue() != null;
    }

    public boolean isLegalKey() {
        return hasKey() && (KEY_CHANNEL.equals(getKey()) || KEY_CHANNEL_MAX_AGE.equals(getKey()) || KEY_GROUP.equals(getKey()) ||
                KEY_MAX_AGE_FRONT_END.equals(getKey()) || KEY_MUST_REVALIDATE.equals(getKey()) || KEY_NO_CACHE.equals(getKey()) || KEY_POST_CHECK.equals(getKey()) ||
                KEY_PRE_CHECK.equals(getKey()));
    }

    public boolean isLegal() {
        if ( !isLegalKey() ) {
            log.warn(
                    "The key ({}) is not legal (channel, max-age, channel-maxage, group, must-revalidate, no-cache, " +
                            "pre-check, post-check). Returning false",
                    getKey());
            return false;
        } else if (getKey().equals(KEY_MUST_REVALIDATE)) {
            log.debug("Received must-revalidate element. This is ignored as this service don't handle caching at all.");
            return false;
        } else if (getKey().equals(KEY_NO_CACHE)) {
            log.debug("Received no-cache element. Ignoring this, in the same manner that must-revalidate is ignored.");
            return false;
        } else if ( getKey().equals(KEY_PRE_CHECK) || getKey().equals(KEY_POST_CHECK)) {
            // pre-check and post-check is filtered since Varnish doesn't care about them
            log.debug("Received {} element is filtered.", getKey());
            return false;
        } else if (!hasValue() && !getKey().equals(KEY_CHANNEL_MAX_AGE)) {
            log.debug("Given value is null. No point in continuing. Returning false");
            return false;
        }
        return true;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("key=");
        if (key != null) {
            sb.append(key);
        } else {
            sb.append("<null>");
        }
        sb.append(", ");
        sb.append("value=");
        if ( value != null) {
            sb.append(value);
        } else {
            sb.append("<null>");
        }
        return sb.toString();
    }

}
