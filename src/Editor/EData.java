package Editor;

import java.util.ArrayList;

class EData {
    public final StringBuilder data;
    public final ArrayList<Integer> length;
    private int row;
    private int column;


    EData() {
        data = new StringBuilder();
        length = new ArrayList<>(1);
        length.add(0);
        row = 0;
        column = 0;
    }

    private int getPos() {
        //System.out.println("r c " + row + " " + column);
        int res = 0;
        for (int i = 0; i < row; i++) {
            res += length.get(i) + 1;
        }
        res += column;
        //System.out.println("pos " + res);
        return res;
    }

}
