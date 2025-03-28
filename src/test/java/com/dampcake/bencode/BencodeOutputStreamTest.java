package com.dampcake.bencode;

import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

/**
 * Unit tests for BencodeOutputStream.
 *
 * @author Adam Peck
 */
public class BencodeOutputStreamTest {

    private ByteArrayOutputStream out;
    private BencodeOutputStream instance;

    @Before
    public void setUp() {
        out = new ByteArrayOutputStream();
        instance = new BencodeOutputStream(out);
    }

    @Test
    @SuppressWarnings("resource")
    public void testConstructorNullStream() throws Exception {
        new BencodeOutputStream(null);
    }

    @Test(expected = NullPointerException.class)
    @SuppressWarnings("resource")
    public void testConstructorNullCharset() throws Exception {
        new BencodeOutputStream(out, null);
    }

    @Test
    public void testWriteString() throws Exception {
        instance.writeString("Hello World!");

        assertEquals("12:Hello World!", new String(out.toByteArray(), instance.getCharset()));
    }

    @Test
    public void testWriteStringEmpty() throws Exception {
        instance.writeString("");

        assertEquals("0:", new String(out.toByteArray(), instance.getCharset()));
    }

    @Test
    public void testWriteStringNull() throws Exception {
        assertThrows(NullPointerException.class, () -> instance.writeString((String) null));
        assertEquals(0, out.toByteArray().length);
    }

    @Test
    public void testWriteStringByteBuffer() throws Exception {
        instance.writeString(ByteBuffer.wrap("Hello World!".getBytes()));

        assertEquals("12:Hello World!", new String(out.toByteArray(), instance.getCharset()));
    }

    @Test
    public void testWriteStringEmptyByteBuffer() throws Exception {
        instance.writeString(ByteBuffer.wrap(new byte[0]));

        assertEquals("0:", new String(out.toByteArray(), instance.getCharset()));
    }

    @Test
    public void testWriteStringByteArray() throws Exception {
        instance.writeString("Hello World!".getBytes());

        assertEquals("12:Hello World!", new String(out.toByteArray(), instance.getCharset()));
    }

    @Test
    public void testWriteStringEmptyByteArray() throws Exception {
        instance.writeString(new byte[0]);

        assertEquals("0:", new String(out.toByteArray(), instance.getCharset()));
    }

    @Test
    public void testWriteStringNullByteArray() throws Exception {
        assertThrows(NullPointerException.class, () -> instance.writeString((ByteBuffer) null));
        assertEquals(0, out.toByteArray().length);
    }

    @Test
    public void testWriteNumber() throws Exception {
        instance.writeNumber(123456);

        assertEquals("i123456e", new String(out.toByteArray(), instance.getCharset()));
    }

    @Test
    public void testWriteNumberDecimal() throws Exception {
        instance.writeNumber(123.456);

        assertEquals("i123e", new String(out.toByteArray(), instance.getCharset()));
    }

    @Test
    public void testWriteNumberNull() throws Exception {
        assertThrows(NullPointerException.class, () -> instance.writeNumber(null));
        assertEquals(0, out.toByteArray().length);
    }

    @Test
    public void testWriteList() throws Exception {
        instance.writeList(new ArrayList<Object>() {{
            add("Hello");
            add(ByteBuffer.wrap("World!".getBytes()));
            add(new ArrayList<Object>() {{
                add(123);
                add(456);
            }});
            add("Foo".getBytes());
        }});

        assertEquals("l5:Hello6:World!li123ei456ee3:Fooe", new String(out.toByteArray(), instance.getCharset()));
    }

    @Test
    public void testWriteListEmpty() throws Exception {
        instance.writeList(new ArrayList<Object>());

        assertEquals("le", new String(out.toByteArray(), instance.getCharset()));
    }

    @Test
    public void testWriteListNullItem() throws Exception {
        ThrowingRunnable runnable = () -> instance.writeList(new ArrayList<Object>() {{
            add("Hello");
            add(ByteBuffer.wrap("World!".getBytes()));
            add(new ArrayList<Object>() {{
                add(null);
                add(456);
            }});
        }});
        assertThrows(NullPointerException.class, runnable);
        assertEquals(0, out.toByteArray().length);
    }

    @Test
    public void testWriteListNull() throws Exception {
        assertThrows(NullPointerException.class, () -> instance.writeList(null));
        assertEquals(0, out.toByteArray().length);
    }

    @Test
    public void testWriteDictionary() throws Exception {
        instance.writeDictionary(new LinkedHashMap<Object, Object>() {{
            put("string", "value");
            put("number", 123456);
            put("list", new ArrayList<Object>() {{
                add("list-item-1");
                add("list-item-2");
            }});
            put("dict", new ConcurrentSkipListMap<Integer, Object>() {{
                put(123, ByteBuffer.wrap("test".getBytes()));
                put(456, "thing");
            }});
        }});

        assertEquals("d4:dictd3:1234:test3:4565:thinge4:listl11:list-item-111:list-item-2e6:numberi123456e6:string5:valuee",
                new String(out.toByteArray(), instance.getCharset()));
    }

    @Test
    public void testWriteDictionaryEmpty() throws Exception {
        instance.writeDictionary(new HashMap<Object, Object>());

        assertEquals("de", new String(out.toByteArray(), instance.getCharset()));
    }

    @Test
    public void testWriteDictionaryKeyCastException() throws Exception {
        ThrowingRunnable runnable = () -> instance.writeDictionary(new TreeMap<Object, Object>() {{
            put("string", "value");
            put(123, "number-key");
        }});
        assertThrows(ClassCastException.class, runnable);
        assertEquals(0, out.toByteArray().length);
    }

    @Test
    public void testWriteDictionaryNull() throws Exception {
        assertThrows(NullPointerException.class, () -> instance.writeDictionary(null));
        assertEquals(0, out.toByteArray().length);
    }
}
