/*
 * Copyright 2016 Carlos Ballesteros Velasco
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package java.io;

import com.jtransc.JTranscBits;
import com.jtransc.io.JTranscSyncIO;

import java.nio.charset.StandardCharsets;

public class RandomAccessFile implements DataOutput, DataInput, Closeable {
    private FileDescriptor fd;
    //private FileChannel channel = null;
    private boolean rw;
    private final String path;
    
    private volatile boolean closed = false;
    
    private final JTranscSyncIO.ImplStream jfd;
    
    private byte[] temp = new byte[8];
    
    public RandomAccessFile(String name, String mode) throws FileNotFoundException {
        this(name != null ? new File(name) : null, mode);
    }
    
    public RandomAccessFile(File file, String mode) throws FileNotFoundException {
        String name = (file != null ? file.getAbsolutePath() : null);
        int imode = JTranscSyncIO.O_RDONLY;
        rw = false;
        if (mode.contains("w")) {
            imode = JTranscSyncIO.O_RDWR;
            rw = true;
        }
        if (mode.contains("s")) imode |= JTranscSyncIO.O_SYNC;
        if (mode.contains("d")) imode |= JTranscSyncIO.O_DSYNC;
        if (imode < 0)
            throw new IllegalArgumentException("Illegal mode \"" + mode + "\" must be one of " + "\"r\", \"rw\", \"rws\"," + " or \"rwd\"");
        if (name == null) throw new NullPointerException();
        if (file.isInvalid()) throw new FileNotFoundException("Invalid file path");
        fd = new FileDescriptor();
        //fd.attach(this);
        path = name;
        //System.out.println("RandomAccessFile");
        jfd = JTranscSyncIO.impl.open(name, imode);
    }
    
    public final FileDescriptor getFD() throws IOException {
        if (fd == null) throw new IOException();
        return fd;
    }
    
    public int read() throws IOException {
        return jfd.read();
    }
    
    public int read(byte b[], int off, int len) throws IOException {
        return jfd.read(b, off, len);
    }
    
    public int read(byte b[]) throws IOException {
        return jfd.read(b, 0, b.length);
    }
    
    public final void readFully(byte b[]) throws IOException {
        readFully(b, 0, b.length);
    }
    
    public final void readFully(byte b[], int off, int len) throws IOException {
        int n = 0;
        do {
            int count = this.read(b, off + n, len - n);
            if (count < 0) throw new EOFException();
            n += count;
        } while (n < len);
    }
    
    public int skipBytes(int n) throws IOException {
        long pos, len, newpos;
        if (n <= 0) return 0;
        pos = getFilePointer();
        len = length();
        newpos = pos + n;
        if (newpos > len) newpos = len;
        seek(newpos);
        return (int) (newpos - pos);
    }
    
    public void write(int b) throws IOException {
        jfd.write(b);
    }
    
    public void write(byte b[]) throws IOException {
        jfd.write(b, 0, b.length);
    }
    
    public void write(byte b[], int off, int len) throws IOException {
        jfd.write(b, off, len);
    }
    
    public long getFilePointer() throws IOException {
        return jfd.getPosition();
    }
    
    public void seek(long pos) throws IOException {
        if (pos < 0) throw new IOException("Negative seek offset");
        jfd.setPosition(pos);
    }
    
    public long length() throws IOException {
        return jfd.getLength();
    }
    
    public void setLength(long newLength) throws IOException {
        jfd.setLength(newLength);
    }
    
    public void close() throws IOException {
        if (closed) return;
        closed = true;
        //if (channel != null) channel.close();
        jfd.close();
    }
    
    public final boolean readBoolean() throws IOException {
        return readByte() != 0;
    }
    
    public final byte readByte() throws IOException {
        int ch = this.read();
        if (ch < 0) throw new EOFException();
        return (byte) (ch);
    }
    
    public final int readUnsignedByte() throws IOException {
        return readByte() & 0xFF;
    }
    
    public final short readShort() throws IOException {
        read(temp, 0, 2);
        return JTranscBits.readInt16BE(temp);
    }
    
    public final int readUnsignedShort() throws IOException {
        return readShort() & 0xFFFF;
    }
    
    public final char readChar() throws IOException {
        return (char) readUnsignedShort();
    }
    
    public final int readInt() throws IOException {
        read(temp, 0, 4);
        return JTranscBits.readInt32BE(temp);
    }
    
    public final long readLong() throws IOException {
        return ((long) (readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
    }
    
    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }
    
    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }
    
    public final String readLine() throws IOException {
        StringBuffer input = new StringBuffer();
        int c = -1;
        boolean eol = false;
        
        while (!eol) {
            switch (c = read()) {
                case -1:
                case '\n':
                    eol = true;
                    break;
                case '\r':
                    eol = true;
                    long cur = getFilePointer();
                    if ((read()) != '\n') seek(cur);
                    break;
                default:
                    input.append((char) c);
                    break;
            }
        }
        
        if ((c == -1) && (input.length() == 0)) return null;
        return input.toString();
    }
    
    public final String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }
    
    public final void writeBoolean(boolean v) throws IOException {
        write(v ? 1 : 0);
    }
    
    public final void writeByte(int v) throws IOException {
        write(v);
    }
    
    public final void writeShort(int v) throws IOException {
        write(JTranscBits.getInt16BE(temp, (short) v), 0, 2);
    }
    
    public final void writeChar(int v) throws IOException {
        writeShort(v);
    }
    
    public final void writeInt(int v) throws IOException {
        write(JTranscBits.getInt32BE(temp, (short) v), 0, 4);
    }
    
    public final void writeLong(long v) throws IOException {
        write(JTranscBits.getInt64BE(temp, v), 0, 8);
    }
    
    public final void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }
    
    public final void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }
    
    public final void writeBytes(String s) throws IOException {
        write(s.getBytes());
    }
    
    public final void writeChars(String s) throws IOException {
        write(s.getBytes("UTF-16"));
    }
    
    public final void writeUTF(String str) throws IOException {
        write(str.getBytes(StandardCharsets.UTF_8));
    }
}
