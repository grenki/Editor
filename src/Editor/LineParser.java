package Editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import Editor.Word.Type;

class LineParser {

    private static final Character[] specialChars = {'{', '}', '[', ']', '(', ')', '/'};
    private static final HashSet<Character> charType = new HashSet<>(Arrays.asList(specialChars));

    private final ArrayList<Word> outputLineInWords;
    private final StringBuilder inputStringToParse;
    private boolean isCommentContinuous;
    private int pos;
    private int startWord;
    private Type state;
    private final FileType fileType;

    LineParser(StringBuilder inputStringToParse, boolean isCommentContinuous, FileType fileType) {
        this.inputStringToParse = inputStringToParse;
        this.isCommentContinuous = isCommentContinuous;
        this.fileType = fileType;

        outputLineInWords = new ArrayList<>();
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
        if (type == Type.Identifier) {
            outputLineInWords.add(new Word(inputStringToParse.substring(start, end), fileType));
        } else {
            outputLineInWords.add(new Word(inputStringToParse.substring(start, end), type));
        }
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

    public ArrayList<Word> getResultLine() {
        return outputLineInWords;
    }

    public boolean isCommentContinuous() {
        return isCommentContinuous;
    }
}
