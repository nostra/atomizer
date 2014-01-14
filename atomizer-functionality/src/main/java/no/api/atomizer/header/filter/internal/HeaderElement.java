package no.api.atomizer.header.filter.internal;

/**
 *
 */
public class HeaderElement<T> {

    private final String name;

    private final T value;

    private final SetOrAddEnum operation;

    public HeaderElement(String name, T value, SetOrAddEnum operation) {
        this.name = name;
        this.value = value;
        this.operation = operation;
    }

    public SetOrAddEnum getOperation() {
        return operation;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }
}
