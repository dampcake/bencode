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

/**
 * Bencode encoder/decoder.
 *
 * @author Adam Peck
 *
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

    /**
     * Creates a new Bencoder.
     */
    public Bencode() {

    }

    /**
     * Determines the first {@link Type} contained within the byte array.
     *
     * @param bytes the bytes to determine the {@link Type} for
     *
     * @return the {@link Type} or {@link Type#UNKNOWN} if it cannot be determined
     *
     * @throws NullPointerException if bytes or charset is null
     * @throws BencodeException if an error occurs during detection
     */
    public Type type(byte[] bytes) {
        return type(bytes, DEFAULT_CHARSET);
    }

    /**
     * Determines the first {@link Type} contained within the byte array.
     *
     * @param bytes the bytes to determine the {@link Type} for
     * @param charset the {@link Charset} to use for decoding
     *
     * @return the {@link Type} or {@link Type#UNKNOWN} if it cannot be determined
     *
     * @throws NullPointerException if bytes or charset is null
     * @throws BencodeException if an error occurs during detection
     */
    public Type type(byte[] bytes, Charset charset) {
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
     * @param type the {@link Type} to decode as
     *
     * @return the decoded object
     *
     * @throws NullPointerException if bytes or type is null
     * @throws IllegalArgumentException if type is {@link Type#UNKNOWN}
     * @throws BencodeException if an error occurs during decoding
     */
    public <T> T decode(byte[] bytes, Type<T> type) {
        return decode(bytes, type, DEFAULT_CHARSET);
    }

    /**
     * Decodes a bencode encoded byte array.
     *
     * @param bytes the bytes to decode
     * @param type the {@link Type} to decode as
     * @param charset the {@link Charset} to use for decoding
     *
     * @return the decoded object
     *
     * @throws NullPointerException if bytes, type or charset is null
     * @throws IllegalArgumentException if type is {@link Type#UNKNOWN}
     * @throws BencodeException if an error occurs during decoding
     */
    @SuppressWarnings("unchecked")
    public <T> T decode(byte[] bytes, Type<T> type, Charset charset) {
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
}
