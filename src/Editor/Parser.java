package Editor;

import Editor.Word.Type;

import java.util.ArrayList;
import java.util.regex.Pattern;

class Parser {

    private final ArrayList<ArrayList<Word>> dataInWords;
    private final ArrayList<StringBuilder> dataInChars;
    private final ArrayList<Boolean> commentContinuousList;
    private Word firstBracket;
    private Word secondBracket;
    private FileType fileType;

    public Parser(ArrayList<ArrayList<Word>> dataInWords, ArrayList<StringBuilder> dataInChars) {
        this.dataInWords = dataInWords;
        this.dataInChars = dataInChars;
        fileType = FileType.Text;
        commentContinuousList = new ArrayList<>(dataInChars.size());
    }

    // Brackets

    private int findWordInLine(int column, int row) {
        int i = 0;
        int wordNumber = 0;
        while (i < column && wordNumber < dataInWords.get(row).size()) {
            i += dataInWords.get(row).get(wordNumber++).s.length();
        }

        if (wordNumber == dataInWords.size()) {
            return 0;
        }

        return wordNumber - 1;
    }

    public void bracketLightOff() {
        if (firstBracket != null) {
            firstBracket.t = Type.Bracket;
        }
        if (secondBracket != null) {
            secondBracket.t = Type.Bracket;
        }
    }

    public void bracketLight(int column, int row) {
        if (column > 0 && isBracket(dataInChars.get(row).charAt(column - 1))) {
            int wordInLine = findWordInLine(column, row);
            if (dataInWords.get(row).get(wordInLine).t != Type.Bracket) {
                return;
            }
            firstBracket = dataInWords.get(row).get(wordInLine);
            firstBracket.t = Type.BracketLight;
            Pattern openBracketPattern = Pattern.compile("[\\{\\[\\(]");
            boolean openBracket = openBracketPattern.matcher(firstBracket.s).matches();

            Word word;
            int k = 1;
            do {
                wordInLine = openBracket ? wordInLine + 1 : wordInLine - 1;
                if (wordInLine >= dataInWords.get(row).size()) {
                    row++;
                    wordInLine = 0;
                }
                if (wordInLine < 0) {
                    row--;
                    if (row < 0 || row >= dataInWords.size()) {
                        return;
                    }
                    wordInLine = Math.max(dataInWords.get(row).size() - 1, 0);
                }
                if (row < 0 || row >= dataInWords.size()) {
                    return;
                }

                word = dataInWords.get(row).size() == 0 ? null : dataInWords.get(row).get(wordInLine);
                if (word != null && word.t == Type.Bracket &&
                        Math.abs(word.s.charAt(0) - firstBracket.s.charAt(0)) <= 2) {
                    k = !(openBracket == openBracketPattern.matcher(word.s).matches()) ? k - 1 : k + 1;
                }

            } while (k > 0);

            if (word != null) {
                secondBracket = word;
                secondBracket.t = Type.BracketLight;
            }
        }
    }

    public boolean setFileType(FileType fileType) {
        boolean res = fileType != this.fileType;
        this.fileType = fileType;
        return res;
    }

    // Words

    private static boolean isBracket(Character ch) {
        Pattern bracket = Pattern.compile("[\\[\\]\\{\\}\\(\\)]");
        return bracket.matcher(ch.toString()).matches();
    }

    public void addLine(int row) {
        commentContinuousList.add(row, false);
    }

    public void removeLine(int row) {
        commentContinuousList.remove(row);
    }

    public void removeLines(int startRow, int endRow) {
        commentContinuousList.subList(startRow, endRow).clear();
    }

    // Parsing

    public void forceParse(int row, int endRow) {
        parse(row, endRow, true);
    }

    public void parse(int row, int endRow) {
        parse(row, endRow, false);
    }

    private void parse(int row, int endRow, boolean forceEnd) {

        boolean parseAll = dataInChars.size() != dataInWords.size();

        dataInWords.subList(Math.min(dataInChars.size(), dataInWords.size()), dataInWords.size()).clear();
        commentContinuousList.subList(Math.min(dataInChars.size(), commentContinuousList.size()), commentContinuousList.size()).clear();
        for (int i = dataInWords.size(); i < dataInChars.size(); i++) {
            dataInWords.add(new ArrayList<>());
            }
        for (int i = commentContinuousList.size(); i < dataInChars.size() + 1; i++) {
            commentContinuousList.add(false);
        }

        endRow = Math.min(dataInChars.size(), endRow);
        for (int i = row; i < (forceEnd ? endRow : dataInChars.size()); i++) {
            LineParser lineParser = new LineParser(dataInChars.get(i), commentContinuousList.get(i), fileType);
            dataInWords.set(i, lineParser.getResultLine());
            lineParser.parseLine();
            if (!parseAll && !forceEnd && endRow <= i && lineParser.isCommentContinuous() == commentContinuousList.get(i + 1)) {
                return;
            }
            dataInWords.set(i, lineParser.getResultLine());
            commentContinuousList.set(i + 1, lineParser.isCommentContinuous());
        }
    }
}
