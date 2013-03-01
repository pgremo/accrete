package iburrell.accrete;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Enumeration;
import java.util.Vector;

import static java.awt.Color.black;
import static java.awt.Color.white;
import static java.lang.Math.log;
import static java.lang.Math.pow;

public class AccreteApplet extends Applet implements MouseListener, Runnable {

  private Accrete gen = null;
  private Vector planets = null;

  public void init() {
    setBackground(white);
    setForeground(black);
    gen = new Accrete();
    addMouseListener(this);
  }

  public void start() {
    run();
  }

  public void run() {
    planets = gen.DistributePlanets();
    repaint();
  }

  public void paint(Graphics g) {
    DrawGrid(g);
    if (planets == null) return;
    DrawPlanets(g);
  }


  private void DrawGrid(Graphics g) {
    int hscale = hscale();
    int vscale = vscale();
    int width = hscale * 3;
    int height = vscale * 2;

    g.setColor(black);
    g.drawRect(0, 0, width, height);
    g.drawLine(0, vscale, width, vscale);

    int ticklen = vscale / 10;
    int ytick = height - ticklen;
    g.drawLine(hscale, height, hscale, height - 2 * ticklen);
    g.drawLine(2 * hscale, height, 2 * hscale, height - 2 * ticklen);

    for (int i = 2; i < 10; i++) {
      int offset = (int) ((double) hscale * log10((double) i));
      g.drawLine(offset, height, offset, ytick);
      g.drawLine(offset + hscale, height, offset + hscale, ytick);
      g.drawLine(offset + 2 * hscale, height, offset + 2 * hscale, ytick);
    }

  }

  private void DrawPlanets(Graphics g) {
    int hscale = hscale();
    int vscale = vscale();
    int rscale = hscale / 30;

    Enumeration e = planets.elements();

    while (e.hasMoreElements()) {
      Planetismal curr = (Planetismal) e.nextElement();
      double au = log10(curr.getOrbitalAxis());
      double rad = pow(curr.getMassEarth(), 1.0 / 3.0);
      int r = (int) (rad * (double) rscale);
      int x0 = (int) (au * (double) hscale);
      int x = x0 + hscale - r;
      int y = vscale - r;
      if (curr.isGasGiant())
        g.drawOval(x, y, 2 * r, 2 * r);
      else
        g.fillOval(x, y, 2 * r, 2 * r);
    }
  }


  private int hscale() {
    int width = getSize().width;
    if ((width % 3) == 0) width--;
    return width / 3;
  }

  private int vscale() {
    int height = getSize().height;
    if ((height % 2) == 0) height--;
    return height / 2;
  }


  private static double log10(double a) {
    return log(a) / log(10.0);
  }


  public static void main(String[] args) {
    new AppletFrame(new AccreteApplet(), 721, 241, "Accrete");
  }

  @Override
  public void mouseClicked(MouseEvent e) {
  }

  @Override
  public void mousePressed(MouseEvent e) {
    if (e.getClickCount() < 2) return;
    start();
    repaint();
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }
}

