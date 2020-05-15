package mathcomp.oletsky.matrixgames;

import mathcomp.oletsky.linprog.LinProgSolver;
import mathcomp.oletsky.mathhelper.VectMatr;

import java.util.stream.DoubleStream;

/**
 * @author O.Oletsky
 * A class for finding pure (if exist)
 * and mixed strategies for matrix antagonistic games
 * Needs SCPSolver and GLPKSolverPack external libraries
 */
public class MatrixGameHelper {

    public static class OptimalMixedStrategy {
        double gameValue;
        double[][] mixedStrategies;

        public OptimalMixedStrategy(double gameValue, double[][] mixedStrategies) {
            this.gameValue = gameValue;
            this.mixedStrategies = mixedStrategies;
        }

        public double getGameValue() {
            return gameValue;
        }

        public double[][] getMixedStrategies() {
            return mixedStrategies;
        }
    }

    public static class OptPair {
        private int strategy;
        private double optValue;

        public OptPair(int strategy, double optValue) {
            this.strategy = strategy;
            this.optValue = optValue;
        }

        public int getStrategy() {
            return strategy;
        }

        public double getOptValue() {
            return optValue;
        }
    }

    public static OptPair findMaxMinStrategy(double[][] matr) {
        int m = matr.length;
        int n = matr[0].length;

        //Most unsuitable responses
        double[] contras = new double[m];
        for (int i = 0; i < m; i++) {
            var mv = DoubleStream.of(matr[i]).min().getAsDouble();
            contras[i] = mv;
        }
        int rind = 0;
        double rv = contras[rind];
        for (int i = 1; i < contras.length; i++) {
            if (contras[i] > rv) {
                rind = i;
                rv = contras[i];
            }
        }

        return new OptPair(rind, rv);
    }

    public static OptPair findMinMaxStrategy(double[][] matr) {
        int m = matr.length;
        int n = matr[0].length;

        //Most unsuitable responses
        double[] contras = new double[n];
        for (int j = 0; j<n; j++) {
            double[] colJ=VectMatr.getMatrixColumn(matr,j);
            var mv = DoubleStream.of(colJ).max().getAsDouble();
            contras[j] = mv;
        }
        int rind = 0;
        double rv = contras[rind];
        for (int i = 1; i < contras.length; i++) {
            if (contras[i] < rv) {
                rind = i;
                rv = contras[i];
            }
        }

        return new OptPair(rind, rv);
    }


    public static OptimalMixedStrategy findMixedStrategies(double[][] matr) {
        int m = matr.length;
        int n = matr[0].length;
        double[][] matrCopy=new double[m][n];
        for (int i=0; i<m; i++)
            for (int j = 0; j <n ; j++) {
                matrCopy[i][j]=matr[i][j];
            }

        double[] u = VectMatr.getUnityVector(m);
        double[] v = VectMatr.getUnityVector(n);
        double betaV = VectMatr.getMinElement(matrCopy);


        if (betaV <= 1.E-5) {
            betaV -= 1.;
            VectMatr.shiftMatrixValues(matrCopy, -betaV);
        }
        System.out.println("Beta in function = " + betaV);
        //First player

        double[][] transpMatr = VectMatr.transposeMatrix(matrCopy);
        double[][] matrX = new double[n + m][m];
        double[] objectiveX = new double[m];
        for (int i = 0; i < m; i++) {
            objectiveX[i] = -u[i];
        }
        double[] constrVectX = new double[n + m];
        for (int i = 0; i < n; i++) {
            constrVectX[i] = v[i];
        }
        for (int i = n; i < n + m; i++) {
            constrVectX[i] = 0.;

        }

        for (int i = 0; i < n; i++) {
            for (int k = 0; k < m; k++) {
                matrX[i][k] = transpMatr[i][k];
            }
        }
        for (int i = n; i < n + m; i++) {
            for (int k = 0; k < m; k++) {
                if (i - n == k) matrX[i][k] = 1.;
                else matrX[i][k] = 0;
            }
        }

        double[] xRawStrats = LinProgSolver.solveLinearProgTask(
                objectiveX,
                matrX,
                constrVectX
        );

        double[] normX = VectMatr.normalizeVectorBySum(xRawStrats);


        double tetaX = VectMatr.calculateScalarProduct(xRawStrats, u);

        double valueX = 1. / tetaX + betaV;

        //Second player
        double[][] matrY = new double[n + m][n];
        double[] objectiveY = new double[n];
        for (int i = 0; i < n; i++) {
            objectiveY[i] = v[i];
        }
        double[] constrVectY = new double[n + m];
        for (int i = 0; i < m; i++) {
            constrVectY[i] = -u[i];
        }
        for (int i = m; i < n + m; i++) {
            constrVectY[i] = 0.;

        }

        for (int i = 0; i < m; i++) {
            for (int k = 0; k < n; k++) {
                matrY[i][k] = -matrCopy[i][k];
            }
        }
        for (int i = m; i < n + m; i++) {
            for (int k = 0; k < n; k++) {
                if (i - m == k) matrY[i][k] = 1.;
                else matrY[i][k] = 0;
            }
        }

        double[] yRawStrats = LinProgSolver.solveLinearProgTask(
                objectiveY,
                matrY,
                constrVectY
        );

        double[] normY = VectMatr.normalizeVectorBySum(yRawStrats);

        double tetaY = VectMatr.calculateScalarProduct(yRawStrats, v);

        double valueY = 1. / tetaY + betaV;
        if (Math.abs(valueX - valueY) > 1.E-5) throw
                new RuntimeException("Game values are not equal!");

        //Returning
        double[][] resStrat = new double[2][];
        resStrat[0] = normX;
        resStrat[1] = normY;
        double value = valueY;

        return new OptimalMixedStrategy(value, resStrat);
    }

    public static double calculateEstimatedGain(double[][] matr,
                                                double[] p,
                                                int rowNumb) {
        final double EPS = 1.E-10;
        if (Math.abs(VectMatr.calculateSumOfComponents(p) - 1.) > EPS)
            throw new IllegalArgumentException("Sum doesn't equal 1");
        double[] row = VectMatr.getMatrixRow(matr, rowNumb);
        return VectMatr.calculateScalarProduct(row, p);

    }

    public static int chooseByRandomEstimation(double[][] matr,
                                               double[] p) {
        final double EPS = 1.E-10;
        if (Math.abs(VectMatr.calculateSumOfComponents(p) - 1.) > EPS)
            throw new IllegalArgumentException("Sum doesn't equal 1");
        int ind = 0;
        double max = calculateEstimatedGain(matr, p, ind);
        for (int i = 1; i < matr.length; i++) {
            double gain = calculateEstimatedGain(matr, p, i);
            if (gain > max) {
                ind = i;
                max = gain;
            }

        }
        return ind;
    }

    public static double[] calculateGains(double[][] matr,
                                          double[] p) {
        final double EPS = 1.E-10;
        if (Math.abs(VectMatr.calculateSumOfComponents(p) - 1.) > EPS)
            throw new IllegalArgumentException("Sum doesn't equal 1");
        return VectMatr.rightMultiply(matr, p);
    }
}
