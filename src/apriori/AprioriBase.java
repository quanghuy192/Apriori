package apriori;

import domain.Item;
import domain.MyItem;
import domain.Record;
import domain.Row;
import util.Utils;

import java.util.ArrayList;
import java.util.List;

public class AprioriBase implements Apriori<Row>, AprioriFindingSubChild_Thread.AprioriFindingSubChild, AprioriFindItemsChild_Thread.AprioriItemsChild {

    private List<Row> rows;
    private double supportMin;
    private double confidenceMin;
    private int size;

    private List<Row> dataResultItems, resultFilter;
    private List<Row> dataOriginalItems;
    private volatile List<Record> itemsRule;
    private volatile List<Row> dataItemsChild;
    private Utils utils = new Utils();

    public AprioriBase(double supportMin, double confidenceMin, List<Row> rawData) {
        this.supportMin = supportMin;
        this.confidenceMin = confidenceMin;
        this.dataOriginalItems = rawData;
        this.resultFilter = rawData;
        dataResultItems = new ArrayList<>();

        execute();
    }

    @Override
    public void execute() {

        itemsRule = new ArrayList<>();

        for (int i = 0; ; i++) {
            if (i == 0) {
                resultFilter = filterFirstTime(resultFilter);
            } else {
                size = dataOriginalItems.size();

                // Run with multi thread
                for (int j = 0; j < AprioriFindingSubChild_Thread.MULTI_THREAD; j++) {
                    AprioriFindingSubChild_Thread thread = new AprioriFindingSubChild_Thread(this, j, resultFilter);
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                int count = 0;

                // make clone from result data
                List<Row> dataResultItemsClone = cloneArray(dataResultItems);
                dataResultItems.clear();

                for (Record h : itemsRule) {
                    double percent = 1.0 * h.getQuantity();
                    List<Item> subList;
                    if (percent >= supportMin) {
                        subList = h.getItemList();
                        Record com = new Record(count, subList);
                        dataResultItems.add(com);
                        count++;
                    }
                }

                if (dataResultItems.size() > 0) {
                    dataResultItemsClone = cloneArray(dataResultItems);
                }

                dataItemsChild = new ArrayList<>();

                // Run with multi thread
                for (int k = 0; k < AprioriFindItemsChild_Thread.MULTI_THREAD; k++) {
                    AprioriFindItemsChild_Thread thread = new AprioriFindItemsChild_Thread(this, k, dataResultItems);
                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                resultFilter = cloneArray(dataItemsChild);

                if (dataItemsChild.size() > 1) {
                    // show(dataItemsChild);
                    System.out.println("Support min = " + supportMin);
                    System.out.println("Count : " + dataItemsChild.size() + " items \n");
                    show(dataItemsChild);
                    System.out.println("----------------------------------------");
                    System.out.println("----------------------------------------");
                    System.out.println("----------------------------------------");
                    System.out.println("----------------------------------------");
                    System.out.println("----------------------------------------");
                } else {
                    System.out.println("Count : " + dataResultItemsClone.size() + " items");
                    show(dataResultItemsClone);
                    System.out.print("------------------THE END----------------------");
                    break;
                }
            }
        }
    }

    private void show(List<Row> results){
        for (Row r : results) {
            for (Object o : r.getItemList()) {
                System.out.print("\t" + ((MyItem) o).getNameItem());
            }
            System.out.print("  --  quantity: " + ((Record) r).getQuantity());
            System.out.println("\n");
        }
    }

    private List<Row> filterFirstTime(List<Row> dataItemsParent) {
        List<Row> itemsFirst = new ArrayList<>();
        List<Item> atomItems = getChildItem(dataItemsParent);
        for (Item i : atomItems) {
            List<Item> lRow = new ArrayList<>();
            lRow.add(i);
            Row row = new Record(lRow);
            itemsFirst.add(row);
        }
        return itemsFirst;
    }

    private List<Item> getChildItem(List<Row> dataItemsParent) {
        List<Item> itemList = new ArrayList<>();
        for (Row row : dataItemsParent) {
            List<Item> itemValues = row.getItemList();
            itemList.addAll(utils.pruneDuplicateItem(itemValues));
        }
        return utils.pruneDuplicateItem(itemList);
    }

    private Record getRecord(List<Item> child, List<Record> records) {
        for (Record i : records) {
            Record temp = new Record(child);
            if (i.equals(temp)) {
                return i;
            }
        }
        return null;
    }

    private List<Row> cloneArray(List<Row> resultItems) {
        List<Row> dataResultItemsClone = new ArrayList<>();
        for (Row c : resultItems) {
            dataResultItemsClone.add(((Record) c).clone());
        }
        return dataResultItemsClone;
    }

    @Override
    public void findSubChild(List<Row> dataItemsParent) {

        List<Record> itemsRuleLocal = new ArrayList<>();
        itemsRule.clear();

        for (Row parent : dataOriginalItems) {
            for (Row child : dataItemsParent) {

                int count = 0; // if count equal size of transaction, delete tag
                if (parent.isDeleteTag()) {
                    break;
                }

                Record i = new Record(parent.getItemList(), child.getItemList());
                if (!itemsRuleLocal.contains(i)) {
                    i.setItemsParent(parent.getItemList());
                    itemsRuleLocal.add(i);
                    count++;
                } else {
                    Record clone = getRecord(child.getItemList(), itemsRuleLocal);
                    if (null != clone) {
                        clone.setItemsParent(parent.getItemList());
                    }
                }

                if (count == parent.getItemList().size()) {
                    parent.setDeleteTag(true);
                }
            }
        }

        itemsRule.addAll(itemsRuleLocal);
    }

    @Override
    public void getItemsChild(List<Row> items) {
        List<Row> dataItemsChildLocal = new ArrayList<>();
        List<Item> itemAtom = getChildItem(items);
        for (Row s : items) {
            for (Item a : itemAtom) {

                List<Item> temp = new ArrayList<>();
                temp.addAll(s.getItemList());
                if (temp.contains(a)) {
                    continue;
                }

                temp.add(a);
                Row complex = new Record(temp);

                if (!dataItemsChildLocal.contains(complex)) {
                    dataItemsChildLocal.add(complex);
                }
            }
        }
        dataItemsChild.addAll(dataItemsChildLocal);
    }
}
