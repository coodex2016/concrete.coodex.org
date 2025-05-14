# org.coodex.util.Common

一组通用方法和常量。

## 常量

- `PATH_SEPARATOR`: 多个路径的分隔符，`windows`平台为`;`, `linux`平台为`:`
- `FILE_SEPARATOR`: 文件分隔符，`windows`平台为`\`, `linux`平台为`/`
- `DEFAULT_DATE_FORMAT`: 默认的日期格式，`yyyy-MM-dd`
- `DEFAULT_TIME_FORMAT`: 默认的时间格式， `HH:mm:ss`
- `DEFAULT_DATETIME_FORMAT`: 默认时间戳格式，`yyyy-MM-dd HH:mm:ss`
- `SYSTEM_START_TIME`: JVM启动的时间
- `PROCESSOR_COUNT`: 处理器的个数

## 方法

### arrayToSet(T[]): Set&lt;T>

将一个数组转为set.

### base16Encode

将byte数组按base16编码。

以[0x00 - 0xFF] 为例，各个接口使用效果如下：

```java
System.out.println(Common.base16Encode(bytes));
```

```txt
000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f404142434445464748494a4b4c4d4e4f505152535455565758595a5b5c5d5e5f606162636465666768696a6b6c6d6e6f707172737475767778797a7b7c7d7e7f808182838485868788898a8b8c8d8e8f909192939495969798999a9b9c9d9e9fa0a1a2a3a4a5a6a7a8a9aaabacadaeafb0b1b2b3b4b5b6b7b8b9babbbcbdbebfc0c1c2c3c4c5c6c7c8c9cacbcccdcecfd0d1d2d3d4d5d6d7d8d9dadbdcdddedfe0e1e2e3e4e5e6e7e8e9eaebecedeeeff0f1f2f3f4f5f6f7f8f9fafbfcfdfeff
```

```java
System.out.println(Common.base16Encode(bytes,16/*每行16个字节*/," "/*每行中列于列之间使用空格隔开*/));
```

```txt
00 01 02 03 04 05 06 07 08 09 0a 0b 0c 0d 0e 0f
10 11 12 13 14 15 16 17 18 19 1a 1b 1c 1d 1e 1f
20 21 22 23 24 25 26 27 28 29 2a 2b 2c 2d 2e 2f
30 31 32 33 34 35 36 37 38 39 3a 3b 3c 3d 3e 3f
40 41 42 43 44 45 46 47 48 49 4a 4b 4c 4d 4e 4f
50 51 52 53 54 55 56 57 58 59 5a 5b 5c 5d 5e 5f
60 61 62 63 64 65 66 67 68 69 6a 6b 6c 6d 6e 6f
70 71 72 73 74 75 76 77 78 79 7a 7b 7c 7d 7e 7f
80 81 82 83 84 85 86 87 88 89 8a 8b 8c 8d 8e 8f
90 91 92 93 94 95 96 97 98 99 9a 9b 9c 9d 9e 9f
a0 a1 a2 a3 a4 a5 a6 a7 a8 a9 aa ab ac ad ae af
b0 b1 b2 b3 b4 b5 b6 b7 b8 b9 ba bb bc bd be bf
c0 c1 c2 c3 c4 c5 c6 c7 c8 c9 ca cb cc cd ce cf
d0 d1 d2 d3 d4 d5 d6 d7 d8 d9 da db dc dd de df
e0 e1 e2 e3 e4 e5 e6 e7 e8 e9 ea eb ec ed ee ef
f0 f1 f2 f3 f4 f5 f6 f7 f8 f9 fa fb fc fd fe ff
```

```java
System.out.println(Common.base16Encode(bytes,line->line + 1/*每行显示数量为行数+1,行号从0开始*/," "/*每行中列于列之间使用空格隔开*/));
```

```txt
00
01 02
03 04 05
06 07 08 09
0a 0b 0c 0d 0e
0f 10 11 12 13 14
15 16 17 18 19 1a 1b
1c 1d 1e 1f 20 21 22 23
24 25 26 27 28 29 2a 2b 2c
2d 2e 2f 30 31 32 33 34 35 36
37 38 39 3a 3b 3c 3d 3e 3f 40 41
42 43 44 45 46 47 48 49 4a 4b 4c 4d
4e 4f 50 51 52 53 54 55 56 57 58 59 5a
5b 5c 5d 5e 5f 60 61 62 63 64 65 66 67 68
69 6a 6b 6c 6d 6e 6f 70 71 72 73 74 75 76 77
78 79 7a 7b 7c 7d 7e 7f 80 81 82 83 84 85 86 87
88 89 8a 8b 8c 8d 8e 8f 90 91 92 93 94 95 96 97 98
99 9a 9b 9c 9d 9e 9f a0 a1 a2 a3 a4 a5 a6 a7 a8 a9 aa
ab ac ad ae af b0 b1 b2 b3 b4 b5 b6 b7 b8 b9 ba bb bc bd
be bf c0 c1 c2 c3 c4 c5 c6 c7 c8 c9 ca cb cc cd ce cf d0 d1
d2 d3 d4 d5 d6 d7 d8 d9 da db dc dd de df e0 e1 e2 e3 e4 e5 e6
e7 e8 e9 ea eb ec ed ee ef f0 f1 f2 f3 f4 f5 f6 f7 f8 f9 fa fb fc
fd fe ff
```

```java
System.out.println(Common.base16Encode(bytes,16/*从下标为16的元素开始*/,8/*编码8个字节*/));
```

```txt
1011121314151617
```

```java
System.out.println(Common.base16Encode(bytes,16/*从下标为16的元素开始*/,8/*编码8个字节*/,4/*每行4列*/," "));
```

```txt
10 11 12 13
14 15 16 17
```

```java
System.out.println(Common.base16Encode(bytes,16/*从下标为16的元素开始*/,10/*编码10个字节*/, line->line+1, " "));
```

```txt
10
11 12
13 14 15
16 17 18 19
```

### calendar

快捷构造Calendar的系列接口，参数从左到右，分别是年、月（从0起）、日、时、分、秒、毫秒

### calendarToStr

Calnedar转字符串，默认格式`yyyy-MM-dd HH:mm:ss`.

### copyStream

将一个InputStream拷贝到OutputStream，可限速

### difference

求两个Set1-Set2的差集

### getResource

在`coodex.resource.path`[详见](org.coodex.util.ResourceScanner.md)和`classpath`中查找指定资源

### intersection

求两个Set的交集
