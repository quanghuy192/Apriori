import apriori.AprioriBase;
import domain.Item;
import domain.Record;
import domain.Row;

import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {

        double SUPPORT_MIN = 2;      // min = 2
        double CONFIDENCE_MIN = 0.8;  // 80%

        List<Row> rawData = new ArrayList<>();

        MyItem m1 = new MyItem("A");
        MyItem m2 = new MyItem("C");
        MyItem m3 = new MyItem("D");
        List<Item> l1 = new ArrayList<>();
        l1.add(m1);
        l1.add(m2);
        l1.add(m3);
        Row r1 = new Record(l1);

        MyItem n1 = new MyItem("B");
        MyItem n2 = new MyItem("C");
        MyItem n3 = new MyItem("E");
        List<Item> l2 = new ArrayList<>();
        l2.add(n1);
        l2.add(n2);
        l2.add(n3);
        Row r2 = new Record(l2);

        MyItem p1 = new MyItem("A");
        MyItem p2 = new MyItem("B");
        MyItem p3 = new MyItem("C");
        MyItem p4 = new MyItem("E");
        List<Item> l3 = new ArrayList<>();
        l3.add(p1);
        l3.add(p2);
        l3.add(p3);
        l3.add(p4);
        Row r3 = new Record(l3);

        MyItem q1 = new MyItem("B");
        MyItem q2 = new MyItem("E");
        List<Item> l4 = new ArrayList<>();
        l4.add(q1);
        l4.add(q2);
        Row r4 = new Record(l4);

        rawData.add(r1);
        rawData.add(r2);
        rawData.add(r3);
        rawData.add(r4);

        new AprioriBase(SUPPORT_MIN, CONFIDENCE_MIN, rawData);
    }

}
