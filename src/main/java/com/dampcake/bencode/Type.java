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

import java.util.List;
import java.util.Map;

/**
 * Data Types in bencode.
 *
 * @author Adam Peck
 */
public class Type<T> {

    public static final Type<String> STRING = new Type<String>(new StringValidator());
    public static final Type<Long> NUMBER = new Type<Long>(new TypeValidator(Bencode.NUMBER));
    public static final Type<List<Object>> LIST = new Type<List<Object>>(new TypeValidator(Bencode.LIST));
    public static final Type<Map<String, Object>> DICTIONARY = new Type<Map<String, Object>>(new TypeValidator(Bencode.DICTIONARY));
    public static final Type<Void> UNKNOWN = new Type<Void>(new Validator() {
        public boolean validate(int token) {
            return false;
        }
    });

    private final Validator validator;

    private Type(final Validator validator) {
        this.validator = validator;
    }

    boolean validate(final int token) {
        return validator.validate(token);
    }

    public static Type[] values() {
        return new Type[] { STRING, NUMBER, LIST, DICTIONARY, UNKNOWN };
    }
}
