
package iburrell.accrete;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AppletStub;
import java.applet.AudioClip;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class AppletFrame extends ExitableFrame
  implements AppletStub, AppletContext {

  public AppletFrame(Applet applet, int width, int height, String title) {
    super(title);

    this.setSize(width, height);
    this.add("Center", applet);

    applet.setStub(this);

    applet.init();
    this.setVisible(true);
    applet.start();
  }

  // AppletStub methods
  @Override
  public boolean isActive() {
    return true;
  }

  @Override
  public URL getDocumentBase() {
    return null;
  }

  @Override
  public URL getCodeBase() {
    return null;
  }

  @Override
  public String getParameter(String name) {
    return "";
  }

  @Override
  public AppletContext getAppletContext() {
    return this;
  }

  @Override
  public void appletResize(int width, int height) {
  }

  // AppletContext methods
  @Override
  public AudioClip getAudioClip(URL url) {
    return null;
  }

  @Override
  public Image getImage(URL url) {
    return null;
  }

  @Override
  public Applet getApplet(String name) {
    return null;
  }

  @Override
  public Enumeration<Applet> getApplets() {
    return null;
  }

  @Override
  public void showDocument(URL url) {
  }

  @Override
  public void showDocument(URL url, String target) {
  }

  @Override
  public void showStatus(String status) {
  }

  private Map<String, InputStream> streams = new HashMap<>();

  @Override
  public void setStream(String key, InputStream stream) throws IOException {
    streams.put(key, stream);
  }

  @Override
  public InputStream getStream(String key) {
    return streams.get(key);
  }

  @Override
  public Iterator<String> getStreamKeys() {
    return streams.keySet().iterator();
  }

}
