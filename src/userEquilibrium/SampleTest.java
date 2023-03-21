package userEquilibrium;
import java.io.*;
public class SampleTest {
    int W;
    double Nt;
    double[] driver_afford_rate;
    double[] passenger_afford_rate;
    double[] size_rate;
    SUE sampleSUE;
    public SampleTest() {

    }
    public SampleTest(int W, double Nt, double[] driver_afford_rate, double[] passenger_afford_rate, double[] size_rate) {
        this.W = W;
        this.Nt = Nt;
        this.driver_afford_rate = driver_afford_rate;
        this.passenger_afford_rate = passenger_afford_rate;
        this.size_rate = size_rate;
    }
    public void generateDefault() {
        W = 3;
        Nt = 1500;
        driver_afford_rate = new double[]{0.3, 0.4 ,0.5};
        passenger_afford_rate = new double[]{0.5, 0.6, 0.7};
        size_rate = new double[]{0.2, 0.2, 0.6};
        sampleSUE = new SUE(W, Nt, size_rate, driver_afford_rate, passenger_afford_rate);
        sampleSUE.solveSUE();
    }
    public void generateSample2() {
        W = 3;
        Nt = 1500;
        size_rate = new double[]{0.8, 0.2, 0};
        driver_afford_rate = new double[]{0.3, 0.4 ,0.5};
        passenger_afford_rate = new double[]{0.5, 0.6, 0.7};
        String file_name = "size_rate.txt";
        double[] matching_sum = new double[100];
        double[] road_sum = new double[100];
        for (int i = 0; i < 100; i++) {
            size_rate[2] += 0.005;
            size_rate[0] -= 0.005;
            SUE sue = new SUE(W, Nt, size_rate, driver_afford_rate, passenger_afford_rate);
            sue.solveSUE();
            matching_sum[i] = sue.N_matching_sum;
            road_sum[i] = sue.N0;
        }
        write(file_name, matching_sum, road_sum);
    }
    public void generateSample3() {
        W = 2;
        Nt = 1500;
        size_rate = new double[]{0.5, 0.5};
        driver_afford_rate = new double[]{0.3, 0.4};
        passenger_afford_rate = new double[]{0.7, 0.5};
        int size = 200;
        double[] matching_rate_passenger = new double[size];
        double[] cost_passenger = new double[size];
        String file_name = "matching_rate_driver.txt";
        for(int i = 0; i < size; i++) {
            passenger_afford_rate[1] += (0.4 / (size));
            SUE sue = new SUE(W, Nt, size_rate, driver_afford_rate, passenger_afford_rate);
            sue.solveSUE();
            int index1 = 1;
            int index2 = 1;
            matching_rate_passenger[i] = sue.matching.solution.matching_rate_sum_passenger[index1];
            cost_passenger[i] = sue.cost[index1][3];
            //     matching_rate_driver[i] = test2.cost[index1][2];
            //     matching_rate_passenger[i] = test2.cost[index1][3];
        }
        write(file_name, matching_rate_passenger, cost_passenger);
    }
    public static void main(String[] args) {

        SampleTest test = new SampleTest();
        test.generateDefault();
  //  SUE test_sue = test.sampleSUE;
  //  test_sue.solveSUE();
  //  int i = 2;
  //  System.out.println("共乘司机匹配率:" + test_sue.matching.solution.matching_rate_sum_driver[i]);
  //  System.out.println("共乘乘客匹配率:" + test_sue.matching.solution.matching_rate_sum_passenger[i]);
  //  System.out.println("共乘司机出行成本：" + test_sue.cost[i][2]);
  //  System.out.println("共乘乘客出行成本：" + test_sue.cost[i][3]);
  //  System.out.println("实际选择公交人数：" + (test_sue.NR[0][0] + test_sue.NR[1][0] + test_sue.NR[2][0]));
  //  System.out.println("实际选择独驾人数：" + (test_sue.NR[0][1] + test_sue.NR[1][1] + test_sue.NR[2][1]));
  //  System.out.println("总匹配成功数：" + test_sue.N_matching_sum);
  //  System.out.println("道路车辆总数:  " + test_sue.N0);
  //  System.out.println("居民总出行成本：" + test_sue.sum_cost);


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
