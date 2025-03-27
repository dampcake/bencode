package com.dampcake.bencode;

import org.junit.Before;
import org.junit.Test;
import org.junit.function.ThrowingRunnable;

import java.io.EOFException;
import java.io.InvalidObjectException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for Bencode.
 */
@SuppressWarnings("unchecked")
public class BencodeTest {
    private Bencode instance;

    @Before
    public void setUp() {
        instance = new Bencode();
    }

    @Test
    public void testConstructorNullCharset() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> new Bencode(null));
        assertEquals("charset cannot be null", exception.getMessage());
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
        BencodeException exception = assertThrows(BencodeException.class, () -> instance.type(new byte[0]));
        assertThat(exception.getCause(), instanceOf(EOFException.class));
    }

    @Test
    public void testTypeNullBytes() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> instance.type(null));
        assertEquals("bytes cannot be null", exception.getMessage());
    }

    @Test
    public void testDecodeNullBytes() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> instance.decode(null, Type.STRING));
        assertEquals("bytes cannot be null", exception.getMessage());
    }

    @Test
    public void testDecodeNullType() {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> instance.decode("12:Hello World!".getBytes(), null));
        assertEquals("type cannot be null", exception.getMessage());
    }

    @Test
    public void testDecodeUnknownType() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> instance.decode("12:Hello World!".getBytes(), Type.UNKNOWN));
        assertEquals("type cannot be UNKNOWN", exception.getMessage());
    }

    @Test
    public void testDecodeString() {
        String decoded = instance.decode("12:Hello World!".getBytes(), Type.STRING);

        assertEquals("Hello World!", decoded);
    }

    @Test
    public void testDecodeStringMultiByteCodePoints() {
        String decoded = instance.decode("7:Garçon".getBytes(), Type.STRING);

        assertEquals("Garçon", decoded);
    }

    @Test
    public void testDecodeEmptyString() {
        String decoded = instance.decode("0:123".getBytes(), Type.STRING);

        assertEquals("", decoded);
    }

    @Test
    public void testDecodeStringNaN() throws Exception {
        BencodeException exception = assertThrows(BencodeException.class, () -> instance.decode("1c3:Testing".getBytes(), Type.STRING));
        assertThat(exception.getCause(), instanceOf(InvalidObjectException.class));
    }

    @Test
    public void testDecodeStringEOF() throws Exception {
        BencodeException exception = assertThrows(BencodeException.class, () -> instance.decode("123456".getBytes(), Type.STRING));
        assertThat(exception.getCause(), instanceOf(EOFException.class));
    }

    @Test
    public void testDecodeStringEmpty() throws Exception {
        BencodeException exception = assertThrows(BencodeException.class, () -> instance.decode("".getBytes(), Type.STRING));
        assertThat(exception.getCause(), instanceOf(EOFException.class));
    }

    @Test
    public void testDecodeNumber() throws Exception {
        long decoded = instance.decode("i123456e123".getBytes(), Type.NUMBER);

        assertEquals(123456, decoded);
    }

    @Test
    public void testDecodeNumberNaN() throws Exception {
        BencodeException exception = assertThrows(BencodeException.class, () -> instance.decode("i123cbve1".getBytes(), Type.NUMBER));
        assertThat(exception.getCause(), instanceOf(NumberFormatException.class));
    }

    @Test
    public void testDecodeNumberEOF() throws Exception {
        BencodeException exception = assertThrows(BencodeException.class, () -> instance.decode("i123".getBytes(), Type.NUMBER));
        assertThat(exception.getCause(), instanceOf(EOFException.class));
    }

    @Test
    public void testDecodeNumberEmpty() throws Exception {
        BencodeException exception = assertThrows(BencodeException.class, () -> instance.decode("".getBytes(), Type.NUMBER));
        assertThat(exception.getCause(), instanceOf(EOFException.class));
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
    public void testDecodeListByteArray() throws Exception {
        instance = new Bencode(true);
        List<Object> decoded = instance.decode("l5:Hello6:World!li123ei456eeetesting".getBytes(), Type.LIST);

        assertEquals(3, decoded.size());

        assertThat(decoded.get(0), instanceOf(ByteBuffer.class));
        assertEquals("Hello", new String(((ByteBuffer) decoded.get(0)).array()));
        assertThat(decoded.get(1), instanceOf(ByteBuffer.class));
        assertEquals("World!", new String(((ByteBuffer) decoded.get(1)).array()));

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
        BencodeException exception = assertThrows(BencodeException.class, () -> instance.decode("l2:Worlde".getBytes(), Type.LIST));
        assertThat(exception.getCause(), instanceOf(InvalidObjectException.class));
    }

    @Test
    public void testDecodeListEOF() throws Exception {
        BencodeException exception = assertThrows(BencodeException.class, () -> instance.decode("l5:Hello".getBytes(), Type.LIST));
        assertThat(exception.getCause(), instanceOf(EOFException.class));
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
    public void testDecodeDebianTracker() throws Exception {
        Map<String, Object> decoded = instance.decode("d8:intervali900e5:peersld2:ip12:146.71.73.514:porti63853eeee".getBytes(), Type.DICTIONARY);

        assertEquals(2, decoded.size());

        assertEquals(900L, decoded.get("interval"));

        List<Object> list = (List<Object>) decoded.get("peers");
        assertEquals(1, list.size());

        Map<String, Object> map = (Map<String, Object>) list.get(0);
        assertEquals("146.71.73.51", map.get("ip"));
        assertEquals(63853L, map.get("port"));
    }

    @Test
    public void testDecodeDictionaryByteArray() throws Exception {
        instance = new Bencode(true);
        Map<String, Object> decoded = instance.decode("d4:dictd3:1234:test3:4565:thinge4:listl11:list-item-111:list-item-2e6:numberi123456e6:string5:valuee".getBytes(), Type.DICTIONARY);

        assertEquals(4, decoded.size());

        assertThat(decoded.get("string"), instanceOf(ByteBuffer.class));
        assertEquals("value", new String(((ByteBuffer) decoded.get("string")).array()));
        assertEquals(123456L, decoded.get("number"));

        List<Object> list = (List<Object>) decoded.get("list");
        assertEquals(2, list.size());
        assertThat(list.get(0), instanceOf(ByteBuffer.class));
        assertEquals("list-item-1", new String(((ByteBuffer) list.get(0)).array()));
        assertThat(list.get(1), instanceOf(ByteBuffer.class));
        assertEquals("list-item-2", new String(((ByteBuffer) list.get(1)).array()));

        Map<String, Object> map = (Map<String, Object>) decoded.get("dict");
        assertEquals(2, map.size());
        assertThat(map.get("123"), instanceOf(ByteBuffer.class));
        assertEquals("test", new String(((ByteBuffer) map.get("123")).array()));
        assertThat(map.get("456"), instanceOf(ByteBuffer.class));
        assertEquals("thing", new String(((ByteBuffer) map.get("456")).array()));
    }

    @Test
    public void testDecodeDictionaryEmpty() throws Exception {
        Map<String, Object> decoded = instance.decode("de123test".getBytes(), Type.DICTIONARY);

        assertTrue(decoded.isEmpty());
    }

    @Test
    public void testDecodeDictionaryInvalidItem() throws Exception {
        BencodeException exception = assertThrows(BencodeException.class, () -> instance.decode("d4:item5:value3:testing".getBytes(), Type.DICTIONARY));
        assertThat(exception.getCause(), instanceOf(InvalidObjectException.class));
    }

    @Test
    public void testDecodeDictionaryEOF() throws Exception {
        BencodeException exception = assertThrows(BencodeException.class, () -> instance.decode("d4:item5:test".getBytes(), Type.DICTIONARY));
        assertThat(exception.getCause(), instanceOf(EOFException.class));
    }

    @Test
    public void testWriteString() throws Exception {
        byte[] encoded = instance.encode("Hello World!");

        assertEquals("12:Hello World!", new String(encoded, instance.getCharset()));
    }

    @Test
    public void testWriteStringMultiByteCodePoints() throws Exception {
        byte[] encoded = instance.encode("Garçon");

        assertEquals("7:Garçon", new String(encoded, instance.getCharset()));
    }
    @Test
    public void testWriteStringEmpty() throws Exception {
        byte[] encoded = instance.encode("");

        assertEquals("0:", new String(encoded, instance.getCharset()));
    }

    @Test
    public void testWriteStringNull() throws Exception {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> instance.encode((String) null));
        assertEquals("s cannot be null", exception.getMessage());
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
        NullPointerException exception = assertThrows(NullPointerException.class, () -> instance.encode((Number) null));
        assertEquals("n cannot be null", exception.getMessage());
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
        ThrowingRunnable runnable = () -> instance.encode(new ArrayList<Object>() {{
            add("Hello");
            add("World!");
            add(new ArrayList<Object>() {{
                add(null);
                add(456);
            }});
        }});
        BencodeException exception = assertThrows(BencodeException.class, runnable);
        assertThat(exception.getCause(), instanceOf(NullPointerException.class));
    }

    @Test
    public void testWriteListNull() throws Exception {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> instance.encode((List<?>) null));
        assertEquals("l cannot be null", exception.getMessage());
    }

    @Test
    public void testWriteDictionary() throws Exception {
        byte[] encoded = instance.encode(new LinkedHashMap<Object, Object>() {{
            put("string", "value");
            put("number", 123456);
            put("list", new ArrayList<Object>() {{
                add("list-item-1");
                add("list-item-2");
            }});
            put("dict", new ConcurrentSkipListMap<Integer, String>() {{
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
        ThrowingRunnable runnable = () -> instance.encode(new TreeMap<Object, Object>() {{
            put("string", "value");
            put(123, "number-key");
        }});
        assertThrows(ClassCastException.class, runnable);
    }

    @Test
    public void testWriteDictionaryNull() throws Exception {
        NullPointerException exception = assertThrows(NullPointerException.class, () -> instance.encode((Map<?, ?>) null));
        assertEquals("m cannot be null", exception.getMessage());
    }
}
