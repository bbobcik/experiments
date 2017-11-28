package cz.auderis.dist;

import org.apache.commons.math3.stat.Frequency;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class RandomSums {

    static final BigDecimal RANGE_MIN = BigDecimal.valueOf(100L);
    static final BigDecimal RANGE_MAX = BigDecimal.valueOf(1000L);
    static final int STEP = 10_000_000;

    public static void main(String[] args) {
        final Frequency freq = new Frequency();
        final int N = 11;
        for (int sample=0; sample<10_000_000; ++sample) {
            final BigDecimal value = randomSum(N);
            freq.addValue(value);
        }
        System.out.println(freq);
    }

    static BigDecimal randomSum(int n) {
        final BigDecimal range = RANGE_MAX.subtract(RANGE_MIN);
        final BigDecimal rangeStep = range.divide(BigDecimal.valueOf(STEP), 18, BigDecimal.ROUND_HALF_UP);
        final Random rnd = new Random();
        BigDecimal sum = BigDecimal.ZERO;
        for (int i=0; i<n; ++i) {
            final int factor = rnd.nextInt(STEP + 1);
            BigDecimal term = BigDecimal.valueOf(factor).multiply(rangeStep).add(RANGE_MIN).setScale(0, RoundingMode.HALF_UP);
            sum = sum.add(term);
        }
        return sum;
    }

}
