package Editor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import java.util.List;

class ETextArea extends JPanel{

    private EDocument doc;
    private final int lineSpacing = 13;
    private final int charWidth = 8;
    private final EListener listener;
    private final int minOffsetRight = 1;
    private final int minOffsetBottom = 2;



    public ETextArea() {
        setOpaque(true);
        doc = new EDocument(null);

        listener = new EListener(doc, this);
        addMouseMotionListener(listener);
        addMouseListener(listener);

        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateSize();
            }
            @Override
            public void componentMoved(ComponentEvent e) {            }
            @Override
            public void componentShown(ComponentEvent e) {            }
            @Override
            public void componentHidden(ComponentEvent e) {            }
        });
    }

    public KeyListener getListener() {
        return listener;
    }

    private void updateSize() {
        doc.setWindowSize(getWidth() / charWidth - minOffsetRight, getHeight() / lineSpacing - minOffsetBottom);
    }

    public void setNewDocument(List<String> list){
        doc = new EDocument(list);
        listener.setEDocument(doc);
        updateSize();
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
        int x;
        int y = - doc.getHeightOffset() * lineSpacing;
        ArrayList<ArrayList<Word>> data = doc.getAllDataInWords();
        for (ArrayList<Word> line: data) {
            if (y > getHeight()) {
                break;
            }
            y += lineSpacing;
            x = - doc.getWidthOffset() * charWidth;
            for (Word word: line) {
                if (x > getWidth()) {
                    break;
                }
                switch (word.t){
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
                graphics2D.drawString(word.s, x, y);
                x += word.s.length() * charWidth;
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
