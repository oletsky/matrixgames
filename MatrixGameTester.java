package mathcomp.oletsky.matrixgames;

import mathcomp.oletsky.mathhelper.VectMatr;

/**
 * @author O.Oletsky
 * Finding optimal strategies of matrix antagonistic games
 */
public class MatrixGameTester {
    public static void main(String[] args) {
        final boolean MIXED = true;
        double[][] matr = {
                {2, 0},
                {0, 6}
        };

        boolean pureExists;
        var maxMinPair = MatrixGameHelper.findMaxMinStrategy(matr);
        System.out.println("Max min value = " + maxMinPair.getOptValue());
        System.out.println("Max min strategy = " + maxMinPair.getStrategy());
        var minMaxPair = MatrixGameHelper.findMinMaxStrategy(matr);
        System.out.println("Min max value = " + minMaxPair.getOptValue());
        System.out.println("Min max strategy = " + minMaxPair.getStrategy());

        if (maxMinPair.getOptValue() == minMaxPair.getOptValue()) {
            pureExists = true;
            System.out.println("The optimal pure strategy is: " +
                    maxMinPair.getStrategy() + " - " + minMaxPair.getStrategy());
            System.out.println("The value of game is " + minMaxPair.getOptValue());
        } else {
            pureExists = false;
            System.out.println("The optimal pure strategy doesn't exist");
        }
        if (!pureExists && MIXED) {
            //Getting mixed strategies

            MatrixGameHelper.OptimalMixedStrategy optStrategies
                    =MatrixGameHelper.findMixedStrategies(matr);
            System.out.println("*********************");
            System.out.println("Mixed strategies:");
            System.out.println("First player:");
            VectMatr.defaultOutputVector(optStrategies.getMixedStrategies()[0]);
            System.out.println("Second player:");
            VectMatr.defaultOutputVector(optStrategies.getMixedStrategies()[1]);
            System.out.println("Game value is "+optStrategies.getGameValue());

            //Test pure strategy
            System.out.println("---------------------------");


            double[] p = optStrategies.getMixedStrategies()[1];

            double[] gains=MatrixGameHelper.calculateGains(matr,p);
            System.out.println("Pure gains:");
            VectMatr.defaultOutputVector(gains);

        }
    }
}
