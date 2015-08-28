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

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Bencode encoder/decoder.
 *
 * @author Adam Peck
 * @since 1.1
 */
public final class Bencode {

    /** Default Charset used by the Streams */
    static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    /** Number Marker */
    static final char NUMBER = 'i';

    /** List Marker */
    static final char LIST = 'l';

    /** Dictionary Marker */
    static final char DICTIONARY = 'd';

    /** End of type Marker */
    static final char TERMINATOR = 'e';

    /** Separator between length and string */
    static final char SEPARATOR = ':';

    private final Charset charset;

    /**
     * Create a new Bencoder using the default {@link Charset} (UTF-8)
     */
    public Bencode() {
        this.charset = DEFAULT_CHARSET;
    }

    /**
     * Creates a new Bencoder using the {@link Charset} passed for encoding/decoding.
     *
     * @param charset the {@link Charset} to use
     *
     * @throws NullPointerException if the {@link Charset} passed is null
     */
    public Bencode(final Charset charset) {
        if (charset == null) throw new NullPointerException("charset cannot be null");

        this.charset = charset;
    }

    /**
     * Gets the {@link Charset} the coder was created with.
     *
     * @return the {@link Charset} of the coder
     */
    public Charset getCharset() {
        return charset;
    }

    /**
     * Determines the first {@link Type} contained within the byte array.
     *
     * @param bytes the bytes to determine the {@link Type} for
     *
     * @return the {@link Type} or {@link Type#UNKNOWN} if it cannot be determined
     *
     * @throws NullPointerException if bytes is null
     * @throws BencodeException     if an error occurs during detection
     */
    public Type type(final byte[] bytes) {
        if (bytes == null) throw new NullPointerException("bytes cannot be null");

        BencodeInputStream in = new BencodeInputStream(new ByteArrayInputStream(bytes), charset);

        try {
            return in.nextType();
        } catch (Throwable t) {
            throw new BencodeException("Exception thrown during type detection", t);
        }
    }

    /**
     * Decodes a bencode encoded byte array.
     *
     * @param bytes the bytes to decode
     * @param type  the {@link Type} to decode as
     *
     * @return the decoded object
     *
     * @throws NullPointerException     if bytes or type is null
     * @throws IllegalArgumentException if type is {@link Type#UNKNOWN}
     * @throws BencodeException         if an error occurs during decoding
     */
    @SuppressWarnings("unchecked")
    public <T> T decode(final byte[] bytes, final Type<T> type) {
        if (bytes == null) throw new NullPointerException("bytes cannot be null");
        if (type == null) throw new NullPointerException("type cannot be null");
        if (type == Type.UNKNOWN) throw new IllegalArgumentException("type cannot be UNKNOWN");

        BencodeInputStream in = new BencodeInputStream(new ByteArrayInputStream(bytes), charset);

        try {
            if (type == Type.NUMBER)
                return (T) in.readNumber();
            if (type == Type.LIST)
                return (T) in.readList();
            if (type == Type.DICTIONARY)
                return (T) in.readDictionary();
            return (T) in.readString();
        } catch (Throwable t) {
            throw new BencodeException("Exception thrown during decoding", t);
        }
    }

    /**
     * Encodes the passed {@link String}.
     *
     * @param s the {@link String} to encode
     *
     * @throws NullPointerException if the {@link String} is null
     */
    public byte[] encode(final String s) {
        return sEncode(s).getBytes(charset);
    }

    /**
     * Encodes the passed {@link Number}.
     * <p>
     * The number is converted to a {@link Long}, meaning any precision is lost as it not supported by the bencode spec.
     *
     * @param n the {@link Number} to encode
     *
     * @throws NullPointerException if the {@link Number} is null
     */
    public byte[] encode(final Number n) {
        return sEncode(n).getBytes(charset);
    }

    /**
     * Encodes the passed {@link Iterable} as a bencode List.
     * <p>
     * Data contained in the List is written as the correct type. Any {@link Iterable} is written as a List,
     * any {@link Number} as a Number, any {@link Map} as a Dictionary and any other {@link Object} is written as a String
     * calling the {@link Object#toString()} method.
     *
     * @param l the List to encode
     *
     * @throws NullPointerException if the List is null
     */
    public byte[] encode(final Iterable<?> l) {
        return sEncode(l).getBytes(charset);
    }

    /**
     * Encodes the passed {@link Map} as a bencode Dictionary.
     * <p>
     * Data contained in the Dictionary is written as the correct type. Any {@link Iterable} is written as a List,
     * any {@link Number} as a Number, any {@link Map} as a Dictionary and any other {@link Object} is written as a String
     * calling the {@link Object#toString()} method.
     *
     * @param m the Map to encode
     *
     * @throws NullPointerException if the Map is null
     */
    public byte[] encode(final Map<?, ?> m) {
        return sEncode(m).getBytes(charset);
    }

    private static String sEncode(final String s) {
        if (s == null) throw new NullPointerException("s cannot be null");

        return String.format("%d%s%s", s.length(), SEPARATOR, s);
    }

    private static String sEncode(final Number n) {
        if (n == null) throw new NullPointerException("n cannot be null");

        return String.format("%s%d%s", NUMBER, n.longValue(), TERMINATOR);
    }

    private static String sEncode(final Iterable<?> l) {
        if (l == null) throw new NullPointerException("l cannot be null");

        StringBuilder buffer = new StringBuilder();
        buffer.append(LIST);
        for (Object o : l)
            buffer.append(sEncodeObject(o));
        buffer.append(TERMINATOR);

        return buffer.toString();
    }

    private static String sEncode(final Map<?, ?> m) {
        if (m == null) throw new NullPointerException("m cannot be null");

        Map<?, ?> map;
        if (!(m instanceof SortedMap<?, ?>))
            map = new TreeMap<Object, Object>(m);
        else
            map = m;

        StringBuilder buffer = new StringBuilder();
        buffer.append(DICTIONARY);
        for (Map.Entry<?, ?> e : map.entrySet()) {
            buffer.append(sEncode(e.getKey().toString()));
            buffer.append(sEncodeObject(e.getValue()));
        }
        buffer.append(TERMINATOR);

        return buffer.toString();
    }

    private static String sEncodeObject(final Object o) {
        if (o == null) throw new NullPointerException("Cannot write null objects");

        if (o instanceof Number)
            return sEncode((Number) o);
        if (o instanceof Iterable<?>)
            return sEncode((Iterable<?>) o);
        if (o instanceof Map<?, ?>)
            return sEncode((Map<?, ?>) o);

        return sEncode(o.toString());
    }
}
