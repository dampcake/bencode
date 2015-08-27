package com.dampcake.bencode;

/**
 * Wraps a {@link Throwable} that is thrown during decode/encode.
 *
 * @author Adam Peck
 */
public class BencodeException extends RuntimeException {

    BencodeException(String message, Throwable cause) {
        super(message, cause);
    }
}
