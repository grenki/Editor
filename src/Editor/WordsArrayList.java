package Editor;

import Editor.Word.Type;
import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;

class WordsArrayList {
    private final TIntArrayList start;
    private final TIntArrayList end;
    private final TByteArrayList type;

    WordsArrayList() {
        start = new TIntArrayList();
        end = new TIntArrayList();
        type = new TByteArrayList();
    }

    public int size() {
        return start.size();
    }

    public void add(int pos, Word word) {
        start.insert(pos, word.start);
        end.insert(pos, word.end);
        type.insert(pos, typeToByte(word.type));
    }

    public void add(int pos, ArrayList<Word> list) {
        int[] startIns = new int[list.size()];
        int[] endIns = new int[list.size()];
        byte[] typeIns = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            Word word = list.get(i);
            startIns[i] = word.start;
            endIns[i] = word.end;
            typeIns[i] = typeToByte(word.type);
        }

        start.insert(pos, startIns);
        end.insert(pos, endIns);
        type.insert(pos, typeIns);
    }

    public void set(int pos, Word word) {
        start.set(pos, word.start);
        end.set(pos, word.end);
        type.set(pos, typeToByte(word.type));
    }

    public Word get(int pos) {
        return new Word(start.get(pos), end.get(pos), byteToType(type.get(pos)));
    }

    public void remove(int startPos, int endPos) {  // delete interval [ )
        int length = endPos - startPos;

        start.remove(startPos, length);
        end.remove(startPos, length);
        type.remove(startPos, length);
    }

    public void clear() {
        start.clear();
        end.clear();
        type.clear();
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
