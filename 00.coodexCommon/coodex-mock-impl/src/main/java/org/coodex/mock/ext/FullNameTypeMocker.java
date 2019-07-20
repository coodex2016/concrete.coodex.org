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

package org.coodex.mock.ext;

import org.coodex.mock.AbstractTypeMocker;
import org.coodex.util.Common;

import java.lang.reflect.Type;

public class FullNameTypeMocker extends AbstractTypeMocker<FullName> {
    /**
     * 姓。数据来源 https://baijiaxing.51240.com/
     */
    public static final String[] SURNAME = {
            "赵", "钱", "孙", "李", "周", "吴", "郑", "王",
            "冯", "陈", "褚", "卫", "蒋", "沈", "韩", "杨",
            "朱", "秦", "尤", "许", "何", "吕", "施", "张",
            "孔", "曹", "严", "华", "金", "魏", "陶", "姜",
            "戚", "谢", "邹", "喻", "柏", "水", "窦", "章",
            "云", "苏", "潘", "葛", "奚", "范", "彭", "郎",
            "鲁", "韦", "昌", "马", "苗", "凤", "花", "方",
            "俞", "任", "袁", "柳", "酆", "鲍", "史", "唐",
            "费", "廉", "岑", "薛", "雷", "贺", "倪", "汤",
            "滕", "殷", "罗", "毕", "郝", "邬", "安", "常",
            "乐", "于", "时", "傅", "皮", "卞", "齐", "康",
            "伍", "余", "元", "卜", "顾", "孟", "平", "黄",
            "和", "穆", "萧", "尹", "姚", "邵", "舒", "汪",
            "祁", "毛", "禹", "狄", "米", "贝", "明", "臧",
            "计", "伏", "成", "戴", "谈", "宋", "茅", "庞",
            "熊", "纪", "屈", "项", "祝", "董", "杜", "阮",
            "蓝", "闵", "席", "季", "麻", "强", "贾", "路",
            "娄", "危", "江", "童", "颜", "郭", "梅", "盛",
            "林", "刁", "钟", "徐", "邱", "骆", "高", "夏",
            "蔡", "田", "樊", "胡", "凌", "霍", "虞", "万",
            "支", "柯", "咎", "管", "卢", "莫", "经", "房",
            "裘", "缪", "干", "解", "应", "宗", "宣", "丁",
            "贲", "邓", "郁", "单", "杭", "洪", "包", "诸",
            "左", "石", "崔", "吉", "钮", "龚", "程", "嵇",
            "邢", "滑", "裴", "陆", "荣", "翁", "荀", "羊",
            "於", "惠", "甄", "加", "封", "芮", "羿", "储",
            "靳", "汲", "邴", "糜", "松", "井", "段", "富",
            "巫", "乌", "焦", "巴", "弓", "牧", "隗", "山",
            "谷", "车", "侯", "宓", "蓬", "全", "郗", "班",
            "仰", "秋", "仲", "伊", "宫", "宁", "仇", "栾",
            "暴", "甘", "钭", "厉", "戎", "祖", "武", "符",
            "刘", "詹", "束", "龙", "叶", "幸", "司", "韶",
            "郜", "黎", "蓟", "薄", "印", "宿", "白", "怀",
            "蒲", "台", "从", "鄂", "索", "咸", "籍", "赖",
            "卓", "蔺", "屠", "蒙", "池", "乔", "阴", "胥",
            "能", "苍", "双", "闻", "莘", "党", "翟", "谭",
            "贡", "劳", "逄", "姬", "申", "扶", "堵", "冉",
            "宰", "郦", "雍", "璩", "桑", "桂", "濮", "牛",
            "寿", "通", "边", "扈", "燕", "冀", "郏", "浦",
            "尚", "农", "温", "别", "庄", "晏", "柴", "瞿",
            "阎", "充", "慕", "连", "茹", "习", "宦", "艾",
            "鱼", "容", "向", "古", "易", "慎", "戈", "廖",
            "庚", "终", "暨", "居", "衡", "步", "都", "耿",
            "满", "弘", "匡", "国", "文", "寇", "广", "禄",
            "阙", "东", "殳", "沃", "利", "蔚", "越", "夔",
            "隆", "师", "巩", "厍", "聂", "晁", "勾", "敖",
            "融", "冷", "訾", "辛", "阚", "那", "简", "饶",
            "空", "曾", "毋", "沙", "乜", "养", "鞠", "须",
            "丰", "巢", "关", "蒯", "相", "查", "后", "红",
            "游", "竺", "权", "逯", "盖", "益", "桓", "公",
            "晋", "楚", "法", "汝", "鄢", "涂", "钦", "缑",
            "亢", "况", "有", "商", "牟", "佘", "佴", "伯",
            "赏", "墨", "哈", "谯", "笪", "年", "爱", "阳",
            "佟", "琴", "言", "福", "岳", "帅",
            "梁丘", "左丘", "东门", "百里", "东郭", "南门", "呼延", "万俟",
            "南宫", "段干", "西门", "司马", "上官", "欧阳", "夏侯", "诸葛",
            "闻人", "东方", "赫连", "皇甫", "尉迟", "公羊", "澹台", "公冶",
            "宗政", "濮阳", "淳于", "仲孙", "太叔", "申屠", "公孙", "乐正",
            "轩辕", "令狐", "钟离", "闾丘", "长孙", "慕容", "鲜于", "宇文",
            "司徒", "司空", "亓官", "司寇", "子车", "颛孙", "端木", "巫马",
            "公西", "漆雕", "壤驷", "公良", "夹谷", "宰父", "微生", "羊舌"
    };
    private static Class[] SUPPORTED = new Class[]{String.class};

    @Override
    protected Class[] getSupportedClasses() {
        return SUPPORTED;
    }

    @Override
    protected boolean accept(FullName annotation) {
        return annotation != null;
    }

    @Override
    public Object mock(FullName mockAnnotation, Type targetType) {
        StringBuilder builder = new StringBuilder(SURNAME[Common.random(SURNAME.length - 1)]);
        builder.append(Common.randomGB2312Char());
        return Math.random() < 0.5 ? builder.toString() : builder.append(Common.randomGB2312Char()).toString();
    }
}
