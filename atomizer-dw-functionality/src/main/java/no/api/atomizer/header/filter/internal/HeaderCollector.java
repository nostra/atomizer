package no.api.atomizer.header.filter.internal;

import no.api.atomizer.cachechannel.CacheControl;
import no.api.atomizer.header.structure.CacheChannelStructure;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static no.api.atomizer.header.filter.internal.SetOrAddEnum.ADD;
import static no.api.atomizer.header.filter.internal.SetOrAddEnum.SET;

/**
 *
 */
public class HeaderCollector extends HttpServletResponseWrapper {

    private final List<Cookie> cookies = new ArrayList<>();

    // TODO Consider: It may be just as well to send date and int directly to super.
    private final AddOrSetHeaderProxy<Long> dateHeaders = new AddOrSetHeaderProxy<>();
    private final AddOrSetHeaderProxy<Integer> intHeaders = new AddOrSetHeaderProxy<>();
    private final AddOrSetHeaderProxy<String> headers = new AddOrSetHeaderProxy<>();

    public HeaderCollector(HttpServletResponse response) {
        super(response);
    }

    @Override
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    protected void flushHeaders() {
        for ( Cookie cookie : cookies ) {
            super.addCookie(cookie);
        }
        for ( HeaderElement<Long> d : dateHeaders) {
            if ( d.getOperation() == SET) {
                super.setDateHeader(d.getName(), d.getValue().longValue());
            } else {
                super.addDateHeader(d.getName(), d.getValue().longValue());
            }
        }
        for ( HeaderElement<Integer> d : intHeaders) {
            if ( d.getOperation() == SET) {
                super.setIntHeader(d.getName(), d.getValue().intValue());
            } else {
                super.addIntHeader(d.getName(), d.getValue().intValue());
            }
        }
        CacheChannelStructure structurizer = new CacheChannelStructure();
        for ( HeaderElement<String> d : headers) {
            if ( d.getName().equalsIgnoreCase(CacheControl.CACHE_CONTROL)) {
                structurizer.addCacheControlHeader( d.getValue() );
            } else if ( d.getOperation() == SET) {
                super.setHeader(d.getName(), d.getValue());
            } else {
                super.addHeader(d.getName(), d.getValue());
            }
        }
        if ( structurizer.hasContents() ) {
            super.setHeader(CacheControl.CACHE_CONTROL, structurizer.toCacheChannelString());
        }
    }

    @Override
    public boolean containsHeader(String name) {
        return getHeader(name) != null;
    }


    @Override
    public void sendError(int sc, String msg) throws IOException {
        flushHeaders();
        super.sendError(sc, msg);
    }

    @Override
    public void sendError(int sc) throws IOException {
        flushHeaders();
        super.sendError(sc);
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        flushHeaders();
        super.sendRedirect(location);
    }

    @Override
    public void setDateHeader(String name, long date) {
        dateHeaders.putHeader(new HeaderElement(name, new Long(date), SET));
    }

    @Override
    public void addDateHeader(String name, long date) {
        dateHeaders.putHeader(new HeaderElement(name, new Long(date), ADD));
    }

    @Override
    public void setHeader(String name, String value) {
        headers.putHeader(new HeaderElement(name, value, SET));
    }

    @Override
    public void addHeader(String name, String value) {
        headers.putHeader(new HeaderElement(name, value, ADD));
    }

    @Override
    public void setIntHeader(String name, int value) {
        intHeaders.putHeader(new HeaderElement(name, Integer.valueOf(value), SET));
    }

    @Override
    public void addIntHeader(String name, int value) {
        intHeaders.putHeader(new HeaderElement(name, Integer.valueOf(value), ADD));
    }


    @Override
    public String getHeader(String name) {
        Collection<String> h = getHeaders(name);
        if ( h.isEmpty()) {
            return null;
        }
        return h.iterator().next();
    }

    @Override
    public Collection<String> getHeaders(String name) {
        List<HeaderElement> elems = new ArrayList<>();
        elems.addAll(dateHeaders.byName(name));
        elems.addAll(intHeaders.byName(name));
        elems.addAll(headers.byName(name));
        List<String> result = new ArrayList<>();
        for ( HeaderElement h : elems ) {
            result.add(""+h.getValue());
        }

        return result;
    }

    @Override
    public Collection<String> getHeaderNames() {
        Set<String> names = new HashSet<>();
        names.addAll(dateHeaders.names());
        names.addAll(intHeaders.names());
        names.addAll(headers.names());

        return names;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new WrappedOutputStream( super.getOutputStream(), this);
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return new PrintWriter( new WrappedPrintWriter( super.getWriter(), this) );
    }

    @Override
    public void flushBuffer() throws IOException {
        flushHeaders();
        super.flushBuffer();
    }

    @Override
    public void reset() {
        // This resets first the buffers, and then the status codes. Super takes care of status codes
        resetBuffer();
        super.reset();
    }

    @Override
    public void resetBuffer() {
        // Only buffer are cleared. That would be header elements if present:
        cookies.clear();
        dateHeaders.clear();
        intHeaders.clear();
        headers.clear();
        super.resetBuffer();
    }

}
