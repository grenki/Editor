package Editor;

import Editor.Word.Type;

import java.util.Arrays;

class WordsArrayList {
    private final static int initialCapacity = 100;
    private int[] start;
    private int[] end;
    private byte[] type;
    private int capacity;
    private int size;

    WordsArrayList() {
        this(initialCapacity);
    }

    WordsArrayList(int capacity) {
        this.capacity = capacity;
        init();
    }

    private void init() {
        size = 0;
        start = new int[capacity];
        end = new int[capacity];
        type = new byte[capacity];
    }

    public int size() {
        return size;
    }

    public void add(int pos, Word word) {
        size++;
        updateCapacityUp();
        //System.out.println(size + " " + capacity);

        shiftArrays(pos, 1, true);
        start[pos] = word.start;
        end[pos] = word.end;
        type[pos] = typeToByte(word.type);
    }

    public Word get(int pos) {
        return new Word(start[pos], end[pos], byteToType(type[pos]));
    }

    public void remove(int pos) {
        remove(pos, pos + 1);
    }

    public void remove(int startPos, int endPos) {  // delete interval [..., ...)
        int length = endPos - startPos;
        size -= length;

        shiftArrays(startPos, length, false);

        updateCapacityDown();
    }

    public void clear() {
        capacity = initialCapacity;
        init();
    }

    private void updateCapacityUp() {
        if (size >= capacity) {
            capacity = size + (size >> 1);

            updateArraysSize(capacity);
        }
    }

    private void updateCapacityDown() {
        if (size > initialCapacity && size < capacity >> 1) {
            capacity = size + (size >> 1);

            updateArraysSize(capacity);
        }
    }

    private void updateArraysSize(int capacity) {
        start = Arrays.copyOf(start, capacity);
        end = Arrays.copyOf(end, capacity);
        type = Arrays.copyOf(type, capacity);
    }

    private void shiftArrays(int offset, int shift, boolean right) {
        int shiftRight = right ? shift : 0;
        int shiftLeft = !right ? shift : 0;
        System.arraycopy(start, offset + shiftLeft, start, offset + shiftRight, size - offset);
        System.arraycopy(end, offset + shiftLeft, end, offset + shiftRight, size - offset);
        System.arraycopy(type, offset + shiftLeft, type, offset + shiftRight, size - offset);
    }

    private Word.Type byteToType(byte type) {
        switch (type) {
            case 1:
                return Type.Key;
            case 2:
                return Type.Identifier;
            case 3:
                return Type.Comment;
            case 4:
                return Type.Bracket;
            case 5:
                return Type.Other;
            case 6:
                return Type.BracketLight;
            default:
                throw new IllegalArgumentException();
        }
    }

    private byte typeToByte(Type type) {
        switch (type) {
            case Key:
                return 1;
            case Identifier:
                return 2;
            case Comment:
                return 3;
            case Bracket:
                return 4;
            case Other:
                return 5;
            case BracketLight:
                return 6;
            default:
                throw new IllegalArgumentException();
        }
    }
}
