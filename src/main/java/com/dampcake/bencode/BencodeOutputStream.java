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
import java.util.SortedMap;
import java.util.TreeMap;

import static com.dampcake.bencode.Bencode.DICTIONARY;
import static com.dampcake.bencode.Bencode.LIST;
import static com.dampcake.bencode.Bencode.NUMBER;
import static com.dampcake.bencode.Bencode.SEPARATOR;
import static com.dampcake.bencode.Bencode.TERMINATOR;

/**
 * OutputStream for writing bencoded data.
 *
 * @author Adam Peck
 */
public class BencodeOutputStream extends FilterOutputStream {

    private final Charset charset;

    /**
     * Creates a new BencodeOutputStream that writes to the OutputStream passed and uses the Charset passed for encoding the data.
     *
     * @param out the OutputStream to write to
     * @param charset the Charset to use
     *
     * @throws NullPointerException if the Charset passed is null
     */
    public BencodeOutputStream(final OutputStream out, final Charset charset) {
        super(out);

        if (charset == null) throw new NullPointerException("charset cannot be null");
        this.charset = charset;
    }

    /**
     * Creates a new BencodeOutputStream that writes to the OutputStream passed and uses UTF-8 Charset for encoding the data.
     *
     * @param out the InputStream to read from
     */
    public BencodeOutputStream(final OutputStream out) {
        this(out, Bencode.DEFAULT_CHARSET);
    }

    /**
     * Gets the Charset the stream was created with.
     *
     * @return the Charset of the stream
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Writes the passed String to the stream.
     *
     * @param s the String to write to the stream
     *
     * @throws IOException if the underlying stream throws
     */
    public void writeString(String s) throws IOException {
        write(encode(s).getBytes(getCharset()));
    }

    /**
     * Writes the passed Number to the stream.
     *
     * The number is converted to a Long, meaning any precision is lost as it not supported by the bencode spec.
     *
     * @param n the Number to write to the stream
     *
     * @throws IOException if the underlying stream throws
     */
    public void writeNumber(Number n) throws IOException {
        write(encode(n).getBytes(getCharset()));
    }

    /**
     * Writes the passed List to the stream.
     *
     * Data contained in the List is written as the correct type. Any {@link Iterable} is written as a List,
     * any {@link Number} as a Number, any {@link Map} as a Dictionary and any other {@link Object} is written as a String
     * calling the {@link Object#toString()} method.
     *
     * @param l the List to write to the stream
     *
     * @throws IOException if the underlying stream throws
     */
    public void writeList(Iterable<?> l) throws IOException {
        write(encode(l).getBytes(getCharset()));
    }

    /**
     * Writes the passed Dictionary to the stream.
     *
     * Data contained in the Dictionary is written as the correct type. Any {@link Iterable} is written as a List,
     * any {@link Number} as a Number, any {@link Map} as a Dictionary and any other {@link Object} is written as a String
     * calling the {@link Object#toString()} method.
     *
     * @param m the Map to write to the stream
     *
     * @throws IOException if the underlying stream throws
     */
    public void writeDictionary(Map<?, ?> m) throws IOException {
        write(encode(m).getBytes(getCharset()));
    }

    private static String encode(final String s) {
        if (s == null) throw new NullPointerException("s cannot be null");

        return String.format("%d%s%s", s.length(), SEPARATOR, s);
    }

    private static String encode(final Number n) {
        if (n == null) throw new NullPointerException("n cannot be null");

        return String.format("%s%d%s", NUMBER, n.longValue(), TERMINATOR);
    }

    private static String encode(final Iterable<?> l) {
        if (l == null) throw new NullPointerException("l cannot be null");

        StringBuilder buffer = new StringBuilder();
        buffer.append(LIST);
        for (Object o : l)
            buffer.append(encodeObject(o));
        buffer.append(TERMINATOR);

        return buffer.toString();
    }

    private static String encode(final Map<?, ?> m) {
        if (m == null) throw new NullPointerException("m cannot be null");

        Map<?, ?> map;
        if (!(m instanceof SortedMap<?, ?>))
            map = new TreeMap<Object, Object>(m);
        else
            map = m;

        StringBuilder buffer = new StringBuilder();
        buffer.append(DICTIONARY);
        for (Map.Entry<?, ?> e : map.entrySet()) {
            buffer.append(encode(e.getKey().toString()));
            buffer.append(encodeObject(e.getValue()));
        }
        buffer.append(TERMINATOR);

        return buffer.toString();
    }

    private static String encodeObject(Object o) {
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
