package no.api.atomizer.cachechannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class AgeAndMaxAgeHolder {
    private static final Logger log = LoggerFactory.getLogger(AgeAndMaxAgeHolder.class);

    private int age = -1;
    private int maxAge = -1;
    private AgeAndMaxAgeHolder previous;

    public int getAge() {
        return age;
    }

    public AgeAndMaxAgeHolder adjustAge(int value) {
        if (value < 0) {
            log.warn("Attempt to add negative age attribute to cache channel. Rejecting attempt.");
        } else if (age == -1) {
            log.debug("Setting age since it is not set: {}", value);
            age = value;
            adjustAgeToNullIfGreaterThanMaxage();
        } else if ( maxAge != -1 ) {
            return currentOrNew().adjustAge(value);
        } else {
            log.debug("Overriding existing age {} as we got a new age without new CC entry. New value {}", age, value);
            // I know that maxage is -1 at this point
            age = value;
        }
        return this;
    }

    private void adjustAgeToNullIfGreaterThanMaxage() {
        if ( maxAge != -1 && age > maxAge) {
            log.warn("Age was set to "+age+" and max-age to "+ maxAge +", adjusting age to 0 in order to avoid problems. " +
                    "This indicates a logical problem, however");
            age = 0;
        }
    }

    public int getMaxAge() {
        return maxAge;
    }

    public AgeAndMaxAgeHolder adjustMaxage(int value) {
        if (value < 0) {
            log.warn("Attempt to add negative max-age attribute to cache channel. Ignoring attempt.");
            return this;
        }
        AgeAndMaxAgeHolder work = currentOrNew();
        log.debug("Setting maxage: {}", value);
        work.maxAge = value;
        work.adjustAgeToNullIfGreaterThanMaxage();
        return work;
    }

    /**
     * If we have a CC header already, we will create a new element
     */
    private AgeAndMaxAgeHolder currentOrNew() {
        if ( maxAge != -1 ) {
            log.debug("Creating new element");
            // The CC header is the thing we use to decide to rotate on
            AgeAndMaxAgeHolder blank = new AgeAndMaxAgeHolder();
            blank.setPrevious(this);
            return blank;
        }
        return this;
    }

    /**
     * Protected for the benefit of junit tests.
     * TODO Temporary public
     */
    public AgeAndMaxAgeHolder calculateMinimal() {
        if ( previous != null ) {
            AgeAndMaxAgeHolder minprev = previous.calculateMinimal();
            if ( calculate() > minprev.calculate()) {
                return minprev;
            }
        }
        return this;
    }

    private int calculate() {
        if ( maxAge == -1 ) {
            return CacheControl.DEFAULT_CHANNEL_MAX_AGE;
        }
        if ( age != -1 ) {
            return Math.max( maxAge - age, 0);
        }
        return maxAge;
    }


    public AgeAndMaxAgeHolder getPrevious() {
        return previous;
    }

    public void setPrevious(AgeAndMaxAgeHolder previous) {
        this.previous = previous;
    }

    /**
     * Used for sanity checking, as the size of the structure should never be
     * more than 2
     */
    public int size() {
        if ( previous != null ) {
            return 1+previous.size();
        }
        return 1;
    }

    public AgeAndMaxAgeHolder resetAge() {
        age=-1;
        return this;
    }
}
