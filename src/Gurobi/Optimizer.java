package Gurobi;

import OA.Model;
import OA.Request;
import gurobi.*;

public class Optimizer {
    private GRBModel model;
    private Model K;
    private GRBLinExpr obj, penalT, penalD, penalA, MAXProxyDay, constraint;
    private int rNum, t, d, a;
    private GRBRequest[] R;
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
        MAXProxyDay = new GRBLinExpr();

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
            addContraints(R[i], i, K.getRequest(i));
            // System.out.println(i);
        }

        model.addConstr(penalA, GRB.EQUAL, phiA, "constr2");
        model.addConstr(penalD, GRB.EQUAL, phiD, "constr3");
        model.addConstr(penalT, GRB.EQUAL, phiT, "constr4");

        model.addConstr(MAXProxyDay, GRB.LESS_EQUAL, K.getMAXNumProxyRequests(), "constr11");

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

    }

    private void addContraints(GRBRequest R, int i, Request K) throws GRBException {

        ts = R.getT();
        ds = R.getD();
        as = R.getA();

        float tetaT = K.getPenalty_T();
        penalT.addConstant(tetaT);
        penalT.addTerm(-1 * tetaT, ts[K.getTime()]);

        float tetaD = K.getPenalty_D();
        penalD.addConstant(tetaD);
        penalD.addTerm(-1 * tetaD, ds[K.getDay()]);

        float tetaA = K.getPenalty_A();
        penalA.addConstant(tetaA);
        penalA.addTerm(-1 * tetaA, as[K.getActivity()]);

        constraint.addTerms(null, ts);
        model.addConstr(constraint, GRB.EQUAL, R.getY(), i + ".constr5");

        constraint = new GRBLinExpr();
        constraint.addTerms(null, ds);
        model.addConstr(constraint, GRB.EQUAL, R.getY(), i + ".constr6");

        constraint = new GRBLinExpr();
        constraint.addTerms(null, as);
        model.addConstr(constraint, GRB.EQUAL, R.getY(), i + ".constr7a");

        // ATTENZIONE, SERVE CHIEDERE A EIL IN DIEIL perché non ho troppo capito la faccenda del mapping, cioè,
        // secondo me non serve
        for (int j = 0; j < this.a; j++) {
            int a = K.getActivities_of_category()[j] ? 1 : 0;
            model.addConstr(as[j], GRB.LESS_EQUAL, a, i + ".constr7b." + j);
        }

        model.addConstr(R.getY(), GRB.GREATER_EQUAL, K.getPROXY() - 1, i + ".constr8");

        model.addConstr(R.getP(), GRB.GREATER_EQUAL, K.getPROXY() - 1, i + ".constr9a");
        model.addConstr(R.getP(), GRB.LESS_EQUAL, K.getPROXY(), i + ".constr9b");

        model.addConstr(R.getP(), GRB.LESS_EQUAL, R.getY(), i + ".constr10");


        for (int j = 0; j < this.d; j++) {
            int a = K.getDays()[j] ? 1 : 0;
            MAXProxyDay.addTerm(a, R.getP());
        }

    }

    public static void main(String args[]) throws Exception {
        Optimizer opt = new Optimizer("inst/istanza_giocattolo.txt");
        opt.buildModel();
    }

}
