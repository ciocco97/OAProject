package Gurobi;

import OA.Model;
import OA.Request;
import gurobi.*;

public class Optimizer {
    private GRBModel model;
    private Model K;
    private GRBRequest[] R;
    private GRBLinExpr obj, penalT, penalD, penalA, constraint;
    private int rNum, t, d, a;
    private GRBVar[] ys, ts, as, ds;
    private GRBVar phiT, phiD, phiA;


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
        constraint = new GRBLinExpr();
        penalT = new GRBLinExpr();
        penalD = new GRBLinExpr();
        penalA = new GRBLinExpr();

        t = K.getNumOfTimeSlots();
        d = K.getNumOfDays();
        a = K.getNumOfActivities();

        ys = new GRBVar[rNum];
        ts = new GRBVar[K.getNumOfTimeSlots()];
        as = new GRBVar[K.getNumOfActivities()];
        ds = new GRBVar[K.getNumOfDays()];

        phiT = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "phiT");
        phiA = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "phiA");
        phiD = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "phiD");
    }


    public void buildModel() throws Exception {


        int timeslot, day, activity;


        for (int i = 0; i < rNum; i++) {
            // generation of the variables
            R[i] = new GRBRequest(model, t, d, a, i);
            // i need these for the objective function
            ys[i] = R[i].getY();
            // now  generate the constraints
            addRequestContraints(R[i], i, K.getRequest(i));
        }

        model.addConstr(penalT, GRB.EQUAL, phiT, "constr2");
        model.addConstr(penalD, GRB.EQUAL, phiD, "constr3");
        model.addConstr(penalA, GRB.EQUAL, phiA, "constr4");

        System.out.println("Objective function is creating... ");

        double[] requestGains = K.getGains();
        obj.addTerms(requestGains, ys);
        obj.addTerm(-1, phiA);
        obj.addTerm(-1, phiD);
        obj.addTerm(-1, phiT);

        model.setObjective(obj, GRB.MAXIMIZE);
        model.update();

        System.out.println("Start optimization");
        model.optimize();

        model.write("out.lp");

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

        model.addConstr(model_R.getY(), GRB.GREATER_EQUAL, data_R.getPROXY() - 1, index_R + ".constr1");

        constraint = new GRBLinExpr();
        constraint.addTerms(null, ts);
        model.addConstr(constraint, GRB.EQUAL, model_R.getY(), index_R + ".constr5");

        constraint = new GRBLinExpr();
        constraint.addTerms(null, ds);
        model.addConstr(constraint, GRB.EQUAL, model_R.getY(), index_R + ".constr6");

        constraint = new GRBLinExpr();
        constraint.addTerms(null, as);
        model.addConstr(constraint, GRB.EQUAL, model_R.getY(), index_R + ".constr7a");

        for (int j = 0; j < this.a; j++) {
            int a = data_R.getActivities_of_category()[j] ? 1 : 0;
            model.addConstr(as[j], GRB.LESS_EQUAL, a, index_R + ".constr7b." + j);
        }

        model.addConstr(model_R.getP(), GRB.LESS_EQUAL, model_R.getY(), index_R + ".constr8");

        model.addConstr(model_R.getP(), GRB.LESS_EQUAL, data_R.getPROXY(), index_R + ".constr9a");
        model.addConstr(model_R.getP(), GRB.GREATER_EQUAL, data_R.getPROXY() - 1, index_R + ".constr9b");

    }

    private void addDayontraints() {

    }

    public static void main(String[] args) throws Exception {
        Optimizer opt = new Optimizer("inst/istanza_giocattolo.txt");
        opt.buildModel();


    }

}
