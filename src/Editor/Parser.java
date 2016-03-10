package Editor;

import Editor.Word.Type;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.regex.Pattern;

class Parser {

    private static final Pattern openBracketPattern = Pattern.compile("[\\{\\[\\(]");
    private static final Pattern bracket = Pattern.compile("[\\[\\]\\{\\}\\(\\)]");
    private final Words dataInWords;
    private final StringBuilder data;
    private final ArrayList<Integer> length;
    private final EDocument doc;
    private Word firstBracket;
    private Word secondBracket;
    private Pair<Integer, Integer> firstBracketPos;
    private Pair<Integer, Integer> secondBracketPos;
    private FileType fileType;

    public Parser(EDocument doc, Words dataInWords, StringBuilder data, ArrayList<Integer> length) {
        this.doc = doc;
        this.dataInWords = dataInWords;
        this.data = data;
        this.length = length;

        fileType = FileType.Text;
    }

    // Brackets

    private static boolean isBracket(Character ch) {
        return bracket.matcher(ch.toString()).matches();
    }

    private int findWordInLine(int column, int row) {
        int i = 0;
        int wordNumber = 0;
        while (i < column && wordNumber < dataInWords.rowSize(row)) {
            i += dataInWords.get(row, wordNumber++).length();
        }

        if (wordNumber == dataInWords.size()) {
            return 0;
        }

        return wordNumber - 1;
    }

    public void bracketLightOff() {
        if (firstBracket != null) {
            firstBracket.type = Type.Bracket;
            dataInWords.set(firstBracketPos.getKey(), firstBracketPos.getValue(), firstBracket);

            firstBracket = null;
        }
        if (secondBracket != null) {
            secondBracket.type = Type.Bracket;
            dataInWords.set(secondBracketPos.getKey(), secondBracketPos.getValue(), secondBracket);

            secondBracket = null;
        }
    }

    public void bracketLight(int column, int row, int pos) {
        if (column > 0 && isBracket(data.charAt(pos - 1))) {
            int wordInLine = findWordInLine(column, row);
            if (dataInWords.get(row, wordInLine).type != Type.Bracket) {
                return;
            }
            firstBracket = dataInWords.get(row, wordInLine);
            firstBracket.type = Type.BracketLight;
            firstBracketPos = new Pair<>(row, wordInLine);
            char firstBracketChar = data.charAt(pos - 1);

            pos -= firstBracket.start + 1;


            dataInWords.set(row, wordInLine, firstBracket);

            secondBracket = null;

            boolean openBracket = openBracketPattern.matcher(Character.toString(firstBracketChar)).matches();

            Word word;
            int k = 1;
            do {
                wordInLine = openBracket ? wordInLine + 1 : wordInLine - 1;
                if (wordInLine >= dataInWords.rowSize(row)) {
                    pos += length.get(row) + 1;
                    row++;
                    if (row >= dataInWords.size()) {
                        return;
                    }
                    wordInLine = 0;
                }
                if (wordInLine < 0) {
                    row--;
                    if (row < 0 || row >= dataInWords.size()) {
                        return;
                    }
                    pos -= length.get(row) + 1;
                    wordInLine = Math.max(dataInWords.rowSize(row) - 1, 0);
                }

                word = dataInWords.rowSize(row) == 0 ? null : dataInWords.get(row, wordInLine);
                if (word != null && word.type == Type.Bracket &&
                        Math.abs(data.charAt(pos + word.start) - firstBracketChar) <= 2) {
                    k = !(openBracket == openBracketPattern.matcher(Character.toString(data.charAt(pos + word.start))).matches()) ?
                            k - 1 : k + 1;
                }

            } while (k > 0);

            secondBracket = word;
            if (word != null) {
                secondBracket.type = Type.BracketLight;
                dataInWords.set(row, wordInLine, secondBracket);
                secondBracketPos = new Pair<>(row, wordInLine);
            }
        }
    }

    // Words

    public boolean setFileType(FileType fileType) {
        boolean res = fileType != this.fileType;
        this.fileType = fileType;
        if (fileType == FileType.Text) {
            dataInWords.clear();
        }
        return res;
    }

    // Parsing

    public void forceParse(int row, int endRow) {
        parse(row, endRow, true);
    }

    public void parse(int row, int endRow) {
        parse(row, endRow, false);
    }

    private void parse(int row, int endRow, boolean forceEnd) {
        row = Math.min(row, length.size() - 1);

        boolean parseAll = length.size() != dataInWords.size();

        dataInWords.updateSize(length.size());

        endRow = Math.min(length.size(), endRow);

        int pos = doc.getPos(row, 0);
        for (int i = row; i < (forceEnd ? endRow : length.size()); i++) {
            dataInWords.clearDataLine(i);
            LineParser lineParser = new LineParser(data.substring(pos, pos + length.get(i)), fileType, dataInWords, i);

            lineParser.parseLine();

            boolean lastCommentContinuous = dataInWords.isCommentContinuous(i + 1);
            dataInWords.setCommentContinuous(i + 1, lineParser.isCommentContinuous());

            if (!parseAll && !forceEnd && endRow <= i && lineParser.isCommentContinuous() == lastCommentContinuous) {
                return;
            }

            pos += length.get(i) + 1;
        }
    }
}
