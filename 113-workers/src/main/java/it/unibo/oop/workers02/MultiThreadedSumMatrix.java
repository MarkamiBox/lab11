package it.unibo.oop.workers02;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * MultiThreadedSumMatrix.
 */
public final class MultiThreadedSumMatrix implements SumMatrix {
    private final int nthread;

    /**
     * Builder.
     *
     * @param n the number of threads
     */
    public MultiThreadedSumMatrix(final int n) {
        this.nthread = n;
    }

    @Override
    public double sum(final double[][] matrix) {
        final int size = matrix.length % nthread + matrix.length / nthread;
        // List of workers
        final List<Worker> workers = IntStream.iterate(0, start -> start + size)
                .limit(nthread)
                .mapToObj(start -> new Worker(matrix, start, size))
                .collect(Collectors.toList());
        // Start workers
        workers.forEach(Thread::start);
        double sum = 0;
        // Join workers
        for (final Worker w : workers) {
            try {
                w.join();
                sum += w.getResult();
            } catch (final InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        return sum;
    }

    private static class Worker extends Thread {
        private final double[][] matrix;
        private final int startpos;
        private final int nelem;
        private double res;

        /**
         * Build a new worker.
         *
         * @param matrix   the matrix to sum
         * @param startpos the initial position for this worker
         * @param nelem    the no. of elems to sum up for this worker
         */
        @SuppressWarnings("PMD.ArrayIsStoredDirectly")
        Worker(final double[][] matrix, final int startpos, final int nelem) {
            super();
            this.matrix = matrix;
            this.startpos = startpos;
            this.nelem = nelem;
        }

        @Override
        @SuppressWarnings("PMD.SystemPrintln")
        public synchronized void run() {
            System.out.println("Working from position " + startpos + " to position " + (startpos + nelem - 1));
            for (int i = startpos; i < matrix.length && i < startpos + nelem; i++) {
                for (int j = 0; j < matrix[i].length; j++) {
                    this.res += matrix[i][j];
                }
            }
        }

        /**
         * Returns the result of summing up the double within the matrix.
         *
         * @return the sum of every element in the array
         */
        public synchronized double getResult() {
            return this.res;
        }

    }
}