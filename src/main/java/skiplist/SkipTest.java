package skiplist;

import java.util.Random;

/**
 * @author rafa gao
 */


public class SkipTest {


    public static void main(String[] args) {
        SkipList<Object> skipList = new SkipList<>();
        Random random = new Random();
        for (int i = 0; i < 100; i++) {
            double score = random.nextDouble();
            skipList.add(score);
        }

        skipList.toString();

    }

}
