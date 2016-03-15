package Editor;

import gnu.trove.list.array.TByteArrayList;
import gnu.trove.list.array.TIntArrayList;

import java.util.ArrayList;

class Words {
    private final WordsArrayList data;
    private final TByteArrayList commentContinuousList;
    private final TIntArrayList length;

    private int row;
    private int pos;

    Words() {
        data = new WordsArrayList();
        length = new TIntArrayList();
        commentContinuousList = new TByteArrayList();
        length.add(0);
        commentContinuousList.add((byte) 0);
        commentContinuousList.add((byte) 0);
        row = 0;
        pos = 0;
    }

    private void popLast(int row, Word word) {
        int pos = find(row, 0);
        data.add(pos + length.get(row), word);
        length.set(row, length.get(row) + 1);
    }

    public void add(int row) {
        find(row - 1, 0);
        length.insert(row, 0);
        commentContinuousList.insert(row, (byte) 0);
    }

    public void addVoidLines(int row, int len) {
        find(row - 1, 0);
        length.insert(row, new int[len]);
        commentContinuousList.insert(row, new byte[len]);
    }

    public void setAll(int startRow, ArrayList<Word> list, TIntArrayList resLength) {
        data.add(find(startRow, 0), list);

        for (int i = 0; i < resLength.size(); i++) {
            length.set(startRow + i, resLength.get(i));
        }
    }

    public void clearDataLines(int startRow, int endRow) { //[]
        int endPos = find(endRow, length.get(endRow));
        int startPos = find(startRow, 0);

        for (int i = startRow; i < endRow; i++) {
            length.set(i, 0);
        }

        data.remove(startPos, endPos);
    }

    public void set(int row, int wordN, Word word) {
        int pos = find(row, 0);
        if (length.get(row) > wordN) {
            data.set(pos + wordN, word);
        } else if (length.get(row) == wordN) {
            popLast(row, word);
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void remove(int row, int endRow) { // [.,.]
        int pos = find(row, 0);
        int endPos = endRow >= length.size() ? data.size() : find(endRow, length.get(endRow));

        find(row, 0);

        data.remove(pos, endPos);
        length.remove(row, endRow + 1 - row);
        commentContinuousList.remove(row, endRow + 1 - row);
    }

    public Word get(int row, int wordN) {
        return data.get(find(row, wordN));
    }

    private int find(int row, int wordN) {
        if (this.row == row) {
            return pos + wordN;
        } else {
            if (this.row < row) {
                for (int i = this.row; i < row; i++) {
                    pos += length.get(i);
                }
            } else {
                if (this.row >> 1 < row) {
                    for (int i = row; i < this.row; i++) {
                        pos -= length.get(i);
                    }
                } else {
                    pos = 0;
                    for (int i = 0; i < row; i++) {
                        pos += length.get(i);
                    }
                }
            }

            this.row = row;
            return pos + wordN;
        }
    }

    public int size() {
        return length.size();
    }

    public int rowSize(int row) {
        return length.get(row);
    }

    public void clear() {
        data.clear();
        length.clear();
        commentContinuousList.clear();
        length.add(0);
        commentContinuousList.add((byte) 0);
        commentContinuousList.add((byte) 0);
        pos = 0;
        row = 0;
    }

    public void updateSize(int size) {
        if (length.size() < size) {
            for (int i = length.size(); i < size; i++) {
                add(length.size());
            }
        }

        if (length.size() > size) {
            remove(size, length.size() - 1);
        }
    }

    public boolean isCommentContinuous(int row) {
        return commentContinuousList.get(row) == 1;
    }

    public void setCommentContinuous(int row, boolean value) {
        commentContinuousList.set(row, value ? (byte) 1 : (byte) 0);
    }
}
