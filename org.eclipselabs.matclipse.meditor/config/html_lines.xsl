<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:template match="/">
  <html>
    <head>
      <meta name="generator" content="Bluefish 1.0.7"/>
      <link rel="stylesheet" type="text/css" href="mltutor_style.css" />
      <title><xsl:apply-templates select="matlab_file/filename"/></title> 
    </head>
    
    <body>
      <xsl:apply-templates />
    </body>
 
  </html>
</xsl:template>

<xsl:template match="matlab_file">
      <xsl:apply-templates select="formatted_content"/>
</xsl:template>

<xsl:template match="formatted_content">
      <xsl:apply-templates/>
</xsl:template>

<xsl:template match="partitioning">
  <table class="code_table">      
    <xsl:apply-templates/>
  </table>
</xsl:template>


<xsl:template match="line">
  <tr>
    <td class="linenumber_cell"><xsl:value-of select="@linenumber" /></td>
    <td class="code_cell">
  <xsl:for-each select="partition">
  <xsl:choose>
    <xsl:when test="@type='__dftl_partition_content_type'">       
       <xsl:value-of select="." />
    </xsl:when>
    <xsl:when test="@type='__matlab_comment'">
      <span class="matlab_comment"><xsl:value-of select="." /></span>
    </xsl:when>
    <xsl:when test="@type='__matlab_singleline_string'">
      <span class="matlab_string"><xsl:value-of select="." /></span>
    </xsl:when>
    <xsl:when test="@type='__matlab_keyword'">
      <span class="matlab_keyword"><xsl:value-of select="." /></span>
    </xsl:when>
    <xsl:when test="@type='__matlab_function'">
      <span class="matlab_function"><xsl:value-of select="." /></span>
    </xsl:when>
    <xsl:when test="@type='__toolbox_function'">
      <span class="toolbox_function"><xsl:value-of select="." /></span>
    </xsl:when>
    <xsl:when test="@type='__matlab_operator'">
      <span class="matlab_keyword"><xsl:value-of select="." /></span>
    </xsl:when>
    <xsl:when test="@type='__matlab_code'">
      <span><xsl:value-of select="." /></span>
    </xsl:when>
    <xsl:when test="@type='__matlab_number'">
      <span class="matlab_number"><xsl:value-of select="." /></span>
    </xsl:when>
    <xsl:when test="@type='__matlab_whitespace'">
      <xsl:value-of select="." /> 
    </xsl:when>
    <xsl:when test="@type='__matlab_continuation'">
      <span class="matlab_comment"><xsl:value-of select="." /></span>
    </xsl:when>
    <xsl:otherwise>
      unknown partition
    </xsl:otherwise>
  </xsl:choose>
  </xsl:for-each>
    </td>
  </tr>       
</xsl:template>

<xsl:template match="markuptext">
  <p class="markup_text">
  <xsl:apply-templates />
  </p>
</xsl:template>
  
<xsl:template match="header1">
    <h1><xsl:value-of select="." /></h1>
</xsl:template>

<xsl:template match="header2">
    <h2><xsl:value-of select="." /></h2>
</xsl:template>

<xsl:template match="header3">
    <h3><xsl:value-of select="." /></h3>
</xsl:template>

<xsl:template match="text">
    <span class="text"><xsl:apply-templates /></span>
</xsl:template>

<xsl:template match="markupnewline">
    <br />
</xsl:template>

<xsl:template match="truetype">
    <span class="truetype"><xsl:value-of select="." /></span>
</xsl:template>

<xsl:template match="link">
    <a><xsl:attribute name="href"><xsl:value-of select="." /></xsl:attribute><xsl:value-of select="." /></a>
</xsl:template>

<xsl:template match="cequation">
    <p class="cequation">
    <img><xsl:attribute name="src"><xsl:value-of select="/matlab_file/filename" />_pics/<xsl:value-of select="@eqnum" />.png</xsl:attribute>
    <xsl:attribute name="alt"><xsl:value-of select="." /></xsl:attribute></img>
    </p>
</xsl:template>

<xsl:template match="list">
<ul>
<xsl:for-each select="listitem">   <li><xsl:value-of select="." /></li></xsl:for-each>
</ul>
</xsl:template>

<xsl:template match="numberedlist">
<ol>
<xsl:for-each select="numberedlistitem">   <li><xsl:value-of select="." /></li></xsl:for-each>
</ol>
</xsl:template>

<xsl:template match="variable">
<table class="matrix">
<xsl:if test="@showvarname='true'">
<tr><td colspan="99"><xsl:value-of select="@name" /> =</td></tr>
</xsl:if>
<xsl:for-each select="row">
<tr>
<xsl:for-each select="cell">
    <td>
  <xsl:choose>
    <xsl:when test="@dots='horizontal'"><xsl:attribute name="class">matrix_dot_cell</xsl:attribute><pre>...</pre></xsl:when>
    <xsl:when test="@dots='vertical'"><xsl:attribute name="class">matrix_dot_cell</xsl:attribute><pre>...</pre></xsl:when>
    <xsl:when test="@dots='diagonal'"><xsl:attribute name="class">matrix_dot_cell</xsl:attribute><pre>...</pre></xsl:when>
    <xsl:otherwise><xsl:attribute name="class">matrix_cell</xsl:attribute><pre><xsl:value-of select="." /></pre></xsl:otherwise>
  </xsl:choose>
    </td> 
</xsl:for-each>
    <td class="filler_cell"><pre><xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text></pre></td>
</tr>
</xsl:for-each>
</table>
</xsl:template>

<xsl:template match="latex_cmd"></xsl:template>

</xsl:stylesheet>