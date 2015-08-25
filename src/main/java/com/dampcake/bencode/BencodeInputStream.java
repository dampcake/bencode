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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * InputStream for reading bencoded data.
 *
 * @author Adam Peck
 */
public class BencodeInputStream extends FilterInputStream {

    // EOF Constant
    private static final int EOF = -1;

    private final Charset charset;
    private final PushbackInputStream in;

    /**
     * Creates a new BencodeInputStream that reads from the InputStream passed and uses the Charset passed for decoding the data.
     *
     * @param in the InputStream to read from
     * @param charset the Charset to use
     *
     * @throws NullPointerException if the Charset passed is null
     */
    public BencodeInputStream(final InputStream in, final Charset charset) {
        super(new PushbackInputStream(in));
        this.in = (PushbackInputStream) super.in;

        if (charset == null) throw new NullPointerException("charset cannot be null");
        this.charset = charset;
    }

    /**
     * Creates a new BencodeInputStream that reads from the InputStream passed and uses the UTF-8 Charset for decoding the data.
     *
     * @param in the InputStream to read from
     */
    public BencodeInputStream(final InputStream in) {
        this(in, Bencode.DEFAULT_CHARSET);
    }

    /**
     * Gets the Charset the stream was created with.
     *
     * @return the Charset of the stream
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
     * Peeks at the next Type.
     *
     * @see Type
     *
     * @return the next Type available
     *
     * @throws IOException if the underlying stream throws
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
     * Reads a String from the stream.
     *
     * @return the String read from the stream
     *
     * @throws IOException if the underlying stream throws
     * @throws EOFException if the end of the stream has been reached
     * @throws InvalidObjectException if the next type in the stream is not a String
     */
    public String readString() throws IOException {
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
        return new String(bytes, getCharset());
    }

    /**
     * Reads a Number from the stream.
     *
     * @return the Number read from the stream
     *
     * @throws IOException if the underlying stream throws
     * @throws EOFException if the end of the stream has been reached
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

        return Long.parseLong(buffer.toString());
    }

    /**
     * Reads a List from the stream.
     *
     * @return the List read from the stream
     *
     * @throws IOException if the underlying stream throws
     * @throws EOFException if the end of the stream has been reached
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
     * @throws IOException if the underlying stream throws
     * @throws EOFException if the end of the stream has been reached
     * @throws InvalidObjectException if the next type in the stream is not a Dictionary, or the list contains invalid types
     */
    public Map<String, Object> readDictionary() throws IOException {
        int token = in.read();
        validateToken(token, Type.DICTIONARY);

        Map<String, Object> map = new TreeMap<String, Object>();
        while ((token = in.read()) != Bencode.TERMINATOR) {
            checkEOF(token);

            in.unread(token);
            map.put(readString(), readObject(in.read()));
        }

        return map;
    }

    private Object readObject(int token) throws IOException {
        in.unread(token);

        switch (typeForToken(token)) {
            case STRING:
                return readString();
            case NUMBER:
                return readNumber();
            case LIST:
                return readList();
            case DICTIONARY:
                return readDictionary();
        }

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
