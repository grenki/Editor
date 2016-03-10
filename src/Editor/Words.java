package Editor;

import java.util.ArrayList;

class Words {
    private final WordsArrayList data;
    private final ArrayList<Boolean> commentContinuousList;
    private final ArrayList<Integer> length;

    private int row;
    private int pos;

    Words() {
        data = new WordsArrayList();
        length = new ArrayList<>();
        commentContinuousList = new ArrayList<>();
        length.add(0);
        commentContinuousList.add(false);
        commentContinuousList.add(false);
        row = 0;
        pos = 0;
    }

    public void popLast(int row, Word word) {
        int pos = find(row, 0);
        data.add(pos + length.get(row), word);
        length.set(row, length.get(row) + 1);
    }

    public void add(int row) {
        length.add(row, 0);
        commentContinuousList.add(row, false);
    }

    public void clearDataLine(int row) {
        int pos = find(row, 0);
        data.remove(pos, pos + length.get(row));
        //data.subList(pos, pos + length.get(row)).clear();
        length.set(row, 0);
    }

    /*public void add(int row, ArrayList<Word> list) {
        int pos = find(row, 0);
        data.addAll(pos, list);
        length.add(row, list.size());
        commentContinuousList.add(row, false);
    }*/

    /*public void set(int row, ArrayList<Word> list) {
        int pos = find(row, 0);
        data.remove(pos, pos + length.get(row));
        //data.subList(pos, pos + length.get(row)).clear();

        data.addAll(pos, list);
        length.add(row, list.size());
    }*/

    public void remove(int row) {
        int pos = find(row, 0);
        //data.subList(pos, pos + length.get(row)).clear();
        data.remove(pos, pos + length.get(row));
        length.remove(row);
        commentContinuousList.remove(row);
    }

    public void remove(int row, int endRow) {
        int pos = find(row, 0);
        int endPos = endRow >= length.size() ? data.size() : find(endRow, length.get(endRow));

        //data.subList(pos, endPos).clear();
        data.remove(pos, endPos);
        length.subList(row, endRow + 1).clear();
        commentContinuousList.subList(row, endRow + 1).clear();
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
                pos = 0;
                for (int i = 0; i < row; i++) {
                    pos += length.get(i);
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
        commentContinuousList.add(false);
        commentContinuousList.add(false);
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
        return commentContinuousList.get(row);
    }

    public void setCommentContinuous(int row, boolean value) {
        commentContinuousList.set(row, value);
    }
}
