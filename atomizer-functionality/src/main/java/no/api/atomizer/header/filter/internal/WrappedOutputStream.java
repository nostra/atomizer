package no.api.atomizer.header.filter.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletOutputStream;
import java.io.IOException;

/**
 *
 */
public class WrappedOutputStream extends ServletOutputStream {
    private static final Logger log = LoggerFactory.getLogger(WrappedOutputStream.class);

    private final ServletOutputStream outputStream;

    private final HeaderCollector headerCollector;

    private boolean hasFlushed = false;

    public WrappedOutputStream(ServletOutputStream outputStream, HeaderCollector headerCollector) {
        this.outputStream = outputStream;
        this.headerCollector = headerCollector;
    }

    @Override
    public void write(int i) throws IOException {
        if ( !hasFlushed ) {
            hasFlushed = true;
            headerCollector.flushHeaders();
        }
        outputStream.write(i);
    }
}
