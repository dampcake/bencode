/*
 * Copyright 2015 Adam Peck.
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
package com.dampcake.bencode;

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InvalidObjectException;
import java.io.PushbackInputStream;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * InputStream for reading bencoded data.
 *
 * @author Adam Peck
 */
public class BencodeInputStream extends FilterInputStream {

    // EOF Constant
    private static final int EOF = -1;

    private final Charset charset;
    private final boolean useBytes;
    private final PushbackInputStream in;

    /**
     * Creates a new BencodeInputStream that reads from the {@link InputStream} passed and uses the {@link Charset} passed for decoding the data
     * and boolean passed to control String parsing.
     *
     * If useBytes is false, then dictionary values that contain byte string data will be coerced to a {@link String}.
     * if useBytes is true, then dictionary values that contain byte string data will be coerced to a {@link ByteBuffer}.
     *
     * @param in       the {@link InputStream} to read from
     * @param charset  the {@link Charset} to use
     * @param useBytes controls coercion of dictionary values
     *
     * @throws NullPointerException if the {@link Charset} passed is null
     * 
     * @since 1.3
     */
    public BencodeInputStream(final InputStream in, final Charset charset, boolean useBytes) {
        super(new PushbackInputStream(in));
        this.in = (PushbackInputStream) super.in;

        if (charset == null) throw new NullPointerException("charset cannot be null");
        this.charset = charset;
        this.useBytes = useBytes;
    }

    /**
     * Creates a new BencodeInputStream that reads from the {@link InputStream} passed and uses the {@link Charset} passed for decoding the data
     * and coerces dictionary values to a {@link String}.
     *
     * @param in      the {@link InputStream} to read from
     * @param charset the {@link Charset} to use
     *
     * @throws NullPointerException if the {@link Charset} passed is null
     *
     * @see #BencodeInputStream(InputStream, Charset, boolean)
     */
    public BencodeInputStream(final InputStream in, final Charset charset) {
        this(in, charset, false);
    }

    /**
     * Creates a new BencodeInputStream that reads from the {@link InputStream} passed and uses the UTF-8 {@link Charset} for decoding the data
     * and coerces dictionary values to a {@link String}.
     *
     * @param in the {@link InputStream} to read from
     *
     * @see #BencodeInputStream(InputStream, Charset, boolean)
     */
    public BencodeInputStream(final InputStream in) {
        this(in, Bencode.DEFAULT_CHARSET);
    }

    /**
     * Gets the {@link Charset} the stream was created with.
     *
     * @return the {@link Charset} of the stream
     */
    public Charset getCharset() {
        return charset;
    }

    private int peek() throws IOException {
        int b = in.read();
        in.unread(b);
        return b;
    }

    /**
     * Peeks at the next {@link Type}.
     *
     * @return the next {@link Type} available
     *
     * @throws IOException  if the underlying stream throws
     * @throws EOFException if the end of the stream has been reached
     */
    public Type nextType() throws IOException {
        int token = peek();
        checkEOF(token);

        return typeForToken(token);
    }

    private Type typeForToken(int token) {
        for (Type type : Type.values()) {
            if (type.validate(token))
                return type;
        }

        return Type.UNKNOWN;
    }

    /**
     * Reads a {@link String} from the stream.
     *
     * @return the {@link String} read from the stream
     *
     * @throws IOException            if the underlying stream throws
     * @throws EOFException           if the end of the stream has been reached
     * @throws InvalidObjectException if the next type in the stream is not a String
     */
    public String readString() throws IOException {
        return new String(readStringBytesInternal(), getCharset());
    }

    /**
     * Reads a Byte String from the stream.
     *
     * @return the {@link ByteBuffer} read from the stream
     *
     * @throws IOException            if the underlying stream throws
     * @throws EOFException           if the end of the stream has been reached
     * @throws InvalidObjectException if the next type in the stream is not a String
     * 
     * @since 1.3
     */
    public ByteBuffer readStringBytes() throws IOException {
        return ByteBuffer.wrap(readStringBytesInternal());
    }

    private byte[] readStringBytesInternal() throws IOException {
        int token = in.read();
        validateToken(token, Type.STRING);

        StringBuilder buffer = new StringBuilder();
        buffer.append((char) token);
        while ((token = in.read()) != Bencode.SEPARATOR) {
            validateToken(token, Type.STRING);

            buffer.append((char) token);
        }

        int length = Integer.parseInt(buffer.toString());
        byte[] bytes = new byte[length];
        read(bytes);
        return bytes;
    }

    /**
     * Reads a Number from the stream.
     *
     * @return the Number read from the stream
     *
     * @throws IOException            if the underlying stream throws
     * @throws EOFException           if the end of the stream has been reached
     * @throws InvalidObjectException if the next type in the stream is not a Number
     */
    public Long readNumber() throws IOException {
        int token = in.read();
        validateToken(token, Type.NUMBER);

        StringBuilder buffer = new StringBuilder();
        while ((token = in.read()) != Bencode.TERMINATOR) {
            checkEOF(token);

            buffer.append((char) token);
        }

        return new BigDecimal(buffer.toString()).longValue();
    }

    /**
     * Reads a List from the stream.
     *
     * @return the List read from the stream
     *
     * @throws IOException            if the underlying stream throws
     * @throws EOFException           if the end of the stream has been reached
     * @throws InvalidObjectException if the next type in the stream is not a List, or the list contains invalid types
     */
    public List<Object> readList() throws IOException {
        int token = in.read();
        validateToken(token, Type.LIST);

        List<Object> list = new ArrayList<Object>();
        while ((token = in.read()) != Bencode.TERMINATOR) {
            checkEOF(token);

            list.add(readObject(token));
        }

        return list;
    }

    /**
     * Reads a Dictionary from the stream.
     *
     * @return the Dictionary read from the stream
     *
     * @throws IOException            if the underlying stream throws
     * @throws EOFException           if the end of the stream has been reached
     * @throws InvalidObjectException if the next type in the stream is not a Dictionary, or the list contains invalid types
     */
    public Map<String, Object> readDictionary() throws IOException {
        int token = in.read();
        validateToken(token, Type.DICTIONARY);

        Map<String, Object> map = new LinkedHashMap<String, Object>();
        while ((token = in.read()) != Bencode.TERMINATOR) {
            checkEOF(token);

            in.unread(token);
            map.put(readString(), readObject(in.read()));
        }

        return map;
    }

    private Object readObject(final int token) throws IOException {
        in.unread(token);

        Type type = typeForToken(token);

        if (type == Type.STRING && !useBytes)
            return readString();
        if (type == Type.STRING && useBytes)
            return readStringBytes();
        if (type == Type.NUMBER)
            return readNumber();
        if (type == Type.LIST)
            return readList();
        if (type == Type.DICTIONARY)
            return readDictionary();

        throw new InvalidObjectException("Unexpected token '" + new String(Character.toChars(token)) + "'");
    }

    private void validateToken(final int token, final Type type) throws IOException {
        checkEOF(token);

        if (!type.validate(token)) {
            in.unread(token);
            throw new InvalidObjectException("Unexpected token '" + new String(Character.toChars(token)) + "'");
        }
    }

    private void checkEOF(final int b) throws EOFException {
        if (b == EOF) throw new EOFException();
    }
}
