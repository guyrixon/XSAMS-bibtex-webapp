package eu.vamdc.xsams.views;

import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Guy Rixon
 */
public class Locations {
  
  public static String getRootLocation(HttpServletRequest request) {
    return String.format("http://%s:%d%s",
                         request.getServerName(),
                         request.getLocalPort(),
                         request.getContextPath());
  }
  
  public static String getServiceLocation(HttpServletRequest request) {
    return getRootLocation(request) + "/service";
  }
  
  public static String getBibtexLocation(HttpServletRequest request, String key) {
    return getRootLocation(request) + "/bibtex/" + key;
  }
  
  public static String getCapabilitiesLocation(HttpServletRequest request) {
    return getRootLocation(request) + "/capabilities";
  }
  
  public static String getCapabilitiesCssLocation(HttpServletRequest request) {
    return getRootLocation(request) + "/Capabilities.xsl";
  }
  
}
