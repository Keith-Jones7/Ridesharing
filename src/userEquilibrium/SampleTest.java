package userEquilibrium;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
public class SampleTest {

    public static void main(String[] args) {
        runChapter3Example1();
//        runChapter3Example2();
//        runChapter3Example3();
//        runChapter4Example1();
//        runChapter4Example2();
    }

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

    }

    public static void runChapter4Example2() {

    }
//    public void generateSample4_2() {
//        W = 3;
//        Nt = 1500;
//        driverAffordRate = new double[]{0.1, 0.1, 0.1};
//        passengerAffordRate = new double[]{0.1, 0.1, 0.1};
//        sizeRate = new double[]{1.0 / 3, 1.0 / 3, 1.0 / 3};
//        double size1 = 0, size3 = 1 - 1.0 / 3;
//        double[] matchSum = new double[67];
//        double[] nc = new double[67];
//        int index = 0;
//        while (size3 > 0) {
//            sizeRate[0] = size1;
//            sizeRate[2] = size3;
//            size1 += 0.01;
//            size3 -= 0.01;
//            SUE sue = new SUE(W, Nt, sizeRate, driverAffordRate, passengerAffordRate);
//            sue.solveSUE();
//            nc[index] = sue.nc;
//            matchSum[index++] = sue.nMatchingSum;
//        }
//        String fileName = "matchSum.txt";
//        write(fileName, matchSum, nc);
//    }


    /**
     *
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
        try (BufferedWriter out = new BufferedWriter(new FileWriter(new File(filePath)))) {
            for (double num : array) {
                out.write(String.format("%.6f", num) + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param chapterName   章节序号
     * @param exampleName   算例序号
     * @param dataSetName   结果名称
     * @param headers       表头
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
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

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
