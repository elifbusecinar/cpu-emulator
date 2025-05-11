import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java Main <program_file> <config_file>");
            return;
        }

        try {
            CPUEmulator cpu = new CPUEmulator(args[0], args[1]);
            cpu.run();
        } catch (IOException e) {
            System.err.println("Initialization error: " + e.getMessage());
        }
    }
}
