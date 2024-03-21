package userEquilibrium;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SampleTest {

    public static void main(String[] args) {
//        runChapter3Example1();
//        runChapter3Example2();
//        runChapter3Example3();
//        runChapter4Example1();
        runChapter4Example2();
    }

    /**
     * 第三章算例1
     */
    public static void runChapter3Example1() {
        int W = 3;
        double Nt = 1500;
        double[] sizeRate = new double[]{1.0 / 3, 1.0 / 3, 1.0 / 3};

        double[] driverAffordRateFixed = new double[]{0.3, 0.4, 0.4};
        double[] passengerAffordRateFixed = new double[]{0.5, 0.6, 0.6};
        double[] driverAffordRate = new double[]{0.3, 0.4, 0.5};
        double[] passengerAffordRate = new double[]{0.5, 0.6, 0.7};

        SUE sueFixed = new SUE(W, Nt, sizeRate, driverAffordRateFixed, passengerAffordRateFixed);
        SUE sue = new SUE(W, Nt, sizeRate, driverAffordRate, passengerAffordRate);

        sueFixed.solveSUE();
        sue.solveSUE();

        matchSolution solutionFixed = sueFixed.matching.matchSolution;
        matchSolution solution = sue.matching.matchSolution;

        double[][] sueNaFixedT = transpose(sueFixed.na);
        double[][] sueNaT = transpose(sue.na);

        double[][] costFixedT = transpose(sueFixed.cost);
        double[][] costT = transpose(sue.cost);

        String[] headers1 = {"出行方式", "意愿成为共乘司机", "意愿成为共乘乘客"};
        String[] headers2 = {"出行方式", "共乘司机总匹配率", "共乘乘客总匹配率"};
        String[] headers3 = {"出行方式", "共乘司机期望成本", "共乘乘客期望成本"};

        saveFormatTable("Chapter3", "Example1", "num",
                headers1, sueNaFixedT[2], sueNaT[2], sueNaFixedT[3], sueNaT[3]);
        saveFormatTable("Chapter3", "Example1", "rate",
                headers2, solutionFixed.matchingRateSumDriver, solution.matchingRateSumDriver,
                solutionFixed.matchingRateSumPassenger, solution.matchingRateSumPassenger);
        saveFormatTable("Chapter3", "Example1", "cost",
                headers3, costFixedT[2], costT[2], costFixedT[3], costT[3]);

        double nsFixed = sueFixed.nc - sueFixed.nMatchingSum;
        double ns = sue.nc - sue.nMatchingSum;
        double nbFixed = sueFixed.Nt - nsFixed - 2 * sueFixed.nMatchingSum;
        double nb = sue.Nt - ns - 2 * sue.nMatchingSum;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Chapter3/Example1/all.txt"))) {
            writer.write(String.format("公交总人数\t%.0f\t%.0f", nbFixed, nb));
            writer.newLine();
            writer.write(String.format("独驾总人数\t%.0f\t%.0f", nsFixed, ns));
            writer.newLine();
            writer.write(String.format("总匹配成功数\t%.0f\t%.0f", sueFixed.nMatchingSum, sue.nMatchingSum));
            writer.newLine();
            writer.write(String.format("道路车辆总数\t%.0f\t%.0f", sueFixed.nc, sue.nc));
            writer.newLine();
            writer.write(String.format("居民总出行成本\t%.2f\t%.2f", sueFixed.sumCost, sue.sumCost));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 第三章算例2
     */
    public static void runChapter3Example2() {
        int W = 3;
        double Nt = 1500;
        double[] sizeRate = new double[]{0.8, 0.2, 0};

        double[] driverAffordRate = new double[]{0.3, 0.4, 0.5};
        double[] passengerAffordRate = new double[]{0.5, 0.6, 0.7};
        double[] driverAffordRateFixed = new double[]{0.3, 0.4, 0.4};
        double[] passengerAffordRateFixed = new double[]{0.5, 0.6, 0.6};

        int size = 100;
        double[] sizeRates = new double[size];
        double[] matchingSum = new double[size];
        double[] roadSum = new double[size];
        double[] matchingSumFixed = new double[size];
        double[] roadSumFixed = new double[size];

        for (int i = 0; i < size; i++) {
            sizeRate[0] -= (0.5 / size);
            sizeRate[2] += (0.5 / size);
            sizeRates[i] = sizeRate[2];

            SUE sue = new SUE(W, Nt, sizeRate, driverAffordRate, passengerAffordRate);
            sue.solveSUE();
            matchingSum[i] = sue.nMatchingSum;
            roadSum[i] = sue.nc;

            SUE sueFixed = new SUE(W, Nt, sizeRate, driverAffordRateFixed, passengerAffordRateFixed);
            sueFixed.solveSUE();
            matchingSumFixed[i] = sueFixed.nMatchingSum;
            roadSumFixed[i] = sueFixed.nc;
        }
        saveResults("Chapter3", "Example2", "size_rate", sizeRates);
        saveResults("Chapter3", "Example2", "matching_sum", matchingSum);
        saveResults("Chapter3", "Example2", "road_sum", roadSum);
        saveResults("Chapter3", "Example2", "matching_sum_fixed", matchingSumFixed);
        saveResults("Chapter3", "Example2", "road_sum_fixed", roadSumFixed);
    }

    /**
     * 第三章算例3
     */
    public static void runChapter3Example3() {
        int W = 2;
        double Nt = 1500;
        double[] sizeRate = new double[]{0.5, 0.5};

        double[] driverAffordRate = new double[]{0.3, 0.4};
        double[] passengerAffordRate = new double[]{0.7, 0.5};

        int size = 200;
        double[] passengerAffordRates = new double[size];
        double[] matchingRatePassenger = new double[size];
        double[] costPassenger = new double[size];

        for (int i = 0; i < size; i++) {
            passengerAffordRate[1] += (0.4 / size);
            passengerAffordRates[i] = passengerAffordRate[1];
            SUE sue = new SUE(W, Nt, sizeRate, driverAffordRate, passengerAffordRate);
            sue.solveSUE();
            matchingRatePassenger[i] = sue.matching.matchSolution.matchingRateSumPassenger[1];
            costPassenger[i] = sue.cost[1][3];
        }
        saveResults("Chapter3", "Example3", "passenger_afford_rate", passengerAffordRates);
        saveResults("Chapter3", "Example3", "matching_rate_passenger", matchingRatePassenger);
        saveResults("Chapter3", "Example3", "cost_passenger", costPassenger);
    }


    public static void runChapter4Example1() {
        Param.isContinuous = true;
        Param.isFree = true;
        Param.ALPHA[0] = 50;
        Param.ALPHA[1] = 60;
        Param.ALPHA[2] = 70;

        int W = 3;
        double Nt = 1500;
        double[] sizeRate = new double[]{1.0 / 3, 1.0 / 3, 1.0 / 3};

        double[] driverAffordRate = new double[]{0.1, 0.1, 0.1};
        double[] passengerAffordRate = new double[]{0.1, 0.1, 0.1};

        SUE sue = new SUE(W, Nt, sizeRate, driverAffordRate, passengerAffordRate);
        sue.solveSUE();

        double[][] prob = sue.prob;
        double[][] cost = sue.cost;

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Chapter4/Example1/cost.txt"))) {
            writer.write("公共交通\t独自驾驶\t共乘司机\t共乘乘客");
            writer.newLine();
            for (int i = 0; i < W; i++) {
                writer.write(String.format("%.3f（%.1f）\t%.3f（%.1f）\t%.3f（%.1f）\t%.3f（%.1f）",
                        prob[i][0], cost[i][0], prob[i][1], cost[i][1], prob[i][2], cost[i][2], prob[i][3], cost[i][3]));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("Chapter4/Example1/rates.txt"))) {
            writer.write("作为共乘司机\t\t作为共乘乘客");
            writer.newLine();
            writer.write("分摊比例\t匹配成功率\t分摊比例\t匹配成功率");
            writer.newLine();
            for (int i = 0; i < W; i++) {
                writer.write(String.format("%.2f\t%.2f\t%.2f\t%.2f",
                        sue.driverAffordRate[i], sue.matching.matchSolution.matchingRateSumDriver[i],
                        sue.passengerAffordRate[i], sue.matching.matchSolution.matchingRateSumPassenger[i]));
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        int size = 21;
        int index = 2;
        double[] vot = new double[size];
        double[] driverAffordRates = new double[size];
        double[] passengerAffordRates = new double[size];
        for (int i = 0; i < size; i++) {
            Param.ALPHA[index] = Param.ALPHA[0] + i;
            vot[i] = Param.ALPHA[2];

            SUE tempSUE = new SUE(W, Nt, sizeRate, driverAffordRate, passengerAffordRate);
            tempSUE.solveSUE();
            driverAffordRates[i] = tempSUE.driverAffordRate[index];
            passengerAffordRates[i] = tempSUE.passengerAffordRate[index];
        }
        saveResults("Chapter4", "Example1", "vot", vot);
        saveResults("Chapter4", "Example1", "driver_afford_rate", driverAffordRates);
        saveResults("Chapter4", "Example1", "passenger_afford_rate", passengerAffordRates);
    }

    public static void runChapter4Example2() {
        Param.isContinuous = true;
        Param.isFree = true;
        Param.ALPHA[0] = 50;
        Param.ALPHA[1] = 60;
        Param.ALPHA[2] = 70;

        int W = 3;
        double Nt = 1500;
        double[] sizeRate = new double[]{0, 1.0 / 3, 2.0 / 3};

        double[] driverAffordRate = new double[]{0.1, 0.1, 0.1};
        double[] passengerAffordRate = new double[]{0.1, 0.1, 0.1};

        int size = 67;
        double[] nums = new double[size];
        double[] matchSum = new double[size];
        double[] nc = new double[size];

        for (int i = 0; i < size; i++) {
            sizeRate[0] += 0.01;
            sizeRate[2] -= 0.01;
            nums[i] = Nt * sizeRate[0];
            SUE sue = new SUE(W, Nt, sizeRate, driverAffordRate, passengerAffordRate);
            sue.solveSUE();
            matchSum[i] = sue.nMatchingSum;
            nc[i] = sue.nc;
        }
        saveResults("Chapter4", "Example2", "nums", nums);
        saveResults("Chapter4", "Example2", "match_sum", matchSum);
        saveResults("Chapter4", "Example2", "nc", nc);

    }

    /**
     * @param chapterName 章节序号
     * @param exampleName 算例序号
     * @param dataSetName 结果名称
     * @param array       结果数据
     */
    private static void saveResults(String chapterName, String exampleName, String dataSetName, double[] array) {
        File directory = new File(chapterName + File.separator + exampleName);
        if (!directory.exists()) {
            directory.mkdirs(); // Create the directory and any necessary parent directories
        }

        String filePath = directory.getPath() + File.separator + dataSetName + ".txt";
        try (BufferedWriter out = new BufferedWriter(new FileWriter(filePath))) {
            for (double num : array) {
                out.write(String.format("%.6f", num) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param chapterName 章节序号
     * @param exampleName 算例序号
     * @param dataSetName 结果名称
     * @param headers     表头
     */
    private static void saveFormatTable(String chapterName, String exampleName, String dataSetName,
                                        String[] headers, double[] array1, double[] array2, double[] array3, double[] array4) {
        File directory = new File(chapterName + File.separator + exampleName);
        if (!directory.exists()) {
            directory.mkdirs(); // Create the directory and any necessary parent directories
        }
        String filePath = directory.getPath() + File.separator + dataSetName + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // 写入表头
            writer.write(headers[0] + "\t" + headers[1] + "\t" + headers[2] + "\n");
            // 写入第二行固定的表头
            writer.write("固定分摊比例\t异质性分摊比例\t固定分摊比例\t异质性分摊比例\n");

            // 写入矩阵数据
            for (int i = 0; i < array1.length; i++) {
                writer.write(
                        String.format("%.2f\t%.2f\t%.2f\t%.2f\n",
                                array1[i], array2[i], array3[i], array4[1])
                );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 矩阵转置
     */
    public static double[][] transpose(double[][] matrix) {
        // 获取原始矩阵的行数和列数
        int rows = matrix.length;
        int cols = matrix[0].length;

        // 创建一个新的矩阵来存储转置后的矩阵
        double[][] transposedMatrix = new double[cols][rows];

        // 遍历原始矩阵，将其转置
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                transposedMatrix[j][i] = matrix[i][j];
            }
        }

        return transposedMatrix;
    }
}
