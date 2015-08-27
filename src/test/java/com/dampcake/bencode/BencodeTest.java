package com.dampcake.bencode;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.EOFException;
import java.io.InvalidObjectException;
import java.util.List;
import java.util.Map;

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
    public void testTypeNullCharset() {
        exception.expect(NullPointerException.class);
        exception.expectMessage("charset cannot be null");

        instance.type(new byte[0], null);
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
    public void testDecodeNullTypeCharset() {
        exception.expect(NullPointerException.class);
        exception.expectMessage("charset cannot be null");

        instance.decode("12:Hello World!".getBytes(), Type.STRING, null);
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
}
