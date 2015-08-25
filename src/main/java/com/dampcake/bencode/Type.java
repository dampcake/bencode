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

/**
 * Data Types in bencode.
 *
 * @author Adam Peck
 */
public enum Type {
    /** String Type */
    STRING(new StringValidator()),
    /** Number Type */
    NUMBER(new TypeValidator(Bencode.NUMBER)),
    /** List Type */
    LIST(new TypeValidator(Bencode.LIST)),
    /** Dictionary Type */
    DICTIONARY(new TypeValidator(Bencode.DICTIONARY)),
    /** Unknown/Invalid Type */
    UNKNOWN(new Validator() {
        public boolean validate(int token) {
            return false;
        }
    });

    private final Validator validator;

    Type(final Validator validator) {
        this.validator = validator;
    }

    boolean validate(int token) {
        return validator.validate(token);
    }
}
