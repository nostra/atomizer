package no.api.atomizer.header.structure;

import no.api.atomizer.cachechannel.CacheChannelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class CacheChannelStructure {
    private static final Logger log = LoggerFactory.getLogger(CacheChannelStructure.class);

    public static final int DEFAULT_CACHE_CHANNEL_MAX_LENGTH = 2048;

    public static final String TOO_LONG ="\"/too_long\"";
    public static final String TOO_LONG_GROUP ="group="+TOO_LONG;

    private final int maxCacheChannelFieldLength;

    private String channel = null;

    private LowestValueMaxAgeSetter maxAge = new LowestValueMaxAgeSetter();

    private boolean inputGiven = false;

    private ChannelMaxAgeSetter channeMaxage = new ChannelMaxAgeSetter();

    private List<String> groups = new ArrayList<>();

    public CacheChannelStructure() {
        this(DEFAULT_CACHE_CHANNEL_MAX_LENGTH);
    }

    public CacheChannelStructure(int maxCacheChannelFieldLength) {
        this.maxCacheChannelFieldLength = maxCacheChannelFieldLength;
    }


    public String toCacheChannelString() {
        StringBuilder sb = new StringBuilder();
        if ( maxAge != null ) {
            addCommaIfNotEmpty( sb );
            sb.append("max-age=").append(maxAgeOrDefault());
        }
        if ( channel != null ) {
            addCommaIfNotEmpty( sb );
            sb.append("channel=").append(channel);
        }
        int calcChannelMaxAge = channelMaxAgeByIntricateRules();
        addCommaIfNotEmpty( sb );
        String channelExpr = "channel-maxage="+calcChannelMaxAge;

        Set <String> groupsToRetain = findGroupsToRetain( groups, sb.length() + channelExpr.length());

        if ( groupsToRetain.contains(CacheChannelStructure.TOO_LONG)) {
            calcChannelMaxAge = Math.min( 300, calcChannelMaxAge ); // TODO 300 is a default
            channeMaxage.setMaxAge(calcChannelMaxAge);
            calcChannelMaxAge = channelMaxAgeByIntricateRules();
            channelExpr = "channel-maxage="+calcChannelMaxAge;
            if ( calcChannelMaxAge < maxAgeOrDefault()) {
                maxAge.setMaxAge(calcChannelMaxAge );
                // Recurse to recalculate with the new value;
                return toCacheChannelString();
            }
        }
        sb.append( channelExpr );

        for ( String g : groupsToRetain ) {
            addCommaIfNotEmpty( sb );
            sb.append("group=").append(g);
        }
        return sb.toString();
    }

    /**
     * Protected for the benefit of junit tests
     */
    protected StringBuilder trimChannelStringAndAddTooLongGroup(StringBuilder sb) {
        if ( sb.length()+TOO_LONG_GROUP.length()+2 <= maxCacheChannelFieldLength ) {
            addCommaIfNotEmpty(sb);
            sb.append(TOO_LONG_GROUP);
            return sb;
        }
        String str = sb.toString();
        int indx = str.lastIndexOf(",");
        if ( indx == -1 ) {
            log.warn("Failed to figure out last element added. Do not expect this in production. Limit: "+
                    maxCacheChannelFieldLength+", Channel string: "+sb);
            return sb;
        }
        // Recursing with one less group statement.
        return trimChannelStringAndAddTooLongGroup( new StringBuilder(str.substring(0, indx)) );
    }

    /**
     * @return If it is actually set, use it. If it does not, set it equal to max-age, if maxage is set. If
     * max-age is not set, use default.
     */
    private int channelMaxAgeByIntricateRules() {
        if ( channeMaxage.getChannelMaxAge() < 0 ) {
            if (maxAge.getMaxAge() < 0 ) {
                return 86400; // TODO Default
            } else {
                return maxAge.getMaxAge();
            }
        }
        return channeMaxage.getChannelMaxAge();
    }

    private int maxAgeOrDefault() {
        if ( maxAge.getMaxAge() < 0 ) {
            return 30; // TODO Default Max age
        }
        return maxAge.getMaxAge();
    }

    private Set<String> findGroupsToRetain(List<String> groups, final int currentLength) {
        Set<String> uniqueGroups = new LinkedHashSet<>();
        Set<String> result = new LinkedHashSet<>();
        uniqueGroups.addAll(groups); // Unique, but in the same sequence as the list
        int groupStrLength = 2; // Starting at 2 due to ", "
        String last = null;
        for ( String g : uniqueGroups ) {
            int lengthOfGroup = Math.max(g.length(), TOO_LONG.length()) + "group=".length();
            // +2 is ", " (comma space)
            if ( currentLength + groupStrLength + lengthOfGroup + 2 > maxCacheChannelFieldLength ) {
                result.remove(last);
                result.add( TOO_LONG);
                return result;
            }
            groupStrLength += lengthOfGroup+2;
            last = g;
            result.add(g);
        }
        return result;
    }

    public boolean hasContents() {
        return inputGiven;
    }

    /** Protected for the sake of junit tests */
    protected int getMaxAge() {
        return maxAge.getMaxAge();
    }

    /** Protected for the sake of junit tests */
    protected int getChannelMaxAge() {
        return channeMaxage.getChannelMaxAge();
    }

    public void addCacheControlHeader(String value) {
        inputGiven = true;
        for ( String elem : value.split(",") ) {
            String[] field = elem.split("=");

            switch ( field[0].trim().toLowerCase() ) {
                case "max-age":
                    if ( field.length > 1 ) { // It is possible that max-age is set without an argument
                        adjustMaxAge( field[1].trim() );
                    }
                    break;
                case "channel-maxage":
                    if ( field.length > 1 ) { // Sometimes channel-maxage is set without argument
                        adjustChannelMaxAge(field[1]);
                    }
                    break;
                case "channel":
                    if ( field.length > 1 ) { // Paranoia
                        keepTheLastChannelIndicated(field[1]);
                    }
                    break;
                case "group":
                    if ( field.length > 1 ) { // Paranoia
                        addGroup(field[1]);
                    }
                    break;
                default:
                    // TODO Will actually just be spooled through (later)
                    throw new CacheChannelException("Not yet supported: "+field[0]);
            }

        }
    }

    private void addGroup(String g ) {
        groups.add( g );
    }

    private void adjustChannelMaxAge(String elem) {
        try {
            channeMaxage.setMaxAge(Integer.parseInt(elem));
            if (  maxAge.getMaxAge() > 0 && maxAge.getMaxAge() > channeMaxage.getChannelMaxAge()) {
                maxAge.setMaxAge( channeMaxage.getChannelMaxAge() );
            }
        } catch (final NumberFormatException e) {
            log.debug("Got exception setting channel-maxage. Element will be ignored: " + elem +
                    ". Intentionally masked exception: " + e);
        }
    }

    private StringBuilder addCommaIfNotEmpty(StringBuilder sb) {
        if ( sb.length() > 0 ) {
            sb.append(", ");
        }
        return sb;
    }

    private void keepTheLastChannelIndicated(String elem) {
        this.channel = elem;
    }

    private void adjustMaxAge(String elem) {
        try {
            maxAge.setMaxAge(Integer.parseInt(elem));
        } catch (NumberFormatException e) {
            log.debug("Got exception setting max-age. Element will be ignored: " + elem +
                    ". Intentionally masked exception: " + e);
        }
    }
}
