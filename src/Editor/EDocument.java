package Editor;

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

    private final ArrayList<ArrayList<Word>> dataInWords;
    public final ArrayList<Integer> length;

    private StringBuilder data;
    private final Parser parser;
    private final Clipboard clipboard;

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
    private JScrollBar scrollBar;

    private static final Pattern javaFilePattern = Pattern.compile(".*\\.java");
    private static final Pattern jsFilePattern = Pattern.compile(".*\\.js");
    private static final String TAB = "    ";

    public EDocument() {

        column = 0;
        row = 0;
        pos = 0;

        insert = false;
        heightOffset = 0;
        widthOffset = 0;
        isShiftPressed = false;
        existSelection = false;
        fileType = FileType.Text;

        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        data = new StringBuilder();
        length = new ArrayList<>(1);
        length.add(0);

        dataInWords = new ArrayList<>();

        parser = new Parser(this, dataInWords, data, length);
    }

    public void recreateDocument(List<String> initData) {
        column = 0;
        row = 0;
        insert = false;
        heightOffset = 0;
        widthOffset = 0;
        isShiftPressed = false;
        existSelection = false;
        fileType = FileType.Text;

        data.delete(0, data.length());
        length.clear();

        if (initData != null) {
            for (String s: initData) {
                data.append(s);
                data.append('\n');
                length.add(s.length());
            }
            data.delete(data.length() - 1, data.length());

        } else {
            length.add(0);
        }
    }

    // add or remove line
    private void addLine(int row, int len) {
        length.add(row, len);
        if (fileType != FileType.Text) {
            dataInWords.add(row, new ArrayList<>());
            parser.addLine(row);
        }
    }

    private void removeLine(int row) {
        length.remove(row);
        if (fileType != FileType.Text) {
            dataInWords.remove(row);
            parser.removeLine(row);
        }
    }

    private void removeLines(int startRow, int endRow) {
        endRow++;
        length.subList(startRow, endRow).clear();
        if (fileType != FileType.Text) {
            dataInWords.subList(startRow, endRow).clear();
            parser.removeLines(startRow, endRow);
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
        // TODO
        if (fileType != FileType.Text) {
            if (startRow >= 0) {
                isShiftPressed = false;
                if (endRow >= 0) {
                    parser.parse(startRow, endRow);
                }
            }

            parser.bracketLightOff();
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

        if (column < 0 && row <= 0) {
            column = 0;
        }

        if (column < 0){
            row--;
            column = length.get(row);
        }

        if (row < 0) {
            row = 0;
        }

        if (row >= length.size()) {
            row = length.size() - 1;
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

    public synchronized void paste() {
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

    public synchronized void cut() {
        if (isExistSelection()) {
            copy();
            deleteSelection();
        }
    }

    private void insertString(String s) {
        existSelection = false;
        int startChangesRow = row;
        StringBuilder sb = new StringBuilder();

        int len = column;
        int endLen = length.get(row) - len;

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
                length.set(row, len);
                row++;
                len = 0;
                addLine(row, 0);
            }
        }

        length.set(row, len + endLen);
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
        //System.out.println(startPos + " " + endPos);

        data.delete(startPos, endPos);
        if (row != endRow) {
            length.set(row, column + length.get(endRow) - endColumn);
            removeLines(row + 1, endRow);
            //length.subList(row + 1, endRow + 1).clear();
            //removeLines(row + 1, endRow); //tODO ??
        } else {
            length.set(row, length.get(row) - (endPos - startPos));
        }

        updateWithChanges(row);
    }

    // Change char

    public synchronized void insertChar(char character) {
        deleteSelection();

        String ch = Character.toString(character);
        if (!insert) {
            data.insert(pos, ch);
            if (ch.equals("\n")) {
                addLine(row + 1, length.get(row) - column);
                //length.add(row + 1, length.get(row) - column);
                length.set(row, column);
                row++;
                column = 0;
                updateWithChanges(row - 1, row);
            } else {
                length.set(row, length.get(row) + 1);
                column++;
                updateWithChanges(row);
            }
        }
        else {
            if (ch.equals("\n")) {
                row++;
                column = 0;
                if (row == length.size()) {
                    //length.add(0);
                    addLine(length.size(), 0);
                    updateWithChanges(row);
                }
                else {
                    updateWithoutChanges();
                }
            }
            else {
                if (column <= length.get(row) - 1) {
                    data.replace(pos, pos + 1, ch);
                }
                else {
                    data.insert(pos, ch);
                    length.set(row, length.get(row) + 1);
                }
                column++;
                updateWithChanges(row);
            }
        }
    }

    public synchronized void backspace() {
        if (isExistSelection()) {
            deleteSelection();
        }
        else {
            if (column == 0) { // concat Lines
                if (row > 0) {
                    row--;
                    column = length.get(row);
                    length.set(row, column + length.get(row + 1));
                    //length.remove(row + 1);
                    removeLine(row + 1);
                    data.delete(pos, pos + 1);
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

    public synchronized void delete() {
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

    public synchronized void insertTab() {
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
        column--;
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

    public synchronized void setFileName(String s, boolean open) {

        Matcher javaFile = javaFilePattern.matcher(s);
        Matcher jsFile = jsFilePattern.matcher(s);

        if (javaFile.matches()) {
            fileType = FileType.Java;
        } else if (jsFile.matches()) {
            fileType = FileType.JS;
        } else {
            fileType = FileType.Text;
        }
        // TODO
        boolean needParse = parser.setFileType(fileType);
        if (fileType != FileType.Text) {
            if (open) {
                parser.forceParse(0, height + 2);
                updateWithoutChanges();
                new Thread(() -> {
                    synchronized (this) {
                        parser.forceParse(height + 2, length.size());
                    }
                }).start();
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

    public void setScrollBar(JScrollBar scrollBar) {
        this.scrollBar = scrollBar;
        updateScrollBar();
    }

    public void setOffsetFromScrollBar(int value) {
        heightOffset = value;
    }

    // Getters

    public boolean isExistSelection() {
        return existSelection && !(startSelectionColumn == column && startSelectionRow == row);
    }

    public int getHeightOffset() {
        return heightOffset;
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
        /*System.out.println("----------------");
        System.out.println(data);
        System.out.println("---------");
        length.forEach(System.out :: println);*/
        int k = 0;
        for (int i = 0; i < length.size(); i++) {
            k += length.get(i) + 1;
        }
        if (k - 1 != data.length()) {
            System.out.println("ERROR " + k + " " + data.length());
        }

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

    public int getWidth() {
        return width;
    }

    // Get all data

    public ArrayList<ArrayList<Word>> getAllDataInWords() {
        return dataInWords;
    }

    public List<CharSequence> getAllDataInLines(){
        ArrayList<String> res = new ArrayList<>(length.size());
        int pos = 0;
        for (Integer aLength : length) {
            res.add(data.substring(pos, pos + aLength));
            pos += aLength + 1;
        }
        return (List) res;
    }

    public StringBuilder getAllDataInString() {
        return data;
    }

    public ArrayList<Integer> getAllLinesLength() {
        return length;
    }

    /*private void verification() {
        int k = 0;
        try {
            for (int i = 0; i < dataInChars.size(); i++) {
                StringBuilder line = dataInChars.get(i);
                for (int j = 0; j < line.length(); j++) {
                    if (line.charAt(j) != data.charAt(k)) {
                        System.out.println("--------data-------");
                        System.out.println(i + " " + j + " " + getPos());
                        System.out.println(data);
                        System.out.println("--dInChars----");
                        dataInChars.forEach(System.out :: println);

                        System.out.println("---------------");
                    }
                    k++;
                }
                k++;
            }
        }
        catch (Exception e) {
            System.out.println("wrong");
        }
    }*/
}