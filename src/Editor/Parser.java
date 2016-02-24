package Editor;

import Editor.Word.Type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

class Parser {

    private final ArrayList<ArrayList<Word>> dataInWords;
    private final ArrayList<StringBuilder> dataInChars;
    private final ArrayList<Boolean> commentContinuousList;
    private Word firstBracket;
    private Word secondBracket;
    private FileType fileType;
    private LineParser lineParser;

    public Parser(ArrayList<ArrayList<Word>> dataInWords, ArrayList<StringBuilder> dataInChars) {
        this.dataInWords = dataInWords;
        this.dataInChars = dataInChars;
        lineParser = new LineParser();
        fileType = FileType.Text;
        commentContinuousList = new ArrayList<>(dataInChars.size());

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
            boolean isNotChangesInLine = lineParser.parseLine(i, !forceEnd && endRow <= i);
            if (!parseAll && isNotChangesInLine && endRow <= i){
                return;
            }
        }
    }

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

    // Brackets

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
            int wordN = findWordInLine(column, row);
            if (dataInWords.get(row).get(wordN).t != Type.Bracket) {
                return;
            }
            firstBracket = dataInWords.get(row).get(wordN);
            firstBracket.t = Type.BracketLight;
            Pattern openBracketPattern = Pattern.compile("[\\{\\[\\(]");
            boolean openBracket = openBracketPattern.matcher(firstBracket.s).matches();

            Word word;
            int k = 1;
            do {
                wordN = openBracket ? wordN + 1 : wordN - 1;
                if (wordN >= dataInWords.get(row).size()) {
                    row++;
                    wordN = 0;
                }
                if (wordN < 0) {
                    row--;
                    if (row < 0 || row >= dataInWords.size()) {
                        return;
                    }
                    wordN = Math.max(dataInWords.get(row).size() - 1, 0);
                }
                if (row < 0 || row >= dataInWords.size()) {
                    return;
                }

                word = dataInWords.get(row).size() == 0 ? null : dataInWords.get(row).get(wordN);
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

    private class LineParser {
        ArrayList<Word> outputLineInWords;
        StringBuilder inputStringToParse;
        boolean isCommentContinuous;
        HashSet<Character> charType;
        int pos;
        int startWord;
        Type state;

        LineParser() {
            charType = new HashSet<>(10);
            char[] brackets = {'{', '}', '[', ']', '(', ')'};
            for (char ch : brackets) {
                charType.add(ch);
            }

            charType.add('/');


        }

        // class

        boolean parseLine(int row, boolean needCheck) {
            inputStringToParse = dataInChars.get(row);
            outputLineInWords = new ArrayList<>();
            isCommentContinuous = commentContinuousList.get(row);
            state = isCommentContinuous ? Type.Comment : Type.Other;
            pos = 0;
            startWord = 0;
// 123asd
            while (pos < inputStringToParse.length()) {
                char ch = inputStringToParse.charAt(pos);
                switch (state) {
                    case Comment:
                        if (ch == '*' && isNextSlash(pos)) {
                            addWord(startWord, pos + 2, Type.Comment);
                            pos += 2;
                            startWord = pos;
                            isCommentContinuous = false;
                            state = Type.Other;
                        } else {
                            pos++;
                        }
                        break;
                    case Identifier:
                        if (Character.isJavaIdentifierPart(ch)) {
                            pos++;
                        } else {
                            if (pos - startWord > 0) {
                                addWord(startWord, pos, Type.Identifier);
                            }
                            startWord = pos;
                            updateState(ch);
                            pos++;
                        }
                        break;
                    case Other:
                        int oldPos = pos;
                        updateState(ch);
                        if (state != Type.Other && startWord != oldPos) {
                            addWord(startWord, oldPos, Type.Other);
                            startWord = oldPos;
                        }
                        pos++;
                        break;
                    case Bracket:
                        addWord(startWord, pos, Type.Bracket);
                        startWord = pos;
                        updateState(ch);
                        pos++;
                        break;
                    default:
                        throw new IllegalStateException();
                }
            }

            if (startWord < pos) {
                addWord(startWord, inputStringToParse.length(), state);
            }

            boolean isThisLineEqualsPreviousParse = needCheck && isCommentContinuous == commentContinuousList.get(row + 1) &&
                    isLinesEquals(outputLineInWords, dataInWords.get(row));
            dataInWords.set(row, outputLineInWords);
            commentContinuousList.set(row + 1, isCommentContinuous);

            return isThisLineEqualsPreviousParse;
        }

        void addWord(int start, int end, Type type) {
            if (end == start) {
                return;
            }
            if (type == Type.Identifier) {
                outputLineInWords.add(new Word(inputStringToParse.substring(start, end), fileType));
            } else {
                outputLineInWords.add(new Word(inputStringToParse.substring(start, end), type));
            }
        }

        void updateState(char ch) {
            if (charType.contains(ch)) {
                if (ch == '/') {
                    if (isNextStar(pos)) {
                        pos++;
                        state = Type.Comment;

                        isCommentContinuous = true;
                    } else if (isNextSlash(pos)) {
                        addWord(startWord, pos, state);
                        addWord(pos, inputStringToParse.length(), Type.Comment);
                        pos = inputStringToParse.length();
                        startWord = pos;
                    } else {
                        state = Type.Other;
                    }
                } else {
                    state = Type.Bracket;
                }
            } else {
                if (Character.isJavaIdentifierStart(ch)) {
                    state = Type.Identifier;
                } else {
                    state = Type.Other;
                }
            }
        }

        boolean isNextSlash(int pos) {
            return pos < inputStringToParse.length() - 1 && inputStringToParse.charAt(pos + 1) == '/';
        }

        boolean isNextStar(int pos) {
            return pos < inputStringToParse.length() - 1 && inputStringToParse.charAt(pos + 1) == '*';
        }
        private boolean isLinesEquals(ArrayList<Word> firstLine, ArrayList<Word> secondLine) {
            if (firstLine == null || secondLine == null) {
                return false;
            }

            if (firstLine.size() != secondLine.size()) {
                return false;
            }

            for (int i = 0; i < firstLine.size(); i++) {
                if (!firstLine.get(i).s.equals(secondLine.get(i).s)) {
                    return false;
                }
            }

            return true;
        }
    }

}
