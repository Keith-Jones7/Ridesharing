package userEquilibrium.common;

public class Param {
    public static boolean isFree = false;
    public static final double LB = 20;            //公共交通里程
    public static final double VB = 30;            //公共交通平均速度
    public static final double FREQ = 10;          //公共交通发车频率
    public static final double PB = 2;            //公共交通票价
    public static final double GAMMA = 2;        //公交车拥挤成本系数
    public static final double ROU = 0.3;          //公交车拥挤惩罚系数
    public static final double B = 30;             //公交车最大容量
    public static final double LD = 2;             //平均绕行距离
    public static final double VS = 50;            //小汽车零流速度
    public static final double LAMBDA = 0.3788597;       //每公里燃料费
    public static final double CF = 0.1;           //每公里车辆折旧成本
    public static final double ALPHA1 = 0.15;      //BPR系数1
    public static final double ALPHA2 = 4;         //BPR系数2
    public static final double Capacity = 600;     //道路车辆容量
    public static final double DISCOMFORT_D = 1;   //司机的舒适度成本
    public static final double DISCOMFORT_P = 1;   //乘客的舒适度成本
    public static final double THETA_D = 1;        //司机参与匹配的固定成本
    public static final double THETA_P = 1;        //乘客参与匹配的固定成本
    public static final double LS = 20;            //独驾里程
    public static double precision = 1e-4;
    public static int maxCount = 3000;
    public static double theta = 0.1;
    public static int M = 4;//出行方式类别 0:bus 1:solo_drive 2:rs_driver 3: rs_passenger
    public static double vt = 40;
}
