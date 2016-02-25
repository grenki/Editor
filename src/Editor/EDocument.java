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
import java.util.stream.Collectors;

class EDocument {

    private final ArrayList<ArrayList<Word>> dataInWords;
    private final ArrayList<StringBuilder> dataInChars;
    private final Parser parser;
    private final Clipboard clipboard;
    private final String TAB = "    ";
    private int width;
    private int height;
    private int widthOffset;
    private int heightOffset;
    private int column;
    private int row;
    private boolean insert;
    private boolean isShiftPressed;
    private boolean existSelection;
    private int startSelectionRow;
    private int startSelectionColumn;
    private FileType fileType;
    private String fileName;
    private JScrollBar scrollBar;

    public EDocument() {

        column = 0;
        row = 0;
        insert = false;
        heightOffset = 0;
        widthOffset = 0;
        isShiftPressed = false;
        existSelection = false;
        fileType = FileType.Text;

        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        dataInChars = new ArrayList<>();
        dataInChars.add(new StringBuilder());

        dataInWords = new ArrayList<>(dataInChars.size());

        parser = new Parser(dataInWords, dataInChars);
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

        dataInChars.clear();
        if (initData != null) {
            dataInChars.addAll(initData.stream().map(StringBuilder::new).collect(Collectors.toList()));
        } else {
            dataInChars.add(new StringBuilder());
        }
    }

    // add or remove line
    private void addLine(int row, StringBuilder sb) {
        dataInChars.add(row, sb);
        if (fileType != FileType.Text) {
            dataInWords.add(row, new ArrayList<>());
            parser.addLine(row);
        }
    }

    private void removeLine(int row) {
        dataInChars.remove(row);
        if (fileType != FileType.Text) {
            dataInWords.remove(row);
            parser.removeLine(row);
        }

    }

    private void removeLines(int startRow, int endRow) {
        endRow++;
        dataInChars.subList(startRow, endRow).clear();
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
        updateOffsetOnCaret();

        if (fileType != FileType.Text) {
            if (startRow >= 0) {
                isShiftPressed = false;
                if (endRow >= 0) {
                    parser.parse(startRow, endRow);
                }
            }

            parser.bracketLightOff();
            parser.bracketLight(column, row);
        }

        updateScrollBar();
    }

    private void updateOffset() {
        if (heightOffset + height >= dataInChars.size()) {
            heightOffset = dataInChars.size() - height;
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
        scrollBar.setMaximum(dataInChars.size() - 1);
        scrollBar.setValue(heightOffset);
        scrollBar.setVisibleAmount(height);
    }

    private void updatePosition(){

        if (column < 0 && row <= 0) {
            column = 0;
        }

        if (column < 0){
            row--;
            column = dataInChars.get(row).length();
        }

        if (row < 0) {
            row = 0;
        }

        if (row >= dataInChars.size()) {
            row = dataInChars.size() - 1;
        }

        if (column > dataInChars.get(row).length()) {
            column = dataInChars.get(row).length();
        }
    }

    // Selection + change selection area

    public void selectAll() {
        startSelectionColumn = 0;
        startSelectionRow = 0;
        row = dataInChars.size() - 1;
        column = dataInChars.get(row).length();
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

            StringBuilder res = new StringBuilder();
            if (startRow != endRow) {
                res.append(dataInChars.get(startRow).substring(startColumn)).append("\n");
                for (int i = startRow + 1; i < endRow; i++) {
                    res.append(dataInChars.get(i)).append("\n");
                }
                res.append(dataInChars.get(endRow).substring(0, endColumn));
            }
            else {
                res.append(dataInChars.get(startRow).substring(startColumn, endColumn));
            }
            clipboard.setContents(new StringSelection(res.toString()), null);
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
        StringBuilder start = dataInChars.get(row);
        String end = start.substring(column);
        start.replace(column, start.length(), "");
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '\n') {
                if (Character.codePointAt(s, i) == 9) {
                    sb.append(TAB);
                }
                sb.append(s.charAt(i));
            } else {
                column = 0;
                dataInChars.get(row).append(sb);
                row++;
                column = 0;
                addLine(row, new StringBuilder());
                sb = new StringBuilder();
            }
        }

        column += sb.length();
        dataInChars.get(row).append(sb).append(end);

        updateWithChanges(startChangesRow, row);
    }

    private void deleteSelection() {
        if (!isExistSelection()) {
            existSelection = false;
            return;
        }
        existSelection = false;

        int[] selectionInterval = getSelectionInterval();
        int startColumn = selectionInterval[0];
        int startRow = selectionInterval[1];
        int endColumn = selectionInterval[2];
        int endRow = selectionInterval[3];

        column = startColumn;
        row = startRow;

        StringBuilder line = dataInChars.get(startRow);
        if (startRow != endRow) {
            line.replace(startColumn, line.length(), "");
            line.append(dataInChars.get(endRow).substring(endColumn));

            removeLines(startRow + 1, endRow);
        } else {
            dataInChars.set(startRow, new StringBuilder(line.substring(0, startColumn) + line.substring(endColumn)));
        }

        updateWithChanges(row);
    }

    // Change char

    public synchronized void insertChar(char character) {
        deleteSelection();
        String ch = Character.toString(character);
        StringBuilder line = dataInChars.get(row);
        if (!insert) {
            if (ch.equals("\n")) {
                dataInChars.set(row, new StringBuilder(line.substring(0, column)));
                addLine(row + 1, new StringBuilder(line.substring(column)));
                row++;
                column = 0;
                updateWithChanges(row - 1, row);
            } else {
                line.insert(column, ch);
                column++;
                updateWithChanges(row);
            }
        }
        else {
            if (ch.equals("\n")) {
                row++;
                column = 0;
                if (row == dataInChars.size()) {
                    addLine(dataInChars.size(), new StringBuilder());
                    updateWithChanges(row);
                }
                else {
                    updateWithoutChanges();
                }
            }
            else {
                if (column <= line.length() - 1) {
                    line.replace(column, column + 1, ch);
                }
                else {
                    line.append(ch);
                }
                column++;
                updateWithChanges(row);
            }
        }
        //updateWithChanges(row, false);
    }

    public synchronized void backspace() {
        if (isExistSelection()) {
            deleteSelection();
        }
        else {
            if (column == 0) { // concat Lines
                if (row > 0) {
                    row--;
                    column = dataInChars.get(row).length();
                    dataInChars.get(row).append(dataInChars.get(row + 1));
                    removeLine(row + 1);
                }
            } else {
                column--;
                dataInChars.get(row).replace(column, column + 1, "");
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
            if (row == dataInChars.size() - 1 && column == dataInChars.get(row).length()) {
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
        if (column < dataInChars.get(row).length()) {
            column++;
        }
        else if (row < dataInChars.size() - 1) {
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
        column = dataInChars.get(row).length();
        updateWithoutChanges();
    }

    public void pageUp() {
        heightOffset = Math.max(0, heightOffset - height);
        row += -height;
        updateWithoutChanges();
    }

    public void pageDown() {
        heightOffset = Math.max(0, Math.min(dataInChars.size() - height, heightOffset + height));
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
        fileName = s;

        Matcher javaFile = Pattern.compile(".*\\.java").matcher(s);
        Matcher jsFile = Pattern.compile(".*\\.js").matcher(s);

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
                parser.forceParse(0, height + 2);
                updateWithoutChanges();
                new Thread(() -> {
                    synchronized (this) {
                        parser.forceParse(height + 2, dataInChars.size());
                    }
                }).start();
            } else {
                if (needParse) {
                    parser.forceParse(0, dataInChars.size());
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
        return (List) dataInChars;
    }
}