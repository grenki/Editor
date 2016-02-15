package Editor;

import java.util.Arrays;
import java.util.HashSet;

class KeyWords {

    private static final String[] s = {
            "abstract",
            "continue",
            "for",
            "new",
            "switch assert",
            "default",
            "goto",
            "package",
            "synchronized",
            "boolean",
            "do",
            "if",
            "private",
            "this",
            "break",
            "double",
            "implements",
            "protected",
            "throw",
            "byte",
            "else",
            "import",
            "public",
            "throws case",
            "enum",
            "instanceof",
            "return",
            "transient",
            "catch",
            "extends",
            "int",
            "short",
            "try",
            "char",
            "final",
            "interface",
            "static",
            "void",
            "class",
            "finally",
            "long",
            "strictfp",
            "volatile",
            "const",
            "float",
            "native",
            "super",
            "while"};

    private static final HashSet<String> set = new HashSet<>(Arrays.asList(s));

    public static boolean isKey(String s) {
        return set.contains(s);
    }
}
