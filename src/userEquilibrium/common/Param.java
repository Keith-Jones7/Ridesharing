package userEquilibrium.common;

public class Param {
    public static final double LB = 22;            //公共交通里程
    public static final double LS = 20;            //独驾里程
    public static final double VB = 30;            //公共交通平均速度
    public static final double FREQ = 3;          //公共交通发车频率
    public static final double PB = 100;            //公共交通票价
    public static final double GAMMA = 2;        //公交车拥挤成本系数
    public static final double ROU = 0.35;          //公交车拥挤惩罚系数
    public static final double B = 60;             //公交车最大容量
    public static final double LD = 5;             //平均绕行距离
    public static final double VS = 50;            //小汽车零流速度
    public static final double LAMBDA = 0.7;       //每公里燃料费
    public static final double CF = 0.7;           //每公里车辆折旧成本
    public static final double ALPHA1 = 0.15;      //BPR系数1
    public static final double ALPHA2 = 4;         //BPR系数2
    public static final double Capacity = 600;     //道路车辆容量
    public static final double DISCOMFORT_D = 0;   //司机的舒适度成本
    public static final double DISCOMFORT_P = 0;   //乘客的舒适度成本
    public static final double THETA_D = 0;        //司机参与匹配的固定成本
    public static final double THETA_P = 0;        //乘客参与匹配的固定成本
    public static final double[] ALPHA = {60, 65, 70, 15, 16, 17, 18, 19, 20, 21};         //行驶过程的VoT
    public static boolean isFree = false;
    public static double precision = 1e-3;
    public static int maxCount = 100000;
    public static double theta = 0.05;
    public static int M = 4;                        //出行方式类别 0:bus 1:solo_drive 2:rs_driver 3: rs_passenger
    public static double vt = 40;
}
