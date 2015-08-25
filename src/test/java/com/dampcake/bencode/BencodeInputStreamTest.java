package com.dampcake.bencode;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.InvalidObjectException;
import java.util.List;
import java.util.Map;

import static com.dampcake.bencode.Assert.assertThrows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for BencodeInputStream
 *
 * @author Adam Peck
 */
public class BencodeInputStreamTest {

    private BencodeInputStream instance;

    private void instantiate(String s) {
        instance = new BencodeInputStream(new ByteArrayInputStream(s.getBytes(Bencode.DEFAULT_CHARSET)));
    }

    @Test
    public void testConstructorNullStream() throws Exception {
        new BencodeInputStream(null);
    }

    @Test(expected = NullPointerException.class)
    public void testConstructorNullCharset() throws Exception {
        new BencodeInputStream(new ByteArrayInputStream(new byte[0]), null);
    }

    @Test
    public void testNextTypeString() throws Exception {
        instantiate("7");

        assertEquals(Type.STRING, instance.nextType());
        assertEquals(1, instance.available());
    }

    @Test
    public void testNextTypeNumber() throws Exception {
        instantiate("i1");

        assertEquals(Type.NUMBER, instance.nextType());
        assertEquals(2, instance.available());
    }

    @Test
    public void testNextTypeList() throws Exception {
        instantiate("l123");

        assertEquals(Type.LIST, instance.nextType());
        assertEquals(4, instance.available());
    }

    @Test
    public void testNextTypeDictionary() throws Exception {
        instantiate("dtesting");

        assertEquals(Type.DICTIONARY, instance.nextType());
        assertEquals(8, instance.available());
    }

    @Test
    public void testNextTypeUnknown() throws Exception {
        instantiate("unknown");

        assertEquals(Type.UNKNOWN, instance.nextType());
        assertEquals(7, instance.available());
    }

    @Test
    public void testReadString() throws Exception {
        instantiate("12:Hello World!123");

        assertEquals("Hello World!", instance.readString());
        assertEquals(3, instance.available());
    }

    @Test
    public void testReadStringEmpty() throws Exception {
        instantiate("0:123");

        assertEquals("", instance.readString());
        assertEquals(3, instance.available());
    }

    @Test
    public void testReadStringNaN() throws Exception {
        instantiate("1c3:Testing");

        assertThrows(InvalidObjectException.class, new Runnable() {
            public void run() throws Exception {
                instance.readString();
            }
        });

        assertEquals(10, instance.available());
    }

    @Test
    public void testReadStringEOF() throws Exception {
        instantiate("123456");

        assertThrows(EOFException.class, new Runnable() {
            public void run() throws Exception {
                instance.readString();
            }
        });

        assertEquals(0, instance.available());
    }

    @Test
    public void testReadStringEmptyStream() throws Exception {
        instantiate("");

        assertThrows(EOFException.class, new Runnable() {
            public void run() throws Exception {
                instance.readString();
            }
        });

        assertEquals(0, instance.available());
    }

    @Test
    public void testReadNumber() throws Exception {
        instantiate("i123456e123");

        assertEquals(123456, instance.readNumber().longValue());
        assertEquals(3, instance.available());
    }

    @Test
    public void testReadNumberNaN() throws Exception {
        instantiate("i123cbve1");

        assertThrows(NumberFormatException.class, new Runnable() {
            public void run() throws Exception {
                instance.readNumber();
            }
        });

        assertEquals(1, instance.available());
    }

    @Test
    public void testReadNumberEOF() throws Exception {
        instantiate("i123");

        assertThrows(EOFException.class, new Runnable() {
            public void run() throws Exception {
                instance.readNumber();
            }
        });

        assertEquals(0, instance.available());
    }

    @Test
    public void testReadNumberEmptyStream() throws Exception {
        instantiate("");

        assertThrows(EOFException.class, new Runnable() {
            public void run() throws Exception {
                instance.readNumber();
            }
        });

        assertEquals(0, instance.available());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testReadList() throws Exception {
        instantiate("l5:Hello6:World!li123ei456eeetesting");

        List<Object> result = instance.readList();

        assertEquals(3, result.size());

        assertEquals("Hello", result.get(0));
        assertEquals("World!", result.get(1));

        List<Object> list = (List<Object>) result.get(2);
        assertEquals(123L, list.get(0));
        assertEquals(456L, list.get(1));

        assertEquals(7, instance.available());
    }

    @Test
    public void testReadListEmpty() throws Exception {
        instantiate("le123");

        assertTrue(instance.readList().isEmpty());
        assertEquals(3, instance.available());
    }

    @Test
    public void testReadListInvalidItem() throws Exception {
        instantiate("l2:Worlde");

        assertThrows(InvalidObjectException.class, new Runnable() {
            public void run() throws Exception {
                instance.readList();
            }
        });

        assertEquals(4, instance.available());
    }

    @Test
    public void testReadListEOF() throws Exception {
        instantiate("l5:Hello");

        assertThrows(EOFException.class, new Runnable() {
            public void run() throws Exception {
                instance.readList();
            }
        });

        assertEquals(0, instance.available());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testReadDictionary() throws Exception {
        instantiate("d4:dictd3:1234:test3:4565:thinge4:listl11:list-item-111:list-item-2e6:numberi123456e6:string5:valuee");

        Map<String, Object> result = instance.readDictionary();

        assertEquals(4, result.size());

        assertEquals("value", result.get("string"));
        assertEquals(123456L, result.get("number"));

        List<Object> list = (List<Object>) result.get("list");
        assertEquals(2, list.size());
        assertEquals("list-item-1", list.get(0));
        assertEquals("list-item-2", list.get(1));

        Map<String, Object> map = (Map<String, Object>) result.get("dict");
        assertEquals(2, map.size());
        assertEquals("test", map.get("123"));
        assertEquals("thing", map.get("456"));

        assertEquals(0, instance.available());
    }

    @Test
    public void testReadDictionaryEmpty() throws Exception {
        instantiate("de123test");

        assertTrue(instance.readDictionary().isEmpty());
        assertEquals(7, instance.available());
    }

    @Test
    public void testReadDictionaryInvalidItem() throws Exception {
        instantiate("d4:item5:value3:testing");

        assertThrows(InvalidObjectException.class, new Runnable() {
            public void run() throws Exception {
                instance.readDictionary();
            }
        });

        assertEquals(4, instance.available());
    }

    @Test
    public void testReadDictionaryEOF() throws Exception {
        instantiate("d4:item5:test");

        assertThrows(EOFException.class, new Runnable() {
            public void run() throws Exception {
                instance.readDictionary();
            }
        });

        assertEquals(0, instance.available());
    }
}
