package Editor;

import gnu.trove.list.array.TIntArrayList;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyListener;
import java.util.List;

class ETextArea extends JPanel{

    private final int lineSpacing = 13;
    private final int charWidth = 8;
    private final EListener listener;
    private final int minOffsetRight = 1;
    private final int minOffsetBottom = 1;
    private final EDocument doc;

    public ETextArea(JScrollBar scrollBar) {
        setOpaque(true);
        doc = new EDocument(scrollBar);

        listener = new EListener(doc, this);
        addMouseMotionListener(listener);
        addMouseListener(listener);
        addMouseWheelListener(listener);
        addComponentListener(listener);
    }

    public KeyListener getKeyListener() {
        return listener;
    }

    public AdjustmentListener getAdjustmentListener() {
        return listener;
    }

    public void updateWindowSize() {
        doc.setWindowSize(getWidth() / charWidth - minOffsetRight, getHeight() / lineSpacing - minOffsetBottom);
    }

    public void setNewDocument(List<String> list){
        doc.recreateDocument(list);
        listener.setEDocument(doc);
        updateWindowSize();
        repaint();
    }

    public void setFileName(String fileName, boolean open) {
        doc.setFileName(fileName, open);
        repaint();
    }

    public List<CharSequence> getLines() {
        return doc.getAllDataInLines();
    }

    int scaleToColumn(int x) {
        return x / charWidth + doc.getWidthOffset();
    }

    int scaleToRow(int y) {
        return y / lineSpacing + doc.getHeightOffset();
    }

    private int scaleFromRowToYPixel(int row) {
        return (row - doc.getHeightOffset()) * lineSpacing + 3;
    }

    private int scaleFromColumnToXPixel(int column) {
        if (column < 0) {
            return 0;
        }

        if (column == Integer.MAX_VALUE) {
            return getWidth();
        }

        return (column - doc.getWidthOffset()) * charWidth;
    }

    private Polygon createScaledPolygon(int x1, int y1, int x2, int y2) {
        x1 = scaleFromColumnToXPixel(x1);
        x2 = scaleFromColumnToXPixel(x2);

        y1 = scaleFromRowToYPixel(y1);
        y2 = scaleFromRowToYPixel(y2);
        return createPolygon(x1, y1, x2, y2);
    }

    private Polygon createPolygon(int x1, int y1, int x2, int y2) {
        y1 = Math.max(0, y1);
        x1 = Math.max(0, x1);
        y2 = Math.min(getHeight(), y2);
        x2 = Math.min(getWidth(), x2);

        int[] x = {x1,x2,x2,x1};
        int[] y = {y1,y1,y2,y2};
        return new Polygon(x,y, 4);
    }

    protected void paintComponent(Graphics graphics) {
        Graphics2D graphics2D = (Graphics2D) graphics;

        // Draw background
        graphics2D.setPaint(Color.WHITE);
        graphics2D.fillPolygon(createPolygon(0, 0, this.getWidth(), this.getHeight()));
        graphics2D.setFont(new Font("MONOSPACED", Font.PLAIN, 14));

        if (doc.isExistSelection()) {
            drawSelectionBackground(graphics2D);
        }

        drawText(graphics2D);

        drawCaret(graphics2D);
    }

    private void drawSelectionBackground(Graphics2D graphics2D) {
        int[] selection = doc.getSelectionInterval();
        int startColumn = selection[0];
        int startRow = selection[1];
        int endColumn = selection[2];
        int endRow = selection[3];

        graphics2D.setPaint(Color.LIGHT_GRAY);
        graphics2D.fillPolygon(createScaledPolygon(-1, startRow, Integer.MAX_VALUE, endRow + 1));

        graphics2D.setPaint(Color.WHITE);
        graphics2D.fillPolygon(createScaledPolygon(-1, startRow, startColumn, startRow + 1));
        graphics2D.fillPolygon(createScaledPolygon(endColumn, endRow, Integer.MAX_VALUE, endRow + 1));
    }

    private void drawText(Graphics2D graphics2D) {

        StringBuilder data = doc.getAllDataInString();
        TIntArrayList length = doc.getAllLinesLength();
        Words dataInWords = doc.getAllDataInWords();

        graphics2D.setPaint(Color.BLACK);

        int startDrawingRow = doc.getHeightOffset() > 0 ? -1 : 0;
        int y = startDrawingRow * lineSpacing;
        boolean isText = doc.isFileTypeText();

        int pos = 0;
        for (int j = 0; j < doc.getHeightOffset() + startDrawingRow; j++) {
            pos += length.get(j) + 1;
        }

        for (int i = doc.getHeightOffset() + startDrawingRow; i < length.size() && y < getHeight(); i++) {
            y += lineSpacing;
            int x = -doc.getWidthOffset() * charWidth;
            if (!isText) {
                for (int j = 0; j < dataInWords.rowSize(i); j++) {
                    Word word = dataInWords.get(i, j);
                    if (x > getWidth()) {
                        break;
                    }
                    switch (word.type) {
                        case Key:
                            graphics2D.setPaint(Color.BLUE);
                            break;
                        case Identifier:
                            graphics2D.setPaint(Color.GRAY);
                            break;
                        case Comment:
                            graphics2D.setPaint(Color.MAGENTA);
                            break;
                        case Bracket:
                            graphics2D.setPaint(Color.BLACK);
                            break;
                        case BracketLight:
                            graphics2D.setPaint(Color.RED);
                            break;
                        case Other:
                            graphics2D.setPaint(Color.BLACK);
                            break;
                        default:
                            graphics2D.setPaint(Color.BLACK);
                            break;
                    }
                    graphics2D.drawString(data.substring(pos + word.start, pos + word.end), x, y);

                    x += word.length() * charWidth;
                }
                pos += length.get(i) + 1;
            } else {
                graphics2D.drawString(data.substring(pos, Math.min(pos + length.get(i), data.length())), x, y);

                pos += length.get(i) + 1;
            }
        }
    }

    private void drawCaret(Graphics2D graphics2D) {
        graphics2D.setPaint(Color.BLACK);
        int x = scaleFromColumnToXPixel(doc.getCaretColumn());
        int y = scaleFromRowToYPixel(doc.getCaretRow());
        graphics2D.drawLine(x, y, x, y + lineSpacing);
    }
}
