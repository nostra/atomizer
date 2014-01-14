package no.api.atomizer.header.filter.internal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * The purpose of this class is to retain header objects until setting all of them at once
 */
public class AddOrSetHeaderProxy<T> implements Iterable<HeaderElement<T>> {
    private List<HeaderElement<T>> container = new ArrayList<>();

    public void putHeader(HeaderElement e) {
        if ( e.getOperation() == SetOrAddEnum.SET ) {
            int indx = lastIndexOf(e.getName());
            if (  indx != -1 ) {
                container.remove(indx);
            }
        }
        container.add(e);
    }

    /**
     * Return last index of header element with name name, or -1 if not found
     */
    private int lastIndexOf(String name) {
        for ( int i=container.size() -1 ; i >= 0 ; i-- ) {
            if ( container.get(i).getName().equals(name)) {
                return i;
            }
        }

        return -1;
    }

    public int size() {
        return container.size();
    }

    @Override
    public Iterator<HeaderElement<T>> iterator() {
        return container.iterator();
    }


    public Collection<? extends HeaderElement> byName(String name) {
        List<HeaderElement<T>> result = new ArrayList<>();
        for ( HeaderElement<T> h : container ) {
            if ( h.getName().equals(name)) {
                result.add(h);
            }
        }
        return result;
    }

    public Collection<? extends String> names() {
        Set<String> names = new HashSet<>();
        for ( HeaderElement h : container ) {
            names.add(h.getName());
        }
        return names;
    }

    public void clear() {
        container.clear();
    }
}
