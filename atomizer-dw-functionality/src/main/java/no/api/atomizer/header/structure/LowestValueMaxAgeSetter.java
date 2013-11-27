package no.api.atomizer.header.structure;

/**
 *
 */
public class LowestValueMaxAgeSetter {

    private int value = -1;

    public void setMaxAge(int value) {
        if ( value >= 0 ) {
            if ( value < this.value || this.value < 0 ) {
                this.value = value;
            }
        }
    }

    public int getMaxAge() {
        return value;
    }

}
