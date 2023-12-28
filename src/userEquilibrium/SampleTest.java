package userEquilibrium;
import java.io.*;
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
    public void generateDefault() {
        W = 10;
        Nt = 1500;
        driverAffordRate = new double[]{1, 1 ,1, 1, 1, 1, 1, 1, 1, 1};
        passengerAffordRate = new double[]{1, 1 ,1, 1, 1, 1, 1, 1, 1, 1};
        sizeRate = new double[]{0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1, 0.1};
        sampleSUE = new SUE(W, Nt, sizeRate, driverAffordRate, passengerAffordRate);
        sampleSUE.solveSUE();
    }
    public void generateSample2() {
        W = 3;
        Nt = 1500;
        sizeRate = new double[]{0.8, 0.2, 0};
        driverAffordRate = new double[]{0.3, 0.4 ,0.5};
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
            road_sum[i] = sue.N0;
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
        double[] matching_rate_passenger = new double[size];
        double[] cost_passenger = new double[size];
        String file_name = "matching_rate_driver.txt";
        for(int i = 0; i < size; i++) {
            passengerAffordRate[1] += (0.4 / (size));
            SUE sue = new SUE(W, Nt, sizeRate, driverAffordRate, passengerAffordRate);
            sue.solveSUE();
            int index1 = 1;
            int index2 = 1;
            matching_rate_passenger[i] = sue.matching.solution.matchingRateSumPassenger[index1];
            cost_passenger[i] = sue.cost[index1][3];
            //     matching_rate_driver[i] = test2.cost[index1][2];
            //     matching_rate_passenger[i] = test2.cost[index1][3];
        }
        write(file_name, matching_rate_passenger, cost_passenger);
    }
    public static void main(String[] args) {
        SampleTest test2 = new SampleTest();
        test2.generateSample3();

//        SampleTest test = new SampleTest();
//        test.generateDefault();
//        SUE test_sue = test.sampleSUE;
//        test_sue.solveSUE();
//        int i = 0;
//        System.out.println("共乘司机匹配率:" + test_sue.matching.solution.matchingRateSumDriver[i]);
//        System.out.println("共乘乘客匹配率:" + test_sue.matching.solution.matchingRateSumPassenger[i]);
//        System.out.println("公共交通出行成本：" + test_sue.cost[i][0]);
//        System.out.println("独自驾驶出行成本：" + test_sue.cost[i][1]);
//        System.out.println("共乘司机出行成本：" + test_sue.cost[i][2]);
//        System.out.println("共乘乘客出行成本：" + test_sue.cost[i][3]);
//        System.out.println("实际选择公交人数：" + (test_sue.nr[0][0] + test_sue.nr[1][0] + test_sue.nr[2][0]));
//        System.out.println("实际选择独驾人数：" + (test_sue.nr[0][1] + test_sue.nr[1][1] + test_sue.nr[2][1]));
//        System.out.println("总匹配成功数：" + test_sue.nMatchingSum);
//        System.out.println("道路车辆总数:  " + test_sue.N0);
//        System.out.println("居民总出行成本：" + test_sue.sumCost);


    }
    public static void write(String file_name, double[] array) {
        try {
            File write_name = new File(file_name);
            write_name.createNewFile();
            try (FileWriter writer = new FileWriter(write_name);
            BufferedWriter out = new BufferedWriter(writer)){
                for(double num : array) {
                    out.write(String.format("%.2f", num) + "\r\n");
                }
                out.flush();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void write(String file_name, double[] array, double[] array1) {
        try {
            File write_name = new File(file_name);
            write_name.createNewFile();
            try (FileWriter writer = new FileWriter(write_name);
                 BufferedWriter out = new BufferedWriter(writer)){
                for(double num : array) {
                    out.write(String.format("%.4f", num) + "\r\n");
                }
                for(double num : array1) {
                    out.write(String.format("%.4f", num) + "\r\n");
                }
                out.flush();
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}
