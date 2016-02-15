package Editor;

class Word {

    public final String s;
    public Type t;

    Word(String string, FileType fileType) {
        s = string;
        if (fileType == FileType.Java) {
            t = KeyWords.isJavaKey(s) ? Type.Key : Type.Identifier;
        } else if (fileType == FileType.JS) {
            t = KeyWords.isJSKey(s) ? Type.Key : Type.Identifier;
        } else {
            t = Type.Other;
        }
    }

    public Word(String s, Type t) {
        this.s = s;
        this.t = t;
    }

    enum Type {
        Key, Identifier, Comment, Bracket, Other, BracketLight
    }
}
