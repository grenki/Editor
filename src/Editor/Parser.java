package Editor;

import Editor.Word.Type;

import java.util.ArrayList;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Parser {

    private final ArrayList<ArrayList<Word>> dataInWords;
    private final StringBuilder data;
    private final ArrayList<Integer> length;
    private final ArrayList<Boolean> commentContinuousList;
    private Word firstBracket;
    private Word secondBracket;
    private FileType fileType;
    private final EDocument doc;

    private static final Pattern openBracketPattern = Pattern.compile("[\\{\\[\\(]");
    private static Pattern bracket = Pattern.compile("[\\[\\]\\{\\}\\(\\)]");

    public Parser(EDocument doc, ArrayList<ArrayList<Word>> dataInWords, StringBuilder data, ArrayList<Integer> length) {
        this.doc = doc;
        this.dataInWords = dataInWords;
        this.data = data;
        this.length = length;

        fileType = FileType.Text;
        commentContinuousList = new ArrayList<>(length.size());
    }

    // Brackets

    private int findWordInLine(int column, int row) {
        int i = 0;
        int wordNumber = 0;
        while (i < column && wordNumber < dataInWords.get(row).size()) {
            i += dataInWords.get(row).get(wordNumber++).length();
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
            if (dataInWords.get(row).get(wordInLine).type != Type.Bracket) {
                return;
            }
            firstBracket = dataInWords.get(row).get(wordInLine);
            firstBracket.type = Type.BracketLight;

            boolean openBracket = openBracketPattern.matcher(Character.toString(data.charAt(pos - 1))).matches();

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

    public boolean setFileType(FileType fileType) {
        boolean res = fileType != this.fileType;
        this.fileType = fileType;
        if (fileType == FileType.Text) {
            dataInWords.clear();
            commentContinuousList.clear();
        }
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
        row = Math.min(row, length.size() - 1);

        boolean parseAll = length.size() != dataInWords.size();

        dataInWords.subList(Math.min(length.size(), dataInWords.size()), dataInWords.size()).clear();
        commentContinuousList.subList(Math.min(length.size(), commentContinuousList.size()), commentContinuousList.size()).clear();
        for (int i = dataInWords.size(); i < length.size(); i++) {
            dataInWords.add(new ArrayList<>());
        }
        for (int i = commentContinuousList.size(); i < length.size() + 1; i++) {
            commentContinuousList.add(false);
        }

        endRow = Math.min(length.size(), endRow);
        int pos = doc.getPos(row, 0);
        //System.out.println("pos " + pos);
        for (int i = row; i < (forceEnd ? endRow : length.size()); i++) {
            //System.out.println(data.substring(pos, pos + length.get(i)));
            LineParser lineParser = new LineParser(data.substring(pos, pos + length.get(i)),
                    commentContinuousList.get(i), fileType, pos);
            dataInWords.set(i, lineParser.getResultLine());
            lineParser.parseLine();
            if (!parseAll && !forceEnd && endRow <= i && lineParser.isCommentContinuous() == commentContinuousList.get(i + 1)) {
                return;
            }
            dataInWords.set(i, lineParser.getResultLine());
            ///final int k = pos;
            //System.out.println(data.substring(pos, pos + length.get(i)));
            //lineParser.getResultLine().forEach((e) -> System.out.print(data.substring(k + e.start, k + e.end)));
            //System.out.println();
            commentContinuousList.set(i + 1, lineParser.isCommentContinuous());

            pos += length.get(i) + 1;
        }
        int k = 0;
        for (ArrayList<Word> l: dataInWords) {
            for (Word w: l) {
                k++;
            }
        }
        //System.out.println("words " + k);
    }
}
