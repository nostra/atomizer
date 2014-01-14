package no.api.atomizer.cachechannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import static no.api.atomizer.cachechannel.CacheChannelHelper.createCacheChannelElementFromToken;
import static no.api.atomizer.cachechannel.CacheChannelHelper.isChannelKey;
import static no.api.atomizer.cachechannel.CacheChannelHelper.isChannelMaxAgeKey;
import static no.api.atomizer.cachechannel.CacheChannelHelper.isGroupKey;
import static no.api.atomizer.cachechannel.CacheChannelHelper.isLegalCacheChannelElement;
import static no.api.atomizer.cachechannel.CacheChannelHelper.isMaxAgeKey;
import static no.api.atomizer.cachechannel.CacheChannelHelper.stripId;
import static no.api.atomizer.cachechannel.CacheChannelHelper.*;

/**
 * Handle insertion of elements into what should end up as a Cache-Control header in the scrutinized output.
 *
 * <p>The class is meant for use by HttpHeaderScrutinizer only.</p>
 *
 * <p>There are some internal logic that it is important to be aware of. First of all a new max-age will only be added
 * if it is the first attempt to set it, or if it is lower than the current one. Negative values will be ignored.</p>
 *
 * <p>Secondly you must know that only group elements can be added in addition to the required ones (max-age, channel
 * and channel-maxage). This is a new limitation that is implemented by purpose. The addElements() method takes great
 * pride in testing the given elements. For instance it is only possible to add a group value once. So if the string
 * 'group="/art1" group="/art1"' is given as input to addElements(), only one will be added. Turn on debug to see full
 * explanation of why an element might be ignored.</p>
 *
 * <p>But the most important issue here to understand is the automagic removal of groups that is done if the addition of
 * elements will cause the Cache-Control field length to be longer than MAX_CACHE_CHANNEL_FIELD_LENGTH. Earlier a poison
 * pill would be set immediately if such were the case. But now we think that it is better to remove the least important
 * groups before poisoning the output (if possible).</p>
 *
 * <p>So what decides if a group is more important than another? We have decided that groups prefixed with /art are less
 * important than /sec groups. And that all other groups are more important than these two. Within /art groups we have
 * decided that a group with a high id (eg: /art99999) is more important than one with a low id (eg: /art10). The same
 * goes for /sec groups. This way we are able to produce a Cache-Control header. If no more headers could be removed (No
 * /art og /sec left), then we give up. And a poison pill is set by the scrutinizer.</p>
 *
 * @since 1.27
 */
public class CacheControl {


    public static final int DEFAULT_CHANNEL_MAX_AGE = 86400;

    public static final int DEFAULT_MAX_AGE = 30;

    public static final String CACHE_CONTROL = "Cache-Control";

    /**
     * In some future, this will be changed to something that can be configured.
     */
    private static final String DEFAULT_CHANNEL = "http://localhost:9006/atomizer/event/current";

    private static final int NUMBER_OF_CLEANUP_ATTEMPTS_BEFORE_CUT_OFF = 499;

    private static final int MAINTAINED_AGE = 300;

    protected static final int DEFAULT_MAX_CACHE_CHANNEL_FIELD_LENGTH = 2048;

    private int maxCacheChannelFieldLength;

    private String channel;

    private List<String> otherGroups;

    private List<Integer> articleGroups;

    private List<Integer> sectionGroups;

    private boolean isMaintained;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private MaxAgeSetter maxAgeSetter;

    private MaxAgeSetter channelMaxAgeSetter;

    /**
     * This constructor is only intended to be used from junit tests.
     */
    protected CacheControl(int maxCacheChannelFieldLength, HttpServletResponse response) {
        this.maxCacheChannelFieldLength = maxCacheChannelFieldLength;
        this.maxAgeSetter = new RegularLowestValueMaxAgeSetter(response);
        this.channelMaxAgeSetter = new RegularLowestValueMaxAgeSetter(response);
        init();
    }

    /**
     * Intended to be used from junit only
     */
    protected CacheControl() {
        this(DEFAULT_MAX_CACHE_CHANNEL_FIELD_LENGTH, null);
    }

    /**
    * Instantiating with a max CC length of 2048, which is about as much as is convenient for jetty without
    * reconfiguration.
    */
    protected CacheControl(MaxAgeSetter channelMaxAgeSetter, MaxAgeSetter maxAgeSetter) {
        maxCacheChannelFieldLength = DEFAULT_MAX_CACHE_CHANNEL_FIELD_LENGTH;
        this.maxAgeSetter = maxAgeSetter;
        this.channelMaxAgeSetter = channelMaxAgeSetter;
        init();
    }

    private void init() {
        channel = DEFAULT_CHANNEL;
        isMaintained = false;
        otherGroups = new ArrayList<String>();
        articleGroups = new ArrayList<Integer>();
        sectionGroups = new ArrayList<Integer>();
    }

    /**
     * Get max-age.
     *
     * @return The current max-age. -1 if not set.
     */
    protected int getMaxAge() {
        return maxAgeSetter.getMaxAge();
    }

    /**
     * Return the max-age value if set. Else return the default max-age.
     *
     * @return The given max-age or the default if not set.
     */
    protected int getMaxAgeOrDefault() {
        if (getMaxAge() == -1) {
            return DEFAULT_MAX_AGE;
        }
        return getMaxAge();
    }

    /**
     * Setting max-age in proxy
     */
    public void setMaxAge(int value) {
        maxAgeSetter.setMaxAge(value);
    }

    protected int getChannelMaxAge() {
        return channelMaxAgeSetter.getMaxAge();
    }

    /**
     * Return the max-age value if set. Else return the default max-age.
     *
     * @return The given max-age or the default if not set.
     */
    protected int getChannelMaxAgeOrDefault() {
        if (getChannelMaxAge() == -1) {
            return DEFAULT_CHANNEL_MAX_AGE;
        }
        return getChannelMaxAge();
    }

    /**
     * Setting max-age in proxy
     */
    public void setChannelMaxAge(int value) {
        channelMaxAgeSetter.setMaxAge(value);
    }

    /**
     * @return true if the contents in cache channel header is modified in some way.
     */
    public boolean isMaintained() {
        return isMaintained;
    }


    /**
     * Get the channel url.
     *
     * @return The channel url as set with setChannel(), or the default one if not set.
     */
    protected String getChannel() {
        return channel;
    }

    /**
     * Set the channel url.
     *
     * @param channel A non-null string with the url to the atomizer event feed
     */
    protected void setChannel(String channel) {
        if (channel == null) {
            log.warn("Attempt to add null value for channel url. Rejecting");
            return;
        } else if (!channel.startsWith("http")) {
            log.warn("Adding channel url that does not start with http. Please check: {}", channel);
        }
        this.channel = channel;
    }


    /**
     * Add one or many elements.
     *
     * <p><i>IMPORTANT:</i> If this method returns false you _MUST_ immediately react to this by setting a poison
     * pill!</p>
     *
     * @param value The element(s) to be added to this cache control object.
     * @return <code>true</code> if the elements were added or the internal organization of the elements still is within
     *         the max limit. <code>false</code> if no more elements could be added, or replaced. A poison pill should
     *         be used in this case.
     */
    public boolean addElements(String value) {
        if (value == null) {
            return true; // since false means poison pill
        }
        StringTokenizer tokenizer = new StringTokenizer(value, CacheChannelHelper.GROUP_DELIMETER);
        while (tokenizer.hasMoreTokens()) {
            addElement(createCacheChannelElementFromToken(tokenizer.nextToken()));
        }
        return cleanup();
    }


    /**
     * Create channel string and test if it exceeds the max field length.
     *
     * @return <code>true</code> if the string length exceeds the max field length for cache control headers, else
     *         <code>false</code> if not.
     */
    private boolean hasExceededMaxFieldLength() {
        return hasExceededMaxFieldLength(createCacheChannelString(
                getMaxAgeOrDefault(),
                getChannel(),
                otherGroups,
                articleGroups,
                sectionGroups,
                getChannelMaxAgeOrDefault()).length());
    }

    /**
     * Get the cache control string.
     *
     * @return The cache control string if not larger than the max limit, else <code>null</code>.
     */
    public String toCacheChannelString() {
        // Code using this method should have reacted to the return statement of addElements
        String s = createCacheChannelString(
                getMaxAgeOrDefault(),
                getChannel(),
                otherGroups,
                articleGroups,
                sectionGroups,
                getChannelMaxAgeOrDefault());
        if (hasExceededMaxFieldLength(s.length())) {
            return null;
        }
        return s;
    }

    /**
     * Add a new element to the cache control object.
     *
     * <p>Only elements with a valid key (channel, max-age, channel-maxage, must-revalidate, no-cache,group) will be
     * tried added.</p>
     *
     * @param element The CC element to be added.
     * @return <code>true</code> if the element was added, <code>false</code> if the element was rejected for some
     *         reason. This can be caused by invalid/dirty input, or internal logic.
     */
    private boolean addElement(no.api.atomizer.cachechannel.CacheChannelElement element) {

        if (element == null) {
            if (log.isDebugEnabled()) {
                log.debug("Given element is null. " +
                        "This is most likely caused by an illegal value string in addElement(String) ");
            }
            return false;
        } else if (!isLegalCacheChannelElement(element)) {
            if (log.isDebugEnabled()) {
                log.debug("Skipping element since it doesn't contain a legal key/pair: {}", element.toString());
            }
            return false;
        } else if (isChannelKey(element.getKey())) {
            setChannel(element.getValue());
        } else if (isChannelMaxAgeKey(element.getKey())) {
            try {
                setChannelMaxAge(Integer.parseInt(element.getValue()));
            } catch (NumberFormatException ignored) {
                log.warn("Skipping channel-maxage element with value: {} since it is not a valid integer.",
                        element.getValue());
                return false;
            }

        } else if (isMaxAgeKey(element.getKey())) {

            try {
                setMaxAge(Integer.parseInt(element.getValue()));
            } catch (NumberFormatException ignored) {
                log.warn("Skipping max-age element with value: {} since it is not a valid integer.",
                        element.getValue());
                return false;
            }
        } else if (isGroupKey(element.getKey()) && element.hasValue()) {
            return addGroupElement(element);
        } else {
            log.warn("Should never reach this point. Given that the isLegalKey() " +
                    "method does what it is intended to do : " + element.toString());
            return false;
        }

        return true;
    }

    private boolean addGroupElement(CacheChannelElement element) {
        if (isArticleGroupElement(element.getValue())) {
            return addArticleGroupElement(element);
        } else if (isSectionGroupElement(element.getValue())) {
            return addSectionGroupElement(element);
        } else {
            if (otherGroups.contains(element.getValue())) {
                if (log.isDebugEnabled()) {
                    log.debug("Will not add this element since it is already in the group list: {}",
                            element.getValue());
                }
                return false;
            }
            otherGroups.add(element.getValue());
            return true;
        }
    }

    private boolean addArticleGroupElement(CacheChannelElement element) {
        Integer i = stripId(element.getValue(), "/art");
        if (i == null) {
            if (otherGroups.contains(element.getValue())) {
                log.debug("Will not add this element since it is already in the group list: {}", element.getValue());
                return false;
            } else {
                log.debug("An article id couldn't be pulled from the group string. " +
                        "The element will not be treated as an article: {}", element.getValue());
                otherGroups.add(element.getValue());
                return true;
            }
        } else if (articleGroups.contains(i)) {
            log.debug("Will not add this element since it is already in the article list: {}", element.getValue());
            return false;
        } else {
            articleGroups.add(i);
            return true;
        }
    }

    private boolean addSectionGroupElement(CacheChannelElement element) {
        Integer i = stripId(element.getValue(), "/sec");
        if (i == null) {
            if (otherGroups.contains(element.getValue())) {
                log.debug("Will not add this element since it is already in the group list: {}", element.getValue());
                return false;
            } else {
                log.debug("An section id couldn't be pulled from the group string. " +
                        "The element will not be treated as an section: {}", element.getValue());
                otherGroups.add(element.getValue());
                return true;
            }
        } else if (sectionGroups.contains(i)) {
            log.debug("Will not add this element since it is already in the section list: {}", element.getValue());
            return false;
        } else {
            sectionGroups.add(i);
            return true;
        }
    }

    private boolean hasExceededMaxFieldLength(int value) {
        return value > maxCacheChannelFieldLength;
    }

    /**
     * Remove least important elements if the CC header has exceeded the max allowed length.
     *
     * <p>In an ideal world we wouldn't have to remove added CC elements. But unfortunately Jetty (maybe others as well)
     * have limitations on how many bytes a HTTP header can contain.</p>
     *
     * @return <code>true</code> if the cleanup process succeeded, <code>false</code> if the method wasn't able to
     *         cleanup properly.
     */
    private boolean cleanup() {
        // Prevent looping forever as an extra precaution
        int attempts = 0;

        while (hasExceededMaxFieldLength() && (containsArticles() || containsSections()) && attempts <= NUMBER_OF_CLEANUP_ATTEMPTS_BEFORE_CUT_OFF) {
            log.debug("The cache control field is to long. Trying to cleanup.");

            if (containsArticles()) {
                removeLeastImportantArticle();
            } else if (containsSections()) {
                removeLeastImportantSection();
            }

            attempts++;
        }

        if (attempts > NUMBER_OF_CLEANUP_ATTEMPTS_BEFORE_CUT_OFF) {
            log.error("The cleanup process has exceeded 500 iterations. " +
                    "This is either due to a bug in the code or because of " +
                    "way to many elements attempted to be added. Turn on debug for full output.");
            if (log.isDebugEnabled()) {
                log.debug("Cache-Control : " + createCacheChannelString(
                        getMaxAgeOrDefault(),
                        getChannel(),
                        otherGroups,
                        articleGroups,
                        sectionGroups,
                        getChannelMaxAgeOrDefault()));
            }
            return false;
        }
        return true;
    }


    private boolean containsArticles() {
        return articleGroups.size() > 0;
    }

    private boolean containsSections() {
        return sectionGroups.size() > 0;
    }

    private void removeLeastImportantArticle() {
        isMaintained = true;
        setChannelMaxAge(MAINTAINED_AGE);
        Collections.sort(articleGroups);
        Integer popped = articleGroups.remove(0);
        if (log.isDebugEnabled()) {
            log.debug("Removed element (index={}) from the list of article groups.", popped);
        }
    }

    private void removeLeastImportantSection() {
        isMaintained = true;
        setChannelMaxAge(MAINTAINED_AGE);
        Collections.sort(sectionGroups);
        Integer popped = sectionGroups.remove(0);
        if (log.isDebugEnabled()) {
            log.debug("Removed element (index={}) from the list of section groups.", popped);
        }
    }
}

