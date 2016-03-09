package Editor;

import Editor.Word.Type;

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
        }
        if (secondBracket != null) {
            secondBracket.type = Type.Bracket;
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

            boolean openBracket = openBracketPattern.matcher(Character.toString(data.charAt(pos - 1))).matches();

            Word word;
            int k = 1;
            do {
                wordInLine = openBracket ? wordInLine + 1 : wordInLine - 1;
                if (wordInLine >= dataInWords.rowSize(row)) {
                    row++;
                    wordInLine = 0;
                }
                if (wordInLine < 0) {
                    row--;
                    if (row < 0 || row >= dataInWords.size()) {
                        return;
                    }
                    wordInLine = Math.max(dataInWords.rowSize(row) - 1, 0);
                }
                if (row < 0 || row >= dataInWords.size()) {
                    return;
                }

                word = dataInWords.rowSize(row) == 0 ? null : dataInWords.get(row, wordInLine);
                if (word != null && word.type == Type.Bracket &&
                        Math.abs(data.charAt(word.start) - data.charAt(firstBracket.start)) <= 2) {
                    k = !(openBracket == openBracketPattern.matcher(Character.toString(data.charAt(word.start))).matches()) ?
                            k - 1 : k + 1;
                }

            } while (k > 0);

            if (word != null) {
                secondBracket = word;
                secondBracket.type = Type.BracketLight;
            }
        }
    }

    // Words

    public boolean setFileType(FileType fileType) {
        boolean res = fileType != this.fileType;
        this.fileType = fileType;
        if (fileType == FileType.Text) {
            dataInWords.clear();
            //commentContinuousList.clear();
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
