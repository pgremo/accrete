
// Author: Ian Burrell  <iburrell@leland.stanford.edu>
// Created: 1996/12/09
// Modified: 1996/12/09

// Copyright 1996 Ian Burrell

package accrete.applet;

import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class ExitableFrame extends Frame implements WindowListener {

  private static final long serialVersionUID = 3021523229948771306L;

  public ExitableFrame(String t) {
    super(t);
    addWindowListener(this);
  }

  @Override
  public void windowOpened(WindowEvent e) {
  }

  @Override
  public void windowClosing(WindowEvent e) {
    dispose();
  }

  @Override
  public void windowClosed(WindowEvent e) {
  }

  @Override
  public void windowIconified(WindowEvent e) {
  }

  @Override
  public void windowDeiconified(WindowEvent e) {
  }

  @Override
  public void windowActivated(WindowEvent e) {
  }

  @Override
  public void windowDeactivated(WindowEvent e) {
  }
}

