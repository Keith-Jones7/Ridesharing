package NestedLogit;

public class MatchingProb {

    double A = 0.6;
    double a = 3;
    double b = 2;
    double c = 10;


    
    int driver_class_count;
    int passenger_class_count;
    double[] driver_afford_rate;
    double[] passenger_afford_rate;
    int[][] matching_matrix;
    double[] driver_count;
    double[] passenger_count;
    Solution solution;


    /**
     * 构造函数
     * @param driver_afford_rate    司机承担比例
     * @param passenger_afford_rate 乘客承担比例
     * @param driver_count          司机对应人数
     * @param passenger_count       乘客对应人数
     */
    public MatchingProb(double[] driver_afford_rate, double[] passenger_afford_rate,
                        double[] driver_count, double[] passenger_count) {
        driver_class_count = driver_afford_rate.length;
        passenger_class_count = passenger_afford_rate.length;
        matching_matrix = new int[driver_class_count][passenger_class_count];
        for(int i = 0; i < driver_class_count; i++) {
            for(int j = 0; j < passenger_class_count; j++) {
                if((driver_afford_rate[i] + passenger_afford_rate[j] >= 1)) {
                    matching_matrix[i][j] = 1;
                }else {
                    matching_matrix[i][j] = 0;
                }
            }
        }
        this.driver_afford_rate = driver_afford_rate;
        this.passenger_afford_rate = passenger_afford_rate;
        this.driver_count = driver_count;
        this.passenger_count = passenger_count;
    }

    public void match() {
        solution = new Solution();
        solution.matching_rate_driver = new double[driver_class_count][passenger_class_count];
        solution.matching_rate_passenger = new double[passenger_class_count][driver_class_count];
        solution.matching_rate_sum_driver = new double[driver_class_count];
        solution.matching_rate_sum_passenger = new double[passenger_class_count];
        for(int i = 0; i < driver_class_count; i++) {
            for(int j = 0; j < passenger_class_count; j++) {
                solution.matching_rate_driver[i][j] = Prob_i_to_j(passenger_count[j], driver_count, i, j);
                solution.matching_rate_sum_driver[i] += solution.matching_rate_driver[i][j];
            }
        }
        for(int j = 0; j < passenger_class_count; j++) {
            for(int i = 0; i < driver_class_count; i++) {
                solution.matching_rate_passenger[j][i] = Prob_j_to_i(driver_count[i], passenger_count, i, j);
                solution.matching_rate_sum_passenger[j] += solution.matching_rate_passenger[j][i];
            }
        }
    }
    public double Prob_i_to_j(double N_j_p, double[] N_i_d, int i, int j) {
        double up = A * matching_matrix[i][j] * N_j_p;
        double down = a * N_j_p + c;
        for(int k = 0; k < driver_class_count; k++) {
            down += (b * matching_matrix[k][j] * N_i_d[k]);
        }
        return up / down;
    }

    public double Prob_j_to_i(double N_i_d, double[] N_j_p, int i, int j) {
        double up = A * matching_matrix[i][j] * N_i_d;
        double down = a * N_i_d + c;
        for(int k = 0; k < passenger_class_count; k++) {
            down += (b * matching_matrix[i][k] * N_j_p[k]);
        }
        return up / down;
    }
    public static void printMatrix(double[][] matrix) {
        for (double[] doubles : matrix) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(String.format("%.2f", doubles[j]) + "  \t");
            }
            System.out.println();
        }
    }
    public static void printArray(double[] array) {
        for (double v : array) {
            System.out.print(String.format("%.2f", v) + "  \t");
        }
        System.out.println();
    }
    public static void main(String[] args) {
        double[] driver_afford_rate = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
        double[] passenger_afford_rate = {0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0};
        double[] driver_count = {100, 80, 75, 65, 75, 85, 25, 26, 27, 10, 2};
        double[] passenger_count = {7, 10, 20, 51, 35, 79, 33, 35, 31, 15, 3};
        MatchingProb test = new MatchingProb(driver_afford_rate, passenger_afford_rate,
                driver_count, passenger_count);
        test.match();
        MatchingProb.printMatrix(test.solution.matching_rate_driver);
        System.out.println();
        MatchingProb.printArray(test.solution.matching_rate_sum_driver);
        System.out.println();
        MatchingProb.printMatrix(test.solution.matching_rate_passenger);
        System.out.println();
        MatchingProb.printArray(test.solution.matching_rate_sum_passenger);
    }
}
class Solution {
    double[][] matching_rate_driver;
    double[][] matching_rate_passenger;
    double[] matching_rate_sum_driver;
    double[] matching_rate_sum_passenger;
}
