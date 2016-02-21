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
                //System.out.println("lines: " + (i - row));
                return;
            }
        }
        //System.out.println("lines: " + (dataInChars.size() - row));
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
        final int multiLineCommentOffset = 2;
        ArrayList<Word> outputLineInWords;
        StringBuilder inputStringToParse;
        Matcher identifier;
        Matcher commentLine;
        Matcher startComment;
        Matcher endComment;
        Matcher bracket;
        int identifierFirstPosition;
        int commentLineFirstPosition;
        int bracketFirstPosition;
        int multilineCommentFirstPosition;
        int deletedChars;
        private Pattern identifierPattern = Pattern.compile("([^а-яА-Я\\w]|^)[a-zA-Z_]+[a-zA-Z0-9_]*($|[^а-яА-я\\w])");
        private Pattern commentLinePattern = Pattern.compile("//");
        private Pattern startCommentPattern = Pattern.compile("/\\*");
        private Pattern endCommentPattern = Pattern.compile("\\*/");
        private Pattern bracketPattern = Pattern.compile("[\\{\\}\\(\\)\\[\\]]");

        boolean parseLine(int row, boolean needCheck) {
            outputLineInWords = new ArrayList<>();
            inputStringToParse = dataInChars.get(row);

            boolean isCommentContinuous = commentContinuousList.get(row);

            if (inputStringToParse.length() > 0) {

                identifierFirstPosition = -10;
                commentLineFirstPosition = -10;
                bracketFirstPosition = -10;
                multilineCommentFirstPosition = -10;
                deletedChars = 0;

                commentLine = commentLinePattern.matcher(inputStringToParse);
                startComment = startCommentPattern.matcher(inputStringToParse);
                bracket = bracketPattern.matcher(inputStringToParse);
                identifier = identifierPattern.matcher(inputStringToParse);

                update(0);
                if (isCommentContinuous) {
                    multilineCommentFirstPosition = -2;
                }

                while (inputStringToParse.length() > deletedChars) {
                    int closestMatch = Math.min(Math.min(inputStringToParse.length(), commentLineFirstPosition),
                            Math.min(bracketFirstPosition, Math.min(multilineCommentFirstPosition, identifierFirstPosition)));
                    if (closestMatch > deletedChars) {
                        outputLineInWords.add(new Word(inputStringToParse.substring(deletedChars, closestMatch), Type.Other));
                        update(closestMatch);
                    } else if (multilineCommentFirstPosition <= deletedChars) {
                        int firstMultilineCommentEnd;
                        endComment = endCommentPattern.matcher(inputStringToParse);
                        if (multilineCommentFirstPosition == -2) {
                            firstMultilineCommentEnd = updateFind(endComment, 0);
                        } else {
                            firstMultilineCommentEnd = updateFind(endComment, multilineCommentFirstPosition + multiLineCommentOffset);
                        }
                        int end = firstMultilineCommentEnd + multiLineCommentOffset;
                        outputLineInWords.add(new Word(inputStringToParse.substring(deletedChars, Math.min(inputStringToParse.length(), end)), Type.Comment));
                        if (firstMultilineCommentEnd < inputStringToParse.length()) { // TODO
                            update(firstMultilineCommentEnd + multiLineCommentOffset);
                            isCommentContinuous = false;
                        } else {
                            isCommentContinuous = true;
                            break;
                        }
                    } else if (bracketFirstPosition == closestMatch) {
                        outputLineInWords.add(new Word(inputStringToParse.substring(deletedChars, deletedChars + 1), Type.Bracket));
                        update(deletedChars + 1);
                    } else if (commentLineFirstPosition == closestMatch) {
                        outputLineInWords.add(new Word(inputStringToParse.substring(closestMatch), Type.Comment));
                        break;
                    } else if (identifierFirstPosition == closestMatch) {
                        int end = updateEndIdentifier(identifier.end() - 1, inputStringToParse) + 1;
                        outputLineInWords.add(new Word(inputStringToParse.substring(deletedChars, end), fileType));
                        update(end);
                    }
                }
            }
            boolean isThisLineEqualsPreviousParse = needCheck && isCommentContinuous == commentContinuousList.get(row + 1) &&
                    isLinesEquals(outputLineInWords, dataInWords.get(row));
            dataInWords.set(row, outputLineInWords);
            commentContinuousList.set(row + 1, isCommentContinuous);
            return isThisLineEqualsPreviousParse;
        }

        private void update(int EndOfClosestMatch) {

            deletedChars = EndOfClosestMatch;

            multilineCommentFirstPosition = updateWhileLess2(multilineCommentFirstPosition, startComment, deletedChars);
            commentLineFirstPosition = updateWhileLess2(commentLineFirstPosition, commentLine, deletedChars);
            bracketFirstPosition = updateWhileLess2(bracketFirstPosition, bracket, deletedChars);

            identifierFirstPosition = updateIdentifier(updateWhileLess(identifierFirstPosition, identifier, Math.max(0, deletedChars - 1)),
                    inputStringToParse);

        }

        private int updateEndIdentifier(int position, StringBuilder s) {
            return position == s.length() - 1 && Character.isJavaIdentifierPart(s.charAt(position)) ? position : position - 1;
        }

        private int updateIdentifier(int position, StringBuilder s) {
            return position >= s.length() || position < s.length() && position >= 0 && Character.isJavaIdentifierStart(s.charAt(position)) ? position : position + 1;
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

        private int updateFind(Matcher m, int start) {
            return start < inputStringToParse.length() && m.find(start) ? m.start() : Integer.MAX_VALUE - 10;
        }
        private int updateFind(Matcher m) {
            return m.find() ? m.start() : Integer.MAX_VALUE - 10;
        }

        private int updateWhileLess(int value, Matcher m, int threshold) {
            if (value <= threshold) {
                return updateFind(m, threshold);
            }
            return value;
        }

        private int updateWhileLess2(int value, Matcher m, int threshold) {
            while (value < threshold) {
                value = updateFind(m);
            }
            return value;
        }
    }
}
