import java.io.*;
import java.util.*;

public class CPUEmulator {
    private final int MAX_MEMORY = 65536;
    private int programCounter = 0, accumulator = 0, flag = 0;
    private int baseAddress;
    private boolean isHalted = false;
    private List<String> instructionSet = new ArrayList<>();
    private Memory memory = new Memory(MAX_MEMORY);
    private Cache cache = new Cache();

    private static final int START = 0, LOAD = 1, LOADM = 2, STORE = 3,
            CMPM = 4, CJMP = 5, JMP = 6, ADD = 7, ADDM = 8, SUB = 9, SUBM = 10,
            MUL = 11, MULM = 12, DISP = 13, HALT = 14;

    public CPUEmulator(String programPath, String configPath) throws IOException {
        loadConfig(configPath);
        loadInstructions(programPath);
    }

    private void loadConfig(String configPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(configPath))) {
            baseAddress = Integer.parseInt(reader.readLine().trim().substring(2), 16);
            programCounter = Integer.parseInt(reader.readLine().trim().substring(2), 16);
        }
    }

    private void loadInstructions(String programPath) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(programPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.matches("[01]{16}")) {
                    instructionSet.add(line.trim());
                }
            }
        }
    }

    public void run() {
        while (!isHalted) {
            String currentInstruction = fetchInstruction();
            if (currentInstruction == null) break;

            int opcode = Integer.parseInt(currentInstruction.substring(0, 4), 2);
            int operand = Integer.parseInt(currentInstruction.substring(4), 2);
            int address = baseAddress + operand;

            switch (opcode) {
                case START -> {}
                case LOAD -> accumulator = operand;
                case LOADM -> accumulator = Byte.toUnsignedInt(cache.read(address, memory));
                case STORE -> cache.write(address, (byte) accumulator, memory);
                case CMPM -> {
                    int memoryValue = Byte.toUnsignedInt(cache.read(address, memory));
                    flag = Integer.compare(accumulator & 0xFF, memoryValue);
                }
                case CJMP -> {
                    if (flag > 0) {
                        programCounter = address;
                        continue;
                    }
                }
                case JMP -> {
                    programCounter = address;
                    continue;
                }
                case ADD -> accumulator = (accumulator + operand) & 0xFFFF;
                case ADDM -> accumulator = (accumulator + Byte.toUnsignedInt(cache.read(address, memory))) & 0xFFFF;
                case SUB -> accumulator = (accumulator - operand) & 0xFFFF;
                case SUBM -> accumulator = (accumulator - Byte.toUnsignedInt(cache.read(address, memory))) & 0xFFFF;
                case MUL -> accumulator = (accumulator * operand) & 0xFFFF;
                case MULM -> accumulator = (accumulator * Byte.toUnsignedInt(cache.read(address, memory))) & 0xFFFF;
                case DISP -> {}
                case HALT -> isHalted = true;
                default -> System.err.println("Unknown opcode: " + opcode);
            }

            programCounter++;
        }

        System.out.printf("Value in accumulator: %d\n", accumulator);
        System.out.printf("Cache hit ratio: %.2f%%\n", cache.hitRate());
    }

    private String fetchInstruction() {
        int index = programCounter - baseAddress;
        if (index < 0 || index >= instructionSet.size()) {
            isHalted = true;
            return null;
        }
        return instructionSet.get(index);
    }
}