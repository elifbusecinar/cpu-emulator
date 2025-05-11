public class Memory {
    private final byte[] storage;

    public Memory(int capacity) {
        storage = new byte[capacity];
    }

    public byte read(int address) {
        return (address >= 0 && address < storage.length) ? storage[address] : 0;
    }

    public void write(int address, byte value) {
        if (address >= 0 && address < storage.length) {
            storage[address] = value;
        }
    }
}