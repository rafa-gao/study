package licai;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * @author rafa gao
 */
public class CalculateWealth {
    // 税前
    private static BigDecimal curSalaryMonthly = new BigDecimal(20000);
    // 税后收入
    private static BigDecimal curSalaryMonthlyAfterTex = curSalaryMonthly.multiply(BigDecimal.valueOf(0.736));
    // 每月支出
    private static BigDecimal expenditureMonthly = BigDecimal.valueOf(2000 + 1260 + 200 + 500);
    // 实际可支配收入
    private static BigDecimal moneyToUseMonthly = curSalaryMonthlyAfterTex.subtract(expenditureMonthly);
    // 期限
    private static int monthCount = 12 * 5+28+25;
    // 规定时间后的资产
    private static BigDecimal moneyBySalary = moneyToUseMonthly.multiply(BigDecimal.valueOf(monthCount));
    // 每月收益率
    private static BigDecimal rate = BigDecimal.valueOf(0.04298);

    /**
     * 五年后资产达到300万
     * <p>
     * 大概每个月需要4.298%的收益率
     * <p>
     * 在第十三个月的时候，投资收入超过月薪
     * <p>
     * 在第52个月，投资月入十万
     */
    private static void arm300() {

        double arm = 3000000;

        // 每月实际可支配收入
        BigDecimal moneyToUse = CalculateWealth.moneyToUseMonthly;
        // 真实收益率
        BigDecimal rateActual = rate.add(BigDecimal.valueOf(1));
        // 总资产
        BigDecimal sum = BigDecimal.valueOf(0);

        for (int i = 0; i < monthCount; i++) {
            sum = sum.add(moneyToUse);
            System.err.println("第" + (i+1) + "个月-----------------------------");
            sum = methodTo300(sum);
        }
        System.out.println("本金：" + moneyBySalary);
        System.out.println("五年后总资产为：" + sum.toPlainString());
        System.out.println("五年收益率：" + sum.divide(moneyBySalary,10, RoundingMode.HALF_UP).subtract(BigDecimal.valueOf(1)));

    }

    /**
     * 该方案的目标
     * 5年后资产达到300万
     *
     * 每月目标：如何实现每月4.298%的收益率
     *
     * 月收入：20000
     * 税后：14720
     * 开支：2000（房租） 1260（吃饭） 200（交通）500（其它）总计：3960
     * 每月可以用于理财的资金： 10760
     *
     * 每月：8%-->定期（0.3583%）
     *     17%-->基金（1.25%）
     *     75%-->股票（5.41%）
     * 平均收益率：4.29867%
     *
     * 如此重复计算复利，五年后即可达成300万资产的目标
     *
     * @param moneyToUse 可支配基数
     */
    private static BigDecimal  methodTo300(BigDecimal moneyToUse) {

        // 打印格式
        DecimalFormat df1 = new DecimalFormat("0.0000");
        DecimalFormat df2 = new DecimalFormat("0.00000");

        // 定期收益率
        BigDecimal regularRate = new BigDecimal(0.043 / 12);
        // 用于投资定期的资金
        BigDecimal regular = moneyToUse.multiply(new BigDecimal(0.08));
        System.out.println("每月投资定期：" + df1.format(regular));
        System.out.println("每月投资定期收益：" + df1.format(regular.multiply(regularRate)) + "\n");

        // 基金收益率
        BigDecimal fundRate = new BigDecimal(0.0125);
        // 用于投资基金的资金
        BigDecimal fund = moneyToUse.multiply(new BigDecimal(0.17));
        System.out.println("每月投资基金：" + df1.format(fund));
        System.out.println("每月投资基金收益：" + df1.format(fund.multiply(fundRate)) + "\n");

        // 股票收益率
        BigDecimal stockRate = new BigDecimal(5.41/100);
        BigDecimal stock = moneyToUse.multiply(new BigDecimal(0.75));
        System.out.println("每月投资股票：" + df1.format(stock));
        System.out.println("每月投资股票收益：" + df1.format(stock.multiply(stockRate)) + "\n");

        // 汇总
        BigDecimal base = BigDecimal.valueOf(1);
        // 每月总的投资收益
        BigDecimal gain = regular.multiply(regularRate).add(fund.multiply(fundRate)).add(stock.multiply(stockRate));
        BigDecimal sum = moneyToUse.add(gain);

        System.out.println("每月收入：" + df1.format(moneyToUse));
        System.out.println("每月投资收入：" + df1.format(gain));
        System.out.println("每月投资后总收入：" + df1.format(sum));
        // 每月总的投资收益率
        BigDecimal sumRate = sum.divide(moneyToUse, 10, RoundingMode.HALF_UP).subtract(base).multiply(BigDecimal.valueOf(100));
        System.out.println("每月投资收益率：" + df2.format(sumRate)+"%" + "\n");

        return sum;
    }

    public static void main(String[] args) {
        arm300();
//        methodTo300(CalculateWealth.moneyToUseMonthly.multiply(BigDecimal.valueOf(60)));

    }

}
