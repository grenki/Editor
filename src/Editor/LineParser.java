package Editor;

import Editor.Word.Type;

import java.util.Arrays;
import java.util.HashSet;

class LineParser {

    private static final Character[] specialChars = {'{', '}', '[', ']', '(', ')', '/'};
    private static final HashSet<Character> charType = new HashSet<>(Arrays.asList(specialChars));

    private final String inputStringToParse;
    private final FileType fileType;
    private final Words dataInWords;
    private final int row;
    private boolean isCommentContinuous;
    private int pos;
    private int startWord;
    private Type state;

    LineParser(String inputStringToParse, FileType fileType, Words words, int row) {
        dataInWords = words;
        this.inputStringToParse = inputStringToParse;
        this.isCommentContinuous = dataInWords.isCommentContinuous(row);
        this.fileType = fileType;
        this.row = row;
    }

    public void parseLine() {

        state = isCommentContinuous ? Type.Comment : Type.Other;
        pos = 0;
        startWord = 0;

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
    }

    private void addWord(int start, int end, Type type) {
        if (end == start) {
            return;
        }
        Type resType = type;
        if (resType == Type.Identifier) {
            String s = inputStringToParse.substring(start, end);

            if (fileType == FileType.Java) {
                resType = KeyWords.isJavaKey(s) ? Type.Key : Type.Identifier;
            } else if (fileType == FileType.JS) {
                resType = KeyWords.isJSKey(s) ? Type.Key : Type.Identifier;
            } else {
                resType = Type.Other;
            }
        }

        dataInWords.popLast(row, new Word(start, end, resType));
    }

    private void updateState(char ch) {
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

    private boolean isNextSlash(int pos) {
        return pos < inputStringToParse.length() - 1 && inputStringToParse.charAt(pos + 1) == '/';
    }

    private boolean isNextStar(int pos) {
        return pos < inputStringToParse.length() - 1 && inputStringToParse.charAt(pos + 1) == '*';
    }

    public boolean isCommentContinuous() {
        return isCommentContinuous;
    }
}
