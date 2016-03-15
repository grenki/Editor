package Editor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
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
        doc.recreateDocument(fromStringBuilderListToStringList(inputData));
        doc.setFileName(javaFileName, true);
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
        ArrayList<String> inputData = new ArrayList<>();
        inputData.add("asd asd asd");
        doc.recreateDocument(inputData);
        doc.setFileName(javaFileName, true);

        mouseClickAt(0, 0);
        doc.right();
        assertEquals(1, doc.getCaretColumn());
    }

    @Test
    public void testRightInMiddleOfLine() {
        ArrayList<String> inputData = new ArrayList<>();
        inputData.add("asd asd asd");
        doc.recreateDocument(inputData);
        doc.setFileName(javaFileName, true);

        mouseClickAt(0, 4);
        doc.right();
        assertEquals(5, doc.getCaretColumn());
    }

    @Test
    public void testRightInTheEndOfLine() {
        ArrayList<String> inputData = new ArrayList<>();
        inputData.add("asd asd asd");
        inputData.add("asd asd asd");
        doc.recreateDocument(inputData);
        doc.setFileName(javaFileName, true);

        mouseClickAt(0, 20);
        doc.right();
        assertEquals(0, doc.getCaretColumn());
        assertEquals(1, doc.getCaretRow());
    }

    @Test
    public void testLeftInTheBeginOfLine() {
        ArrayList<String> inputData = new ArrayList<>();
        String inputString = "asd asd asd";
        inputData.add(inputString);
        inputData.add(inputString);
        doc.recreateDocument(inputData);
        doc.setFileName(javaFileName, true);

        mouseClickAt(1, 0);
        doc.left();
        assertEquals(0, doc.getCaretRow());
        assertEquals(inputString.length(), doc.getCaretColumn());
    }

    @Test
    public void testLeftInTheMiddleOfLine() {
        ArrayList<String> inputData = new ArrayList<>();
        String inputString = "asd asd asd";
        inputData.add(inputString);
        doc.recreateDocument(inputData);
        doc.setFileName(javaFileName, true);

        mouseClickAt(0, 5);
        doc.left();
        assertEquals(0, doc.getCaretRow());
        assertEquals(4, doc.getCaretColumn());
    }

    @Test
    public void testUpInTheMiddleString() {
        ArrayList<String> inputData = new ArrayList<>();
        inputData.add("asd asd asd");
        inputData.add("asd asd asd");
        doc.recreateDocument(inputData);
        doc.setFileName(javaFileName, true);
        mouseClickAt(1, 5);

        doc.up();
        assertEquals(0, doc.getCaretRow());
        assertEquals(5, doc.getCaretColumn());
    }

    @Test
    public void testUpInTheFirstString() {
        ArrayList<String> inputData = new ArrayList<>();
        inputData.add("asd asd asd");
        doc.recreateDocument(inputData);
        doc.setFileName(javaFileName, true);
        mouseClickAt(0, 5);

        doc.up();
        assertEquals(0, doc.getCaretRow());
        assertEquals(5, doc.getCaretColumn());
    }

    @Test
    public void testDownInTheLastString() {
        ArrayList<String> inputData = new ArrayList<>();
        inputData.add("asd asd asd");
        doc.recreateDocument(inputData);
        doc.setFileName(javaFileName, true);

        mouseClickAt(0, 5);

        doc.down();
        assertEquals(0, doc.getCaretRow());
        assertEquals(5, doc.getCaretColumn());
    }

    @Test
    public void testDownInTheMiddleString() {
        ArrayList<String> inputData = new ArrayList<>();
        inputData.add("asd asd asd");
        inputData.add("asd asd asd");
        doc.recreateDocument(inputData);
        doc.setFileName(javaFileName, true);

        mouseClickAt(0, 5);

        doc.down();
        assertEquals(1, doc.getCaretRow());
        assertEquals(5, doc.getCaretColumn());
    }

    @Test
    public void testPageUpInFirstString() {
        inputData = randomText.nextText(200, 210, 10, 100);
        doc.recreateDocument(fromStringBuilderListToStringList(inputData));
        doc.setFileName(javaFileName, true);

        mouseClickAt(0, 8);

        doc.pageUp();

        assertEquals(0, doc.getCaretRow());
        assertEquals(8, doc.getCaretColumn());
    }

    @Test
    public void testPageUpInStringInFirstWindow() {
        inputData = randomText.nextText(200, 210, 10, 100);
        doc.recreateDocument(fromStringBuilderListToStringList(inputData));
        doc.setFileName(javaFileName, true);

        mouseClickAt(50, 8);

        doc.pageUp();

        assertEquals(0, doc.getCaretRow());
        assertEquals(8, doc.getCaretColumn());
    }

    @Test
    public void testPageDownInMiddleOfText() {
        inputData = randomText.nextText(200, 210, 10, 100);
        doc.recreateDocument(fromStringBuilderListToStringList(inputData));
        doc.setFileName(javaFileName, true);

        mouseClickAt(50, 8);

        doc.pageDown();

        assertEquals(50 + windowRowSize, doc.getCaretRow());
        assertEquals(8, doc.getCaretColumn());
    }

    @Test
    public void testPageUpDownStringInLastWindow() {
        inputData = randomText.nextText(200, 200, 10, 100);
        doc.recreateDocument(fromStringBuilderListToStringList(inputData));
        doc.setFileName(javaFileName, true);

        mouseClickAt(150, 8);

        doc.pageDown();

        assertEquals(199, doc.getCaretRow());
        assertEquals(8, doc.getCaretColumn());
    }

    @Test
    public void testPageUpInMiddleOfText() {
        inputData = randomText.nextText(200, 210, 10, 100);
        doc.recreateDocument(fromStringBuilderListToStringList(inputData));
        doc.setFileName(javaFileName, true);

        mouseClickAt(50 + windowRowSize, 8);

        doc.pageUp();

        assertEquals(50, doc.getCaretRow());
        assertEquals(8, doc.getCaretColumn());
    }

    @Test
    public void testHomeVoidLine() {
        ArrayList<String> input = new ArrayList<>();
        input.add("");
        doc.recreateDocument(input);
        doc.setFileName(javaFileName, true);

        doc.home();
        assertEquals(0, doc.getCaretColumn());
    }

    @Test
    public void testHomeCaretInMiddleOfLine() {
        ArrayList<String> input = new ArrayList<>();
        input.add(randomText.randomLine(10, 20).toString());
        doc.recreateDocument(input);
        doc.setFileName(javaFileName, true);

        mouseClickAt(0, 8);

        doc.home();
        assertEquals(0, doc.getCaretColumn());
    }

    @Test
    public void testEndVoidLine() {
        ArrayList<String> input = new ArrayList<>();
        input.add("");
        doc.recreateDocument(input);
        doc.setFileName(javaFileName, true);

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
        inputData = new ArrayList<>();
        inputData.add(randomText.randomLine(10, 10));
        doc.recreateDocument(fromStringBuilderListToStringList(inputData));
        doc.setFileName(javaFileName, true);

        doc.selectAll();
        int[] selectionInterval = doc.getSelectionInterval();

        assertEquals(0, selectionInterval[0]);
        assertEquals(0, selectionInterval[1]);
        assertEquals(10, selectionInterval[2]);
        assertEquals(0, selectionInterval[3]);
    }

    @Test
    public void testPasteVoidString() {
        inputData = randomText.nextText(10, 10, 20, 20);
        doc.recreateDocument(fromStringBuilderListToStringList(inputData));
        doc.setFileName(javaFileName, true);

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
        inputData = randomText.nextText(10, 10, 20, 20);
        doc.recreateDocument(fromStringBuilderListToStringList(inputData));
        doc.setFileName(javaFileName, true);

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
        inputData = randomText.nextText(10, 10, 20, 20);
        doc.recreateDocument(fromStringBuilderListToStringList(inputData));
        doc.setFileName(javaFileName, true);

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
    public void testInsertCaretReturnRandom() {
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
    public void testInsertCaretReturnWithInsertModeRandom() {
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



    private void randomNavigationFunction() {
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
}

class RandomText {
    private final StringBuilder symbols;
    private final Random rand;

    RandomText() {
        symbols = new StringBuilder();
        /*for (char i = 'a'; i <= 'z'; i++) {
            symbols.append(i);
        }
        for (char i = '0'; i <= '9'; i++) {
            symbols.append(i);
        }*/
        symbols.append(' ');
        symbols.append("{}[]()");
        //symbols.append("{}[]()!@#$%^&*_+=-`");

        rand = new Random();
    }

    public StringBuilder randomLine(int maxLength) {
        return randomLine(0, maxLength);
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
