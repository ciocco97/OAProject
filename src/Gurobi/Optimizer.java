package Gurobi;

import OA.Model;
import OA.Request;
import gurobi.*;

public class Optimizer {
    private GRBModel model;
    private Model K;
    private GRBLinExpr obj, penalT, penalD, penalA, constraint;
    private int rNum, t, d, a;
    private GRBRequest[] R;
    private GRBVar[] ys, ts, as, ds;
    private GRBVar phiT, phiD, phiA;


    public Optimizer(String filename) throws Exception {
        K = new Model(filename);
        rNum = K.getNumOfRequests();
        R = new GRBRequest[rNum];
        GRBEnv env = new GRBEnv(true);
        env.set("logFile", "mip1.log");
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
    }


    public void buildModel() throws Exception {


        int timeslot, day, activity;


        for (int i = 0; i < rNum; i++) {
            timeslot = K.getRequest(i).getTime();
            day = K.getRequest(i).getDay();
            activity = K.getRequest(i).getActivity();
            // generation of the variables
            R[i] = new GRBRequest(model, t, d, a, i);
            // i need these for the objective function
            ys[i] = R[i].getY();
            // now  generate the constraints
            addContraints(R[i], i, K.getRequest(i));
            // System.out.println(i);
        }

        model.addConstr(penalA, GRB.EQUAL, phiA, "const2");
        model.addConstr(penalD, GRB.EQUAL, phiD, "const3");
        model.addConstr(penalT, GRB.EQUAL, phiT, "const4");

        System.out.println("funzione obiettivo");

        obj.addTerms(null, ys);

        System.out.println("comincia l'ottimizzazione");
        model.optimize();


        //sum of y

      /*

      // Create variables
      GRBVar x = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "x");
      GRBVar y = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "y");
      GRBVar z = model.addVar(0.0, 1.0, 0.0, GRB.BINARY, "z");

      // Set objective: maximize x + y + 2 z
      GRBLinExpr expr = new GRBLinExpr();
      expr.addTerm(1.0, x); expr.addTerm(1.0, y); expr.addTerm(2.0, z);
      model.setObjective(expr, GRB.MAXIMIZE);

      // Add constraint: x + 2 y + 3 z <= 4
      expr = new GRBLinExpr();
      expr.addTerm(1.0, x); expr.addTerm(2.0, y); expr.addTerm(3.0, z);
      model.addConstr(expr, GRB.LESS_EQUAL, 4.0, "c0");

      // Add constraint: x + y >= 1
      expr = new GRBLinExpr();
      expr.addTerm(1.0, x); expr.addTerm(1.0, y);
      model.addConstr(expr, GRB.GREATER_EQUAL, 1.0, "c1");

      // Optimize model
      model.optimize();

      System.out.println(x.get(GRB.StringAttr.VarName)
                         + " " +x.get(GRB.DoubleAttr.X));
      System.out.println(y.get(GRB.StringAttr.VarName)
                         + " " +y.get(GRB.DoubleAttr.X));
      System.out.println(z.get(GRB.StringAttr.VarName)
                         + " " +z.get(GRB.DoubleAttr.X));

      System.out.println("Obj: " + model.get(GRB.DoubleAttr.ObjVal));

      // Dispose of model and environment
      model.dispose();
      env.dispose();
*/

    }

    private void addContraints(GRBRequest R, int i, Request K) throws GRBException {

        ts = R.getT();
        ds = R.getD();
        as = R.getA();

        float tetaT = K.getPenalty_T();
        penalT.addConstant(K.getPenalty_T());
        penalT.addTerm(-1 * tetaT, ts[K.getTime() - 1]);

        float tetaD = K.getPenalty_D();
        penalD.addConstant(K.getPenalty_D());
        penalD.addTerm(-1 * tetaD, ds[K.getDay() - 1]);

        float tetaA = K.getPenalty_A();
        penalA.addConstant(K.getPenalty_A());
        penalA.addTerm(-1 * tetaA, as[K.getActivity() - 1]);

        constraint.addTerms(null, ts);
        model.addConstr(constraint, GRB.EQUAL, R.getY(), i + ".const5");

        constraint = new GRBLinExpr();
        constraint.addTerms(null, ds);
        model.addConstr(constraint, GRB.EQUAL, R.getY(), i + ".const6");

        constraint = new GRBLinExpr();
        constraint.addTerms(null, as);
        model.addConstr(constraint, GRB.EQUAL, R.getY(), i + ".const7a");

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

    }

    public static void main(String args[]) throws Exception {
        Optimizer opt = new Optimizer("src/OTSP1.txt");
        opt.buildModel();
    }

}
