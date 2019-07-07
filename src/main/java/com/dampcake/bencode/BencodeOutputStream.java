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

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * OutputStream for writing bencoded data.
 *
 * @author Adam Peck
 */
public class BencodeOutputStream extends FilterOutputStream {

    private final Charset charset;

    /**
     * Creates a new BencodeOutputStream that writes to the {@link OutputStream} passed and uses the {@link Charset} passed for encoding the data.
     *
     * @param out     the {@link OutputStream} to write to
     * @param charset the {@link Charset} to use
     *
     * @throws NullPointerException if the {@link Charset} passed is null
     */
    public BencodeOutputStream(final OutputStream out, final Charset charset) {
        super(out);

        if (charset == null) throw new NullPointerException("charset cannot be null");
        this.charset = charset;
    }

    /**
     * Creates a new BencodeOutputStream that writes to the {@link OutputStream} passed and uses UTF-8 {@link Charset} for encoding the data.
     *
     * @param out the {@link OutputStream} to write to
     */
    public BencodeOutputStream(final OutputStream out) {
        this(out, Bencode.DEFAULT_CHARSET);
    }

    /**
     * Gets the {@link Charset} the stream was created with.
     *
     * @return the {@link Charset} of the stream
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Writes the passed String to the stream.
     *
     * @param s the String to write to the stream
     *
     * @throws NullPointerException if the String is null
     * @throws IOException          if the underlying stream throws
     */
    public void writeString(final String s) throws IOException {
        write(encode(s).getBytes(charset));
    }

    /**
     * Writes the passed Number to the stream.
     * <p>
     * The number is converted to a Long, meaning any precision is lost as it not supported by the bencode spec.
     *
     * @param n the Number to write to the stream
     *
     * @throws NullPointerException if the Number is null
     * @throws IOException          if the underlying stream throws
     */
    public void writeNumber(final Number n) throws IOException {
        write(encode(n).getBytes(charset));
    }

    /**
     * Writes the passed List to the stream.
     * <p>
     * Data contained in the List is written as the correct type. Any {@link Iterable} is written as a List,
     * any {@link Number} as a Number, any {@link Map} as a Dictionary and any other {@link Object} is written as a String
     * calling the {@link Object#toString()} method.
     *
     * @param l the List to write to the stream
     *
     * @throws NullPointerException if the List is null
     * @throws IOException          if the underlying stream throws
     */
    public void writeList(final Iterable<?> l) throws IOException {
        write(encode(l).getBytes(charset));
    }

    /**
     * Writes the passed Dictionary to the stream.
     * <p>
     * Data contained in the Dictionary is written as the correct type. Any {@link Iterable} is written as a List,
     * any {@link Number} as a Number, any {@link Map} as a Dictionary and any other {@link Object} is written as a String
     * calling the {@link Object#toString()} method.
     *
     * @param m the Map to write to the stream
     *
     * @throws NullPointerException if the Map is null
     * @throws IOException          if the underlying stream throws
     */
    public void writeDictionary(final Map<?, ?> m) throws IOException {
        write(encode(m).getBytes(charset));
    }

    private static String encode(final String s) {
        if (s == null) throw new NullPointerException("s cannot be null");

        StringBuilder buffer = new StringBuilder();
        buffer.append(s.length());
        buffer.append(Bencode.SEPARATOR);
        buffer.append(s);

        return buffer.toString();
    }

    private static String encode(final Number n) {
        if (n == null) throw new NullPointerException("n cannot be null");

        StringBuilder buffer = new StringBuilder();
        buffer.append(Bencode.NUMBER);
        buffer.append(n.longValue());
        buffer.append(Bencode.TERMINATOR);

        return buffer.toString();
    }

    private static String encode(final Iterable<?> l) {
        if (l == null) throw new NullPointerException("l cannot be null");

        StringBuilder buffer = new StringBuilder();
        buffer.append(Bencode.LIST);
        for (Object o : l)
            buffer.append(encodeObject(o));
        buffer.append(Bencode.TERMINATOR);

        return buffer.toString();
    }

    private static String encode(final Map<?, ?> map) {
        if (map == null) throw new NullPointerException("m cannot be null");

        StringBuilder buffer = new StringBuilder();
        buffer.append(Bencode.DICTIONARY);
        for (Map.Entry<?, ?> e : map.entrySet()) {
            buffer.append(encode(e.getKey().toString()));
            buffer.append(encodeObject(e.getValue()));
        }
        buffer.append(Bencode.TERMINATOR);

        return buffer.toString();
    }

    private static String encodeObject(final Object o) {
        if (o == null) throw new NullPointerException("Cannot write null objects");

        if (o instanceof Number)
            return encode((Number) o);
        if (o instanceof Iterable<?>)
            return encode((Iterable<?>) o);
        if (o instanceof Map<?, ?>)
            return encode((Map<?, ?>) o);

        return encode(o.toString());
    }
}
