package accrete.postscript;
// Author: Ian Burrell  <iburrell@leland.stanford.edu>
// Created: 1997/01/15
// Modified: 1997/02/09

// Copyright 1997 Ian Burrell

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Postscript {

  int page = 0;
  double base = 50.0;
  double xscale = 1.0;
  double yscale = 1.0;
  double xoff = 0.0;
  double yoff = 0.0;
  static final double width = 450;

  PrintStream out;

  public Postscript(String psfile) {
    try {
      out = new PrintStream(new FileOutputStream(psfile));
    } catch (IOException e) {
      System.err.println(e);
    }
  }

  void begin(int numpage) {
    out.println("%!PS-Adobe-2.1");
    out.printf("%%%%Pages: %d%n", numpage);
    out.println("%%EndComments");
    out.println("/Helvetica findfont 12 scalefont setfont");
    out.println("0 setlinewidth");
    out.println("newpath");
    out.println("%%EndProlog");
    beginpage();
  }

  void end() {
    out.println("%%Trailer");
    out.println("end");
  }

  void beginpage() {
    beginpage(++page);
  }

  void beginpage(int pg) {
    out.printf("%%%%Page: %d %d%n", pg, pg);
    out.printf("%s %s%s translate%n", xoff + base, yoff, base);
    out.printf("%s %s  scale%n", xscale, yscale);
    out.printf("/Helvetica findfont %s scalefont setfont%n", 9 / xscale);
    out.println("0 setlinewidth");
  }

  void showpage() {
    out.println("showpage");
  }

  void window(double x1, double y1, double x2, double y2) {
    double xspan = x2 - x1;
    double yspan = y2 - y1;
    xscale = width / xspan;
    yscale = width / yspan;
    xoff = -xscale * x1;
    yoff = -yscale * y1;
  }

  void circle(double x, double y, double radius, boolean fill) {
    out.printf("%s %s %s 0 360 arc ", x, y, radius);
    out.println(fill ? "fill" : "stroke");
  }

  void line(double x1, double y1, double x2, double y2) {
    out.printf("%s %s moveto ", x1, y1);
    out.printf("%s %s lineto stroke", x2, y2);
    out.println();
  }

  void text(double x, double y, String s) {
    out.printf("%s %s moveto (%s) show newpath%n", x, y, s);
  }

}
