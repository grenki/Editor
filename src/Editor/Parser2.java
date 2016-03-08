package Editor;

import Editor.Word.Type;

import java.util.ArrayList;
import java.util.regex.Pattern;

class Parser2 {

    private final ArrayList<Word> dataInWords;
    private final StringBuilder data;
    private final ArrayList<Integer> length;
    private final ArrayList<Boolean> commentContinuousList;
    private Word firstBracket;
    private Word secondBracket;
    private FileType fileType;
    private final EDocument doc;

    private static final Pattern openBracketPattern = Pattern.compile("[\\{\\[\\(]");
    private static Pattern bracket = Pattern.compile("[\\[\\]\\{\\}\\(\\)]");

    public Parser2(EDocument doc, ArrayList<Word> dataInWords, StringBuilder data, ArrayList<Integer> length) {
        this.dataInWords = dataInWords;
        this.data = data;
        this.length = length;
        this.doc = doc;

        fileType = FileType.Text;
        commentContinuousList = new ArrayList<>(length.size());
    }

    // Brackets

    private int findWordInLine(int column, int row, int pos) {
        int i = 0;
        int wordNumber = 0;
        while (i < pos) {
            i += dataInWords.get(wordNumber++).length();
        }

        if (wordNumber == dataInWords.size()) {
            return 0;
        }

        return wordNumber - 1;
    }

    private int findWordInLine(int column, int row) {
        int i = 0;
        int wordNumber = 0;
        int pos = doc.getPos(row, 0);

        while (i < pos) {
            i += dataInWords.get(wordNumber++).length();
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
            int wordInLine = findWordInLine(column, row, pos);
            if (dataInWords.get(wordInLine).type != Type.Bracket) {
                return;
            }
            firstBracket = dataInWords.get(wordInLine);
            firstBracket.type = Type.BracketLight;

           /* boolean openBracket = openBracketPattern.matcher(firstBracket.string()).matches();

            Word word = null;
            int k = 1;

            wordInLine = openBracket ? wordInLine + 1 : wordInLine - 1;

            while (k > 0 && wordInLine >= 0 && wordInLine < dataInWords.size()) {
                word = dataInWords.get(wordInLine);
                if (word != null && word.type == Type.Bracket &&
                        Math.abs(word.string().charAt(0) - firstBracket.string().charAt(0)) <= 2) {
                    k = !(openBracket == openBracketPattern.matcher(word.string()).matches()) ? k - 1 : k + 1;
                }
                wordInLine = openBracket ? wordInLine + 1 : wordInLine - 1;
            }

            if (word != null) {
                secondBracket = word;
                secondBracket.type = Type.BracketLight;
            }*/
        }
    }

    public boolean setFileType(FileType fileType) {
        boolean res = fileType != this.fileType;
        this.fileType = fileType;
        return res;
    }

    // Words

    private static boolean isBracket(Character ch) {
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

        //boolean parseAll = dataInChars.size() != dataInWords.size();

        //dataInWords.subList(Math.min(dataInChars.size(), dataInWords.size()), dataInWords.size()).clear();
        //commentContinuousList.subList(Math.min(length.size(), commentContinuousList.size()), commentContinuousList.size()).clear();

        /*for (int i = dataInWords.size(); i < dataInChars.size(); i++) {
            dataInWords.add(new ArrayList<>());
            }*/
        for (int i = commentContinuousList.size(); i < length.size() + 1; i++) {
            commentContinuousList.add(false);
        }

        endRow = Math.min(length.size(), endRow);

        int wordN = findWordInLine(0, row);
        for (int i = row; i < (forceEnd ? endRow : length.size()); i++) {
            //LineParser lineParser = new LineParser(dataInChars.get(i), commentContinuousList, fileType);
            /*dataInWords.set(i, lineParser.getResultLine());
            lineParser.parseLine();
            if (!parseAll && !forceEnd && endRow <= i && lineParser.isCommentContinuous() == commentContinuousList.get(i + 1)) {
                return;
            }
            dataInWords.set(i, lineParser.getResultLine());
            commentContinuousList.set(i + 1, lineParser.isCommentContinuous());*/
        }
    }
}
