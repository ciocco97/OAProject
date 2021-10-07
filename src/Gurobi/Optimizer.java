package Gurobi;

import Log.MyLog;
import OA.Model;
import OA.Request;
import gurobi.*;

public class Optimizer {
    private GRBModel model;
    private Model K;
    private GRBRequest[] R;
    private GRBLinExpr obj, penalT, penalD, penalA, expr_temp1, expr_temp2;
    private int rNum, tNum, dNum, aNum;
    private GRBVar[] ys, ts, as, ds;
    private GRBVar phiT, phiD, phiA, y_step, x_step;
    private final double stepx_2[] = {0, 1.5, 2, 3}; private final double stepx_0[] = {-1, -0.5, 0, 1};
    private final double stepy[] = {0, 0, 1, 1};

    public Optimizer(String filename) throws Exception {
        K = new Model(filename);

        rNum = K.getNumOfRequests();
        R = new GRBRequest[rNum];
        GRBEnv env = new GRBEnv(true);
        env.set("logFile", "log/mip1.log");
        env.start();
        // create an empty model that will be filled
        model = new GRBModel(env);

        obj = new GRBLinExpr();
        penalT = new GRBLinExpr();
        penalD = new GRBLinExpr();
        penalA = new GRBLinExpr();

        tNum = K.getNumOfTimeSlots();
        dNum = K.getNumOfDays();
        aNum = K.getNumOfActivities();

        ys = new GRBVar[rNum];
        ts = new GRBVar[K.getNumOfTimeSlots()];
        as = new GRBVar[K.getNumOfActivities()];
        ds = new GRBVar[K.getNumOfDays()];

        phiT = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "phiT");
        phiA = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "phiA");
        phiD = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "phiD");
    }

    public void buildModel() throws Exception {

        MyLog.log("Generating requests general constraints");
        for (int i = 0; i < rNum; i++) {
            // generation of the variables
            R[i] = new GRBRequest(model, tNum, dNum, aNum, i);
            // i need these for the objective function
            ys[i] = R[i].getY();
            // now  generate the constraints
            addRequestContraints(R[i], i, K.getRequest(i));
        }

        MyLog.log("Generating requests, days, times... constraints");
        for (int index_D = 0; index_D < dNum; index_D++) {
            addProxyCapacityContraint(index_D);
            for (int index_T = 0; index_T < tNum; index_T++) {
                for (int index_A = 0; index_A < aNum; index_A++) {
                    addActivityCapacity(index_D, index_T, index_A);
                }
            }
        }

        model.addConstr(penalT, GRB.EQUAL, phiT, "constr2");
        model.addConstr(penalD, GRB.EQUAL, phiD, "constr3");
        model.addConstr(penalA, GRB.EQUAL, phiA, "constr4");

        MyLog.log("Objective function is creating... ");

        double[] requestGains = K.getGains();
        obj.addTerms(requestGains, ys);
        obj.addTerm(-1, phiA);
        obj.addTerm(-1, phiD);
        obj.addTerm(-1, phiT);

        model.setObjective(obj, GRB.MAXIMIZE);
        model.update();

    }

    private void addRequestContraints(GRBRequest model_R, int index_R, Request data_R) throws GRBException {

        ts = model_R.getT();
        ds = model_R.getD();
        as = model_R.getA();

        float tetaT = data_R.getPenalty_T();
        penalT.addConstant(tetaT);
        penalT.addTerm(-1 * tetaT, ts[data_R.getTime()]);

        float tetaD = data_R.getPenalty_D();
        penalD.addConstant(tetaD);
        penalD.addTerm(-1 * tetaD, ds[data_R.getDay()]);

        float tetaA = data_R.getPenalty_A();
        penalA.addConstant(tetaA);
        penalA.addTerm(-1 * tetaA, as[data_R.getActivity()]);

        model.addConstr(model_R.getY(), GRB.GREATER_EQUAL, data_R.getPROXY() - 1, "r" + index_R + ".constr1");

        expr_temp1 = new GRBLinExpr();
        expr_temp1.addTerms(null, ts);
        model.addConstr(expr_temp1, GRB.EQUAL, model_R.getY(), "r" + index_R + ".constr5");

        expr_temp1 = new GRBLinExpr();
        expr_temp1.addTerms(null, ds);
        model.addConstr(expr_temp1, GRB.EQUAL, model_R.getY(), "r" + index_R + ".constr6");

        expr_temp1 = new GRBLinExpr();
        expr_temp1.addTerms(null, as);
        model.addConstr(expr_temp1, GRB.EQUAL, model_R.getY(), "r" + index_R + ".constr7a");

        for (int j = 0; j < this.aNum; j++) {
            int a = data_R.getActivities_of_category()[j] ? 1 : 0;
            model.addConstr(as[j], GRB.LESS_EQUAL, a, "r" + index_R + ".constr7b." + j);
        }

        model.addConstr(model_R.getP(), GRB.LESS_EQUAL, model_R.getY(), "r" + index_R + ".constr8");

        model.addConstr(model_R.getP(), GRB.LESS_EQUAL, data_R.getPROXY(), "r" + index_R + ".constr9a");
        model.addConstr(model_R.getP(), GRB.GREATER_EQUAL, data_R.getPROXY() - 1, "r" + index_R + ".constr9b");

    }

    private void addProxyCapacityContraint(int index_D) throws Exception {
        expr_temp1 = new GRBLinExpr();
        for (int index_R = 0; index_R < rNum; index_R++) {
            expr_temp2 = new GRBLinExpr();
            x_step = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "d" +index_D + ".r" + index_R + ".xstep");
            y_step = model.addVar(0, 1, 0, GRB.BINARY, "d" +index_D + ".r" + index_R + ".ystep");
            expr_temp2.addTerm(1, R[index_R].getP());
            expr_temp2.addTerm(1, R[index_R].getD()[index_D]);
            model.addConstr(x_step, GRB.EQUAL, expr_temp2, "d" +index_D + ".r" + index_R + ".temp.xstep");
            model.addGenConstrPWL(x_step, y_step, stepx_2, stepy, "d" +index_D + ".r" + index_R + ".step.constr10");
            expr_temp1.addTerm(1, y_step);
        }
        model.addConstr(expr_temp1, GRB.LESS_EQUAL, K.getNumProxyRequest(), "d" + index_D + ".constr10");
    }

    private void addActivityCapacity(int index_D, int index_T, int index_A) {

    }

    public static void main(String[] args) throws Exception {
        Optimizer opt = new Optimizer("inst/istanza_giocattolo.txt");

        MyLog.log("Start model generation");
        opt.buildModel();
        MyLog.log("Model complete");

        MyLog.log("Start optimization");
        opt.model.optimize();

        MyLog.log("Writing model in inst/out.lp");
        opt.model.write("inst/out.lp");

        MyLog.log("End");
    }

}
