/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.coodex.mock;

import java.util.ArrayList;
import java.util.List;

/**
 * unicode 字符分布。数据来源 https://zh.wikipedia.org/wiki/Unicode%E5%AD%97%E7%AC%A6%E5%B9%B3%E9%9D%A2%E6%98%A0%E5%B0%84
 */
public enum CharCodeSet {

    /**
     * 基本拉丁文
     */
    BASIC_LATIN(0x0000, 0x007F),
    /**
     * 拉丁文-1补充
     */
    LATIN_1_SUPPLEMENT(0x0080, 0x00FF),
    /**
     * 拉丁文扩展-A
     */
    LATIN_EXTENDED_A(0x0100, 0x017F),
    /**
     * 拉丁文扩展-B
     */
    LATIN_EXTENDED_B(0x0180, 0x024F),
    /**
     * 国际音标扩展
     */
    IPA_EXTENSIONS(0x0250, 0x02AF),
    /**
     * 占位修饰符号
     */
    SPACING_MODIFIER_LETTERS(0x02B0, 0x02FF),
    /**
     * 结合附加符号
     */
    COMBINING_DIACRITICS_MARKS(0x0300, 0x036F),
    /**
     * 希腊字母及科普特字母
     */
    GREEK_AND_COPTIC(0x0370, 0x03FF),
    /**
     * 西里尔字母
     */
    CYRILLIC(0x0400, 0x04FF),
    /**
     * 西里尔字母补充
     */
    CYRILLIC_SUPPLEMENT(0x0500, 0x052F),
    /**
     * 亚美尼亚字母
     */
    ARMENIAN(0x0530, 0x058F),
    /**
     * 希伯来文
     */
    HEBREW(0x0590, 0x05FF),
    /**
     * 阿拉伯文
     */
    ARABIC(0x0600, 0x06FF),
    /**
     * 叙利亚文
     */
    SYRIAC(0x0700, 0x074F),
    /**
     * 阿拉伯文补充
     */
    ARABIC_SUPPLEMENT(0x0750, 0x077F),
    /**
     * 它拿字母
     */
    THAANA(0x0780, 0x07BF),
    /**
     * 西非书面文字
     */
    N_KO(0x07C0, 0x07FF),
    /**
     * 撒玛利亚字母
     */
    SAMARITAN(0x0800, 0x083F),
    /**
     * 曼达文字
     */
    MANDAIC(0x0840, 0x085F),
    /**
     * 叙利亚文补充
     */
    SYRIAC_SUPPLEMENT(0x0860, 0x086F),
    /**
     * 阿拉伯文扩展-A
     */
    ARABIC_EXTENDED_A(0x08A0, 0x08FF),
    /**
     * 天城文
     */
    DEVANAGARI(0x0900, 0x097F),
    /**
     * 孟加拉文
     */
    BENGALI(0x0980, 0x09FF),
    /**
     * 古木基文
     */
    GURMUKHI(0x0A00, 0x0A7F),
    /**
     * 古吉拉特文
     */
    GUJARATI(0x0A80, 0x0AFF),
    /**
     * 奥里亚文
     */
    ORIYA(0x0B00, 0x0B7F),
    /**
     * 泰米尔文
     */
    TAMIL(0x0B80, 0x0BFF),
    /**
     * 泰卢固文
     */
    TELUGU(0x0C00, 0x0C7F),
    /**
     * 卡纳达文
     */
    KANNADA(0x0C80, 0x0CFF),
    /**
     * 马拉雅拉姆文
     */
    MALAYALAM(0x0D00, 0x0D7F),
    /**
     * 僧伽罗文
     */
    SINHALA(0x0D80, 0x0DFF),
    /**
     * 泰文
     */
    THAI(0x0E00, 0x0E7F),
    /**
     * 老挝文
     */
    LAO(0x0E80, 0x0EFF),
    /**
     * 藏文
     */
    TIBETAN(0x0F00, 0x0FFF),
    /**
     * 缅甸文
     */
    MYANMAR(0x1000, 0x109F),
    /**
     * 格鲁吉亚字母
     */
    GEORGIAN(0x10A0, 0x10FF),
    /**
     * 谚文字母
     */
    HANGUL_JAMO(0x1100, 0x11FF),
    /**
     * 吉兹字母
     */
    ETHIOPIC(0x1200, 0x137F),
    /**
     * 吉兹字母补充
     */
    ETHIOPIC_SUPPLEMENT(0x1380, 0x139F),
    /**
     * 切罗基字母
     */
    CHEROKEE(0x13A0, 0x13FF),
    /**
     * 统一加拿大原住民音节文字
     */
    UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS(0x1400, 0x167F),
    /**
     * 欧甘字母
     */
    OGHAM(0x1680, 0x169F),
    /**
     * 卢恩字母
     */
    RUNIC(0x16A0, 0x16FF),
    /**
     * 他加禄字母
     */
    TAGALOG(0x1700, 0x171F),
    /**
     * 哈努诺文 - HANUNÓO
     */
    HANUNOO(0x1720, 0x173F),
    /**
     * 布希德文
     */
    BUHID(0x1740, 0x175F),
    /**
     * 塔格班瓦文
     */
    TAGBANWA(0x1760, 0x177F),
    /**
     * 高棉文
     */
    KHMER(0x1780, 0x17FF),
    /**
     * 蒙古文
     */
    MONGOLIAN(0x1800, 0x18AF),
    /**
     * 加拿大原住民音节文字扩展
     */
    UNIFIED_CANADIAN_ABORIGINAL_SYLLABICS_EXTENDED(0x18B0, 0x18FF),
    /**
     * 林布文
     */
    LIMBU(0x1900, 0x194F),
    /**
     * 德宏傣文
     */
    TAI_LE(0x1950, 0x197F),
    /**
     * 新傣仂文
     */
    NEW_TAI_LUE(0x1980, 0x19DF),
    /**
     * 高棉文符号
     */
    KHMER_SYMBOLS(0x19E0, 0x19FF),
    /**
     * 布吉文
     */
    BUGINESE(0x1A00, 0x1A1F),
    /**
     * 老傣文
     */
    TAI_THAM(0x1A20, 0x1AAF),
    /**
     * 组合变音标记扩展
     */
    COMBINING_DIACRITICAL_MARKS_EXTENDED(0x1AB0, 0x1AFF),
    /**
     * 巴厘字母
     */
    BALINESE(0x1B00, 0x1B7F),
    /**
     * 巽他字母
     */
    SUNDANESE(0x1B80, 0x1BBF),
    /**
     * 巴塔克文
     */
    BATAK(0x1BC0, 0x1BFF),
    /**
     * 雷布查字母
     */
    LEPCHA(0x1C00, 0x1C4F),
    /**
     * 桑塔利文
     */
    OL_CHIKI(0x1C50, 0x1C7F),
    /**
     * 西里尔字母扩充-C
     */
    CYRILLIC_EXTENDED_C(0x1C80, 0x1C8F),
    /**
     * 格鲁吉亚字母扩展
     */
    GEORGIAN_EXTENDED(0x1C90, 0x1CBF),
    /**
     * 巽他字母补充
     */
    SUDANESE_SUPPLEMENT(0x1CC0, 0x1CCF),
    /**
     * 梵文吠陀扩展
     */
    VEDIC_EXTENSIONS(0x1CD0, 0x1CFF),
    /**
     * 音标扩展
     */
    PHONETIC_EXTENSIONS(0x1D00, 0x1D7F),
    /**
     * 音标扩展补充
     */
    PHONETIC_EXTENSIONS_SUPPLEMENT(0x1D80, 0x1DBF),
    /**
     * 结合附加符号补充
     */
    COMBINING_DIACRITICS_MARKS_SUPPLEMENT(0x1DC0, 0x1DFF),
    /**
     * 拉丁文扩展附加
     */
    LATIN_EXTENDED_ADDITIONAL(0x1E00, 0x1EFF),
    /**
     * 希腊文扩展
     */
    GREEK_EXTENDED(0x1F00, 0x1FFF),
    /**
     * 常用标点
     */
    GENERAL_PUNCTUATION(0x2000, 0x206F),
    /**
     * 上标及下标
     */
    SUPERSCRIPTS_AND_SUBSCRIPTS(0x2070, 0x209F),
    /**
     * 货币符号
     */
    CURRENCY_SYMBOLS(0x20A0, 0x20CF),
    /**
     * 组合用记号
     */
    COMBINING_DIACRITICS_MARKS_FOR_SYMBOLS(0x20D0, 0x20FF),
    /**
     * 字母式符号
     */
    LETTERLIKE_SYMBOLS(0x2100, 0x214F),
    /**
     * 数字形式
     */
    NUMBER_FORMS(0x2150, 0x218F),
    /**
     * 箭头
     */
    ARROWS(0x2190, 0x21FF),
    /**
     * 数学运算符
     */
    MATHEMATICAL_OPERATORS(0x2200, 0x22FF),
    /**
     * 杂项工业符号
     */
    MISCELLANEOUS_TECHNICAL(0x2300, 0x23FF),
    /**
     * 控制图片
     */
    CONTROL_PICTURES(0x2400, 0x243F),
    /**
     * 光学识别符
     */
    OPTICAL_CHARACTER_RECOGNITION(0x2440, 0x245F),
    /**
     * 带圈字母和数字
     */
    ENCLOSED_ALPHANUMERICS(0x2460, 0x24FF),
    /**
     * 制表符
     */
    BOX_DRAWING(0x2500, 0x257F),
    /**
     * 方块元素
     */
    BLOCK_ELEMENTS(0x2580, 0x259F),
    /**
     * 几何图形
     */
    GEOMETRIC_SHAPES(0x25A0, 0x25FF),
    /**
     * 杂项符号
     */
    MISCELLANEOUS_SYMBOLS(0x2600, 0x26FF),
    /**
     * 装饰符号
     */
    DINGBATS(0x2700, 0x27BF),
    /**
     * 杂项数学符号-A
     */
    MISCELLANEOUS_MATHEMATICAL_SYMBOLS_A(0x27C0, 0x27EF),
    /**
     * 追加箭头-A
     */
    SUPPLEMENTAL_ARROWS_A(0x27F0, 0x27FF),
    /**
     * 盲文点字模型
     */
    BRAILLE_PATTERNS(0x2800, 0x28FF),
    /**
     * 追加箭头-B
     */
    SUPPLEMENTAL_ARROWS_B(0x2900, 0x297F),
    /**
     * 杂项数学符号-B
     */
    MISCELLANEOUS_MATHEMATICAL_SYMBOLS_B(0x2980, 0x29FF),
    /**
     * 追加数学运算符
     */
    SUPPLEMENTAL_MATHEMATICAL_OPERATOR(0x2A00, 0x2AFF),
    /**
     * 杂项符号和箭头
     */
    MISCELLANEOUS_SYMBOLS_AND_ARROWS(0x2B00, 0x2BFF),
    /**
     * 格拉哥里字母
     */
    GLAGOLITIC(0x2C00, 0x2C5F),
    /**
     * 拉丁文扩展-C
     */
    LATIN_EXTENDED_C(0x2C60, 0x2C7F),
    /**
     * 科普特字母
     */
    COPTIC(0x2C80, 0x2CFF),
    /**
     * 格鲁吉亚字母补充
     */
    GEORGIAN_SUPPLEMENT(0x2D00, 0x2D2F),
    /**
     * 提非纳文
     */
    TIFINAGH(0x2D30, 0x2D7F),
    /**
     * 吉兹字母扩展
     */
    ETHIOPIC_EXTENDED(0x2D80, 0x2DDF),
    /**
     * 西里尔字母扩展-A
     */
    CYRILLIC_EXTENDED_A(0x2DE0, 0x2DFF),
    /**
     * 追加标点
     */
    SUPPLEMENTAL_PUNCTUATION(0x2E00, 0x2E7F),
    /**
     * 中日韩汉字部首补充
     */
    CJK_RADICALS_SUPPLEMENT(0x2E80, 0x2EFF),
    /**
     * 康熙部首
     */
    KANGXI_RADICALS(0x2F00, 0x2FDF),
    /**
     * 表意文字序列
     */
    IDEOGRAPHIC_DESCRIPTION_CHARACTERS(0x2FF0, 0x2FFF),
    /**
     * 中日韩符号和标点
     */
    CJK_SYMBOLS_AND_PUNCTUATION(0x3000, 0x303F),
    /**
     * 日文平假名
     */
    HIRAGANA(0x3040, 0x309F),
    /**
     * 日文片假名
     */
    KATAKANA(0x30A0, 0x30FF),
    /**
     * 注音符号
     */
    BOPOMOFO(0x3100, 0x312F),
    /**
     * 谚文兼容字母
     */
    HANGUL_COMPATIBILITY_JAMO(0x3130, 0x318F),
    /**
     * 汉文注释标志
     */
    KANBUN(0x3190, 0x319F),
    /**
     * 注音字母扩展
     */
    BOPOMOFO_EXTENDED(0x31A0, 0x31BF),
    /**
     * 中日韩笔画
     */
    CJK_STROKES(0x31C0, 0x31EF),
    /**
     * 日文片假名拼音扩展
     */
    KATAKANA_PHONETIC_EXTENSIONS(0x31F0, 0x31FF),
    /**
     * 带圈的CJK字符及月份
     */
    ENCLOSED_CJK_LETTERS_AND_MONTHS(0x3200, 0x32FF),
    /**
     * 中日韩兼容字符
     */
    CJK_COMPATIBILITY(0x3300, 0x33FF),
    /**
     * 中日韩统一表意文字扩展区A
     */
    CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A(0x3400, 0x4DBF),
    /**
     * 易经六十四卦符号
     */
    YIJING_HEXAGRAMS_SYMBOLS(0x4DC0, 0x4DFF),
    /**
     * 中日韩统一表意文字
     */
    CJK_UNIFIED_IDEOGRAPHS(0x4E00, 0x9FFF),
    /**
     * 彝文音节
     */
    YI_SYLLABLES(0xA000, 0xA48F),
    /**
     * 彝文字根
     */
    YI_RADICALS(0xA490, 0xA4CF),
    /**
     * 老傈僳文
     */
    LISU(0xA4D0, 0xA4FF),
    /**
     * 瓦伊语
     */
    VAI(0xA500, 0xA63F),
    /**
     * 西里尔字母扩展-B
     */
    CYRILLIC_EXTENDED_B(0xA640, 0xA69F),
    /**
     * 巴姆穆文字
     */
    BAMUM(0xA6A0, 0xA6FF),
    /**
     * 修饰用声调符号
     */
    MODIFIER_TONE_LETTERS(0xA700, 0xA71F),
    /**
     * 拉丁文扩展-D
     */
    LATIN_EXTENDED_D(0xA720, 0xA7FF),
    /**
     * 锡尔赫特文
     */
    SYLOTI_NAGRI(0xA800, 0xA82F),
    /**
     * 通用印度数字格式
     */
    COMMON_INDIC_NUMBER_FORMS(0xA830, 0xA83F),
    /**
     * 八思巴字
     */
    PHAGS_PA(0xA840, 0xA87F),
    /**
     * 索拉什特拉文
     */
    SAURASHTRA(0xA880, 0xA8DF),
    /**
     * 天城文扩展
     */
    DEVANAGARI_EXTENDED(0xA8E0, 0xA8FF),
    /**
     * 克耶里字母
     */
    KAYAH_LI(0xA900, 0xA92F),
    /**
     * 勒姜字母
     */
    REJANG(0xA930, 0xA95F),
    /**
     * 谚文扩展-A
     */
    HANGUL_JAMO_EXTENDED_A(0xA960, 0xA97F),
    /**
     * 爪哇字母
     */
    JAVANESE(0xA980, 0xA9DF),
    /**
     * 缅甸文扩展-B
     */
    MYANMAR_EXTENDED_B(0xA9E0, 0xA9FF),
    /**
     * 占语字母
     */
    CHAM(0xAA00, 0xAA5F),
    /**
     * 缅甸文扩展-A
     */
    MYANMAR_EXTENDED_A(0xAA60, 0xAA7F),
    /**
     * 越南傣文
     */
    TAI_VIET(0xAA80, 0xAADF),
    /**
     * 曼尼普尔文扩展
     */
    MEETEI_MAYEK_EXTENSIONS(0xAAE0, 0xAAFF),
    /**
     * 吉兹字母扩展-A
     */
    ETHIOPIC_EXTENDED_A(0xAB00, 0xAB2F),
    /**
     * 拉丁文扩展-E
     */
    LATIN_EXTENDED_E(0xAB30, 0xAB6F),
    /**
     * 切罗基语补充
     */
    CHEROKEE_SUPPLEMENT(0xAB70, 0xABBF),
    /**
     * 曼尼普尔文
     */
    MEETEI_MAYEK(0xABC0, 0xABFF),
    /**
     * 谚文音节
     */
    HANGUL_SYLLABLES(0xAC00, 0xD7AF),
    /**
     * 谚文字母扩展-B
     */
    HANGUL_JAMO_EXTENDED_B(0xD7B0, 0xD7FF),
    /**
     * 私用区
     */
    PRIVATE_USE_AREA(0xE000, 0xF8FF),
    /**
     * 中日韩兼容表意文字
     */
    CJK_COMPATIBILITY_IDEOGRAPHS(0xF900, 0xFAFF),
    /**
     * 字母表达形式（拉丁字母连字、亚美尼亚字母连字、希伯来文表现形式）
     */
    ALPHABETIC_PRESENTATION_FORMS(0xFB00, 0xFB4F),
    /**
     * 阿拉伯字母表达形式-A
     */
    ARABIC_PRESENTATION_FORMS_A(0xFB50, 0xFDFF),
    /**
     * 异体字选择器
     */
    VARIATION_SELECTOR(0xFE00, 0xFE0F),
    /**
     * 竖排形式
     */
    VERTICAL_FORMS(0xFE10, 0xFE1F),
    /**
     * 组合用半符号
     */
    COMBINING_HALF_MARKS(0xFE20, 0xFE2F),
    /**
     * 中日韩兼容形式
     */
    CJK_COMPATIBILITY_FORMS(0xFE30, 0xFE4F),
    /**
     * 小写变体形式
     */
    SMALL_FORM_VARIANTS(0xFE50, 0xFE6F),
    /**
     * 阿拉伯文表达形式-B
     */
    ARABIC_PRESENTATION_FORMS_B(0xFE70, 0xFEFF),
    /**
     * 半角及全角字符
     */
    HALFWIDTH_AND_FULLWIDTH_FORMS(0xFF00, 0xFFEF),
    /**
     * 特殊
     */
    SPECIALS(0xFFF0, 0xFFFF),
    /**
     * 线形文字B音节文字
     */
    LINEAR_B_SYLLABARY(0x10000, 0x1007F),
    /**
     * 线形文字B表意文字
     */
    LINEAR_B_IDEOGRAMS(0x10080, 0x100FF),
    /**
     * 爱琴海数字
     */
    AEGEAN_NUMBERS(0x10100, 0x1013F),
    /**
     * 古希腊数字
     */
    ANCIENT_GREEK_NUMBERS(0x10140, 0x1018F),
    /**
     * 古代记数系统
     */
    ANCIENT_SYMBOLS(0x10190, 0x101CF),
    /**
     * 费斯托斯圆盘
     */
    PHAISTOS_DISC(0x101D0, 0x101FF),
    /**
     * 吕基亚字母
     */
    LYCIAN(0x10280, 0x1029F),
    /**
     * 卡利亚字母
     */
    CARIAN(0x102A0, 0x102DF),
    /**
     * 科普特闰余数字
     */
    COPTIC_EPACT_NUMBERS(0x102E0, 0x102FF),
    /**
     * 古意大利字母
     */
    OLD_ITALIC(0x10300, 0x1032F),
    /**
     * 哥特字母
     */
    GOTHIC(0x10330, 0x1034F),
    /**
     * 古彼尔姆文
     */
    OLD_PERMIC(0x10350, 0x1037F),
    /**
     * 乌加里特字母
     */
    UGARITIC(0x10380, 0x1039F),
    /**
     * 古波斯楔形文字
     */
    OLD_PERSIAN(0x103A0, 0x103DF),
    /**
     * 德赛莱特字母
     */
    DESERET(0x10400, 0x1044F),
    /**
     * 萧伯纳字母
     */
    SHAVIAN(0x10450, 0x1047F),
    /**
     * 奥斯曼亚字母
     */
    OSMANYA(0x10480, 0x104AF),
    /**
     * 欧塞奇字母
     */
    OSAGE(0x104B0, 0x104FF),
    /**
     * 艾尔巴桑字母
     */
    ELBASAN(0x10500, 0x1052F),
    /**
     * 高加索阿尔巴尼亚文
     */
    CAUCASIAN_ALBANIAN(0x10530, 0x1056F),
    /**
     * 线形文字A
     */
    LINEAR_A(0x10600, 0x1077F),
    /**
     * 塞浦路斯音节文字
     */
    CYPRIOT_SYLLABARY(0x10800, 0x1083F),
    /**
     * 帝国亚兰文字
     */
    IMPERIAL_ARAMAIC(0x10840, 0x1085F),
    /**
     * 帕尔迈拉字母
     */
    PALMYRENE(0x10860, 0x1087F),
    /**
     * 纳巴泰字母
     */
    NABATAEAN(0x10880, 0x108AF),
    /**
     * 哈特兰文
     */
    HATRAN(0x108E0, 0x108FF),
    /**
     * 腓尼基字母
     */
    PHOENICIAN(0x10900, 0x1091F),
    /**
     * 吕底亚字母
     */
    LYDIAN(0x10920, 0x1093F),
    /**
     * 麦罗埃文圣书体
     */
    MEROITIC_HIEROGLYPHS(0x10980, 0x1099F),
    /**
     * 麦罗埃文草书体
     */
    MEROITIC_CURSIVE(0x109A0, 0x109FF),
    /**
     * 佉卢文
     */
    KHAROSHTHI(0x10A00, 0x10A5F),
    /**
     * 古南阿拉伯字母
     */
    OLD_SOUTH_ARABIAN(0x10A60, 0x10A7F),
    /**
     * 古北阿拉伯字母
     */
    OLD_NORTH_ARABIAN(0x10A80, 0x10A9F),
    /**
     * 摩尼字母
     */
    MANICHAEAN(0x10AC0, 0x10AFF),
    /**
     * 阿维斯陀字母
     */
    AVESTAN(0x10B00, 0x10B3F),
    /**
     * 碑刻帕提亚文
     */
    INSCRIPTIONAL_PARTHIAN(0x10B40, 0x10B5F),
    /**
     * 碑刻巴列维文
     */
    INSCRIPTIONAL_PAHLAVI(0x10B60, 0x10B7F),
    /**
     * 诗篇巴列维文
     */
    PSALTER_PAHLAVI(0x10B80, 0x10BAF),
    /**
     * 古突厥文
     */
    OLD_TURKIC(0x10C00, 0x10C4F),
    /**
     * 哈乃斐罗兴亚文字
     */
    HANIFI_ROHINGYA(0x10D00, 0x10D3F),
    /**
     * 古匈牙利字母
     */
    OLD_HUNGARIAN(0x10C80, 0x10CFF),
    /**
     * 卢米文数字
     */
    RUMI_NUMERAL_SYMBOLS(0x10E60, 0x10E7F),
    /**
     * 古粟特字母
     */
    OLD_SOGDIAN(0x10F00, 0x10F2F),
    /**
     * 粟特字母
     */
    SOGDIAN(0x10F30, 0x10F6F),
    /**
     * 埃利迈斯字母
     */
    ELYMAIC(0x10FE0, 0x10FFF),
    /**
     * 婆罗米文字
     */
    BRAHMI(0x11000, 0x1107F),
    /**
     * 凯提文
     */
    KAITHI(0x11080, 0x110CF),
    /**
     * 索拉僧平文字
     */
    SORA_SOMPENG(0x110D0, 0x110FF),
    /**
     * 查克马文
     */
    CHAKMA(0x11100, 0x1114F),
    /**
     * 马哈佳尼文
     */
    MAHAJANI(0x11150, 0x1117F),
    /**
     * 夏拉达文
     */
    SHARADA(0x11180, 0x111DF),
    /**
     * 古僧伽罗文数字
     */
    SINHALA_ARCHAIC_NUMBERS(0x111E0, 0x111FF),
    /**
     * 可吉文
     */
    KHOJKI(0x11200, 0x1124F),
    /**
     * 穆尔塔尼文
     */
    MULTANI(0x11280, 0x112AF),
    /**
     * 库达瓦迪文
     */
    KHUDAWADI(0x112B0, 0x112FF),
    /**
     * 古兰塔文
     */
    GRANTHA(0x11300, 0x1137F),
    /**
     * 尼泊尔纽瓦字母
     */
    NEWA(0x11400, 0x1147F),
    /**
     * 提尔胡塔文
     */
    TIRHUTA(0x11480, 0x114DF),
    /**
     * 悉昙文字
     */
    SIDDHAM(0x11580, 0x115FF),
    /**
     * 莫迪文
     */
    MODI(0x11600, 0x1165F),
    /**
     * 蒙古文补充
     */
    MONGOLIAN_SUPPLEMENT(0x11660, 0x1167F),
    /**
     * 塔克里字母
     */
    TAKRI(0x11680, 0x116CF),
    /**
     * 阿洪姆文
     */
    AHOM(0x11700, 0x1173F),
    /**
     * 多格拉文
     */
    DOGRA(0x11800, 0x1184F),
    /**
     * 瓦兰齐地文
     */
    WARANG_CITI(0x118A0, 0x118FF),
    /**
     * 南迪那嘎黎文
     */
    NANDINAGARI(0x119A0, 0x119FF),
    /**
     * 札那巴札尔方形字母
     */
    ZANABAZAR_SQUARE(0x11A00, 0x11A4F),
    /**
     * 索永布文字
     */
    SOYOMBO(0x11A50, 0x11AAF),
    /**
     * 包钦豪文
     */
    PAU_CIN_HAU(0x11AC0, 0x11AFF),
    /**
     * 拜克舒基文
     */
    BHAIKSUKI(0x11C00, 0x11C6F),
    /**
     * 玛钦文
     */
    MARCHEN(0x11C70, 0x11CBF),
    /**
     * 马萨拉姆共地文字
     */
    MASARAM_GONDI(0x11D00, 0x11D5F),
    /**
     * 古吉拉共地文字
     */
    GUNJALA_GONDI(0x11D60, 0x11DAF),
    /**
     * 玛卡莎文
     */
    MAKASAR(0x11EE0, 0x11EFF),
    /**
     * 泰米尔文补充
     */
    TAMIL_SUPPLEMENT(0x11FC0, 0x11FFF),
    /**
     * 楔形文字
     */
    CUNEIFORM(0x12000, 0x123FF),
    /**
     * 楔形文字数字和标点符号
     */
    CUNEIFORM_NUMBERS_AND_PUNCTUATION(0x12400, 0x1247F),
    /**
     * 早期王朝楔形文字
     */
    EARLY_DYNASTIC_CUNEIFORM(0x12480, 0x1254F),
    /**
     * 埃及圣书体
     */
    EGYPTIAN_HIEROGLYPHS(0x13000, 0x1342F),
    /**
     * 埃及圣书体格式控制
     */
    EGYPTIAN_HIEROGLYPHS_FORMAT_CONTROLS(0x13430, 0x1343F),
    /**
     * 安纳托利亚象形文字
     */
    ANATOLIAN_HIEROGLYPHS(0x14400, 0x1467F),
    /**
     * 巴姆穆文字补充
     */
    BAMUM_SUPPLEMENT(0x16800, 0x16A3F),
    /**
     * 默文
     */
    MRO(0x16A40, 0x16A6F),
    /**
     * 巴萨哇文字
     */
    BASSA_VAH(0x16AD0, 0x16AFF),
    /**
     * 救世苗文
     */
    PAHAWH_HMONG(0x16B00, 0x16B8F),
    /**
     * 梅德法伊德林文
     */
    MEDEFAIDRIN(0x16E40, 0x16E9F),
    /**
     * 柏格理苗文
     */
    MIAO(0x16F00, 0x16F9F),
    /**
     * 表意符号和标点符号
     */
    IDEOGRAPHIC_SYMBOLS_AND_PUNCTUATION(0x16FE0, 0x16FFF),
    /**
     * 西夏文
     */
    TANGUT(0x17000, 0x187FF),
    /**
     * 西夏文部首
     */
    TANGUT_COMPONENTS(0x18800, 0x18AFF),
    /**
     * 日文假名补充
     */
    KANA_SUPPLEMENT(0x1B000, 0x1B0FF),
    /**
     * 日文假名扩展-A
     */
    KANA_EXTENDED_A(0x1B100, 0x1B12F),
    /**
     * 小假名扩充
     */
    SMALL_KANA_EXTENSION(0x1B130, 0x1B16F),
    /**
     * 女书
     */
    NUSHU(0x1B170, 0x1B2FF),
    /**
     * 杜普雷速记
     */
    DUPLOYAN(0x1BC00, 0x1BC9F),
    /**
     * 速记格式控制符
     */
    SHORTHAND_FORMAT_CONTROLS(0x1BCA0, 0x1BCAF),
    /**
     * 拜占庭音乐符号
     */
    BYZANTINE_MUSICAL_SYMBOLS(0x1D000, 0x1D0FF),
    /**
     * 音乐符号
     */
    MUSICAL_SYMBOLS(0x1D100, 0x1D1FF),
    /**
     * 古希腊音乐记号
     */
    ANCIENT_GREEK_MUSICAL_NOTATION(0x1D200, 0x1D24F),
    /**
     * 玛雅数字
     */
    MAYAN_NUMERALS(0x1D2E0, 0x1D2FF),
    /**
     * 太玄经符号
     */
    TAI_XUAN_JING_SYMBOLS(0x1D300, 0x1D35F),
    /**
     * 算筹
     */
    COUNTING_ROD_NUMERALS(0x1D360, 0x1D37F),
    /**
     * 数学字母数字符号
     */
    MATHEMATICAL_ALPHANUMERIC_SYMBOLS(0x1D400, 0x1D7FF),
    /**
     * 萨顿书写符号
     */
    SUTTON_SIGNWRITING(0x1D800, 0x1DAAF),
    /**
     * 格拉哥里字母补充
     */
    GLAGOLITIC_SUPPLEMENT(0x1E000, 0x1E02F),
    /**
     * 创世纪苗文
     */
    NYIAKENG_PUACHUE_HMONG(0x1E100, 0x1E14F),
    /**
     * 万秋文
     */
    WANCHO(0x1E2C0, 0x1E2FF),
    /**
     * 门地奇卡奎文
     */
    MENDE_KIKAKUI(0x1E800, 0x1E8DF),
    /**
     * 阿德拉姆字母
     */
    ADLAM(0x1E900, 0x1E95F),
    /**
     * 印度西亚克数字
     */
    INDIC_SIYAQ_NUMBERS(0x1EC70, 0x1ECBF),
    /**
     * 奥斯曼西亚克数字
     */
    OTTOMAN_SIYAQ_NUMBERS(0x1ED00, 0x1ED4F),
    /**
     * 阿拉伯字母数字符号
     */
    ARABIC_MATHEMATICAL_ALPHANUMERIC_SYMBOLS(0x1EE00, 0x1EEFF),
    /**
     * 麻将牌
     */
    MAHJONG_TILES(0x1F000, 0x1F02F),
    /**
     * 多米诺骨牌
     */
    DOMINO_TILES(0x1F030, 0x1F09F),
    /**
     * 扑克牌
     */
    PLAYING_CARDS(0x1F0A0, 0x1F0FF),
    /**
     * 带圈字母数字补充
     */
    ENCLOSED_ALPHANUMERIC_SUPPLEMENT(0x1F100, 0x1F1FF),
    /**
     * 带圈表意文字补充
     */
    ENCLOSED_IDEOGRAPHIC_SUPPLEMENT(0x1F200, 0x1F2FF),
    /**
     * 杂项符号和象形文字
     */
    MISCELLANEOUS_SYMBOLS_AND_PICTOGRAPHS(0x1F300, 0x1F5FF),
    /**
     * 表情符号
     */
    EMOTIONS(0x1F600, 0x1F64F),
    /**
     * 装饰符号
     */
    ORNAMENTAL_DINGBATS(0x1F650, 0x1F67F),
    /**
     * 交通和地图符号
     */
    TRANSPORT_AND_MAP_SYMBOLS(0x1F680, 0x1F6FF),
    /**
     * 炼金术符号
     */
    ALCHEMICAL_SYMBOLS(0x1F700, 0x1F77F),
    /**
     * 几何图形扩展
     */
    GEOMETRIC_SHAPES_EXTENDED(0x1F780, 0x1F7FF),
    /**
     * 追加箭头-C
     */
    SUPPLEMENTAL_ARROWS_C(0x1F800, 0x1F8FF),
    /**
     * 补充符号和象形文字
     */
    SUPPLEMENTAL_SYMBOLS_AND_PICTOGRAPHS(0x1F900, 0x1F9FF),
    /**
     * 国际象棋符号
     */
    CHESS_SYMBOLS(0x1FA00, 0x1FA6F),
    /**
     * 符号和象形文字扩展-A
     */
    SYMBOLS_AND_PICTOGRAPHS_EXTENDED_A(0x1FA70, 0x1FAFF),
    /**
     * 中日韩统一表意文字扩展B区
     */
    CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B(0x20000, 0x2A6DF),
    /**
     * 中日韩统一表意文字扩展C区
     */
    CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C(0x2A700, 0x2B73F),
    /**
     * 中日韩统一表意文字扩展D区
     */
    CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D(0x2B740, 0x2B81F),
    /**
     * 中日韩统一表意文字扩展E区
     */
    CJK_UNIFIED_IDEOGRAPHS_EXTENSION_E(0x2B820, 0x2CEAF),
    /**
     * 中日韩统一表意文字扩展F区
     */
    CJK_UNIFIED_IDEOGRAPHS_EXTENSION_F(0x2CEB0, 0x2EBEF),
    /**
     * 中日韩兼容表意文字增补
     */
    CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT(0x2F800, 0x2FA1F),
    /**
     * 标签
     */
    TAGS(0xE0000, 0xE007F),
    /**
     * 选择器变化补充
     */
    VARIATION_SELECTORS_SUPPLEMENT(0xE0100, 0xE01EF),
    ;
    private static CharCodeSet[] JAVA_CHAR_CODE_SET = null;
    private int min;
    private int max;
    private int count;
    private boolean bmp;

    CharCodeSet(int min, int max) {
        this.min = min;
        this.max = max;
        this.count = max - min + 1;
        this.bmp = this.max < 0x10000;
    }

    @SuppressWarnings("unused")
    public static CharCodeSet[] getBasicMultilingualPlane() {
        if (JAVA_CHAR_CODE_SET == null) {
            List<CharCodeSet> list = new ArrayList<>();
            for (CharCodeSet charCodeSet : CharCodeSet.class.getEnumConstants()) {
                if (charCodeSet.isBmp()) {
                    list.add(charCodeSet);
                }
            }

            JAVA_CHAR_CODE_SET = list.toArray(new CharCodeSet[0]);
        }
        return JAVA_CHAR_CODE_SET;
    }

    public boolean isBmp() {
        return bmp;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public int getCount() {
        return count;
    }

}
