<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
  xmlns:xsams="http://vamdc.org/xml/xsams/0.3">

  <xsl:output method="text" encoding="UTF-8" omit-xml-declaration="yes"/>
    
  <xsl:template match="xsams:Source">
    <xsl:text>
    </xsl:text>
    <xsl:choose>
      <xsl:when test="xsams:BibTex">
        <xsl:value-of select="xsams:BibTex"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:choose>
          <xsl:when test="xsams:Category='journal'">
            <xsl:call-template name="article"/>
          </xsl:when>
          <xsl:when test="xsams:Category='database'">
            <xsl:call-template name="database"/>
          </xsl:when>
          <xsl:when test="xsams:Category='book'">
            <xsl:call-template name="book"/>
          </xsl:when>
          <!-- Detect books with wrong or missing catagories -->
          <xsl:when test="(xsams:Publisher and not (xsams:Publisher=''))">
            <xsl:call-template name="book"/>
          </xsl:when>
          <!-- Detect journal articles with wrong or missing catagories -->
          <xsl:when test="(xsams:Volume and not (xsams:Volume=''))">
            <xsl:call-template name="article"/>
          </xsl:when>
        </xsl:choose>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="article">
    <xsl:text>@article {</xsl:text>
    <xsl:value-of select="@sourceID"/>
    <xsl:call-template name="author-list"/>
    <xsl:call-template name="title"/>
    <xsl:call-template name="journal"/>
    <xsl:call-template name="volume"/>
    <xsl:call-template name="pages"/>
    <xsl:call-template name="year"/>
    <xsl:text>}
    </xsl:text>
  </xsl:template>

  <xsl:template name="book">
    <xsl:text>@book {</xsl:text>
    <xsl:value-of select="@sourceID"/>
    <xsl:call-template name="author-list"/>
    <xsl:call-template name="title"/>
    <xsl:call-template name="volume"/>
    <xsl:call-template name="publisher"/>
    <xsl:call-template name="year"/>
    <xsl:text>}
    </xsl:text>
  </xsl:template>
    
  <xsl:template name="database">
    <xsl:text>@misc {</xsl:text>
    <xsl:value-of select="@sourceID"/>
    <xsl:text>, howpublished={database}</xsl:text>
    <xsl:call-template name="author-list"/>
    <xsl:call-template name="title"/>
    <xsl:call-template name="year"/>
    <xsl:call-template name="uri"/>
    <xsl:text>}
    </xsl:text>
  </xsl:template>

  <xsl:template name="author-list">
    <xsl:if test="xsams:Authors/xsams:Author/xsams:Name">
      <xsl:text>, author = {</xsl:text>
      <xsl:for-each select="xsams:Authors/xsams:Author">
        <xsl:if test="not(position()=1)">
          <xsl:text> and </xsl:text>
        </xsl:if>
        <xsl:value-of select="xsams:Name"/>
      </xsl:for-each>
      <xsl:text>}</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template name="title">
    <xsl:if test="xsams:Title">
      <xsl:text>, title = {</xsl:text>
      <xsl:value-of select="xsams:Title"/>
      <xsl:text>}</xsl:text>
    </xsl:if>
  </xsl:template>
    
  <xsl:template name="journal">
    <xsl:if test="xsams:SourceName">
      <xsl:text>, journal = {</xsl:text>
      <xsl:value-of select="xsams:SourceName"/>
      <xsl:text>}</xsl:text>
    </xsl:if>
  </xsl:template>
    
  <xsl:template name="volume">
    <xsl:if test="xsams:Volume">
      <xsl:text>, volume = {</xsl:text>
      <xsl:value-of select="xsams:Volume"/>
      <xsl:text>}</xsl:text>
    </xsl:if>
  </xsl:template>
    
  <xsl:template name="pages">
    <xsl:if test="xsams:PageBegin">
      <xsl:text>, pages = {</xsl:text>
      <xsl:value-of select="xsams:PageBegin"/>
      <xsl:if test="xsams:PageEnd">
        <xsl:text>,</xsl:text>
        <xsl:value-of select="xsams:PageEnd"/>
      </xsl:if>
      <xsl:text>}</xsl:text>
    </xsl:if>
  </xsl:template>

  <xsl:template name="year">
    <xsl:if test="xsams:Year">
      <xsl:text>, year = {</xsl:text>
      <xsl:value-of select="xsams:Year"/>
      <xsl:text>}</xsl:text>
    </xsl:if>
  </xsl:template>
    
  <xsl:template name="uri">
    <xsl:if test="xsams:UniformResourceIdentifier">
      <xsl:text>, url={</xsl:text>
      <xsl:value-of select="xsams:UniformResourceIdentifier[1]"/>
      <xsl:text>}</xsl:text>
    </xsl:if>
  </xsl:template>
    
  <xsl:template name="publisher">
    <xsl:if test="xsams:Publisher">
      <xsl:text>, publisher = {</xsl:text>
      <xsl:value-of select="xsams:Publisher"/>
      <xsl:if test="xsams:City">
        <xsl:text>, </xsl:text>
        <xsl:value-of select="xsams:City"/>
      </xsl:if>
      <xsl:text>}</xsl:text>
    </xsl:if>
  </xsl:template>
    
  <xsl:template match="text()|@*"/>
    
</xsl:stylesheet>
