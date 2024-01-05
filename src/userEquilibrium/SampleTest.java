package userEquilibrium;

import userEquilibrium.common.Param;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SampleTest {
    int W;
    double Nt;
    double[] driverAffordRate;
    double[] passengerAffordRate;
    double[] sizeRate;
    SUE sampleSUE;

    public SampleTest() {

    }

    public SampleTest(int W, double Nt, double[] driverAffordRate, double[] passengerAffordRate, double[] sizeRate) {
        this.W = W;
        this.Nt = Nt;
        this.driverAffordRate = driverAffordRate;
        this.passengerAffordRate = passengerAffordRate;
        this.sizeRate = sizeRate;
    }

    public static void main(String[] args) {
        Param.isFree = true;
//        SampleTest test2 = new SampleTest();
//        test2.generateSample3();

        SampleTest test = new SampleTest();
        test.generateDefault();
        SUE testSue = test.sampleSUE;
        testSue.solveSUE();
        int i = 1;
        System.out.println("共乘司机匹配率:" + testSue.matching.matchSolution.matchingRateSumDriver[i]);
        System.out.println("共乘乘客匹配率:" + testSue.matching.matchSolution.matchingRateSumPassenger[i]);
        System.out.println("公共交通出行成本：" + testSue.cost[i][0]);
        System.out.println("独自驾驶出行成本：" + testSue.cost[i][1]);
        System.out.println("共乘司机出行成本：" + testSue.cost[i][2]);
        System.out.println("共乘乘客出行成本：" + testSue.cost[i][3]);
        System.out.println("实际选择公交人数：" + (testSue.nr[0][0] + testSue.nr[1][0] + testSue.nr[2][0]));
        System.out.println("实际选择独驾人数：" + (testSue.nr[0][1] + testSue.nr[1][1] + testSue.nr[2][1]));
        System.out.println("总匹配成功数：" + testSue.nMatchingSum);
        System.out.println("道路车辆总数:  " + testSue.nc);
        System.out.println("居民总出行成本：" + testSue.sumCost);


    }

    public static void write(String fileName, double[] array) {
        try {
            File writeName = new File(fileName);
            writeName.createNewFile();
            try (FileWriter writer = new FileWriter(writeName);
                 BufferedWriter out = new BufferedWriter(writer)) {
                for (double num : array) {
                    out.write(String.format("%.2f", num) + "\r\n");
                }
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void write(String fileName, double[] array, double[] array1) {
        try {
            File writeName = new File(fileName);
            writeName.createNewFile();
            try (FileWriter writer = new FileWriter(writeName);
                 BufferedWriter out = new BufferedWriter(writer)) {
                for (double num : array) {
                    out.write(String.format("%.4f", num) + "\r\n");
                }
                for (double num : array1) {
                    out.write(String.format("%.4f", num) + "\r\n");
                }
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void generateDefault() {
        W = 3;
        Nt = 1500;
//        driverAffordRate = new double[]{0.1, 0.5 ,0.3, 0.4, 0.4, 0.1, 0.5 ,0.3, 0.4, 0.4};
//        passengerAffordRate = new double[]{0.1, 0.5 ,0.3, 0.4, 0.4, 0.1, 0.5 ,0.3, 0.4, 0.4};
//        sizeRate = new double[]{0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1};
        driverAffordRate = new double[]{0.1, 0.1, 0.1};
        passengerAffordRate = new double[]{0.1, 0.1, 0.1};
        sizeRate = new double[]{1.0 / 3, 1.0 / 3, 1.0 / 3};
        sampleSUE = new SUE(W, Nt, sizeRate, driverAffordRate, passengerAffordRate);
//        sampleSUE.solveSUE();
    }

    public void generateSample2() {
        W = 3;
        Nt = 1500;
        sizeRate = new double[]{0.8, 0.2, 0};
        driverAffordRate = new double[]{0.3, 0.4, 0.5};
        passengerAffordRate = new double[]{0.5, 0.6, 0.7};
        String file_name = "size_rate.txt";
        double[] matching_sum = new double[100];
        double[] road_sum = new double[100];
        for (int i = 0; i < 100; i++) {
            sizeRate[2] += 0.005;
            sizeRate[0] -= 0.005;
            SUE sue = new SUE(W, Nt, sizeRate, driverAffordRate, passengerAffordRate);
            sue.solveSUE();
            matching_sum[i] = sue.nMatchingSum;
            road_sum[i] = sue.nc;
        }
        write(file_name, matching_sum, road_sum);
    }

    public void generateSample3() {
        W = 2;
        Nt = 1500;
        sizeRate = new double[]{0.5, 0.5};
        driverAffordRate = new double[]{0.3, 0.4};
        passengerAffordRate = new double[]{0.7, 0.5};
        int size = 200;
        double[] matchingRatePassenger = new double[size];
        double[] costPassenger = new double[size];
        String file_name = "matching_rate_driver.txt";
        driverAffordRate[1] = 0;
        for (int i = 0; i < size; i++) {
            driverAffordRate[1] += (0.4 / (size));
            SUE sue = new SUE(W, Nt, sizeRate, driverAffordRate, passengerAffordRate);
            sue.solveSUE();
            int index1 = 1;
            int index2 = 1;
            matchingRatePassenger[i] = sue.matching.matchSolution.matchingRateSumDriver[index1];
            costPassenger[i] = sue.cost[index1][2];
            //     matching_rate_driver[i] = test2.cost[index1][2];
            //     matchingRatePassenger[i] = test2.cost[index1][3];
        }
        write(file_name, matchingRatePassenger, costPassenger);
    }
}
