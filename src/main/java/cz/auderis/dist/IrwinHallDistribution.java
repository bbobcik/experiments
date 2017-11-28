package cz.auderis.dist;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class IrwinHallDistribution {

    public static void main(String[] args) {
        Locale.setDefault(Locale.US);
        final IrwinHallDistribution dist = new IrwinHallDistribution();
        final BigDecimal targetP = BigDecimal.valueOf(995L, 3);
        for (int n=2; n<=30; ++n) {
            final ResultPair result = dist.findSum(n, targetP);
            System.out.println(result.getX());
        }
    }


    static final BigDecimal ONE_HALF = BigDecimal.valueOf(5L, 1);
    static final BigDecimal X_EPSILON = BigDecimal.valueOf(1L, 8);
    static final BigDecimal P_EPSILON = BigDecimal.valueOf(1L, 8);
    final NavigableMap<BigDecimal, BigDecimal> factorialCache;

    public IrwinHallDistribution() {
        this.factorialCache = new TreeMap<>();
        BigDecimal factorial = BigDecimal.ONE;
        for (long i=1L; i<10L; ++i) {
            final BigDecimal x = BigDecimal.valueOf(i);
            factorial = x.multiply(factorial);
            factorialCache.put(x, factorial);
        }
    }

    public ResultPair findSum(int n, BigDecimal thresholdProbability) {
        if ((thresholdProbability.signum() < 0) || (thresholdProbability.compareTo(BigDecimal.ONE) > 0)) {
            throw new IllegalArgumentException();
        } else if (thresholdProbability.signum() == 0) {
            return new ResultPair(BigDecimal.ZERO, thresholdProbability);
        } else if (thresholdProbability.compareTo(BigDecimal.ONE) == 0) {
            return new ResultPair(BigDecimal.valueOf(n), thresholdProbability);
        }
        final int cmpHalf = thresholdProbability.compareTo(ONE_HALF);
        final BigDecimal decimalN = BigDecimal.valueOf(n);
        final BigDecimal halfInterval = decimalN.multiply(ONE_HALF);
        final SearchRange range;
        if (0 == cmpHalf) {
            range = new SearchRange(BigDecimal.ZERO, decimalN);
        } else if (cmpHalf > 0) {
            range = new SearchRange(halfInterval, decimalN);
        } else {
            range = new SearchRange(BigDecimal.ZERO, halfInterval);
        }
        do {
            final BigDecimal x = range.getMidpoint();
            final BigDecimal prob = computeCDF(decimalN, x);
            final BigDecimal targetDiff = prob.subtract(thresholdProbability);
            if (targetDiff.abs().compareTo(P_EPSILON) <= 0) {
                range.zeroInterval(x);
            } else {
                final boolean followToLowerHalf = targetDiff.signum() > 0;
                range.divide(followToLowerHalf);
            }
        } while (range.isWide());
        final BigDecimal foundX = range.midPoint;
        final BigDecimal probX = computeCDF(decimalN, foundX);
        return new ResultPair(foundX, probX);
    }

    private BigDecimal computeCDF(BigDecimal n, BigDecimal x) {
        int intN = n.intValueExact();
        BigDecimal sum = BigDecimal.ZERO;
        BigDecimal threshold = x.setScale(0, RoundingMode.FLOOR);
        boolean oddStep = (1L == (1L & threshold.longValueExact()));
        for (BigDecimal k=threshold; k.signum() >= 0; k = k.subtract(BigDecimal.ONE), oddStep = !oddStep) {
            BigDecimal fk = factorial(k);
            BigDecimal fnk = factorial(n.subtract(k));
            BigDecimal divisor = fk.multiply(fnk);
            BigDecimal xk = x.subtract(k);
            BigDecimal xkn = xk.pow(intN);
            BigDecimal value = xkn.divide(divisor, 120, RoundingMode.HALF_UP).stripTrailingZeros();
            if (oddStep) {
                sum = sum.subtract(value);
            } else {
                sum = sum.add(value);
            }
        }
        return sum;
    }

    private BigDecimal factorial(BigDecimal value) {
        if (0 == value.signum()) {
            return BigDecimal.ONE;
        }
        final BigDecimal cached = factorialCache.get(value);
        if (null != cached) {
            return cached;
        }
        final Map.Entry<BigDecimal, BigDecimal> cachedEntry = factorialCache.floorEntry(value);
        BigDecimal x = cachedEntry.getKey();
        BigDecimal factorial = cachedEntry.getValue();
        while (x.compareTo(value) < 0) {
            x = x.add(BigDecimal.ONE);
            factorial = factorial.multiply(x);
            factorialCache.put(x, factorial);
        }
        return factorial;
    }

    public static class ResultPair {
        private final BigDecimal x;
        private final BigDecimal p;

        public ResultPair(BigDecimal x, BigDecimal p) {
            this.x = x;
            this.p = p;
        }

        public BigDecimal getX() {
            return x;
        }

        public BigDecimal getP() {
            return p;
        }

        @Override
        public String toString() {
            return "X=" + x + ", p=" + p;
        }
    }

    private static class SearchRange {
        BigDecimal low;
        BigDecimal high;
        BigDecimal midPoint;

        SearchRange(BigDecimal low, BigDecimal high) {
            this.low = low;
            this.high = high;
            this.midPoint = low.add(high).multiply(ONE_HALF);
        }

        BigDecimal getMidpoint() {
            return midPoint;
        }

        void divide(boolean useLowerHalf) {
            if (useLowerHalf) {
                high = midPoint;
            } else {
                low = midPoint;
            }
            midPoint = low.add(high).multiply(ONE_HALF);
        }

        void zeroInterval(BigDecimal value) {
            low = value;
            high = value;
            midPoint = value;
        }

        boolean isWide() {
            final BigDecimal span = high.subtract(low);
            if (span.signum() <= 0) {
                return false;
            }
            return span.compareTo(X_EPSILON) > 0;
        }

        @Override
        public String toString() {
            return "[" + low + ", " + high + "], mid=" + midPoint;
        }
    }

}
