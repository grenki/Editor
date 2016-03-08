package Editor;

class Word {

    public Type type;
    public int start;
    public int end;

    public Word(int start, int end, Type type) {
        this.type = type;
        this.start = start;
        this.end = end;
    }

    /*public Word(StringBuilder value, int start, int end, FileType fileType) {
        //this.value = value;
        this.end = end;
        this.start = start;

        if (fileType == FileType.Java) {
            type = KeyWords.isJavaKey(string()) ? Type.Key : Type.Identifier;
        } else if (fileType == FileType.JS) {
            type = KeyWords.isJSKey(string()) ? Type.Key : Type.Identifier;
        } else {
            type = Type.Other;
        }
    }*/

    /*public String string() {
        return value.substring(start, end);
    }*/

    public int length() {
        return end - start;
    }

    enum Type {
        Key, Identifier, Comment, Bracket, Other, BracketLight, CR
    }
}
