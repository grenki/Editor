package Editor;

class Word {

    public final int start;
    public final int end;
    public Type type;

    public Word(int start, int end, Type type) {
        this.type = type;
        this.start = start;
        this.end = end;
    }

    public int length() {
        return end - start;
    }

    enum Type {
        Key, Identifier, Comment, Bracket, Other, BracketLight
    }
}
