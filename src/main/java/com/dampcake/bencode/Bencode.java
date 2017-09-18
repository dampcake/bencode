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
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.Map;

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
     * @throws BencodeException     if an error occurs during encoding
     */
    public byte[] encode(final String s) {
        if (s == null) throw new NullPointerException("s cannot be null");

        return encode(s, Type.STRING);
    }

    /**
     * Encodes the passed {@link Number}.
     * <p>
     * The number is converted to a {@link Long}, meaning any precision is lost as it not supported by the bencode spec.
     *
     * @param n the {@link Number} to encode
     *
     * @throws NullPointerException if the {@link Number} is null
     * @throws BencodeException     if an error occurs during encoding
     */
    public byte[] encode(final Number n) {
        if (n == null) throw new NullPointerException("n cannot be null");

        return encode(n, Type.NUMBER);
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
     * @throws BencodeException     if an error occurs during encoding
     */
    public byte[] encode(final Iterable<?> l) {
        if (l == null) throw new NullPointerException("l cannot be null");

        return encode(l, Type.LIST);
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
     * @throws BencodeException     if an error occurs during encoding
     */
    public byte[] encode(final Map<?, ?> m) {
        if (m == null) throw new NullPointerException("m cannot be null");

        return encode(m, Type.DICTIONARY);
    }

    private byte[] encode(final Object o, final Type type) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BencodeOutputStream bencode = new BencodeOutputStream(out, charset);

        try {
            if (type == Type.STRING)
                bencode.writeString((String) o);
            else if (type == Type.NUMBER)
                bencode.writeNumber((Number) o);
            else if (type == Type.LIST)
                bencode.writeList((Iterable) o);
            else if (type == Type.DICTIONARY)
                bencode.writeDictionary((Map) o);
        } catch (Throwable t) {
            throw new BencodeException("Exception thrown during encoding", t);
        }

        return out.toByteArray();
    }
}
