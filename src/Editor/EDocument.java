package Editor;

import gnu.trove.list.array.TIntArrayList;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class EDocument {

    private static final Pattern javaFilePattern = Pattern.compile(".*\\.java");
    private static final Pattern jsFilePattern = Pattern.compile(".*\\.js");
    private static final String TAB = "    ";
    private final TIntArrayList length;
    private final Words dataInWords;
    private final Parser parser;
    private final Clipboard clipboard;
    private final StringBuilder data;
    private final JScrollBar scrollBar;
    private int width;
    private int height;
    private int widthOffset;
    private int heightOffset;
    private int column;
    private int row;
    private int pos;
    private boolean insert;
    private boolean isShiftPressed;
    private boolean existSelection;
    private int startSelectionRow;
    private int startSelectionColumn;
    private FileType fileType;

    public EDocument(JScrollBar scrollBar) {

        column = 0;
        row = 0;
        pos = 0;

        insert = false;
        heightOffset = 0;
        widthOffset = 0;
        isShiftPressed = false;
        existSelection = false;
        fileType = FileType.Text;

        this.scrollBar = scrollBar;

        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        data = new StringBuilder();
        length = new TIntArrayList();
        length.add(0);

        dataInWords = new Words();

        parser = new Parser(this, dataInWords, data, length);

        updatePosition();
    }

    public void recreateDocument(List<String> initData) {
        column = 0;
        row = 0;
        pos = 0;
        insert = false;
        heightOffset = 0;
        widthOffset = 0;
        isShiftPressed = false;
        existSelection = false;
        fileType = FileType.Text;

        data.delete(0, data.length());
        length.clear();
        dataInWords.clear();

        if (initData != null && initData.size() > 0) {
            for (String s: initData) {
                data.append(s);
                data.append('\n');
                length.add(s.length());
            }
            data.delete(data.length() - 1, data.length());
        } else {
            length.add(0);
        }

        updatePosition();
    }

    // add or remove line
    private void addLine(int row, int len) {
        length.insert(row, len);
        if (fileType != FileType.Text) {
            parser.bracketLightOff();
            dataInWords.add(row);
        }
    }

    private void addLines(int row, int[] len) {
        length.insert(row, len);
        if (fileType != FileType.Text) {
            parser.bracketLightOff();
            dataInWords.addVoidLines(row, len.length);
        }
    }

    private void removeLine(int row) {
        removeLines(row, row);
    }

    private void removeLines(int startRow, int endRow) {
        length.remove(startRow, endRow + 1 - startRow);
        if (fileType != FileType.Text) {
            parser.bracketLightOff();
            dataInWords.remove(startRow, endRow);
        }

    }

    // Update
    private void updateWithoutChanges() {
        updateWithChanges(-1, -1);
    }

    private void updateWithChanges(int startRow) {
        updateWithChanges(startRow, startRow);
    }

    private void updateWithChanges(int startRow, int endRow) {
        updatePosition();

        if (startRow >= 0 && endRow >= 0) {
            isShiftPressed = false;
        }

        if (fileType != FileType.Text) {
            parser.bracketLightOff();

            if (startRow >= 0 && endRow >= 0) {
                parser.parse(startRow, endRow);
            }

            parser.bracketLight(column, row, pos);
        }
    }

    private void updateOffset() {
        if (heightOffset + height >= length.size()) {
            heightOffset = length.size() - height;
        }

        if (heightOffset < 0) {
            heightOffset = 0;
        }

        if (widthOffset < 0) {
            widthOffset = 0;
        }
        updateScrollBar();
    }

    private void updateOffsetOnCaret() {
        if (column > widthOffset + width) {
            widthOffset = column - width;
        }

        if (column < widthOffset) {
            widthOffset = column;
        }

        if (row > height + heightOffset) {
            heightOffset = row - height;
        }

        if (row < heightOffset) {
            heightOffset = row;
        }

    }

    private void updateScrollBar() {
        int heightOffset = this.heightOffset;
        scrollBar.setMaximum(length.size() - 1);
        scrollBar.setValue(heightOffset);
        scrollBar.setVisibleAmount(height);
    }

    private void updatePosition(){

        if (column < 0) {
            column = 0;
        }

        if (row >= length.size()) {
            row = length.size() - 1;
        }

        if (row < 0) {
            row = 0;
        }

        if (column > length.get(row)) {
            column = length.get(row);
        }

        updatePos();
        updateOffsetOnCaret();
        updateScrollBar();
    }

    private void updatePos() {
        pos = getPos(row, column);
    }

    // Selection + change selection area

    public void selectAll() {
        startSelectionColumn = 0;
        startSelectionRow = 0;
        row = length.size() - 1;
        column = length.get(row);
        existSelection = true;
    }

    public void paste() {
        if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
            deleteSelection();
            try {
                insertString((String) clipboard.getData(DataFlavor.stringFlavor));
            } catch (IOException | UnsupportedFlavorException e) {
                System.out.println("Problem with clipboard");
                e.printStackTrace();
            }
        }
    }

    public void copy() {
        if (isExistSelection()) {
            int[] selectionInterval = getSelectionInterval();
            int startColumn = selectionInterval[0];
            int startRow = selectionInterval[1];
            int endColumn = selectionInterval[2];
            int endRow = selectionInterval[3];

            int startPos = getPos(startRow, startColumn);
            int endPos = getPos(endRow, endColumn);

            String res = data.substring(startPos, endPos);

            clipboard.setContents(new StringSelection(res), null);
        }
    }

    public void cut() {
        if (isExistSelection()) {
            copy();
            deleteSelection();
        }
    }

    private void insertString(String s) {
        existSelection = false;
        final int startChangesRow = row;
        StringBuilder sb = new StringBuilder();

        int len = column;
        int endLen = length.get(row) - len;

        TIntArrayList insLength = new TIntArrayList();
        for (int i = 0; i < s.length(); i++) {
            if (Character.codePointAt(s, i) == 9) {
                sb.append(TAB);
            }
            else {
                sb.append(s.charAt(i));
            }

            if (s.charAt(i) != '\n') {
                len++;
            } else {
                if (row == startChangesRow) {
                    length.set(startChangesRow, len);
                } else {
                    insLength.add(len);
                }
                row++;
                len = 0;
            }
        }
        if (row == startChangesRow) {
            length.set(startChangesRow, len + endLen);
        } else {
            insLength.add(len + endLen);
            addLines(startChangesRow + 1, insLength.toArray());
        }
        column = len;
        data.insert(pos, sb);
        updateWithChanges(startChangesRow, row);
    }

    private void deleteSelection() {
        if (!isExistSelection()) {
            existSelection = false;
            return;
        }
        existSelection = false;

        int[] selectionInterval = getSelectionInterval();
        column = selectionInterval[0];
        row = selectionInterval[1];
        int endColumn = selectionInterval[2];
        int endRow = selectionInterval[3];

        int startPos = getPos(row, column);
        int endPos = getPos(endRow, endColumn);

        data.delete(startPos, endPos);
        if (row != endRow) {
            length.set(row, column + length.get(endRow) - endColumn);
            removeLines(row + 1, endRow);
        } else {
            length.set(row, length.get(row) - (endPos - startPos));
        }

        updateWithChanges(row);
    }

    // Change char

    public void insertChar(char character) {
        deleteSelection();

        String ch = Character.toString(character);
        if (!insert) {
            data.insert(pos, ch);
            if (ch.equals("\n")) {
                addLine(row + 1, length.get(row) - column);
                length.set(row, column);
                row++;
                column = 0;
                updateWithChanges(row - 1, row);
            } else {
                length.set(row, length.get(row) + 1);
                column++;
                updateWithChanges(row);
            }
        } else {
            if (ch.equals("\n")) {
                row++;
                column = 0;
                if (row == length.size()) {
                    addLine(length.size(), 0);
                    data.append('\n');
                    updateWithChanges(row);
                } else {
                    updateWithoutChanges();
                }
            } else {
                if (column <= length.get(row) - 1) {
                    data.replace(pos, pos + 1, ch);
                } else {
                    data.insert(pos, ch);
                    length.set(row, length.get(row) + 1);
                }
                column++;
                updateWithChanges(row);
            }
        }
    }

    public void backspace() {
        if (isExistSelection()) {
            deleteSelection();
        }
        else {
            if (column == 0) { // concat Lines
                if (row > 0) {
                    row--;
                    column = length.get(row);
                    length.set(row, column + length.get(row + 1));
                    removeLine(row + 1);
                    data.delete(pos - 1, pos);
                }
            } else {
                column--;
                length.set(row, length.get(row) - 1);
                data.delete(pos - 1, pos);
            }

            existSelection = false;
            updateWithChanges(row);
        }
    }

    public void delete() {
        if (isExistSelection()) {
            deleteSelection();
        }
        else {
            if (row == length.size() - 1 && column == length.get(row)) {
                return;
            }

            right();
            backspace();
        }
    }

    public void insertTab() {
        insertString(TAB);
    }

    // Mouse

    public  void mousePressed(int column, int row) {
        this.column = column;
        this.row = row;
        updatePosition();
        startSelectionColumn = this.column;
        startSelectionRow = this.row;
        existSelection = true;
    }

    public  void mouseMoved(int column, int row) {
        this.column = column;
        this.row = row;
        updateWithoutChanges();
    }

    // Navigation

    public void right() {
        if (column < length.get(row)) {
            column++;
        }
        else if (row < length.size() - 1) {
            row ++;
            column = 0;
        }
        updateWithoutChanges();
    }

    public void left() {
        if (column > 0) {
            column--;
        } else {
            if (row > 0) {
                row--;
                column = Integer.MAX_VALUE;
            }
        }
        updateWithoutChanges();
    }

    public void up() {
        row--;
        updateWithoutChanges();
    }

    public void down() {
        row++;
        updateWithoutChanges();
    }

    public void home() {
        column = 0;
        updateWithoutChanges();
    }

    public void end() {
        column = length.get(row);
        updateWithoutChanges();
    }

    public void pageUp() {
        heightOffset = Math.max(0, heightOffset - height);
        row += -height;
        updateWithoutChanges();
    }

    public void pageDown() {
        heightOffset = Math.max(0, Math.min(length.size() - height, heightOffset + height));
        row += height;
        updateWithoutChanges();
    }

    // Setters

    public void setExistSelectionFalse() {
        if (!isShiftPressed) {
            existSelection = false;
        }
    }

    public void setWindowSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setShiftPressed(boolean shiftPressed) {
        if (!isExistSelection() && shiftPressed) {
            startSelectionRow = row;
            startSelectionColumn = column;
        }

        this.isShiftPressed = shiftPressed;

        if (shiftPressed) {
            existSelection = true;
        }
    }

    public void switchInsert(){
        insert = !insert;
    }

    public void setFileName(String s, boolean open) {

        Matcher javaFile = javaFilePattern.matcher(s);
        Matcher jsFile = jsFilePattern.matcher(s);

        if (javaFile.matches()) {
            fileType = FileType.Java;
        } else if (jsFile.matches()) {
            fileType = FileType.JS;
        } else {
            fileType = FileType.Text;
        }

        boolean needParse = parser.setFileType(fileType);
        if (fileType != FileType.Text) {
            if (open) {
                parser.forceParse(0, length.size());
                updateWithoutChanges();
            } else {
                if (needParse) {
                    parser.forceParse(0, length.size());
                    updateWithoutChanges();
                }
            }
        }
    }

    public void updateHeightOffset(int diff) {
        heightOffset += diff;
        updateOffset();
    }

    public boolean isExistSelection() {
        return existSelection && !(startSelectionColumn == column && startSelectionRow == row);
    }

    // Getters

    public int getHeightOffset() {
        return heightOffset;
    }

    public void setHeightOffset(int value) {
        heightOffset = value;
    }

    public int getWidthOffset() {
        return widthOffset;
    }

    public int getCaretRow() {
        return row;
    }

    public int getCaretColumn() {
        return column;
    }

    public int getPos(int row, int column) {
        int res = 0;
        for (int i = 0; i < row; i++) {
            res += length.get(i) + 1;
        }
        res += column;

        return res;
    }

    int[] getSelectionInterval() {
        if (startSelectionRow > row || startSelectionRow == row && startSelectionColumn > column) {
            return new int[] {column, row, startSelectionColumn, startSelectionRow};
        }

        return new int[] {startSelectionColumn, startSelectionRow, column, row};
    }

    public boolean isFileTypeText() {
        return fileType == FileType.Text;
    }

    // Get all data

    public Words getAllDataInWords() {
        return dataInWords;
    }

    public List<CharSequence> getAllDataInLines(){
        ArrayList<String> res = new ArrayList<>(length.size());
        int pos = 0;

        for (int i = 0; i < length.size(); i++) {
            int aLength = length.get(i);
            res.add(data.substring(pos, pos + aLength));
            pos += aLength + 1;
        }
        return (List) res;
    }

    public StringBuilder getAllDataInString() {
        return data;
    }

    public TIntArrayList getAllLinesLength() {
        return length;
    }
}