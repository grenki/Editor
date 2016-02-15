package Editor;

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

    public EDocument(List<String> initData) {

        column = 0;
        row = 0;
        insert = false;
        heightOffset = 0;
        widthOffset = 0;
        isShiftPressed = false;
        existSelection = false;
        fileType = FileType.Text;

        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();

        if (initData != null) {
            dataInChars = new ArrayList<>(initData.size());
            dataInChars.addAll(initData.stream().map(StringBuilder::new).collect(Collectors.toList()));
        }
        else {
            dataInChars = new ArrayList<>();
            dataInChars.add(new StringBuilder());
        }


        dataInWords = new ArrayList<>(dataInChars.size());

        parser = new Parser(dataInWords, dataInChars);
    }

    // add or remove line

    private void addLine(int row, StringBuilder sb) {
        dataInChars.add(row, sb);
        dataInWords.add(row, new ArrayList<>());
        parser.addLine(row);
    }

    private void removeLine(int row) {
        dataInChars.remove(row);
        dataInWords.remove(row);
        parser.removeLine(row);

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
        updateOffset();

        if (fileType != FileType.Text) {
            if (startRow >= 0) {
                isShiftPressed = false;
                parser.parse(startRow, endRow);
            }

            parser.bracketLightOff();
            parser.bracketLight(column, row);
        }

        //dataInChars.forEach(System.out :: println);

        /*dataInWords.forEach(e -> {
            e.forEach(c -> System.out.print(c.s));
            System.out.println();
        });*/
    }

    private void updateOffset() {
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

    private void insertString(String s) {
        int startChangesRow = row;
        StringBuilder sb = new StringBuilder();
        StringBuilder start =  dataInChars.get(row);
        String end = start.substring(column);
        start.replace(column, start.length(), "");
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '\n') {
                sb.append(s.charAt(i));
            }
            else {
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
        if (! existSelection) {
            return;
        }

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

            for (int i = startRow + 1; i <= endRow; i++) {
                removeLine(startRow + 1);
            }
        }
        else {
            dataInChars.set(startRow, new StringBuilder(line.substring(0, startColumn) + line.substring(endColumn)));
        }

        updateWithChanges(row);
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

    public void cut() {
        if (isExistSelection()) {
            copy();
            deleteSelection();
        }
    }

    // Change char

    public void insertChar(char character) {
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
                if (row == dataInChars.size() - 1) {
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

    public void backspace() {
        if (existSelection){
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

            updateWithChanges(row);
        }
    }

    public void delete(){
        if (existSelection) {
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
        existSelection = false;
    }

    public void setWindowSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setShiftPressed(boolean shiftPressed) {
        if (!this.isShiftPressed && shiftPressed) {
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

    public void setFileName(String s) {
        fileName = s;

        Matcher javaFile = Pattern.compile("\\w*\\.java").matcher(s);
        Matcher jsFile = Pattern.compile("\\w*\\.js").matcher(s);

        if (javaFile.matches()) {
            fileType = FileType.Java;
        } else if (jsFile.matches()) {
            fileType = FileType.JS;
        } else {
            fileType = FileType.Text;
        }

        parser.setFileType(fileType);
    }

    // Getters

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

    public boolean isExistSelection() {
        return existSelection;
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