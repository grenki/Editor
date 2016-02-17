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
            boolean isNotChangesInLine = new ParseLine(i).isThisLineEqualsPreviousParse();
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

    public void setFileType(FileType fileType) {
        if (fileType != this.fileType) {
            this.fileType = fileType;
            parse(0, dataInChars.size());
        }
        this.fileType = fileType;
    }

    private class ParseLine {
        ArrayList<Word> outputLineInWords;
        StringBuilder inputStringToParse;
        Matcher identifier;
        Matcher commentLine;
        Matcher startComment;
        Matcher endComment;
        Matcher bracket;
        //int firstNotParsedChar = 0;
        int identifierFirstPosition;
        int commentLineFirstPosition;
        int bracketFirstPosition;
        int multilineCommentFirstPosition;
        boolean isCommentContinuous;
        private boolean isThisLineEqualsPreviousParse;
        private Pattern identifierPattern = Pattern.compile("([^а-яА-Я\\w]|^)[a-zA-Z_]+[a-zA-Z0-9_]*($|[^а-яА-я\\w])");
        private Pattern commentLinePattern = Pattern.compile("//");
        private Pattern startCommentPattern = Pattern.compile("/\\*");
        private Pattern endCommentPattern = Pattern.compile("\\*/");
        private Pattern bracketPattern = Pattern.compile("[\\{\\}\\(\\)\\[\\]]");

        ParseLine(int row) {
            final int multiLineCommentOffset = 2;

            outputLineInWords = new ArrayList<>();
            inputStringToParse = new StringBuilder(dataInChars.get(row));

            update(0);

            isCommentContinuous = commentContinuousList.get(row);
            if (isCommentContinuous) {
                multilineCommentFirstPosition = -2;
            }

            while (inputStringToParse.length() > 0) {
                int closestMatch = Math.min(Math.min(inputStringToParse.length(), commentLineFirstPosition),
                        Math.min(bracketFirstPosition, Math.min(multilineCommentFirstPosition, identifierFirstPosition)));

                if (closestMatch > 0) {
                    outputLineInWords.add(new Word(inputStringToParse.substring(0, closestMatch), Type.Other));
                    update(closestMatch);
                } else if (multilineCommentFirstPosition <= 0) {
                    int firstMultilineCommentEnd;
                    if (multilineCommentFirstPosition == -2) {
                        firstMultilineCommentEnd = updateFind(endComment);
                    } else {
                        firstMultilineCommentEnd = updateWhileLess(-1, endComment, multilineCommentFirstPosition + multiLineCommentOffset);
                        System.out.println(firstMultilineCommentEnd);
                    }
                    int end = firstMultilineCommentEnd + multiLineCommentOffset;
                    outputLineInWords.add(new Word(inputStringToParse.substring(0, Math.min(inputStringToParse.length(), end)), Type.Comment));
                    if (firstMultilineCommentEnd < inputStringToParse.length()) {
                        update(firstMultilineCommentEnd + multiLineCommentOffset);
                        isCommentContinuous = false;
                    } else {
                        update(inputStringToParse.length());
                        isCommentContinuous = true;
                    }
                } else if (bracketFirstPosition == closestMatch) {
                    outputLineInWords.add(new Word(inputStringToParse.substring(0, 1), Type.Bracket));
                    update(1);
                } else if (commentLineFirstPosition == closestMatch) {
                    outputLineInWords.add(new Word(inputStringToParse.substring(closestMatch), Type.Comment));
                    update(inputStringToParse.length());
                } else if (identifierFirstPosition == closestMatch) {
                    int end = updateEndIdentifier(identifier.end() - 1, inputStringToParse) + 1;
                    outputLineInWords.add(new Word(inputStringToParse.substring(0, end), fileType));
                    update(end);
                }
            }

            isThisLineEqualsPreviousParse = isCommentContinuous == commentContinuousList.get(row + 1) &&
                    isLinesEquals(outputLineInWords, dataInWords.get(row));
            dataInWords.set(row, outputLineInWords);
            commentContinuousList.set(row + 1, isCommentContinuous);
        }

        public boolean isThisLineEqualsPreviousParse() {
            return isThisLineEqualsPreviousParse;
        }

        private void update(int closestMatch) {
            inputStringToParse.replace(0, closestMatch, "");

            identifier = identifierPattern.matcher(inputStringToParse);
            commentLine = commentLinePattern.matcher(inputStringToParse);
            startComment = startCommentPattern.matcher(inputStringToParse);
            endComment = endCommentPattern.matcher(inputStringToParse);
            bracket = bracketPattern.matcher(inputStringToParse);

            identifierFirstPosition = updateIdentifier(updateFind(identifier), inputStringToParse);
            commentLineFirstPosition = updateFind(commentLine);
            bracketFirstPosition = updateFind(bracket);
            multilineCommentFirstPosition = updateFind(startComment);
        }

        private int updateEndIdentifier(int position, StringBuilder s) {
            return position == s.length() - 1 && Character.isJavaIdentifierPart(s.charAt(position)) ? position : position - 1;
        }

        private int updateIdentifier(int position, StringBuilder s) {
            return position < s.length() && Character.isJavaIdentifierStart(s.charAt(position)) ? position : position + 1;
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
    }
}
