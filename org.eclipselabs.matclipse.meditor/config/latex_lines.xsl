<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output omit-xml-declaration="yes" encoding="ISO-8859-1"/>

<xsl:template match="/">
\documentclass[12pt,a4paper]{article}
\input{./shorts}
\input{./shortsmltutor}

\begin{document}
<xsl:apply-templates />
\end{document}
</xsl:template>

<xsl:template match="matlab_file">
      <xsl:apply-templates select="formatted_content"/>
</xsl:template>

<xsl:template match="formatted_content">
      <xsl:apply-templates/>
</xsl:template>

<xsl:template match="partitioning">\begin{lcodebox}
<xsl:for-each select="line">\linenumber{<xsl:value-of select="@linenumber" />}<xsl:text disable-output-escaping="yes"> &amp; </xsl:text> 
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
\end{lcodebox}
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

<xsl:variable name="rstring">rrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrrr</xsl:variable>
<xsl:template match="variable">
\begin{outputbox}
\begin{tabular}{<xsl:value-of select="substring($rstring, 1, @numcols)" />}
<xsl:if test="@showvarname='true'">\multicolumn{<xsl:value-of select="@numcols" />}{l}{\verb°<xsl:value-of select="@name" /> = °} \\
</xsl:if>
<xsl:for-each select="row">
<xsl:for-each select="cell">
<xsl:choose>
<xsl:when test="@dots='horizontal'">\multicolumn{1}{c}{$\cdots$}</xsl:when>
<xsl:when test="@dots='vertical'">\multicolumn{1}{c}{$\vdots$}</xsl:when>
<xsl:when test="@dots='diagonal'">\multicolumn{1}{c}{$\ddots$}</xsl:when>
<xsl:otherwise>\verb°<xsl:value-of select="." />°</xsl:otherwise>
</xsl:choose>
<xsl:if test="position() != last()">
<xsl:text disable-output-escaping="yes"> &amp; </xsl:text>
</xsl:if>
</xsl:for-each>
<xsl:if test="position() != last()">
<xsl:text>\\&#xA;</xsl:text>
</xsl:if>
</xsl:for-each>
\end{tabular}
\end{outputbox}
</xsl:template>

<xsl:template match="latex_cmd">
<xsl:value-of select="." />
</xsl:template>

</xsl:stylesheet>