package Gurobi;

import gurobi.GRB;
import gurobi.GRBException;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class GRBRequest {
    private int index;
    private GRBVar[] t, d, a;
    private GRBVar p, y;

    public GRBRequest(GRBModel model, int t, int d, int a, int index) throws GRBException {
        this.index = index;
        this.t = new GRBVar[t];
        for (int i = 0; i < t; i++)
            this.t[i] = model.addVar(0, 1, 0, GRB.BINARY, index + ".t." + i);
        this.d = new GRBVar[d];
        for (int i = 0; i < d; i++)
            this.d[i] = model.addVar(0, 1, 0, GRB.BINARY, index + ".d." + i);
        this.a = new GRBVar[a];
        for (int i = 0; i < a; i++)
            this.a[i] = model.addVar(0, 1, 0, GRB.BINARY, index + ".a." + i);

        this.p = model.addVar(0, 1, 0, GRB.BINARY, index + ".p");
        this.y = model.addVar(0, 1, 0, GRB.BINARY, index + ".y");
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public GRBVar[] getT() {
        return t;
    }

    public void setT(GRBVar[] t) {
        this.t = t;
    }

    public GRBVar[] getD() {
        return d;
    }

    public void setD(GRBVar[] d) { this.d = d; }

    public GRBVar[] getA() { return a; }

    public void setA(GRBVar[] a) {
        this.a = a;
    }

    public GRBVar getP() {
        return p;
    }

    public void setP(GRBVar p) {
        this.p = p;
    }

    public GRBVar getY() {
        return y;
    }

    public void setY(GRBVar y) {
        this.y = y;
    }

}
