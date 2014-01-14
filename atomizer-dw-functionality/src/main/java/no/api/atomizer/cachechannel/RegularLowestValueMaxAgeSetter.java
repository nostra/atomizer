package no.api.atomizer.cachechannel;

import javax.servlet.http.HttpServletResponse;

/**
 * @deprecated To be replaced with version not dependant on response
 */
public class RegularLowestValueMaxAgeSetter implements MaxAgeSetter {
    private int value = -1;

    private final HttpServletResponse response;

    public RegularLowestValueMaxAgeSetter(HttpServletResponse response) {
        this.response = response;
    }

    @Override
    public void setMaxAge(int value) {
        if ( value < this.value || this.value < 0 ) {
            this.value = value;
            if ( response != null ) {
                // Need to test due to junit tests
                response.setHeader(CacheControl.CACHE_CONTROL, CacheChannelHelper.KEY_MAX_AGE_FRONT_END+"="+Math.max(this.value, 0));
            }
        }
    }

    @Override
    public int getMaxAge() {
        return value;
    }
}
