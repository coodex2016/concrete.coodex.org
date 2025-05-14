# Section

一维空间(必须是Comarable的)上的线段，可以进行合并，减法，交集等操作。

```java
import org.coodex.util.Section;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;

public class IntSection extends Section<Integer> {
    public static final Builder<Integer, IntSection> builder = IntSection::new;

    protected IntSection(Integer start, Integer end) {
        super(start, end);
    }


    private static void trace(List<IntSection> list) {
        if (list == null || list.size() == 0) {
            System.out.println("empty");
            return;
        }
        StringJoiner joiner = new StringJoiner(", ");
        list.forEach(section -> joiner.add(section.toString()));
        System.out.println(joiner.toString());
    }

    public static void main(String[] args) {
        IntSection section1 = builder.create(0, 3);// 创建一个[0,3]的线段
        IntSection section2 = builder.create(5, 7); //创建一个[5,7]的线段
        IntSection section3 = builder.create(1, 6);// 创建一个[1,6]的线段
        IntSection section4 = builder.create(-1, 10);//创建[-1.10]的线段

        // 合并
        trace(Section.merge(Arrays.asList(section1, section2), builder));
        trace(Section.merge(Arrays.asList(section1, section2, section3), builder));

        // 交集
        trace(Section.intersect(Collections.singletonList(section1), Collections.singletonList(section2), builder));
        trace(Section.intersect(Arrays.asList(section1, section2), Collections.singletonList(section3), builder));

        // 减
        trace(Section.sub(Collections.singletonList(section4), Arrays.asList(section1, section2), builder));
    }

    @Override
    public String toString() {
        return "[" + getStart() + ", " + getEnd() + "]";
    }
}
```

```txt
[0, 3], [5, 7]
[0, 7]
empty
[1, 3], [5, 6]
[-1, 0], [3, 5], [7, 10]
```
