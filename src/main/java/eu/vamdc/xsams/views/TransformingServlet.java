package eu.vamdc.xsams.views;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * A servlet that transforms data to web pages using XSLT. The servlet uses
 * the data cache shared throughout the web application.
 * 
 * @author Guy Rixon
 */
public abstract class TransformingServlet extends HttpServlet {
  
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) 
      throws IOException, ServletException {
    try {
      produceDocument(request, response);
    }
    catch (RequestException e) {
      e.printStackTrace();
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.toString());
    }
    catch (Exception e) {
      e.printStackTrace();
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
    }
  }
  
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) 
      throws IOException, ServletException {
    try {
      produceDocument(request, response);
    }
    catch (RequestException e) {
      e.printStackTrace();
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.toString());
    }
    catch (Exception e) {
      e.printStackTrace();
      response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
    }
  }
  
  
  protected StreamSource getData(String key) throws ServletException, FileNotFoundException {
    DataCache cache = (DataCache) getServletContext().getAttribute(CacheFilter.CACHE_ATTRIBUTE);
    if (cache == null) {
      throw new ServletException("The data cache is missing");
    }
    cache.purge();
    CachedDataSet x = cache.get(key);
    if (x == null) {
      throw new RequestException("Nothing is cached under " + key);
    }
    try {
      FileReader fr = new FileReader(x.getCacheFile());
      return new StreamSource(fr);
    }
    catch (FileNotFoundException e) {
      throw new ServletException("Cache file " + x.getCacheFile() + " is missing");
    }
  }
  
  protected String getKey(HttpServletRequest request) throws RequestException {
    String q = request.getPathInfo();
    log("q=" + q);
    return (q.startsWith("/"))? q.substring(1) : q;
  }
  
  protected String getOriginalUrlEncoded(String key) throws RequestException {
    DataCache cache = (DataCache) getServletContext().getAttribute(CacheFilter.CACHE_ATTRIBUTE);
    if (cache == null) {
      throw new RequestException("The data cache is missing");
    }
    CachedDataSet x = cache.get(key);
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
  
  
  
  
  protected void produceDocument(HttpServletRequest request, HttpServletResponse response) throws RequestException, ServletException, FileNotFoundException, IOException {
    String key = getKey(request);
    StreamSource in = getData(key);
    response.setContentType("text/html");
    response.setCharacterEncoding("UTF-8");
    String u = getOriginalUrlEncoded(key);
    String reloadUrl = (u == null)? "" : Locations.getServiceLocation(request) + "?url=" + u;
    StreamResult out = new StreamResult(response.getWriter());
    Transformer t = getTransformer(Locations.getLineListLocation(request, key),
                                   Locations.getStateListLocation(request, key),
                                   Locations.getStateLocation(request, key),
                                   Locations.getBroadeningLocation(request, key),
                                   reloadUrl,
                                   request.getParameter("id"));
    transform(in, out, t);
  }
  
  protected void transform(StreamSource in, StreamResult out, Transformer t)
      throws ServletException {
    try {
      t.transform(in, out);
    }
    catch (Exception e) {
      throw new ServletException(e);
    }
  }
  
  
  protected abstract Transformer getTransformer(String lineListUrl,
                                                String stateListUrl,
                                                String selectedStateUrl,
                                                String broadeningUrl,
                                                String reloadUrl,
                                                String id) throws ServletException;
  
}
