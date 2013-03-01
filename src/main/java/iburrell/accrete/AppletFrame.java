
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
implements AppletStub, AppletContext
{

    public AppletFrame(Applet applet, int width, int height, String title)
    {
        super(title);

        this.resize(width, height);
        this.add("Center", applet);

        applet.setStub(this);

        applet.init();
        this.show();
        applet.start();
    }

    public AppletFrame(Applet a, int x, int y) {
        this(a, x, y, a.getClass().getName());
    }
    
    // AppletStub methods
    public boolean isActive() { return true; }
    public URL getDocumentBase() { return null; }
    public URL getCodeBase() { return null; }
    public String getParameter(String name) { return ""; }
    public AppletContext getAppletContext() { return this; }
    public void appletResize(int width, int height) {}

    // AppletContext methods
    public AudioClip getAudioClip(URL url) { return null; }
    public Image getImage(URL url) { return null; }
    public Applet getApplet(String name) { return null; }
    public Enumeration getApplets() { return null; }
    public void showDocument(URL url) { }
    public void showDocument(URL url, String target) { }
    public void showStatus(String status) { }

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
