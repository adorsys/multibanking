<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet version="2.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:ut="urn:jboss:domain:undertow:3.0"
    xmlns:logging="urn:jboss:domain:logging:3.0">

    <xsl:output method="xml" indent="yes"/>

    <!-- Enable SSL -->
    <xsl:template match="//ut:subsystem/ut:server/ut:http-listener">
      <http-listener name="default" socket-binding="http" redirect-socket="https" proxy-address-forwarding="true"/>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>
