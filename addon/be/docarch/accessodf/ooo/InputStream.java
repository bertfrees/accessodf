package be.docarch.accessodf.ooo;

import java.io.File;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;

import com.sun.star.io.XInputStream;
import com.sun.star.io.XSeekable;

import java.io.IOException;
import java.io.FileNotFoundException;

import com.sun.star.io.BufferSizeExceededException;
import com.sun.star.io.NotConnectedException;


/**
 * <a href="http://www.oooforum.org/forum/viewtopic.phtml?t=13205">From the thread <b>OOo-Java: Using XInputStream...</b></a>
 */
public class InputStream extends ByteArrayInputStream
                      implements XInputStream,
                                 XSeekable {

    public static InputStream newInstance(File input)
                                   throws IOException,
                                          FileNotFoundException {

        java.io.InputStream inputFile = new BufferedInputStream(new FileInputStream(input));
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        byte[] byteBuffer = new byte[4096];
        int byteBufferLength = 0;
        while ((byteBufferLength = inputFile.read(byteBuffer)) > 0) {
            bytes.write(byteBuffer,0,byteBufferLength);
        }
        inputFile.close();

        return new InputStream(bytes.toByteArray());

    }

    public InputStream(byte[] buf) {
        super(buf);
    }

    public int readBytes(byte[][] buffer,
                         int bufferSize)
                  throws NotConnectedException,
                         BufferSizeExceededException,
                         com.sun.star.io.IOException {

        int numberOfReadBytes;
        try {
            byte[] bytes = new byte[bufferSize];
            numberOfReadBytes = super.read(bytes);
            if(numberOfReadBytes > 0) {
                if(numberOfReadBytes < bufferSize) {
                    byte[] smallerBuffer = new byte[numberOfReadBytes];
                    System.arraycopy(bytes, 0, smallerBuffer, 0, numberOfReadBytes);
                    bytes = smallerBuffer;
                }
            }
            else {
                bytes = new byte[0];
                numberOfReadBytes = 0;
            }

            buffer[0]=bytes;
            return numberOfReadBytes;
        }
        catch (IOException e) {
            throw new com.sun.star.io.IOException(e.getMessage(),this);
        }
    }

    public int readSomeBytes(byte[][] buffer,
                             int bufferSize)
                          throws NotConnectedException,
                                 BufferSizeExceededException,
                                 com.sun.star.io.IOException {

        return readBytes(buffer, bufferSize);
    }

    public void skipBytes(int skipLength)
                   throws NotConnectedException,
                          BufferSizeExceededException,
                          com.sun.star.io.IOException {

        skip(skipLength);
    }

    public void closeInput() throws NotConnectedException,
                                    com.sun.star.io.IOException {
        try {
            close();
        }
        catch (java.io.IOException e) {
            throw new com.sun.star.io.IOException(e.getMessage(), this);
        }
    }

    public long getLength() throws com.sun.star.io.IOException {
        return count;
    }

    public long getPosition() throws com.sun.star.io.IOException {
        return pos;
    }

    public void seek(long position)
              throws IllegalArgumentException,
                     com.sun.star.io.IOException {

        pos = (int) position;
    }
}
