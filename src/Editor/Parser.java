package Editor;

import Editor.Word.Type;

import java.util.ArrayList;
import java.util.regex.Matcher;
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

        parse(0,dataInChars.size());
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

    public void parse(int row, int endRow) {
        boolean parseAll = dataInChars.size() != dataInWords.size();

        dataInWords.subList(Math.min(dataInChars.size(), dataInWords.size()), dataInWords.size()).clear();
        commentContinuousList.subList(Math.min(dataInChars.size(), commentContinuousList.size()), commentContinuousList.size()).clear();
        for (int i = dataInWords.size(); i < dataInChars.size(); i++) {
            dataInWords.add(new ArrayList<>());
        }
        for (int i = commentContinuousList.size(); i < dataInChars.size() + 1; i++) {
            commentContinuousList.add(false);
        }

        for (int i = row; i < dataInChars.size(); i++) {
            boolean isNotChangesInLine = parseLine(i);
            if (!parseAll && isNotChangesInLine && endRow <= i){
                return;
            }
        }
    }

    private boolean parseLine(int row) {
        final int multiLineCommentOffset = 2;

        ArrayList<Word> outputLineInWords = new ArrayList<>();
        String inputStringToParse = dataInChars.get(row).toString();

        Matcher identifier = Pattern.compile("(\\W|^)[a-zA-Z]+[a-zA-Z0-9_]*").matcher(inputStringToParse);
        Matcher commentLine = Pattern.compile("//").matcher(inputStringToParse);
        Matcher startComment = Pattern.compile("/\\*").matcher(inputStringToParse);
        Matcher endComment = Pattern.compile("\\*/").matcher(inputStringToParse);
        Matcher bracket= Pattern.compile("[\\{\\}\\(\\)\\[\\]]").matcher(inputStringToParse);

        int firstNotParsedChar = 0;
        int identifierFirstPosition = updateIdentifier(updateFind(identifier), inputStringToParse);
        int commentLineFirstPosition = updateFind(commentLine);
        int bracketFirstPosition = updateFind(bracket);
        int multilineCommentFirstPosition;
        boolean isCommentContinuous = commentContinuousList.get(row);

        if (!isCommentContinuous) {
            multilineCommentFirstPosition = updateFind(startComment);
        }
        else {
            multilineCommentFirstPosition = -2;
        }

        while (firstNotParsedChar < inputStringToParse.length()) {
            int closestMatch = Math.min(Math.min(inputStringToParse.length(), commentLineFirstPosition),
                    Math.min(bracketFirstPosition, Math.min(multilineCommentFirstPosition, identifierFirstPosition)));

            if (closestMatch - firstNotParsedChar > 0) {
                outputLineInWords.add(new Word(inputStringToParse.substring(firstNotParsedChar, closestMatch), Type.Other));
                firstNotParsedChar = closestMatch;
            }


            if (multilineCommentFirstPosition == closestMatch) {
                int firstMultilineCommentEnd = updateWhileLess(-10, endComment,
                        multilineCommentFirstPosition + multiLineCommentOffset);
                if (firstMultilineCommentEnd < inputStringToParse.length()) {
                    firstNotParsedChar = firstMultilineCommentEnd + multiLineCommentOffset ;
                    multilineCommentFirstPosition = updateWhileLess(multilineCommentFirstPosition,
                            startComment, firstNotParsedChar);
                    identifierFirstPosition = updateIdentifier(
                            updateWhileLess(identifierFirstPosition, identifier, firstNotParsedChar - 1), inputStringToParse);
                    commentLineFirstPosition = updateWhileLess(commentLineFirstPosition, commentLine, firstNotParsedChar);
                    bracketFirstPosition = updateWhileLess(bracketFirstPosition, bracket, firstNotParsedChar);
                    isCommentContinuous = false;
                }
                else {
                    firstNotParsedChar = inputStringToParse.length();
                    isCommentContinuous = true;
                }
                outputLineInWords.add(new Word(inputStringToParse.substring(Math.max(closestMatch, 0), firstNotParsedChar),
                        Type.Comment));
            }
            else if (bracketFirstPosition == closestMatch) {
                firstNotParsedChar++;
                outputLineInWords.add(new Word(inputStringToParse.substring(closestMatch, closestMatch + 1), Type.Bracket));
                bracketFirstPosition = updateFind(bracket);
            }
            else if (commentLineFirstPosition == closestMatch) {
                outputLineInWords.add(new Word(inputStringToParse.substring(closestMatch), Type.Comment));
                firstNotParsedChar = inputStringToParse.length();
            }
            else if (identifierFirstPosition == closestMatch) {
                firstNotParsedChar = identifier.end();
                outputLineInWords.add(new Word(inputStringToParse.substring(identifierFirstPosition, firstNotParsedChar), fileType));
                identifierFirstPosition = updateIdentifier(updateFind(identifier), inputStringToParse);
            }
        }

        boolean res = isCommentContinuous == commentContinuousList.get(row + 1) &&
                isLinesEquals(outputLineInWords, dataInWords.get(row));
        dataInWords.set(row, outputLineInWords);
        commentContinuousList.set(row + 1, isCommentContinuous);
        return res;
    }

    private int updateIdentifier(int position, String s) {
        return position < s.length() && Character.isAlphabetic(s.charAt(position)) ? position : position + 1;
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

    private int updateFind(Matcher m) {
        return m.find() ? m.start() : Integer.MAX_VALUE - 10;
    }

    private int updateWhileLess(int value, Matcher m, int threshold) {
        while (value < threshold) {
            value = updateFind(m);
        }
        return value;
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

    public void setFileType(FileType fileType) {
        if (fileType != this.fileType) {
            this.fileType = fileType;
            parse(0, dataInChars.size());
        }
        this.fileType = fileType;
    }
}
