package no.api.atomizer.header.filter.internal;

import java.io.IOException;
import java.io.Writer;

/**
 *
 */
public class WrappedPrintWriter extends Writer {

    private final HeaderCollector headerCollector;

    private final Writer writer;

    private boolean hasFlushed = false;

    public WrappedPrintWriter(Writer writer, HeaderCollector headerCollector) {
        this.writer = writer;
        this.headerCollector = headerCollector;
    }


    @Override
    public void write(char[] chars, int i, int i2) throws IOException {
        flushHeaders();
        writer.write( chars, i, i2 );
    }

    @Override
    public void flush() throws IOException {
        flushHeaders();
        writer.flush();
    }

    @Override
    public void close() throws IOException {
        flushHeaders();
        writer.close();
    }

    private void flushHeaders() {
        if ( !hasFlushed ) {
            hasFlushed = true;
            headerCollector.flushHeaders();
        }
    }
}
