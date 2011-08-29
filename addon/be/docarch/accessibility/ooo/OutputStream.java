package be.docarch.accessibility.ooo;

import java.io.ByteArrayOutputStream;

import com.sun.star.io.BufferSizeExceededException;
import com.sun.star.io.NotConnectedException;
import com.sun.star.io.XOutputStream;

/**
 * <a href="http://www.oooforum.org/forum/viewtopic.phtml?t=13205">From the thread <b>OOo-Java: Using XInputStream...</b></a>
 */
public class OutputStream extends ByteArrayOutputStream
                       implements XOutputStream {

    public OutputStream() {
        super(32768);
    }

    public void writeBytes(byte[] values)
                    throws NotConnectedException,
                           BufferSizeExceededException,
                           com.sun.star.io.IOException {
        try {
            this.write(values);
        }
        catch (java.io.IOException e) {
            throw(new com.sun.star.io.IOException(e.getMessage()));
        }
    }

    public void closeOutput() throws NotConnectedException,
                                     BufferSizeExceededException,
                                     com.sun.star.io.IOException {
        try {
            super.flush();
            super.close();
        }
        catch (java.io.IOException e) {
            throw(new com.sun.star.io.IOException(e.getMessage()));
        }
    }

    @Override
    public void flush() {
        try {
            super.flush();
        }
        catch (java.io.IOException e) {
        }
    }
}