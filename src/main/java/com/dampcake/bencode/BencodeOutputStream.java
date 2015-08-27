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
    private final Bencode bencode;

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
        this.bencode = new Bencode(charset);
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
     * @throws NullPointerException if the String is null
     * @throws IOException if the underlying stream throws
     */
    public void writeString(final String s) throws IOException {
        write(bencode.encode(s));
    }

    /**
     * Writes the passed Number to the stream.
     *
     * The number is converted to a Long, meaning any precision is lost as it not supported by the bencode spec.
     *
     * @param n the Number to write to the stream
     *
     * @throws NullPointerException if the Number is null
     * @throws IOException if the underlying stream throws
     */
    public void writeNumber(final Number n) throws IOException {
        write(bencode.encode(n));
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
     * @throws NullPointerException if the List is null
     * @throws IOException if the underlying stream throws
     */
    public void writeList(final Iterable<?> l) throws IOException {
        write(bencode.encode(l));
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
     * @throws NullPointerException if the Map is null
     * @throws IOException if the underlying stream throws
     */
    public void writeDictionary(final Map<?, ?> m) throws IOException {
        write(bencode.encode(m));
    }
}
