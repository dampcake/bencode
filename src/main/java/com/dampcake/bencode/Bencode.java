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

import java.nio.charset.Charset;

/**
 * Constants.
 *
 * @author Adam Peck
 */
final class Bencode {

    /** Default Charset used by the Streams */
    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    /** Number Marker */
    public static final char NUMBER = 'i';

    /** List Marker */
    public static final char LIST = 'l';

    /** Dictionary Marker */
    public static final char DICTIONARY = 'd';

    /** End of type Marker */
    public static final char TERMINATOR = 'e';

    /** Separator between length and string */
    public static final char SEPARATOR = ':';
}
