package userEquilibrium;

import java.util.Random;

//
public class SampleGenerator {
    public double[] driver_count;
    public double[] passenger_count;
    public double[] driver_afford_rate;
    public double[] passenger_afford_rate;
    public static int maxCount = 100;
    public SampleGenerator(int driver_class_count, int passenger_class_count) {
        this.driver_count = new double[driver_class_count];
        this.passenger_count = new double[passenger_class_count];
        this.driver_afford_rate = new double[]{0.4};
        this.passenger_afford_rate = new double[]{0.6};
    }

    /**
     *
     * @param maxCount 出行人数随机阈值
     */
    public void setLimit(int maxCount) {
        SampleGenerator.maxCount = maxCount;
    }

    /**
     *
     * @param _default 是否为默认算例
     */
    public void generate(boolean _default) {
        if(_default) {
            generateDefault();
            return;
        }
        Random random = new Random(100);
        for(int i = 0; i < driver_count.length; i++) {
            driver_count[i] = random.nextInt(maxCount);
        }
        for(int i = 0; i < passenger_count.length; i++) {
            passenger_count[i] = random.nextInt(maxCount);
        }
    }
    public void generateDefault() {
        this.driver_count = new double[]{0.001};
        this.passenger_count = new double[]{1};
    }
    public void printSample() {
        System.out.println("司机对应类别及其数为:");
        for(double rate : driver_afford_rate) {
            System.out.print(rate + "   \t");
        }
        System.out.println();
        for(double count : driver_count) {
            System.out.print(count + "\t");
        }
        System.out.println();
        System.out.println("乘客对应类别及其数为:");
        for(double rate : passenger_afford_rate) {
            System.out.print(rate + "   \t");
        }
        System.out.println();
        for(double count : passenger_count) {
            System.out.print(count + "\t");
        }
        System.out.println();
    }
}
