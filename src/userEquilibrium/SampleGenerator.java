package userEquilibrium;

import java.util.Random;

//
public class SampleGenerator {
    public static int maxCount = 100;
    public double[] driverCount;
    public double[] passengerCount;
    public double[] driverAffordRate;
    public double[] passengerAffordRate;

    public SampleGenerator(int driverClassCount, int passengerClassCount) {
        this.driverCount = new double[driverClassCount];
        this.passengerCount = new double[passengerClassCount];
        this.driverAffordRate = new double[]{0.4};
        this.passengerAffordRate = new double[]{0.6};
    }

    /**
     * @param maxCount 出行人数随机阈值
     */
    public void setLimit(int maxCount) {
        SampleGenerator.maxCount = maxCount;
    }

    /**
     * @param _default 是否为默认算例
     */
    public void generate(boolean _default) {
        if (_default) {
            generateDefault();
            return;
        }
        Random random = new Random(100);
        for (int i = 0; i < driverCount.length; i++) {
            driverCount[i] = random.nextInt(maxCount);
        }
        for (int i = 0; i < passengerCount.length; i++) {
            passengerCount[i] = random.nextInt(maxCount);
        }
    }

    public void generateDefault() {
        this.driverCount = new double[]{0.001};
        this.passengerCount = new double[]{1};
    }

    public void printSample() {
        System.out.println("司机对应类别及其数为:");
        for (double rate : driverAffordRate) {
            System.out.print(rate + "   \t");
        }
        System.out.println();
        for (double count : driverCount) {
            System.out.print(count + "\t");
        }
        System.out.println();
        System.out.println("乘客对应类别及其数为:");
        for (double rate : passengerAffordRate) {
            System.out.print(rate + "   \t");
        }
        System.out.println();
        for (double count : passengerCount) {
            System.out.print(count + "\t");
        }
        System.out.println();
    }
}
