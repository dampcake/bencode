package com.dampcake.bencode;

/**
 * Wraps a {@link Throwable} that is thrown during decode/encode.
 *
 * @author Adam Peck
 * @since 1.1
 */
public class BencodeException extends RuntimeException {

    BencodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
