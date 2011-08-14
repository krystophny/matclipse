<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output omit-xml-declaration="yes" encoding="ISO-8859-1"/>

<xsl:template match="/">
<xsl:apply-templates />
</xsl:template>

<xsl:template match="matlab_file">
      <xsl:apply-templates select="formatted_content"/>
</xsl:template>

<xsl:template match="formatted_content">
      <xsl:apply-templates/>
</xsl:template>

<xsl:template match="partitioning">\begin{codebox}
<xsl:for-each select="line">
<xsl:for-each select="partition">
  <xsl:choose>
    <xsl:when test="@type='__dftl_partition_content_type'">\verb°<xsl:value-of select="." />°</xsl:when>
    <xsl:when test="@type='__matlab_comment'">\matlabcomment{\%<xsl:value-of select="substring-after(.,'%')" />}</xsl:when>
    <xsl:when test="@type='__matlab_singleline_string'">{\color{stringcolor}{\verb°<xsl:value-of select="." />°}}</xsl:when>
    <xsl:when test="@type='__matlab_keyword'">{\color{keywordcolor}{\verb°<xsl:value-of select="." />°}}</xsl:when>
    <xsl:when test="@type='__matlab_function'">{\color{functioncolor}{\verb°<xsl:value-of select="." />°}}</xsl:when>
    <xsl:when test="@type='__toolbox_function'">{\color{toolboxcolor}{\verb°<xsl:value-of select="." />°}}</xsl:when>
    <xsl:when test="@type='__matlab_operator'">{\color{keywordcolor}{\verb°<xsl:value-of select="." />°}}</xsl:when>
    <xsl:when test="@type='__matlab_code'">\verb°<xsl:value-of select="." />°</xsl:when>
    <xsl:when test="@type='__matlab_number'">{\color{numbercolor}{\verb°<xsl:value-of select="." />°}}</xsl:when>
    <xsl:when test="@type='__matlab_whitespace'">\matlabwhitespace{<xsl:value-of select="." />}</xsl:when>
    <xsl:when test="@type='__matlab_continuation'">\matlab_comment{<xsl:value-of select="." />}</xsl:when>
    <xsl:otherwise>unknown partition</xsl:otherwise>
  </xsl:choose>
</xsl:for-each>
<xsl:if test="count(partition) = 0"><xsl:text>\ </xsl:text></xsl:if>
<xsl:if test="position() != last()">
  <xsl:text>\\&#xA;</xsl:text>
</xsl:if>
</xsl:for-each>
\end{codebox}
</xsl:template>

<xsl:template match="markuptext"><xsl:apply-templates /></xsl:template>

<xsl:template match="header1">
\mlsection{<xsl:value-of select="." />}
</xsl:template>

<xsl:template match="header2">
\mlsubsection{<xsl:value-of select="." />}
</xsl:template>

<xsl:template match="header3">
\mlsubsubsection{<xsl:value-of select="." />}
</xsl:template>

<xsl:template match="text"><xsl:apply-templates /></xsl:template>

<xsl:template match="markupnewline"><xsl:text>\rule{0pt}{1em}&#xA;&#xA;</xsl:text></xsl:template>

<xsl:template match="truetype">\verb°<xsl:value-of select="." />°</xsl:template>

<xsl:template match="link">\href{<xsl:value-of select="." />}{<xsl:value-of select="." />}</xsl:template>

<xsl:template match="cequation">
\begin{equation*}
  <xsl:value-of select="." />
\end{equation*}
</xsl:template>

<xsl:template match="list">
\begin{itemize}<xsl:for-each select="listitem">
  \item <xsl:value-of select="." />
</xsl:for-each>
\end{itemize}
</xsl:template>

<xsl:template match="numberedlist">
\begin{enumerate}<xsl:for-each select="numberedlistitem">
  \item <xsl:value-of select="." />
</xsl:for-each>
\end{enumerate}
</xsl:template>

</xsl:stylesheet>