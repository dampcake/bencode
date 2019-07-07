package com.dampcake.bencode;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.EOFException;
import java.io.InvalidObjectException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.hamcrest.core.IsInstanceOf.any;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for Bencode.
 */
@SuppressWarnings("unchecked")
public class BencodeTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Bencode instance;

    @Before
    public void setUp() {
        instance = new Bencode();
    }

    @Test
    public void testConstructorNullCharset() {
        exception.expect(NullPointerException.class);
        exception.expectMessage("charset cannot be null");

        new Bencode(null);
    }

    @Test
    public void testConstructorWithCharset() {
        Bencode bencode = new Bencode(Charset.forName("US-ASCII"));

        assertEquals(bencode.getCharset(), Charset.forName("US-ASCII"));
    }

    @Test
    public void testTypeString() {
        assertSame(Type.STRING, instance.type("7".getBytes()));
    }

    @Test
    public void testTypeNumber() {
        assertSame(Type.NUMBER, instance.type("i1".getBytes()));
    }

    @Test
    public void testTypeList() {
        assertSame(Type.LIST, instance.type("l123".getBytes()));
    }

    @Test
    public void testTypeDictionary() {
        assertSame(Type.DICTIONARY, instance.type("dtesting".getBytes()));
    }

    @Test
    public void testTypeUnknown() {
        assertSame(Type.UNKNOWN, instance.type("unknown".getBytes()));
    }

    @Test
    public void testTypeEmpty() {
        exception.expect(BencodeException.class);
        exception.expectCause(any(EOFException.class));

        instance.type(new byte[0]);
    }

    @Test
    public void testTypeNullBytes() {
        exception.expect(NullPointerException.class);
        exception.expectMessage("bytes cannot be null");

        instance.type(null);
    }

    @Test
    public void testDecodeNullBytes() {
        exception.expect(NullPointerException.class);
        exception.expectMessage("bytes cannot be null");

        instance.decode(null, Type.STRING);
    }

    @Test
    public void testDecodeNullType() {
        exception.expect(NullPointerException.class);
        exception.expectMessage("type cannot be null");

        instance.decode("12:Hello World!".getBytes(), null);
    }

    @Test
    public void testDecodeString() {
        String decoded = instance.decode("12:Hello World!".getBytes(), Type.STRING);

        assertEquals("Hello World!", decoded);
    }

    @Test
    public void testDecodeEmptyString() {
        String decoded = instance.decode("0:123".getBytes(), Type.STRING);

        assertEquals("", decoded);
    }

    @Test
    public void testDecodeStringNaN() throws Exception {
        exception.expect(BencodeException.class);
        exception.expectCause(any(InvalidObjectException.class));

        instance.decode("1c3:Testing".getBytes(), Type.STRING);
    }

    @Test
    public void testDecodeStringEOF() throws Exception {
        exception.expect(BencodeException.class);
        exception.expectCause(any(EOFException.class));

        instance.decode("123456".getBytes(), Type.STRING);
    }

    @Test
    public void testDecodeStringEmpty() throws Exception {
        exception.expect(BencodeException.class);
        exception.expectCause(any(EOFException.class));

        instance.decode("".getBytes(), Type.STRING);
    }

    @Test
    public void testDecodeNumber() throws Exception {
        long decoded = instance.decode("i123456e123".getBytes(), Type.NUMBER);

        assertEquals(123456, decoded);
    }

    @Test
    public void testDecodeNumberNaN() throws Exception {
        exception.expect(BencodeException.class);
        exception.expectCause(any(NumberFormatException.class));

        instance.decode("i123cbve1".getBytes(), Type.NUMBER);
    }

    @Test
    public void testDecodeNumberEOF() throws Exception {
        exception.expect(BencodeException.class);
        exception.expectCause(any(EOFException.class));

        instance.decode("i123".getBytes(), Type.NUMBER);
    }

    @Test
    public void testDecodeNumberEmpty() throws Exception {
        exception.expect(BencodeException.class);
        exception.expectCause(any(EOFException.class));

        instance.decode("".getBytes(), Type.NUMBER);
    }

    @Test
    public void testDecodeList() throws Exception {
        List<Object> decoded = instance.decode("l5:Hello6:World!li123ei456eeetesting".getBytes(), Type.LIST);

        assertEquals(3, decoded.size());

        assertEquals("Hello", decoded.get(0));
        assertEquals("World!", decoded.get(1));

        List<Object> list = (List<Object>) decoded.get(2);
        assertEquals(123L, list.get(0));
        assertEquals(456L, list.get(1));
    }

    @Test
    public void testDecodeListEmpty() throws Exception {
        List<Object> decoded = instance.decode("le123".getBytes(), Type.LIST);

        assertTrue(decoded.isEmpty());
    }

    @Test
    public void testDecodeListInvalidItem() throws Exception {
        exception.expect(BencodeException.class);
        exception.expectCause(any(InvalidObjectException.class));

        instance.decode("l2:Worlde".getBytes(), Type.LIST);
    }

    @Test
    public void testDecodeListEOF() throws Exception {
        exception.expect(BencodeException.class);
        exception.expectCause(any(EOFException.class));

        instance.decode("l5:Hello".getBytes(), Type.LIST);
    }

    @Test
    public void testDecodeDictionary() throws Exception {
        Map<String, Object> decoded = instance.decode("d4:dictd3:1234:test3:4565:thinge4:listl11:list-item-111:list-item-2e6:numberi123456e6:string5:valuee".getBytes(), Type.DICTIONARY);

        assertEquals(4, decoded.size());

        assertEquals("value", decoded.get("string"));
        assertEquals(123456L, decoded.get("number"));

        List<Object> list = (List<Object>) decoded.get("list");
        assertEquals(2, list.size());
        assertEquals("list-item-1", list.get(0));
        assertEquals("list-item-2", list.get(1));

        Map<String, Object> map = (Map<String, Object>) decoded.get("dict");
        assertEquals(2, map.size());
        assertEquals("test", map.get("123"));
        assertEquals("thing", map.get("456"));
    }

    @Test
    public void testDecodeDictionaryEmpty() throws Exception {
        Map<String, Object> decoded = instance.decode("de123test".getBytes(), Type.DICTIONARY);

        assertTrue(decoded.isEmpty());
    }

    @Test
    public void testDecodeDictionaryInvalidItem() throws Exception {
        exception.expect(BencodeException.class);
        exception.expectCause(any(InvalidObjectException.class));

        instance.decode("d4:item5:value3:testing".getBytes(), Type.DICTIONARY);
    }

    @Test
    public void testDecodeDictionaryEOF() throws Exception {
        exception.expect(BencodeException.class);
        exception.expectCause(any(EOFException.class));

        instance.decode("d4:item5:test".getBytes(), Type.DICTIONARY);
    }

    @Test
    public void testWriteString() throws Exception {
        byte[] encoded = instance.encode("Hello World!");

        assertEquals("12:Hello World!", new String(encoded, instance.getCharset()));
    }

    @Test
    public void testWriteStringEmpty() throws Exception {
        byte[] encoded = instance.encode("");

        assertEquals("0:", new String(encoded, instance.getCharset()));
    }

    @Test
    public void testWriteStringNull() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("s cannot be null");

        instance.encode((String) null);
    }

    @Test
    public void testWriteNumber() throws Exception {
        byte[] encoded = instance.encode(123456);

        assertEquals("i123456e", new String(encoded, instance.getCharset()));
    }

    @Test
    public void testWriteNumberDecimal() throws Exception {
        byte[] encoded = instance.encode(123.456);

        assertEquals("i123e", new String(encoded, instance.getCharset()));
    }

    @Test
    public void testWriteNumberNull() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("n cannot be null");

        instance.encode((Number) null);
    }

    @Test
    public void testWriteList() throws Exception {
        byte[] encoded = instance.encode(new ArrayList<Object>() {{
            add("Hello");
            add("World!");
            add(new ArrayList<Object>() {{
                add(123);
                add(456);
            }});
        }});

        assertEquals("l5:Hello6:World!li123ei456eee", new String(encoded, instance.getCharset()));
    }

    @Test
    public void testWriteListEmpty() throws Exception {
        byte[] encoded = instance.encode(new ArrayList<Object>());

        assertEquals("le", new String(encoded, instance.getCharset()));
    }

    @Test
    public void testWriteListNullItem() throws Exception {
        exception.expect(BencodeException.class);
        exception.expectCause(any(NullPointerException.class));

        instance.encode(new ArrayList<Object>() {{
            add("Hello");
            add("World!");
            add(new ArrayList<Object>() {{
                add(null);
                add(456);
            }});
        }});
    }

    @Test
    public void testWriteListNull() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("l cannot be null");

        instance.encode((List) null);
    }

    @Test
    public void testWriteDictionary() throws Exception {
        byte[] encoded = instance.encode(new TreeMap<Object, Object>() {{
            put("string", "value");
            put("number", 123456);
            put("list", new ArrayList<Object>() {{
                add("list-item-1");
                add("list-item-2");
            }});
            put("dict", new ConcurrentSkipListMap() {{
                put(123, "test");
                put(456, "thing");
            }});
        }});

        assertEquals("d4:dictd3:1234:test3:4565:thinge4:listl11:list-item-111:list-item-2e6:numberi123456e6:string5:valuee",
                new String(encoded, instance.getCharset()));
    }

    @Test
    public void testWriteDictionaryEmpty() throws Exception {
        byte[] encoded = instance.encode(new HashMap<Object, Object>());

        assertEquals("de", new String(encoded, instance.getCharset()));
    }

    @Test
    public void testWriteDictionaryKeyCastException() throws Exception {
        exception.expect(any(ClassCastException.class));

        instance.encode(new TreeMap<Object, Object>() {{
            put("string", "value");
            put(123, "number-key");
        }});
    }

    @Test
    public void testWriteDictionaryNull() throws Exception {
        exception.expect(NullPointerException.class);
        exception.expectMessage("m cannot be null");

        instance.encode((Map) null);
    }
}
