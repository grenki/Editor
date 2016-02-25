package Editor;

import org.junit.Assert;

import java.util.ArrayList;
import java.util.Random;

import Editor.Word.Type;
import org.junit.Test;

public class LineParserTest extends Assert {
    private final int countOfRandomTests = 1000;
    private final int maxLength = 400;

    @org.junit.Test
    public void testParseLineAtAllLineOtherText() throws Exception {
        String[] inputStrings = {
                "1",
                "123",
                "1 2 3",
                "12 23 12",
                "123456"
        };

        for (String inputString : inputStrings) {
            LineParser lineParser = new LineParser(new StringBuilder(inputString), false, FileType.Java);
            lineParser.parseLine();
            lineParser.getResultLine().forEach((e) -> assertEquals(e.t, Type.Other));
        }
    }

    @org.junit.Test
    public void testParseLineAtAllLineComments() throws Exception {
        String[] inputStrings = {
                "//asdasd",
                "/*sadasdasdas*/",
                "/* asdasdasd",
                "/*asdasdasd*/// asdaasd",
                "/* asd,  *///,  */",
                "/*,   asd *//* asd asd*/"
        };

        for (String inputString : inputStrings) {
            LineParser lineParser = new LineParser(new StringBuilder(inputString), false, FileType.Java);
            lineParser.parseLine();
            lineParser.getResultLine().forEach((e) -> assertEquals(e.t, Type.Comment));
        }
    }

    @org.junit.Test
    public void testParseLineAtAllLineBrackets() throws Exception {

        String[] inputStrings = {
                "{",
                "}",
                "[",
                "]",
                "(",
                ")",
                "{((}{][][}[(}[}({)]{)}]"
        };

        for (String inputString : inputStrings) {

            LineParser lineParser = new LineParser(new StringBuilder(inputString), false, FileType.Java);
            lineParser.parseLine();
            lineParser.getResultLine().forEach((e) -> {
                assertEquals(e.t, Type.Bracket);
                assertEquals(e.s.length(), 1);
            });
        }
    }

    @org.junit.Test
    public void testParseLineAtSaveAllChars() throws Exception {

        Random random = new Random();
        for (int i = 0; i < countOfRandomTests; i++) {
            StringBuilder inputString = randomString(random.nextInt(maxLength));

            LineParser lineParser = new LineParser(inputString, false, FileType.Java);
            lineParser.parseLine();
            ArrayList<Word> resultList = lineParser.getResultLine();
            StringBuilder result = new StringBuilder();
            resultList.forEach((e) -> result.append(e.s));

            assertEquals(inputString.toString(), result.toString());
        }
    }

    @org.junit.Test
    public void testIsCommentContinuousFalse() throws Exception {
        String[] inputStrings = {
                "*/",
                "/*/*/**/",
                "/* */,    */,   /*, /*,   */ asd / *"
        };

        for (String inputString : inputStrings) {

            LineParser lineParser = new LineParser(new StringBuilder(inputString), false, FileType.Java);
            lineParser.parseLine();
            assertEquals(lineParser.isCommentContinuous(), false);
        }
    }

    @org.junit.Test
    public void testIsCommentContinuousTrue() throws Exception {

        String[] inputStrings = {
                "/*",
                "/*/*/ */*",
                "/* */,    */,   / *, /*,    asd / *",
                "/** /",
                "asd/*asd"
        };

        for (String inputString : inputStrings) {
            LineParser lineParser = new LineParser(new StringBuilder(inputString), false, FileType.Java);
            lineParser.parseLine();
            assertEquals(lineParser.isCommentContinuous(), true);
        }
    }

    @Test
    public void testParseLineAtIdentifiers() throws Exception {
        String[] inputStrings = {
                "asd",
                "_asd",
                "asd123",
                "asdфыв123",
                "____",
                "_"
        };

        for (String inputString : inputStrings) {
            LineParser lineParser = new LineParser(new StringBuilder(inputString), false, FileType.Java);
            lineParser.parseLine();
            lineParser.getResultLine().forEach((e) -> assertEquals(e.t, Type.Identifier));
        }
    }

    @Test
    public void testParseLineAtKeyWords() throws Exception {
        String[] inputStrings = {
                "class",
                "abstract",
                "volatile",
                "for",
                "int",
                "public"
        };

        for (String inputString : inputStrings) {
            LineParser lineParser = new LineParser(new StringBuilder(inputString), false, FileType.Java);
            lineParser.parseLine();
            lineParser.getResultLine().forEach((word) -> assertEquals(word.t, Type.Key));
        }
    }

    @Test
    public void testParseLineAtNotIdentifiers() throws Exception {
        String[] inputStrings = {
                "123asd",
                "asd ",
                "asd*",
                "*asd",
                "@asd",
                "   .asd",
                "___a as"
        };

        for (String inputString : inputStrings) {
            LineParser lineParser = new LineParser(new StringBuilder(inputString), false, FileType.Java);
            lineParser.parseLine();
            assertNotEquals(lineParser.getResultLine().size(), 1);
        }
    }

    @Test
    public void testParseLineAtComplexStrings() throws Exception {
        String[] inputStrings = {
                "class 123 {}",
                "123asd//asd",
                "/* asd */__cl",
                "",
                "{asd{fds.qwe/* qwe **/"
        };

        Type[][] expectedTypeResult = {
                {Type.Key, Type.Other, Type.Bracket, Type.Bracket},
                {Type.Other, Type.Identifier, Type.Comment},
                {Type.Comment, Type.Identifier},
                {Type.Other},
                {Type.Bracket, Type.Identifier, Type.Bracket, Type.Identifier, Type.Other, Type.Identifier, Type.Comment}
        };
        int[][] expectedWordsLength = {
                {5, 5, 1, 1},
                {3, 3, 5},
                {9, 4},
                {0},
                {1, 3, 1, 3, 1, 3, 10}
        };

        for (int i=0; i < inputStrings.length; i++) {
            String inputString = inputStrings[i];
            LineParser lineParser = new LineParser(new StringBuilder(inputString), false, FileType.Java);
            lineParser.parseLine();

            for (int j = 0; j < lineParser.getResultLine().size(); j++) {
                assertEquals(expectedTypeResult[i][j], lineParser.getResultLine().get(j).t);
                assertEquals(expectedWordsLength[i][j], lineParser.getResultLine().get(j).s.length());
            }
        }
    }

    @Test
    public void testIsCommentContinuousTrueWithCommentContinuousFromPreviousLine() throws Exception {
        String[] inputStrings = {
                "asd",
                "asd* /",
                "asd /* asd"
        };

        for (String inputString : inputStrings) {

            LineParser lineParser = new LineParser(new StringBuilder(inputString), true, FileType.Java);
            lineParser.parseLine();
            assertEquals(lineParser.isCommentContinuous(), true);
        }
    }

    @Test
    public void testIsCommentContinuousFalseWithCommentContinuousFromPreviousLine() throws Exception {
        String[] inputStrings = {
                "a*/sd",
                "*/",
                "asd /* asd */",
                "/*asd /* asd */",
                "*/ * /",
        };

        for (String inputString : inputStrings) {

            LineParser lineParser = new LineParser(new StringBuilder(inputString), true, FileType.Java);
            lineParser.parseLine();
            assertEquals(lineParser.isCommentContinuous(), false);
        }
    }

    private StringBuilder randomString(int length) {
        Random random = new Random();
        StringBuilder res = new StringBuilder();
        for (int j = 0; j < length; j++) {
            res.append(Character.toChars(random.nextInt(1000)));
        }
        return res;
    }
}