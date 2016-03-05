package Editor;

class Word {

    public Type type;
    private int start;
    private int end;
    private StringBuilder value;

    public Word(StringBuilder value, int start, int end, Type type) {
        this.value = value;
        this.end = end;
        this.start = start;
        this.type = type;
    }

    public Word(StringBuilder value, int start, int end, FileType fileType) {
        this.value = value;
        this.end = end;
        this.start = start;

        if (fileType == FileType.Java) {
            type = KeyWords.isJavaKey(string()) ? Type.Key : Type.Identifier;
        } else if (fileType == FileType.JS) {
            type = KeyWords.isJSKey(string()) ? Type.Key : Type.Identifier;
        } else {
            type = Type.Other;
        }
    }

    public String string() {
        return value.substring(start, end);
    }

    public int length() {
        return end - start;
    }

    enum Type {
        Key, Identifier, Comment, Bracket, Other, BracketLight
    }
}
