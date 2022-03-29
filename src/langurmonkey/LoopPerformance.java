package langurmonkey;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class LoopPerformance {

    private static int ROUNDS = 10;
    private static int SIZE_WARM = 5_000_000;
    private static int SIZE = 10_000_000;

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

        this.df = new DecimalFormat("#.0#");
    }

    private void test() {
        DATA_WARM = Arrays.asList(createArray(SIZE_WARM));
        DATA = Arrays.asList(createArray(SIZE));


        String sizeWarm;
        if(SIZE_WARM > 1e6) {

        }

        log.info(pad("Java version", 20) + ": " + System.getProperty("java.version"));
        log.info(pad("ROUNDS", 20) + ": " + formatNumber(ROUNDS));
        log.info(pad("SIZE", 20) + ": " + formatNumber(SIZE));
        log.info(pad("SIZE (warm)", 20) + ": " + formatNumber(SIZE_WARM));
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
        log.info("Testing...");
        for (Loop loop : loops) {
            loop.run(DATA, ROUNDS, true);
        }
    }

    private String pad(String str, int len) {
        String strPad = str;
        while (strPad.length() < 20) {
            strPad += " ";
        }
        return strPad;
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

    abstract class Loop {

        public void run(List<Byte> array, int rounds, boolean record) {
            String name = getClass().getSimpleName();
            String namePad = pad(name, 20);

            long[] elapsed = new long[rounds];
            for (int i = 0; i < rounds; i++) {
                long startTime = System.nanoTime();
                runLoop(array);
                elapsed[i] = System.nanoTime() - startTime;
            }
            if (record) {
                double meanMs = mean(elapsed) / 1000_000d;
                log.info(namePad + ": " + meanMs + " ms");
            }
        }

        private double mean(long[] array) {
            long total = 0;
            for (int i = 0; i < array.length; i++) {
                total += array[i];
            }
            return total / array.length;
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
            while (i < DATA.size()) {
                Byte s = DATA.get(i);
                i++;
            }
        }
    }

    class LoopIterator extends Loop {
        public void runLoop(List<Byte> array) {
            Iterator<Byte> iterator = DATA.iterator();
            while (iterator.hasNext()) {
                Byte next = iterator.next();
            }
        }
    }

    class LoopIteratorImplicit extends Loop {
        public void runLoop(List<Byte> array) {
            for (Byte next : DATA) {
            }
        }
    }

    class LoopForEach extends Loop {
        public void runLoop(List<Byte> array) {
            DATA.forEach((s) -> {
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
