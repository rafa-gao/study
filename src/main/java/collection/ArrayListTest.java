package collection;

import java.lang.reflect.Field;
import java.util.ArrayList;

/**
 * 测试空的ArrayList和手动指定长度为0的ArrayList有什么不同
 * @author rafa gao
 */
public class ArrayListTest {

    /**
     * 测试无参构造器的ArrayList和手动指定长度为0的ArrayList有什么不同
     *
     * 结论：当参数为0或者使用无参构造器的时候的时候，并不会分配内存。
     *      但是参数为0和午餐狗在其的区别在于：
     *          使用无参构造器，第一次使用放入元素的时候，分配的数组长度为10
     *          使用有参构造器时，分配的数组长度为1
     *
     * res: defaultList(无参构造器):10
     *      emptyList(参数为0):1
     */
    private static void compareArrayListWithConstrParam() {
        // 直接创建
        ArrayList<Object> defaultList = new ArrayList<>();
        // 参数为0的情况创建
        ArrayList<Object> emptyList = new ArrayList<>(0);
        defaultList.add(null);
        emptyList.add(null);
        System.out.println("defaultList(无参构造器):"+getAccessibleElementDataInArrayList(defaultList).length);
        System.out.println("emptyList(参数为0):"+getAccessibleElementDataInArrayList(emptyList).length);
    }

    /**
     * 测试ArrayList何时扩容
     *
     * 结论：在数组完全满了之后扩容，扩容长度是原来长度的1.5倍
     * @see ArrayList#grow(int)
     * 扩容时计算新长度的源码：int newCapacity = oldCapacity + (oldCapacity >> 1);
     */
    @SuppressWarnings("JavadocReference")
    private static void whenToGrowCap() {
        int originSize = 5;
        ArrayList<Object> arrayList = new ArrayList<>(originSize);
        Object[] elementData = getAccessibleElementDataInArrayList(arrayList);
        int count = 0;
        while (true) {
            arrayList.add(new Object());
            count++;
            // 扩容了
            Object[] elementDataNew = getAccessibleElementDataInArrayList(arrayList);
            if (elementDataNew != elementData) {
                String msg = String.format("在添加第[%s]个元素的时候扩容了,原长度:[%s],现长度:[%s]",
                        count,
                        elementData.length,
                        elementDataNew.length);
                System.out.println(msg);
                break;
            }
        }
    }

    /**
     * 获取ArrayList中可使用的elementData
     *
     * @return elementData
     */
    private static Object[] getAccessibleElementDataInArrayList(Object arrayListObj){
        Class<ArrayList> arrayListClass = ArrayList.class;
        try {
            Field elementData = arrayListClass.getDeclaredField("elementData");
            elementData.setAccessible(true);
            return (Object[]) elementData.get(arrayListObj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        compareArrayListWithConstrParam();
        whenToGrowCap();
    }

}
