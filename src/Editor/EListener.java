package Editor;

import java.awt.event.*;

class EListener implements KeyListener, MouseMotionListener, MouseListener, MouseWheelListener {

    private final ETextArea area;
    private EDocument doc;
    private boolean isMouseDown;

    public EListener(EDocument doc, ETextArea area) {
        this.doc = doc;
        this.area = area;
        isMouseDown = false;
    }

    public void setEDocument(EDocument doc) {
        this.doc = doc;
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case 16: //Shift
                doc.setShiftPressed(false);
                break;
            case 17: //Control
                break;
            case 155: //Insert
                doc.switchInsert();
                break;
        }
    }

    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (!e.isAltDown()) {
            if (!e.isControlDown()) {
                switch (keyCode) {
                    case 8: // backspace
                        doc.backspace();
                        break;
                    case 9: //tab
                        doc.insertTab();
                        break;
                    case 155: // insert
                        break;
                    case 127: // delete
                        doc.delete();
                        break;
                    case 38: // up
                        doc.up();
                        break;
                    case 37: // left
                        doc.left();
                        break;
                    case 39: // right
                        doc.right();
                        break;
                    case 40: // down
                        doc.down();
                        break;
                    case 16: // shift
                        doc.setShiftPressed(true);
                        break;
                    case 17: // control
                        break;
                    case 36: // home
                        doc.home();
                        break;
                    case 35: //end
                        doc.end();
                        break;
                    case 33: //pageUp
                        doc.pageUp();
                        break;
                    case 34: //pageDown
                        doc.pageDown();
                        break;
                    //case 18: // Alt
                    //case 20: // Caps Lock
                    //case 27: // Escape
                    //    break;
                    //case 112-123: // F1 - 12

                    default:
                        if (e.getKeyChar() != KeyEvent.CHAR_UNDEFINED) {
                            doc.insertChar(e.getKeyChar());
                            doc.setExistSelectionFalse();
                            //System.out.println("keyTyped " + e.getKeyChar());
                        }
                        break;
                }

                if (!e.isShiftDown()) {
                    doc.setExistSelectionFalse();
                }
            } else {
                switch (keyCode) {
                    case 65: //A
                        doc.selectAll();
                        break;
                    case 88: //X
                        doc.cut();
                        break;
                    case 67: //C
                        doc.copy();
                        break;
                    case 86: //V
                        doc.paste();
                        break;
                }
            }

            //System.out.println("keyPressed " + e.getKeyCode());
            area.repaint();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (isMouseDown) {
            doc.mouseMoved(area.scaleToColumn(e.getX()), area.scaleToRow(e.getY()));
            area.repaint();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) { }

    @Override
    public void mouseClicked(MouseEvent e) { }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            isMouseDown = true;
            if (e.isShiftDown()) {
                doc.mouseMoved(area.scaleToColumn(e.getX()), area.scaleToRow(e.getY()));
            } else {
                doc.mousePressed(area.scaleToColumn(e.getX()), area.scaleToRow(e.getY()));
            }
            area.repaint();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON1) {
            isMouseDown = false;
            doc.mouseMoved(area.scaleToColumn(e.getX()), area.scaleToRow(e.getY()));
            area.repaint();
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
            doc.updateHeightOffset(e.getUnitsToScroll());
            area.repaint();
        }
    }
}