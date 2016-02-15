package Editor;

public class Word {

    enum Type {
        Key, Identifier, Comment, Bracket, Other, BracketLight
    }
    public final String s;
    public Type t;

    Word(String string) {
        s = string;
        t = KeyWords.isKey(s) ? Type.Key : Type.Identifier;
    }

    public Word(String s, Type t) {
        this.s = s;
        this.t = t;
    }
}
