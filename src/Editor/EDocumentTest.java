package Editor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class EDocumentTest extends Assert {

    private final static int countOfRandomOperations = 10000;
    private final static int maxCountOfRows = 200;
    private final static int maxLineLength = 200;
    private final static int windowRowSize = 100;
    private final static int windowColumnSize = 100;
    private final static String javaFileName = "a.java";
    private static Random rand;
    private static EDocument doc;
    private static RandomText randomText;
    private static List<StringBuilder> inputData;
    private static Clipboard clipboard;

    private static List<String> fromStringBuilderListToStringList(List<StringBuilder> list) {
        final ArrayList<String> res = new ArrayList<>(list.size());
        list.forEach((e) -> res.add(e.toString()));
        return res;
    }

    private static void setUpCaretRandom() {
        mouseClickAt(rand.nextInt(windowRowSize + 20) - 10, rand.nextInt(windowColumnSize + 20) - 10);
    }

    private static void mouseClickAt(int row, int column) {
        doc.mousePressed(column, row);
        doc.mouseMoved(column, row);
    }

    private static void removeFromInputData(int startRow, int startColumn, int endRow, int endColumn) {
        if (startRow < endRow) {
            inputData.get(startRow).delete(startColumn, inputData.get(startRow).length())
                    .append(inputData.get(endRow).substring(endColumn));
            inputData.subList(startRow + 1, endRow + 1).clear();
        } else {
            inputData.get(startRow).delete(startColumn, endColumn);
        }
    }

    private static void setUpSelectionAndDeleteFromInputData(int startRow, int startColumn, int endRow, int endColumn) {
        doc.mousePressed(startColumn, startRow);
        startRow = doc.getCaretRow();
        startColumn = doc.getCaretColumn();
        doc.mouseMoved(endColumn, endRow);
        endColumn = doc.getCaretColumn();
        endRow = doc.getCaretRow();

        removeFromInputData(startRow, startColumn, endRow, endColumn);
    }

    private static void randomNavigationFunction() {
        int n = rand.nextInt(10);
        switch (n) {
            case 0:
                doc.right();
                break;
            case 1:
                doc.left();
                break;
            case 2:
                doc.up();
                break;
            case 3:
                doc.down();
                break;
            case 4:
                doc.pageDown();
                break;
            case 5:
                doc.pageUp();
                break;
            case 6:
                doc.home();
                break;
            case 7:
                doc.end();
                break;

            case 8:
                doc.pageDown();
                break;
            case 9:
                doc.pageDown();
                break;
        }
    }

    @Before
    public void init() {
        clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        randomText = new RandomText();
        rand = new Random();
        JScrollBar scrollbar = new JScrollBar();
        doc = new EDocument(scrollbar);
        doc.setWindowSize(windowColumnSize, windowRowSize);
        inputData = randomText.nextText(maxCountOfRows, maxLineLength);
        doc.recreateDocument(fromStringBuilderListToStringList(inputData));
        doc.setFileName(javaFileName, true);
    }

    @Test
    public void testRecreateDocumentWithNull() {
        doc.recreateDocument(null);
        doc.setFileName(javaFileName, true);
    }

    @Test
    public void testRecreateDocumentWithVoidList() {
        inputData = new ArrayList<>();
        recreateDocWithInputData();
    }

    @Test
    public void testInputEqualsGetAllData() {
        List<CharSequence> outputData = doc.getAllDataInLines();
        for (int i = 0; i < outputData.size(); i++) {
            assertEquals(inputData.get(i).toString(), outputData.get(i));
        }
    }

    @Test
    public void testNavigationFunctionsNotChangesTextRandom() {
        for (int i = 0; i < countOfRandomOperations; i++) {
            randomNavigationFunction();
        }
        testInputEqualsGetAllData();
    }

    @Test
    public void testRightInBeginOfLine() {
        recreateDoc("asd asd asd");

        mouseClickAt(0, 0);
        doc.right();
        assertEquals(1, doc.getCaretColumn());
    }

    @Test
    public void testRightInMiddleOfLine() {
        recreateDoc("asd asd asd");

        mouseClickAt(0, 4);
        doc.right();
        assertEquals(5, doc.getCaretColumn());
    }

    @Test
    public void testRightInTheEndOfLine() {
        recreateDoc(new String[]{"asd asd asd", "asd asd asd"});

        mouseClickAt(0, 20);
        doc.right();
        assertEquals(0, doc.getCaretColumn());
        assertEquals(1, doc.getCaretRow());
    }

    @Test
    public void testLeftInTheBeginOfLine() {
        recreateDoc(new String[]{"asd asd asd", "asd asd asd"});

        mouseClickAt(1, 0);
        doc.left();
        assertEquals(0, doc.getCaretRow());
        assertEquals(inputData.get(0).length(), doc.getCaretColumn());
    }

    @Test
    public void testLeftInTheMiddleOfLine() {
        recreateDoc("asd asd asd");

        mouseClickAt(0, 5);
        doc.left();
        assertEquals(0, doc.getCaretRow());
        assertEquals(4, doc.getCaretColumn());
    }

    @Test
    public void testUpInTheMiddleString() {
        recreateDoc(new String[]{"asd asd asd", "asd asd asd"});
        mouseClickAt(1, 5);

        doc.up();
        assertEquals(0, doc.getCaretRow());
        assertEquals(5, doc.getCaretColumn());
    }

    @Test
    public void testUpInTheFirstString() {
        recreateDoc("asd asd asd");
        mouseClickAt(0, 5);

        doc.up();
        assertEquals(0, doc.getCaretRow());
        assertEquals(5, doc.getCaretColumn());
    }

    @Test
    public void testDownInTheLastString() {
        recreateDoc("asd asd asd");

        mouseClickAt(0, 5);
        doc.down();

        assertEquals(0, doc.getCaretRow());
        assertEquals(5, doc.getCaretColumn());
    }

    @Test
    public void testDownInTheMiddleString() {
        recreateDoc(new String[]{"asd asd asd", "asd asd asd"});

        mouseClickAt(0, 5);
        doc.down();

        assertEquals(1, doc.getCaretRow());
        assertEquals(5, doc.getCaretColumn());
    }

    @Test
    public void testPageUpInFirstString() {
        recreateDoc(200, 210, 10, 100);

        mouseClickAt(0, 8);
        doc.pageUp();

        assertEquals(0, doc.getCaretRow());
        assertEquals(8, doc.getCaretColumn());
    }

    @Test
    public void testPageUpInStringInFirstWindow() {
        recreateDoc(200, 210, 10, 100);

        mouseClickAt(50, 8);
        doc.pageUp();

        assertEquals(0, doc.getCaretRow());
        assertEquals(8, doc.getCaretColumn());
    }

    @Test
    public void testPageDownInMiddleOfText() {
        recreateDoc(200, 210, 10, 100);

        mouseClickAt(50, 8);
        doc.pageDown();

        assertEquals(50 + windowRowSize, doc.getCaretRow());
        assertEquals(8, doc.getCaretColumn());
    }

    @Test
    public void testPageUpDownStringInLastWindow() {
        recreateDoc(200, 200, 10, 100);

        mouseClickAt(150, 8);
        doc.pageDown();

        assertEquals(199, doc.getCaretRow());
        assertEquals(8, doc.getCaretColumn());
    }

    @Test
    public void testPageUpInMiddleOfText() {
        recreateDoc(200, 210, 10, 100);

        mouseClickAt(50 + windowRowSize, 8);
        doc.pageUp();

        assertEquals(50, doc.getCaretRow());
        assertEquals(8, doc.getCaretColumn());
    }

    @Test
    public void testHomeVoidLine() {
        recreateDoc("");

        doc.home();

        assertEquals(0, doc.getCaretColumn());
    }

    @Test
    public void testHomeCaretInMiddleOfLine() {
        recreateDoc(1, 1, 10, 20);

        mouseClickAt(0, 8);

        doc.home();
        assertEquals(0, doc.getCaretColumn());
    }

    @Test
    public void testEndVoidLine() {
        recreateDoc("");

        doc.end();

        assertEquals(0, doc.getCaretColumn());
    }

    @Test
    public void testSelectAllAtVoidText() {
        doc.recreateDocument(null);
        doc.setFileName(javaFileName, true);

        doc.selectAll();
        int[] selectionInterval = doc.getSelectionInterval();

        assertEquals(0, selectionInterval[0]);
        assertEquals(0, selectionInterval[1]);
        assertEquals(0, selectionInterval[2]);
        assertEquals(0, selectionInterval[3]);
    }

    @Test
    public void testSelectAllAtRandomText() {
        doc.selectAll();
        int[] selectionInterval = doc.getSelectionInterval();

        assertEquals(0, selectionInterval[0]);
        assertEquals(0, selectionInterval[1]);
        int lastRow = Math.max(inputData.size() - 1, 0);
        assertEquals(inputData.get(lastRow).length(), selectionInterval[2]);
        assertEquals(lastRow, selectionInterval[3]);
    }

    @Test
    public void testSelectAllAtOneLine() {
        recreateDoc(1, 1, 10, 10);

        doc.selectAll();
        int[] selectionInterval = doc.getSelectionInterval();

        assertEquals(0, selectionInterval[0]);
        assertEquals(0, selectionInterval[1]);
        assertEquals(10, selectionInterval[2]);
        assertEquals(0, selectionInterval[3]);
    }

    @Test
    public void testPasteVoidString() {
        recreateDoc(10, 10, 20, 20);

        clipboard.setContents(new StringSelection(""), null);

        mouseClickAt(0, 0);
        doc.paste();
        mouseClickAt(5, 5);
        doc.paste();
        mouseClickAt(10, 20);
        doc.paste();

        testInputEqualsGetAllData();
    }

    @Test
    public void testPasteOneString() {
        recreateDoc(10, 10, 20, 20);

        String pasteString = "pasteString";
        clipboard.setContents(new StringSelection(pasteString), null);

        mouseClickAt(0, 0);
        doc.paste();
        inputData.get(0).insert(0, pasteString);

        mouseClickAt(5, 5);
        doc.paste();
        inputData.get(5).insert(5, pasteString);

        mouseClickAt(9, 20);
        doc.paste();
        inputData.get(9).insert(20, pasteString);

        testInputEqualsGetAllData();
    }

    @Test
    public void testPasteFewString() {
        recreateDoc(10, 10, 20, 20);

        String pasteString = "pasteString\npasteString2";
        String firstString = "pasteString";
        String secondString = "pasteString2";
        clipboard.setContents(new StringSelection(pasteString), null);

        mouseClickAt(0, 0);
        doc.paste();
        inputData.add(0, new StringBuilder(firstString));
        inputData.get(1).insert(0, secondString);

        testInputEqualsGetAllData();

        mouseClickAt(5, 5);
        doc.paste();
        StringBuilder line = inputData.get(5);
        inputData.set(5, new StringBuilder(line.substring(0, 5) + firstString));
        inputData.add(6, new StringBuilder(secondString + line.substring(5)));
        testInputEqualsGetAllData();

        mouseClickAt(9, 20);
        doc.paste();
        inputData.get(9).insert(20, firstString);
        inputData.add(10, new StringBuilder(secondString));

        testInputEqualsGetAllData();
    }

    @Test
    public void testSelectTextInOneLine() {
        recreateDoc(10, 10, 20, 20);

        doc.mousePressed(1, 0);
        doc.mouseMoved(8, 0);
        assertTrue(doc.isExistSelection());

        int[] selection = doc.getSelectionInterval();
        assertArrayEquals(selection, new int[]{1, 0, 8, 0});
    }

    @Test
    public void testSelectTextInOneLineInReverse() {
        recreateDoc(10, 10, 20, 20);

        doc.mousePressed(8, 0);
        doc.mouseMoved(1, 0);
        assertTrue(doc.isExistSelection());

        int[] selection = doc.getSelectionInterval();
        assertArrayEquals(selection, new int[]{1, 0, 8, 0});
    }

    @Test
    public void testSelectTextInNextLines() {
        recreateDoc(10, 10, 20, 20);

        doc.mousePressed(1, 1);
        doc.mouseMoved(8, 4);
        assertTrue(doc.isExistSelection());

        int[] selection = doc.getSelectionInterval();
        assertArrayEquals(selection, new int[]{1, 1, 8, 4});
    }

    @Test
    public void testSelectTextInNextLinesInReverse() {
        recreateDoc(10, 10, 20, 20);

        doc.mousePressed(8, 4);
        doc.mouseMoved(1, 1);
        assertTrue(doc.isExistSelection());

        int[] selection = doc.getSelectionInterval();
        assertArrayEquals(selection, new int[]{1, 1, 8, 4});
    }

    @Test
    public void testCopyInOneLine() throws IOException, UnsupportedFlavorException {
        recreateDoc(10, 10, 20, 20);

        doc.mousePressed(1, 0);
        doc.mouseMoved(8, 0);
        doc.copy();


        assertEquals(clipboard.getData(DataFlavor.stringFlavor), inputData.get(0).substring(1, 8));
    }

    @Test
    public void testCopyInFewLines() throws IOException, UnsupportedFlavorException {
        recreateDoc(10, 10, 20, 20);

        doc.mousePressed(1, 1);
        doc.mouseMoved(8, 3);
        doc.copy();

        System.out.println(Arrays.toString(doc.getSelectionInterval()));

        String expected = inputData.get(1).substring(1) + '\n' + inputData.get(2) +
                '\n' + inputData.get(3).substring(0, 8);

        assertEquals(expected, clipboard.getData(DataFlavor.stringFlavor));
    }

    @Test
    public void testDeleteSelectionInOneLine() {
        recreateDoc(1, 1, 30, 30);

        setUpSelectionAndDeleteFromInputData(0, 1, 0, 20);
        doc.delete();

        testInputEqualsGetAllData();
    }

    @Test
    public void testDeleteSelectionInFewLines() {
        setUpSelectionAndDeleteFromInputData(1, 1, 20, 20);
        doc.delete();
        testInputEqualsGetAllData();
    }

    @Test
    public void testBackspaceSelectionInOneLine() {
        recreateDoc(1, 1, 30, 30);

        setUpSelectionAndDeleteFromInputData(0, 1, 0, 20);
        doc.backspace();

        testInputEqualsGetAllData();
    }

    @Test
    public void testBackspaceSelectionInFewLines() {
        setUpSelectionAndDeleteFromInputData(1, 1, 20, 20);
        doc.backspace();
        testInputEqualsGetAllData();
    }

    @Test
    public void testCutInOneLine() throws IOException, UnsupportedFlavorException {
        recreateDoc("asd asd asd");

        doc.mousePressed(4, 0);
        doc.mouseMoved(7, 0);
        doc.cut();

        inputData.get(0).delete(4, 7);
        testInputEqualsGetAllData();

        assertEquals("asd", clipboard.getData(DataFlavor.stringFlavor));
    }

    @Test
    public void testCutInFewLines() throws IOException, UnsupportedFlavorException {
        recreateDoc(10, 10, 10, 20);

        doc.mousePressed(4, 3);
        doc.mouseMoved(7, 5);
        doc.cut();

        String sb = inputData.get(3).substring(4, inputData.get(3).length()) + '\n' +
                inputData.get(4) + '\n' +
                inputData.get(5).substring(0, 7);

        removeFromInputData(3, 4, 5, 7);

        testInputEqualsGetAllData();
        assertEquals(sb, clipboard.getData(DataFlavor.stringFlavor));
    }

    @Test
    public void testBackspaceInBeginOfFirstLine() {
        mouseClickAt(0, 0);
        doc.backspace();

        assertEquals(0, doc.getCaretColumn());
        assertEquals(0, doc.getCaretRow());
        testInputEqualsGetAllData();
    }

    @Test
    public void testBackspaceInMiddleOfString() {
        recreateDoc(1, 1, 20, 20);

        mouseClickAt(0, 5);
        doc.backspace();

        inputData.get(0).deleteCharAt(4);
        assertEquals(4, doc.getCaretColumn());
        assertEquals(0, doc.getCaretRow());
        testInputEqualsGetAllData();
    }

    @Test
    public void testBackspaceInTheBeginOfLine() {
        recreateDoc(3, 3, 20, 20);

        mouseClickAt(1, 0);
        doc.backspace();

        assertEquals(inputData.get(0).length(), doc.getCaretColumn());
        assertEquals(0, doc.getCaretRow());

        inputData.get(0).append(inputData.get(1));
        inputData.remove(1);

        testInputEqualsGetAllData();
    }

    @Test
    public void testDeleteInTheEndOfText() {
        mouseClickAt(Integer.MAX_VALUE, Integer.MAX_VALUE);
        doc.delete();

        testInputEqualsGetAllData();
    }

    @Test
    public void testDeleteInMiddleOfString() {
        recreateDoc(1, 1, 20, 20);

        mouseClickAt(0, 5);
        doc.delete();

        inputData.get(0).deleteCharAt(5);
        assertEquals(5, doc.getCaretColumn());
        assertEquals(0, doc.getCaretRow());
        testInputEqualsGetAllData();
    }

    @Test
    public void testDeleteInTheEndOfLine() {
        recreateDoc(3, 3, 20, 20);

        mouseClickAt(0, 20);
        doc.delete();

        assertEquals(20, doc.getCaretColumn());
        assertEquals(0, doc.getCaretRow());

        inputData.get(0).append(inputData.get(1));
        inputData.remove(1);

        testInputEqualsGetAllData();
    }

    @Test
    public void testInstallCaretByMouseRandom() {
        for (int i = 0; i < countOfRandomOperations; i++) {
            int row = rand.nextInt(windowRowSize + 20) - 10;
            int column = rand.nextInt(windowColumnSize + 20) - 10;
            mouseClickAt(row, column);

            assertEquals(doc.getCaretRow(), row < 0 ? 0 :
                    row >= inputData.size() ? inputData.size() - 1 : row);
            row = doc.getCaretRow();

            assertEquals(column < 0 ? 0 :
                            (column > inputData.get(row).length() ? inputData.get(row).length() : column),
                    doc.getCaretColumn());
        }

        testInputEqualsGetAllData();
    }

    @Test
    public void testInsertCharNotCaretReturnRandom() {
        for (int i = 0; i < countOfRandomOperations; i++) {
            int column = doc.getCaretColumn();
            int row = doc.getCaretRow();
            char ch = randomText.randomChar();

            doc.insertChar(ch);
            inputData.get(row).insert(column, ch);
        }

        testInputEqualsGetAllData();
    }

    @Test
    public void testInsertCharCaretReturnRandom() {
        for (int i = 0; i < countOfRandomOperations; i++) {
            setUpCaretRandom();
            int row = doc.getCaretRow();
            int column = doc.getCaretColumn();

            doc.insertChar('\n');

            StringBuilder line = inputData.get(row);
            inputData.add(row + 1, new StringBuilder(line.substring(column)));
            line.delete(column, line.length());
        }

        testInputEqualsGetAllData();
    }

    @Test
    public void testInsertCharNotCaretReturnWithInsertModeRandom() {
        doc.switchInsert();
        for (int i = 0; i < countOfRandomOperations; i++) {
            int column = doc.getCaretColumn();
            int row = doc.getCaretRow();
            char ch = randomText.randomChar();

            doc.insertChar(ch);
            StringBuilder line = inputData.get(row);
            if (column == line.length()) {
                line.append(ch);
            } else {
                line.setCharAt(column, ch);
            }
        }
        doc.switchInsert();
        testInputEqualsGetAllData();
    }

    @Test
    public void testInsertCharCaretReturnWithInsertModeRandom() {
        doc.switchInsert();
        for (int i = 0; i < countOfRandomOperations; i++) {
            int column;
            int row = doc.getCaretRow();

            doc.insertChar('\n');
            if (row == inputData.size() - 1) {
                inputData.add(new StringBuilder());
            }

            column = 0;
            row++;

            assertEquals(row, doc.getCaretRow());
            assertEquals(column, doc.getCaretColumn());


            testInputEqualsGetAllData();
        }
        doc.switchInsert();
    }

    @Test
    public void testInsertCharNotCaretReturnWithSelection() {
        recreateDoc(10, 10, 20, 20);

        doc.mousePressed(5, 1);
        doc.mouseMoved(2, 3);
        doc.insertChar('a');

        assertEquals(6, doc.getCaretColumn());
        assertEquals(1, doc.getCaretRow());

        removeFromInputData(1, 5, 3, 2);
        inputData.get(1).insert(5, 'a');

        testInputEqualsGetAllData();
    }

    @Test
    public void testSetFileNameIsText() {
        String[] input = {
                "asd.jva",
                "123.j s",
                "123.java.txt",
                "123.javat",
                "jsjsjsjs",
                "java",
                "js.java.",
                "js.jqva",
                "js"
        };
        for (String anInput : input) {
            doc.setFileName(anInput, true);
            assertTrue(doc.isFileTypeText());
        }
    }

    @Test
    public void testSetFileNameIsNotText() {
        String[] input = {
                ".java",
                "123.txt.js",
                "123.java",
                "123///.java",
                "jsjs.js.js",
                "*.java",
                "   .js",
                ".js",
                "_.js"
        };
        for (String anInput : input) {
            doc.setFileName(anInput, true);
            assertFalse(doc.isFileTypeText());
        }
    }

    private void recreateDoc(int minRows, int maxRows, int minLength, int maxLength) {
        inputData = randomText.nextText(minRows, maxRows, minLength, maxLength);
        doc.recreateDocument(fromStringBuilderListToStringList(inputData));
        doc.setFileName(javaFileName, true);
    }

    private void recreateDocWithInputData() {
        doc.recreateDocument(fromStringBuilderListToStringList(inputData));
        doc.setFileName(javaFileName, true);
    }

    private void recreateDoc(String s) {
        String[] init = new String[1];
        init[0] = s;
        recreateDoc(init);
    }

    private void recreateDoc(String[] init) {
        inputData = new ArrayList<>();
        for (String s : init) {
            inputData.add(new StringBuilder(s));
        }
        doc.recreateDocument(fromStringBuilderListToStringList(inputData));
        doc.setFileName(javaFileName, true);
    }
}

class RandomText {
    private final StringBuilder symbols;
    private final Random rand;
    private final boolean onlyBrackets = true;

    RandomText() {
        symbols = new StringBuilder();
        if (!onlyBrackets) {
            for (char i = 'a'; i <= 'z'; i++) {
                symbols.append(i);
            }
            for (char i = '0'; i <= '9'; i++) {
                symbols.append(i);
            }
            symbols.append("!@#$%^&*_+=-`");
        }
        symbols.append(' ');
        symbols.append("{}[]()");

        rand = new Random();
    }

    public StringBuilder randomLine(int minLength, int maxLength) {
        int length = rand.nextInt(maxLength - minLength + 1) + minLength;
        StringBuilder sb = new StringBuilder(length);
        for (int j = 0; j < length; j++) {
            sb.append(symbols.charAt(rand.nextInt(symbols.length())));
        }
        return sb;
    }

    public List<StringBuilder> nextText(int minRows, int maxRows, int minLength, int maxLength) {
        int rowsCount = rand.nextInt(maxRows - minRows + 1) + minRows;
        List<StringBuilder> res = new ArrayList<>(rowsCount);

        for (int i = 0; i < rowsCount; i++) {
            res.add(randomLine(minLength, maxLength));
        }

        return res;
    }

    public List<StringBuilder> nextText(int maxRows, int maxLength) {
        return nextText(1, maxRows, 0, maxLength);
    }

    // public List<String> nextTextInStrings(int maxRows, int maxLength) {
    //   return fromStringBuilderListToStringList(nextText(maxRows, maxLength));
    //}

    public char randomChar() {
        return symbols.charAt(rand.nextInt(symbols.length()));
    }
}
