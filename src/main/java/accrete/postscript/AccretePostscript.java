package accrete.postscript;
// Author: Ian Burrell  <iburrell@leland.stanford.edu>
// Created: 1997/01/15
// Modified: 1997/02/09

// Copyright 1997 Ian Burrell

import accrete.Accrete;
import accrete.Planetesimal;

import java.util.Random;

public class AccretePostscript extends Postscript {

  Accrete gen;

  AccretePostscript() {
    super("accrete.ps");
    gen = new Accrete();
    window(-1, -1, 2, 1);
  }

  void run() {
    begin(1);
    logscale("AU");

    Iterable<Planetesimal> system = gen.distributePlanets(new Random());

    for (Planetesimal curr : system) {
      double au = log10(curr.axis());
      double r = Math.pow(curr.mass(), 1.0 / 3.0);
      circle(au, 0, r, curr.gasGiant());
    }
    showpage();
    end();
  }

  void logscale(String xlabel) {
    logscale(xlabel, "");
  }

  void logscale(String xlabel, String ylabel) {
    line(-1, -1, 3, -1);
    line(3, -1, 3, 1);
    line(3, 1, 3, -1);
    line(3, -1, -1, -1);

    line(-1, 1, 3, 1);
    for (int i = 1; i <= 10; i++) {
      double au = (double) i;
      line(log10(au / 10), 1, log10(au / 10), .95);
      line(log10(au), 1, log10(au), .95);
      line(log10(au * 10), 1, log10(au * 10), .95);
    }

    text(-1, 1, ".1");
    text(0, 1, "1");
    text(1, 1, "10");
    text(2, 1, "100");

    text(2.3, 1, xlabel);
    text(-1, .9, ylabel);

  }

  private double log10(double a) {
    return Math.log(a) / Math.log(10.0);
  }


  public static void main(String[] args) {
    AccretePostscript app = new AccretePostscript();
    app.run();
  }

}

