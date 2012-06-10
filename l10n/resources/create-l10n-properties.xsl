<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
	xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
	xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
	xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
	xmlns:my="http://sourceforge.net/projects/accessodf">
	
	<xsl:output method="text" encoding="UTF-8" name="text"/>
	
	<xsl:param name="dest" as="xs:string" select="'.'"/>
	<xsl:param name="name" as="xs:string" select="'Bundle'"/>
	<xsl:param name="default-lang" select="'en'"/>
	
	<xsl:variable name="spreadsheet" select="/office:document-content/office:body/office:spreadsheet"/>
	<xsl:variable name="languages" as="xs:string*">
		<xsl:for-each select="$spreadsheet/table:table[1]/table:table-row[1]
		                      /table:table-cell[position() &gt; 1 and string-length(string(text:p)) &gt; 0]">
			<xsl:sequence select="string(text:p)"/>
		</xsl:for-each>
	</xsl:variable>
	
	<xsl:template match="/">
		<xsl:for-each select="$languages">
			<xsl:variable name="file">
				<xsl:choose>
					<xsl:when test=".=$default-lang">
						<xsl:value-of select="concat($dest, '/', $name, '.properties')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="concat($dest, '/', $name, '_', ., '.properties')"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<xsl:variable name="column" select="position()"/>
			<xsl:result-document href="{$file}" format="text">
				<xsl:for-each select="$spreadsheet/table:table">
					<xsl:for-each select="table:table-row[position() &gt; 1]">
						<xsl:variable name="key-cell" select="table:table-cell[1]"/>
						<xsl:variable name="value-cell" select="my:get-table-cell(table:table-cell[1], $column)"/>
						<xsl:if test="string-length(string($key-cell/text:p)) &gt; 0 and
						              string-length(string($value-cell/text:p)) &gt; 0">
							<xsl:value-of select="string($key-cell/text:p)"/>
							<xsl:text>=</xsl:text>
							<xsl:value-of select="my:to-ascii(string($value-cell/text:p))"/>
							<xsl:text>&#xA;</xsl:text>
						</xsl:if>
					</xsl:for-each>
				</xsl:for-each>
			</xsl:result-document>
		</xsl:for-each>
	</xsl:template>
	
	<xsl:function name="my:get-table-cell">
		<xsl:param name="base-cell"/>
		<xsl:param name="offset"/>
		<xsl:variable name="columns-repeated" select="$base-cell/@table:number-columns-repeated"/>
		<xsl:variable name="next-cell" select="$base-cell/following-sibling::table:table-cell[1]"/>
		<xsl:variable name="next-offset" select="$offset - (if ($columns-repeated) then number($columns-repeated) else 1)"/>
		<xsl:sequence select="if ($next-offset &gt;= 0) then my:get-table-cell($next-cell, $next-offset) else $base-cell"/>
	</xsl:function>
	
	<xsl:function name="my:to-ascii" as="xs:string">
		<xsl:param name="string"/>
		<xsl:variable name="codepoints" select="string-to-codepoints($string)"/>
		<xsl:variable name="ascii" as="xs:string*">
			<xsl:for-each select="string-to-codepoints($string)">
				<xsl:choose>
					<xsl:when test=". &lt; 128">
						<xsl:value-of select="codepoints-to-string(.)"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select=" 
							if (. &gt; 4095)     then concat('\u', my:int-to-hex(.))
							else if (. &gt; 255) then concat('\u0', my:int-to-hex(.))
							else                      concat('\u00', my:int-to-hex(.))"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:variable>
		<xsl:sequence select="string-join($ascii, '')"/>
	</xsl:function>
	
	<xsl:function name="my:int-to-hex" as="xs:string">
		<xsl:param name="in" as="xs:integer"/>
		<xsl:sequence select="if ($in = 0) then '0' else concat(
			if ($in &gt; 16) then my:int-to-hex($in idiv 16) else '',
			substring('0123456789abcdef', ($in mod 16) + 1, 1))"/>
	</xsl:function>
	
</xsl:stylesheet>