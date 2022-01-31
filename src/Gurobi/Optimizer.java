package Gurobi;

import Log.MyLog;
import OA.Model;
import OA.Request;
import gurobi.*;

public class Optimizer {
    private GRBModel model;
    private Model K; //these are the constant of the problem, coming from the file of the instance
    private GRBRequest[] R; //these are the variable of the problem.
    private GRBLinExpr obj, penalT, penalD, penalA, notPenal, expr_temp1, expr_temp2, expr_temp3;
    private int rNum, tNum, dNum, aNum; //total number of requests; # of timeslots, days and activities
    private GRBVar[] ys, ts, as, ds;
    private GRBVar phiT, phiD, phiA, phiNot, y_step_a, y_step_b, x_step;
    private final double[] stepx_peak2 = {0, 1.5, 2, 2.5, 3};
    private final double[] stepx_peak3 = {0, 2.5, 3, 3.5, 4};
    private final double[] stepx_peak7 = {0, 6.5, 7, 7.5, 8};
    private final double[] stepx_step1 = {0, 0.5, 1, 1.5, 2};
    private final double[] stepy_peak = {0, 0, 1, 0, 0}; //a particular step that is equal to 1 only in a specific value of x
    private final double[] stepy_step = {0, 0, 1, 0, 0}; //a particular step that is equal to 1 only in a specific value of x

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
        notPenal = new GRBLinExpr();

        tNum = K.getNumOfTimeSlots();
        dNum = K.getNumOfDays();
        aNum = K.getNumOfActivities();

        ys = new GRBVar[rNum];
        ts = new GRBVar[tNum];
        as = new GRBVar[aNum];
        ds = new GRBVar[dNum];

    }

    public void buildModel() throws Exception {

        MyLog.log("Generating requests general constraints");
        for (int i = 0; i < rNum; i++) {
            // generation of the variables
            R[i] = new GRBRequest(model, tNum, dNum, aNum, i);
            // need these for the objective function
            ys[i] = R[i].getY();
            // now  generate the constraints
            addRequestContraints(R[i], i, K.getRequest(i));
        }

        MyLog.log("Generating requests, days, times... constraints");
        for (int index_D = 0; index_D < dNum; index_D++) {
            addProxyCapacityContraint(index_D);
            addActivityCapacityConstraint(index_D);
        }

        MyLog.log("Objective function is creating... ");


        model.setObjective(obj, GRB.MAXIMIZE);
        model.update();

    }

    private void addRequestContraints(GRBRequest R, int index_R, Request K) throws GRBException {
        ts = R.getT();
        ds = R.getD();
        as = R.getA();


        //here we build the objective function, which is a specific terms for each request.
        //First we add the gain if the request is scheduled (if R.y==1)
        obj.addTerm(K.getGain(), R.getY());

        //Now we add the penalties, first we take away all the penalties
        obj.addTerm(-K.getPenalty_T(), R.getY());
        obj.addTerm(-K.getPenalty_A(), R.getY());
        obj.addTerm(-K.getPenalty_D(), R.getY());


        //then, if the day is correct, we sum again the penalty to the obj so that the sum is equal to 0
        //if the day is not correct, now we add 0, and the total balance is -penalty
        obj.addTerm(K.getPenalty_D(), R.getD()[K.getDay()]);
        obj.addTerm(K.getPenalty_T(), R.getT()[K.getTime()]);
        obj.addTerm(K.getPenalty_A(), R.getA()[K.getActivity()]);

        expr_temp1 = new GRBLinExpr();
        expr_temp1.addTerms(null, ts);
        model.addConstr(expr_temp1, GRB.EQUAL, R.getY(), "r" + index_R + ".constr10");

        expr_temp1 = new GRBLinExpr();
        expr_temp1.addTerms(null, ds);
        model.addConstr(expr_temp1, GRB.EQUAL, R.getY(), "r" + index_R + ".constr11");

        expr_temp1 = new GRBLinExpr();
        expr_temp1.addTerms(null, as);
        model.addConstr(expr_temp1, GRB.EQUAL, R.getY(), "r" + index_R + ".constr12");

        for (int j = 0; j < this.aNum; j++) {
            int a = K.getActivities_of_category()[j] ? 1 : 0;
            model.addConstr(as[j], GRB.LESS_EQUAL, a, "r" + index_R + ".a" + j + ".constr13");
        }

        //this constraint ensure that is a request has PROXY = 2, which means that the request MUST be done by a proxy
        //than R.y will be 1, because is mandatory to perform an activity with PROXY = 2
        //for more details about the constraints, watch the pdf of the model
        model.addConstr(R.getY(), GRB.GREATER_EQUAL, K.getPROXY() - 1, "r" + index_R + ".constr14");

        model.addConstr(R.getP(), GRB.LESS_EQUAL, K.getPROXY(), "r" + index_R + ".constr15.2");
        model.addConstr(R.getP(), GRB.GREATER_EQUAL, K.getPROXY() - 1, "r" + index_R + ".constr15.1");

        model.addConstr(R.getP(), GRB.LESS_EQUAL, R.getY(), "r" + index_R + ".constr16");

    }

    /**
     * This method builds up the constraint 17 of the model, using the peak function implemented by PWL
     * @param index_D the day of the constraint
     * @throws Exception
     * for more details about the constraint, watch the pdf of the model
     */
    private void addProxyCapacityContraint(int index_D) throws Exception {
        expr_temp1 = new GRBLinExpr();
        for (int index_R = 0; index_R < rNum; index_R++) {
            expr_temp2 = new GRBLinExpr();
            x_step = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "PEAK2.x_step_constr17.d" + index_D + ".r" + index_R + ".x");
            y_step_a = model.addVar(0, 1, 0, GRB.BINARY, "PEAK2.y_step_constr17.d" + index_D + ".r" + index_R + ".y");
            expr_temp2.addTerm(1, R[index_R].getP());
            expr_temp2.addTerm(1, R[index_R].getD()[index_D]);
            model.addConstr(x_step, GRB.EQUAL, expr_temp2, "d" + index_D + ".r" + index_R + ".x17");
            model.addGenConstrPWL(x_step, y_step_a, stepx_peak2, stepy_peak, "d" + index_D + ".r" + index_R + ".y17");
            expr_temp1.addTerm(1, y_step_a);

        }
        model.addConstr(expr_temp1, GRB.LESS_EQUAL, K.getNumProxyRequest(), "d" + index_D + ".constr17");
    }

    private void addActivityCapacityConstraint(int index_D) throws Exception {

        for (int index_T = 0; index_T < tNum; index_T++) {
            for (int index_A = 0; index_A < aNum; index_A++) {
                expr_temp1 = new GRBLinExpr(); //this expression will be used as the outer sum
                expr_temp3 = new GRBLinExpr(); //this will be used as
                for (int index_R = 0; index_R < rNum; index_R++) {
                    x_step = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "d" + index_D + ".t" + index_T + ".a" + index_A + ".r" + index_R + ".x18");
                    y_step_a = model.addVar(0, 1, 0, GRB.BINARY, "d" + index_D + ".t" + index_T + ".a" + index_A + ".r" + index_R + ".y18.a");
                    y_step_b = model.addVar(0, 1, 0, GRB.BINARY, "d" + index_D + ".t" + index_T + ".a" + index_A + ".r" + index_R + ".y18.b");

                    expr_temp2 = new GRBLinExpr();
                    expr_temp2.addTerm(1, R[index_R].getD()[index_D]);
                    expr_temp2.addTerm(1, R[index_R].getT()[index_T]);
                    expr_temp2.addTerm(1, R[index_R].getA()[index_A]);
                    expr_temp2.addTerm(4, R[index_R].getP());
                    model.addConstr(x_step, GRB.EQUAL, expr_temp2, "d" + index_D + ".t" + index_T + ".a" + index_A + ".r" + index_R + ".x18.a");

                    model.addGenConstrPWL(x_step, y_step_a, stepx_peak3, stepy_peak, "d" + index_D + ".t" + index_T + ".a" + index_A + ".r" + index_R + ".y18.a");
                    model.addGenConstrPWL(x_step, y_step_b, stepx_peak7, stepy_peak, "d" + index_D + ".t" + index_T + ".a" + index_A + ".r" + index_R + ".y18.b");

                    expr_temp1.addTerm(1, y_step_a);
                    expr_temp3.addTerm(1, y_step_b);
                }
                x_step = model.addVar(0, GRB.INFINITY, 0, GRB.CONTINUOUS, "d" + index_D + ".t" + index_T + ".a" + index_A + ".xp18");
                y_step_a = model.addVar(0, 1, 0, GRB.BINARY, "d" + index_D + ".t" + index_T + ".a" + index_A + ".yp18.a");
                model.addConstr(x_step, GRB.EQUAL, expr_temp3, "d" + index_D + ".t" + index_T + ".a" + index_A + ".xp18.a");

                model.addGenConstrPWL(x_step, y_step_a, stepx_step1, stepy_step, "d" + index_D + ".t" + index_T + ".a" + index_A + ".yp18.a");

                expr_temp1.addTerm(1, y_step_a);
                model.addConstr(expr_temp1, GRB.LESS_EQUAL, K.getActivityCapacity(index_A), "d" + index_D + ".t" + index_T + ".a" + index_A + "constr20");
            }
        }

    }

    public void GRB_optimize_IIS() throws Exception {
        model.optimize();

        int optimstatus = model.get(GRB.IntAttr.Status);

        if (optimstatus == GRB.Status.INF_OR_UNBD) {
            model.set(GRB.IntParam.Presolve, 0);
            model.optimize();
            optimstatus = model.get(GRB.IntAttr.Status);
        }

        if (optimstatus == GRB.Status.OPTIMAL) {
            double objval = model.get(GRB.DoubleAttr.ObjVal);
            MyLog.log(" ");
            MyLog.log("Optimal objective: " + objval);
            printModelVariables();
        } else if (optimstatus == GRB.Status.INFEASIBLE) {
            MyLog.log("Model is infeasible");

            // Compute and write out IIS
            model.computeIIS();
            model.write("inst/model.ilp");
            model.feasRelax(GRB.FEASRELAX_LINEAR, true, false, true);
        } else if (optimstatus == GRB.Status.UNBOUNDED) {
            MyLog.log("Model is unbounded");
        } else {
            MyLog.log("Optimization was stopped with status = " + optimstatus);
        }

        // Dispose of model and environment
        model.dispose();
        model.getEnv().dispose();
    }

    public static void main(String[] args) throws Exception {
        Optimizer opt = new Optimizer("inst/istanza_prova.txt");

        MyLog.log("Start model generation");
        opt.buildModel();
        MyLog.log("Model complete");

        MyLog.log("Writing model in inst/out.lp");
        //opt.model.write("inst/out.lp");

        MyLog.log("Start optimization");
        opt.GRB_optimize_IIS();

        MyLog.log("End");
    }

    public void printModelVariables() {
        MyLog.log("VARIABLES");
        try {
            for (GRBVar var : model.getVars()) {
                MyLog.log(var.get(GRB.StringAttr.VarName) + " \t" + var.get(GRB.DoubleAttr.X));
            }
        } catch (GRBException e) {
            MyLog.log("Error code: " + e.getErrorCode() + ". " + e.getMessage());
        }
    }

}
