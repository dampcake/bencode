# bencode

[![Build Status](https://github.com/dampcake/bencode/workflows/Build/badge.svg?branch=master)](https://github.com/dampcake/bencode/actions?query=branch%3Amaster)
[![Coverage Status](https://codecov.io/gh/dampcake/bencode/branch/master/graph/badge.svg)](https://codecov.io/gh/dampcake/bencode)
[![Maven](https://img.shields.io/maven-central/v/com.dampcake/bencode.svg)](http://search.maven.org/#search%7Cga%7C1%7Ccom.dampcake.bencode)
[![GitHub license](https://img.shields.io/github/license/dampcake/bencode.svg)](https://github.com/dampcake/bencode/blob/master/LICENSE)

Bencode Input/Output Streams for Java

Requires JDK 1.8 or higher

[Bencode Spec](https://wiki.theory.org/BitTorrentSpecification#Bencoding)

[Bencode Wikipedia](https://en.wikipedia.org/wiki/Bencode)

## Javadoc
http://dampcake.github.io/bencode

## Usage

### Maven
```xml
<dependency>
    <groupId>com.dampcake</groupId>
    <artifactId>bencode</artifactId>
    <version>1.3.2</version>
</dependency>
```

### Gradle
```groovy
compile 'com.dampcake:bencode:1.3.2'
```

### Examples

#### Bencode Data
```java
Bencode bencode = new Bencode();
byte[] encoded = bencode.encode(new HashMap<Object, Object>() {{
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

System.out.println(new String(encoded, bencode.getCharset()));
```

Outputs: ```d4:dictd3:1234:test3:4565:thinge4:listl11:list-item-111:list-item-2e6:numberi123456e6:string5:valuee```

#### Decode Bencoded Data:
```java
Bencode bencode = new Bencode();
Map<String, Object> dict = bencode.decode("d4:dictd3:1234:test3:4565:thinge4:listl11:list-item-111:list-item-2e6:numberi123456e6:string5:valuee".getBytes(), Type.DICTIONARY);

System.out.println(dict);
```

Outputs: ```{dict={123=test, 456=thing}, list=[list-item-1, list-item-2], number=123456, string=value}```

#### Write bencoded data to a Stream:
```java
ByteArrayOutputStream out = new ByteArrayOutputStream();
BencodeOutputStream bencoder = new BencodeOutputStream(out);

bencoder.writeDictionary(new HashMap<Object, Object>() {{
    put("string", "value");
    put("number", 123456);
    put("list", new ArrayList<Object>() {{
        add("list-item-1");
        add("list-item-2");
    }});
    put("dict", new ConcurrentSkipListMap() {{
        put("dict-item-1", "test");
        put("dict-item-2", "thing");
    }});
}});

System.out.println(new String(out.toByteArray()));
```

Outputs: ```d4:dictd11:dict-item-14:test11:dict-item-25:thinge4:listl11:list-item-111:list-item-2e6:numberi123456e6:string5:valuee```

#### Read bencoded data to a Stream:
```java
String input = "d4:dictd11:dict-item-14:test11:dict-item-25:thinge4:listl11:list-item-111:list-item-2e6:numberi123456e6:string5:valuee";
ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
BencodeInputStream bencode = new BencodeInputStream(in);

Type type = bencode.nextType(); // Returns Type.DICTIONARY
Map<String, Object> dict = bencode.readDictionary();

System.out.println(dict);
```

Outputs: ```{dict={dict-item-1=test, dict-item-2=thing}, list=[list-item-1, list-item-2], number=123456, string=value}```
