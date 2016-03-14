package Editor;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EDocumentTest extends Assert {

    private final static int countOfRandomOperations = 10000;
    private final static int maxCountOfRows = 200;
    private final static int maxLineLength = 200;
    private final static int windowRowSize = 100;
    private final static int windowColumnSize = 100;
    private static Random rand;
    private static EDocument doc;
    private static RandomText randomText;
    private static JScrollBar scrollbar;
    private static List<StringBuilder> inputData;

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
        randomText = new RandomText();
        rand = new Random();
        scrollbar = new JScrollBar();
        doc = new EDocument(scrollbar);
        doc.setWindowSize(windowColumnSize, windowRowSize);
        inputData = randomText.nextText(maxCountOfRows, maxLineLength);
        doc.recreateDocument(fromStringBuilderListToStringList(inputData));
        doc.setFileName("a.java", true);
    }

    @Test
    public void testOpenVoidFile() {
        inputData = new ArrayList<>();
        doc.recreateDocument(fromStringBuilderListToStringList(inputData));
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

        mouseClickAt(0, 0);
        doc.right();
        assertEquals(1, doc.getCaretColumn());
    }

    @Test
    public void testRightInMiddleOfLine() {
        ArrayList<String> inputData = new ArrayList<>();
        inputData.add("asd asd asd");
        doc.recreateDocument(inputData);

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

        mouseClickAt(0, 5);
        doc.left();
        assertEquals(0, doc.getCaretRow());
        assertEquals(4, doc.getCaretColumn());
    }

    @Test
    public void testCaretInstallByMouseRandom() {
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
            int column = doc.getCaretColumn();
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
        for (char i = 'a'; i <= 'z'; i++) {
            symbols.append(i);
        }
        for (char i = '0'; i <= '9'; i++) {
            symbols.append(i);
        }
        symbols.append(' ');
        symbols.append("{}[]()!@#$%^&*_+=-`");

        rand = new Random();
    }

    public StringBuilder randomLine(int maxLength) {
        return randomLine(0, maxLength);
    }

    public StringBuilder randomLine(int minLength, int maxLength) {
        int length = rand.nextInt(maxLength - minLength) + minLength;
        StringBuilder sb = new StringBuilder(length);
        for (int j = 0; j < length; j++) {
            sb.append(symbols.charAt(rand.nextInt(symbols.length())));
        }
        return sb;
    }

    public List<StringBuilder> nextText(int maxRows, int maxLength) {
        int rowsCount = rand.nextInt(maxRows - 1) + 1;
        List<StringBuilder> res = new ArrayList<>(rowsCount);

        for (int i = 0; i < rowsCount; i++) {
            res.add(randomLine(maxLength));
        }

        return res;
    }

    // public List<String> nextTextInStrings(int maxRows, int maxLength) {
    //   return fromStringBuilderListToStringList(nextText(maxRows, maxLength));
    //}

    public char randomChar() {
        return symbols.charAt(rand.nextInt(symbols.length()));
    }
}
