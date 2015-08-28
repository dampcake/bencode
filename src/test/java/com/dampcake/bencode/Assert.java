package com.dampcake.bencode;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Custom assertions.
 *
 * @author Adam Peck
 */
class Assert {

    public static void assertThrows(Class<? extends Throwable> expected, Runnable block) {
        try {
            block.run();
            fail("No exception thrown");
        } catch (Throwable t) {
            assertEquals("Unexpected exception thrown: " + t.getClass() + "\n" + t.getMessage(), expected, t.getClass());
        }
    }
}
