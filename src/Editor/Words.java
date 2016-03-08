package Editor;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Base64;

class Words {
    private ArrayList<Integer> start = new ArrayList<>(10000);
    private ArrayList<Integer> end;
    private ArrayList<Byte> type = new ArrayList<>(10000);

    Words() {
        for (int i = 0; i < 1000; i++) {
            start.add(1000002);
            type.add((byte)(i % 126));
        }
    }

}
