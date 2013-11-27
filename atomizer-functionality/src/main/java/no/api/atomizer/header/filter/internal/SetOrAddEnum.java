package no.api.atomizer.header.filter.internal;

/**
 *
 */
public enum SetOrAddEnum {
    SET( 1 ),
    ADD( 2 );

    private final int number;

    SetOrAddEnum( int number ) {
        this.number = number;
    }

    int number() {
        return number;
    }
}
