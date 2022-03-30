package langurmonkey;

import java.text.DecimalFormat;
import java.lang.Math;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.lang.management.ManagementFactory;

public class LoopPerformance {

    private static int ROUNDS = 10;
    private static int SIZE_WARM = 5_000_000;
    private static int SIZE = 50_000_000;

    public static void main(String[] argv) {
        if(argv != null && argv.length > 0) {
            // SIZE
            try {
                SIZE = Integer.parseInt(argv[0].trim());
            }catch(Exception e) {
                help();
                return;
            }

            // ROUNDS
            if(argv.length > 1) {
                try {
                    ROUNDS = Integer.parseInt(argv[1].trim());
                }catch(Exception e) {
                    help();
                    return;
                }
            }
        }
        // RUN
        new LoopPerformance().test();
    }

    private static void help() {
        System.out.println("Usage:");
        System.out.println("loopperformance [SIZE] [ROUNDS]");
        System.out.println();
        System.out.println("SIZE\tnumber of elements in the array to iterate");
        System.out.println("ROUNDS\tnumber of rounds to run each batch");
    }

    protected Logger log;
    private List<Byte> DATA_WARM;
    private List<Byte> DATA;

    private DecimalFormat df;

    public LoopPerformance() {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%4$-2s] %5$s %n");
        log = Logger.getLogger(getClass().getSimpleName());
        log.setLevel(Level.INFO);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new SimpleFormatter());

        this.df = new DecimalFormat("0.0#");
    }

    private void test() {
        DATA_WARM = Arrays.asList(createArray(SIZE_WARM));
        DATA = Arrays.asList(createArray(SIZE));

        log.info(pad("Java version", 22) + System.getProperty("java.version"));
        log.info(pad("ROUNDS", 22) + formatNumber(ROUNDS, 22));
        log.info(pad("SIZE", 22) + formatNumber(SIZE, 22));
        log.info(pad("SIZE (warm)", 22) + formatNumber(SIZE_WARM, 22));
        log.info("");

        Loop[] loops = new Loop[] {
            new LoopFor(),
            new LoopWhile(),
            new LoopForEach(),
            new LoopIterator(),
            new LoopIteratorImplicit()
        };

        // Warm up
        log.info("Warming up...");
        for (Loop loop : loops) {
            loop.run(DATA_WARM, 1, false);
        }
        log.info("Warm-up completed");
        log.info("");

        // Test
        log.info(pad("LOOP VARIANT", 22) + pad("WALL CLOCK TIME", 28) + pad("CPU TIME", 28));
        for (Loop loop : loops) {
            loop.run(DATA, ROUNDS, true);
        }

    }

    private String pad(String str, int len) {
        String strPad = str;
        while (strPad.length() < len) {
            strPad += " ";
        }
        return strPad;
    }

    private String format(double num) {
        return df.format(num);
    }

    private String formatNumber(int num) {
        if(num > 1e9) {
            return df.format(num / 1_000_000_000d) + " G";
        } else if(num > 1e6) {
            return df.format(num / 1_000_000d) + " M";
        } else if(num > 1e3) {
            return df.format(num / 1_000d) + " k";
        } else {
            return Integer.toString(num);
        }
    }
    
    private String formatNumber(int num, int pad) {
        return pad(formatNumber(num), 22);
    }



    abstract class Loop {

        public void run(List<Byte> array, int rounds, boolean record) {
            String name = getClass().getSimpleName().substring(4, getClass().getSimpleName().length());
            String namePad = pad(name, 22);

            long[][] elapsed = new long[2][rounds];
            for (int i = 0; i < rounds; i++) {
                long clockStart = System.nanoTime();
                long cpuStart = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
                runLoop(array);
                elapsed[0][i] = System.nanoTime() - clockStart;
                elapsed[1][i] = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - cpuStart;
            }
            if (record) {
                double meanClockMs = mean(elapsed[0]) / 1_000_000d;
                double stdevClock = stdev(elapsed[0], meanClockMs);

                double meanCpuMs = mean(elapsed[1]) / 1_000_000d;
                double stdevCpu = stdev(elapsed[1], meanCpuMs);
                
                log.info(namePad + pad(meanClockMs + " (±" + format(stdevCpu) + ") ms", 28) + pad(meanCpuMs + " (±" + format(stdevClock) + ") ms", 28));
            }
        }

        private double mean(long[] array) {
            long total = 0;
            for (int i = 0; i < array.length; i++) {
                total += array[i];
            }
            return total / array.length;
        }

        private double stdev(long[] array, double mean) {
            double sum = 0;
            double n = array.length;
            for(int i = 0; i < n; i++) {
                sum += Math.pow(array[i] / 1_000_000d - mean, 2.0);
            }
            return Math.sqrt(sum / n);
        }

        abstract void runLoop(List<Byte> array);
    }

    class LoopFor extends Loop {
        public void runLoop(List<Byte> array) {
            for (int i = 0; i < array.size(); i++) {
                Byte s = array.get(i);
            }
        }
    }

    class LoopWhile extends Loop {
        public void runLoop(List<Byte> array) {
            int i = 0;
            while (i < array.size()) {
                Byte s = array.get(i);
                i++;
            }
        }
    }

    class LoopIterator extends Loop {
        public void runLoop(List<Byte> array) {
            Iterator<Byte> iterator = array.iterator();
            while (iterator.hasNext()) {
                Byte next = iterator.next();
            }
        }
    }

    class LoopIteratorImplicit extends Loop {
        public void runLoop(List<Byte> array) {
            for (Byte next : array) {
            }
        }
    }

    class LoopForEach extends Loop {
        public void runLoop(List<Byte> array) {
            array.forEach((s) -> {
            });
        }
    }

    private static Byte[] createArray(int size) {
        Byte sArray[] = new Byte[size];
        for (int i = 0; i < size; i++) {
            sArray[i] = (byte) i;
        }
        return sArray;
    }
}
