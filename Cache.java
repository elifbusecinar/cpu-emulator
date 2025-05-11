public class Cache {
    private static final int CACHE_SIZE = 16;
    private static final int BLOCK_SIZE = 2;
    private final int[][] cacheLines = new int[CACHE_SIZE / BLOCK_SIZE][4];
    private int cacheHits = 0, cacheMisses = 0;

    public Cache() {
        for (int[] line : cacheLines) {
            line[0] = 0; 
            line[1] = -1;
        }
    }

    private int[] calculateIndices(int address) {
        int blockIndex = (address / BLOCK_SIZE) % cacheLines.length;
        int tag = address / (cacheLines.length * BLOCK_SIZE);
        int offset = address % BLOCK_SIZE;
        return new int[]{tag, blockIndex, offset};
    }

    public byte read(int address, Memory memory) {
        int[] indices = calculateIndices(address);
        int tag = indices[0], index = indices[1], offset = indices[2];
        int[] block = cacheLines[index];

        if (block[0] == 1 && block[1] == tag) {
            cacheHits++;
            return (byte) block[2 + offset];
        } else {
            cacheMisses++;
            int base = address - offset;
            byte b0 = memory.read(base), b1 = memory.read(base + 1);
            block[0] = 1;
            block[1] = tag;
            block[2] = b0 & 0xFF;
            block[3] = b1 & 0xFF;
            return (offset == 0) ? b0 : b1;
        }
    }

    public void write(int address, byte value, Memory memory) {
        int[] indices = calculateIndices(address);
        int tag = indices[0], index = indices[1], offset = indices[2];
        memory.write(address, value);
        int[] block = cacheLines[index];

        if (block[0] == 1 && block[1] == tag) {
            cacheHits++;
            block[2 + offset] = value & 0xFF;
        } else {
            cacheMisses++;
            int base = address - offset;
            byte b0 = memory.read(base), b1 = memory.read(base + 1);
            block[0] = 1;
            block[1] = tag;
            block[2] = b0 & 0xFF;
            block[3] = b1 & 0xFF;
            block[2 + offset] = value & 0xFF;
        }
    }

    public double hitRate() {
        int totalAccesses = cacheHits + cacheMisses;
        return totalAccesses == 0 ? 0 : (double) cacheHits / totalAccesses * 100.0;
    }
}