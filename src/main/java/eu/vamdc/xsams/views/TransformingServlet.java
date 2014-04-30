package eu.vamdc.xsams.views;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * A servlet that transforms data to web pages using XSLT. The servlet uses
 * the data cache shared throughout the web application.
 * 
 * @throws RequestException If the request does not identified the cached data to view.
 * @throws RequestException If the specified data are not in the cache.
 * @throws FileNotFoundException If the data are known to the cache but their file is missing.
 * @throws IOException If the response cannot be written.
 * @throws IllegalStateException If the data cache is not available.
 * @throws IllegalStateException If the transforming stylesheet is not available.
 * @throws TransformerException If the XSAMS cannot be transformed to HTML.
 * @author Guy Rixon
 */
public class TransformingServlet extends ErrorReportingServlet {
  
  @Override
  public void get(HttpServletRequest request, HttpServletResponse response) 
      throws RequestException, IllegalStateException, FileNotFoundException, 
             IOException, TransformerException, DownloadException, ServletException {
    String key = getKey(request);
    
    CachedDataSet x = getCache().get(key);
    if (x.isReady()) {
      transformXsams(request, key, response);
    }
    else {
      writeDeferral(request, x, response);
    }
  }
  
  @Override
  public void post(HttpServletRequest request, HttpServletResponse response) 
      throws RequestException, IllegalStateException, FileNotFoundException, 
             IOException, TransformerException, DownloadException, ServletException {
    get(request, response);
  }
  
  public void transformXsams(HttpServletRequest request, String key, HttpServletResponse response) 
      throws RequestException, IllegalStateException, FileNotFoundException, IOException, TransformerException {
    String version = getXsamsVersion(getData(key));
    StreamSource in = getData(key);
    response.setContentType("text/plain");
    response.setCharacterEncoding("UTF-8");
    StreamResult out = new StreamResult(response.getWriter());
    
    Transformer t = TransformerFactory.newInstance().newTransformer(getXslt(version));
    t.transform(in, out);
  }
  
  
  
  protected StreamSource getData(String key) 
      throws RequestException, IllegalStateException, FileNotFoundException {
    DataCache cache = (DataCache) getServletContext().getAttribute(DataCache.CACHE_ATTRIBUTE);
    if (cache == null) {
      throw new IllegalStateException("The data cache is missing");
    }
    getCache().purge();
    CachedDataSet x = getCache().get(key);
    if (x == null) {
      throw new RequestException("Nothing is cached under " + key);
    }
    try {
      return new StreamSource(new FileInputStream(x.getCacheFile()));
    }
    catch (FileNotFoundException e) {
      throw new FileNotFoundException("Cached XSAMS file " + x.getCacheFile() + " is missing");
    }
  }
  
  protected String getKey(HttpServletRequest request) throws RequestException {
    String q = request.getPathInfo();
    log("q=" + q);
    return (q.startsWith("/"))? q.substring(1) : q;
  }
  
  protected String getOriginalUrlEncoded(String key) throws RequestException {
    CachedDataSet x = getCache().get(key);
    if (x == null) {
      throw new RequestException("Nothing is cached under " + key);
    }
    URL u = x.getOriginalUrl();
    try {
      return (u == null)? null : URLEncoder.encode(u.toString(), "UTF-8");
    }
    catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }
  
  protected Source getXslt(String version) {
    String stylesheetName = getInitParameter("stylesheet");
    InputStream in = this.getClass().getResourceAsStream("/"+version+"/"+stylesheetName);
    if (in == null) {
      throw new IllegalStateException("Can't find the stylesheet " + stylesheetName);
    }
    return new StreamSource(in);
  }
  
  
  protected DataCache getCache() throws IllegalStateException {
    DataCache cache = (DataCache) getServletContext().getAttribute(DataCache.CACHE_ATTRIBUTE);
    if (cache == null) {
      throw new IllegalStateException("The data cache is missing");
    }
    return cache;
  }
  
  protected String getXsamsVersion(Source xml) throws RequestException {
    XMLEventReader in = null;
    try {
      in = XMLInputFactory.newFactory().createXMLEventReader(xml);
      while (in.hasNext()) {
        XMLEvent x = (XMLEvent) in.next();
        if (x.isStartElement()) {
          String n = x.asStartElement().getName().getNamespaceURI();
          if ("http://vamdc.org/xml/xsams/0.3".equals(n)) {
            LOG.info("XSAMS v0.3");
            return "0.3";
          }
          else if ("http://vamdc.org/xml/xsams/1.0".equals(n)) {
            LOG.info("XSAMS v1.0");
            return "1.0";
          }
          else {
            throw new RequestException("XSAMS version was not recognized");
          }
        }
      }
      throw new RequestException("XSAMS version was not given");
    }
    catch(Exception e) {
      throw new RequestException("XSAMS version was not found", e);
    }
    finally {
      if (in != null) {
        try {
          in.close();
        }
        catch (Exception e) {
          // Ignore it.
        }
      }
    }
  }

  private void writeDeferral(HttpServletRequest request, CachedDataSet x, HttpServletResponse response) 
      throws ServletException, IOException {
    long bytesDownloaded = x.getByteCounter().get();
    request.setAttribute("eu.vamdc.xsams.views.bytesdownloaded", bytesDownloaded);
    request.getRequestDispatcher("/later.jsp").forward(request, response);
  }
  
  
}
