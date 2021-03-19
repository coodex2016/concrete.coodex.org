# CRC - Cyclic redundancy check

主要用于各种算法的CRC运算。

coodex crc 实际上是基于 https://github.com/snksoft/java-crc 进行的修改和补充

各种算法参数源于 https://crccalc.com/

支持的算法包括：

- CRC-8
  - CRC8(new Parameters(8, 0x07, 0x00, false, false, 0x0))
  - CRC8_CDMA2000(new Parameters(8, 0x9b, 0xff, false, false 0x0))
  - CRC8_DARC(new Parameters(8, 0X39, 0X00, true, true, 0x0))
  - CRC8_DVB_S2(new Parameters(8, 0xd5, 0x00, false, false, 0x0))
  - CRC8_EBU(new Parameters(8, 0x1d, 0xff, true, true, 0x0))
  - CRC8_I_CODE(new Parameters(8, 0x1d, 0xfd, false, false, 0x0))
  - CRC8_ITU(new Parameters(8, 0x07, 0x00, false, false, 0x55))
  - CRC8_MAXIM(new Parameters(8, 0X31, 0X00, true, true, 0x00))
  - CRC8_ROHC(new Parameters(8, 0x07, 0xff, true, true, 0x00))
  - CRC8_WCDMA(new Parameters(8, 0X9B, 0X00, true, true, 0x00))
- CRC-16
  - CRC16_CCITT_FALSE(new Parameters(16, 0x1021, 0x00FFFF, false, false, 0x0))
  - CRC16_ARC(new Parameters(16, 0x8005, 0x0000, true, true, 0x0))
  - CRC16_AUG_CCITT(new Parameters(16, 0x1021, 0x1D0F, false, false, 0x0))
  - CRC16_BUYPASS(new Parameters(16, 0x8005, 0x0000, false, false, 0x0))
  - CRC16_CDMA2000(new Parameters(16, 0xC867, 0xFFFF, false, false, 0x0))
  - CRC16_DDS110(new Parameters(16, 0x8005, 0x800d, false, false, 0x0))
  - CRC16_DECT_R(new Parameters(16, 0x0589, 0x0000, false, false, 0x0001))
  - CRC16_DECT_X(new Parameters(16, 0x0589, 0x0000, false, false, 0x0))
  - CRC16_DNP(new Parameters(16, 0x3d65, 0x0000, true, true, 0xffff))
  - CRC16_EN13757(new Parameters(16, 0x3d65, 0x0000, false, false, 0xffff))
  - CRC16_GENIBUS(new Parameters(16, 0x1021, 0x00FFFF, false, false, 0xffff))
  - CRC16_MAXIM(new Parameters(16, 0x8005, 0x0000, true, true, 0xffff))
  - CRC16_MCRF4XX(new Parameters(16, 0x1021, 0x00FFFF, true, true, 0x0))
  - CRC16_RIELLO(new Parameters(16, 0x1021, 0x00B2AA, true, true, 0x0))
  - CRC16_T10DIF(new Parameters(16, 0x8bb7, 0x0000, false, false, 0x0000))
  - CRC16_TELEDISK(new Parameters(16, 0xa097, 0x0000, false, false, 0x0000))
  - CRC16_TMS37157(new Parameters(16, 0X1021, 0X89EC, true, true, 0x0000))
  - CRC16_USB(new Parameters(16, 0X8005, 0xffff, true, true, 0xffff))
  - CRC_A(new Parameters(16, 0x1021, 0xc6c6, true, true, 0x0))
  - CRC16_KERMIT(new Parameters(16, 0x1021, 0x0000, true, true, 0x0))
  - CRC16_MODBUS(new Parameters(16, 0X8005, 0XFFFF, true, true, 0x0))
  - CRC16_X25(new Parameters(16, 0x1021, 0xffff, true, true, 0xffff))
  - CRC16_XMODEM(new Parameters(16, 0x1021, 0x0000, false, false, 0x0000))
- CRC32
  - CRC32(new Parameters(32, 0x04C11DB7, 0xFFFFFFFF, true, true, 0xFFFFFFFF))
  - CRC32_BZIP2(new Parameters(32, 0x04C11DB7, 0xFFFFFFFF, false, false, 0xFFFFFFFF))
  - CRC32C(new Parameters(32, 0x1EDC6F41, 0xFFFFFFFF, true, true, 0xFFFFFFFF))
  - CRC32D(new Parameters(32, 0xA833982B, 0xFFFFFFFF, true, true, 0xFFFFFFFF))
  - CRC32_MPEG2(new Parameters(32, 0x04C11DB7, 0xFFFFFFFF, false, false, 0x00000000))
  - CRC32_POSIX(new Parameters(32, 0x04C11DB7, 0x00000000, false, false, 0xFFFFFFFF))
  - CRC32Q(new Parameters(32, 0x814141AB, 0x00000000, false, false, 0x00000000))
  - CRC32_JAMCRC(new Parameters(32, 0x04C11DB7, 0xFFFFFFFF, true, true, 0x00000000))
  - CRC32_XFER(new Parameters(32, 0x000000AF, 0x00000000, false, false, 0x00000000))

## usage

```java
    // content是一个字节数组
    CRC.calculateCRC(CRC.Algorithm.CRC16_XMODEM, content);
```
