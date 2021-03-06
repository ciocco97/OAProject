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
            this.t[i] = model.addVar(0, 1, 0, GRB.BINARY, "r" + index + ".t" + i);
        this.d = new GRBVar[d];
        for (int i = 0; i < d; i++)
            this.d[i] = model.addVar(0, 1, 0, GRB.BINARY, "r" + index + ".d" + i);
        this.a = new GRBVar[a];
        for (int i = 0; i < a; i++)
            this.a[i] = model.addVar(0, 1, 0, GRB.BINARY, "r" + index + ".a" + i);

        this.p = model.addVar(0, 1, 0, GRB.BINARY, "r" + index + ".p");
        this.y = model.addVar(0, 1, 0, GRB.BINARY, "r" + index + ".y");
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

    public int getPValue() throws GRBException {
        return (int) p.get(GRB.DoubleAttr.X);
    }
    public int getYValue() throws GRBException {
        return (int) y.get(GRB.DoubleAttr.X);
    }
    public int getTValue(int index) throws GRBException {
        return (int) t[index].get(GRB.DoubleAttr.X);
    }
    public int getDValue(int index) throws GRBException {
        return (int) d[index].get(GRB.DoubleAttr.X);
    }
    public int getAValue(int index) throws GRBException {
        return (int) a[index].get(GRB.DoubleAttr.X);
    }

}
