<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
   exclude-result-prefixes="xs xsi xsl xd d2r"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
   xmlns:xs="http://www.w3.org/2001/XMLSchema"
   xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
   xmlns:d2r="http://wwww.versi.edu.au/projects/dc3d/daris2rifcs"
   xmlns="http://ands.org.au/standards/rif-cs/registryObjects"
   xsi:schemaLocation="http://ands.org.au/standards/rif-cs/registryObjects http://services.ands.org.au/documentation/rifcs/1.2.0/schema/registryObjects.xsd">

   <xsl:output method="xml" encoding="UTF-8" indent="yes"/>
   <xd:doc scope="stylesheet">
      <xd:desc>
         <xd:p><xd:b>Created on:</xd:b> Oct 17, 2011</xd:p>
         <xd:p><xd:b>Author:</xd:b>Cyrus Keong, King Lung Chiu, Neil Killeen</xd:p>
         <xd:p>XSLT for transforming DaRIS's XML export to RIF-CS 1.2.</xd:p>
      </xd:desc>
   </xd:doc>
    
<!--   <xsl:param name="originatingSource" select="'http://rdr.unimelb.edu.au/vivo'"/>-->
   <xsl:param name="originatingSource" select="d2r:originatingSource(//repository-harvest/repository)"/>
   
   <xsl:variable name="REPOSITORY" select="//repository-harvest/repository"/>
   <xsl:variable name="REPOSITORY_LOCATION" select="$REPOSITORY/custodian/address"/>
<!--  <xsl:variable name="registryObjectGroup" select="concat('DaRIS ', $REPOSITORY/id, ' - ', $REPOSITORY_LOCATION/department)"/> -->
<!--  change requested by ANDS -->
   <xsl:variable name="registryObjectGroup" select="$REPOSITORY_LOCATION/institution"/>
   <xsl:variable name="LINEBREAK" select="'&#xA;'"/>
   

   <xsl:template match="//repository-harvest">
      <registryObjects
         xsi:schemaLocation="http://ands.org.au/standards/rif-cs/registryObjects http://services.ands.org.au/documentation/rifcs/1.2.0/schema/registryObjects.xsd">

         <xsl:apply-templates select="repository"/>

         <xsl:apply-templates select="repository/custodian[not(exists(NLA-ID))]"/>
         
         <xsl:apply-templates select="projects/project[allow-ANDS='true']"/>

         <!-- create a party object for each unique project-owner that has no NLA-ID -->
         <xsl:for-each-group
            select="projects/project[allow-ANDS='true']/project-owner[not(exists(NLA-ID))]"
            group-by="email">
            <xsl:apply-templates select="current-group()[1]"/>
         </xsl:for-each-group>

      </registryObjects>
   </xsl:template>


   <xsl:template match="repository">
      <registryObject group="{$registryObjectGroup}">
         <key><xsl:value-of select="d2r:rifcsKeyValue(.)"/></key>
         <originatingSource>
            <xsl:value-of select="$originatingSource"/>
         </originatingSource>

         <collection type="repository">
            <!-- duplicate registryObject's key for muRIFCS -->
            <identifier type="local"><xsl:value-of select="d2r:rifcsKeyValue(.)"/></identifier>
            <identifier type="local"><xsl:value-of select="id"/></identifier>
            
            <name type="primary">
               <namePart type="text">
                  <!--<xsl:value-of select="name/text()"/>-->
                  <xsl:value-of select="concat(name, ', ', custodian/address/department, ', ', custodian/address/institution)"/>
               </namePart>
            </name>
            
            <xsl:apply-templates select="$REPOSITORY_LOCATION"/>
            
            <xsl:if test="data-holdings/start-date != '' or data-holdings/end-date !=''">
               <coverage>
                  <temporal>
                     <xsl:apply-templates select="data-holdings/start-date"/>
                     <xsl:apply-templates select="data-holdings/end-date"/>
                  </temporal>
               </coverage>
            </xsl:if>            

            <!-- Custodian of repo -->
            <relatedObject>
               <key><xsl:value-of select="d2r:rifcsKeyValue(custodian)"/></key>
               <relation type="isManagedBy"/>
            </relatedObject>

            <!-- Descriptions of repo-->
            <description type="full">
               <xsl:value-of select="data-holdings/description"/>
            </description>
            <description type="rights">
               <xsl:value-of select="rights/description"/>
            </description>            

            <xsl:call-template name="default-field-of-research"/>
            
         </collection>
      </registryObject>
   </xsl:template>


   <xsl:template match="projects/project">
      <registryObject group="{$registryObjectGroup}">
         <key><xsl:value-of select="d2r:rifcsKeyValue(.)"/></key>
         <originatingSource>
            <xsl:value-of select="$originatingSource"/>
         </originatingSource>

         <collection type="collection">
            <!-- duplicate registryObject's key for muRIFCS -->
            <identifier type="local"><xsl:value-of select="d2r:rifcsKeyValue(.)"/></identifier>
            <identifier type="local"><xsl:value-of select="id"/></identifier>

            <name type="primary">
               <namePart><xsl:value-of select="name"/></namePart>
            </name>

            <xsl:apply-templates select="$REPOSITORY_LOCATION"/>
            
            <xsl:if test="first-acquisition-date != '' or last-acquisition-date !=''">
               <coverage>
                  <temporal>
                     <xsl:apply-templates select="first-acquisition-date"/>
                     <xsl:apply-templates select="last-acquisition-date"/>
                  </temporal>
               </coverage>
            </xsl:if>

            <description type="full">
               <xsl:value-of select="description"/>
            </description>

            <description type="note">
               <xsl:value-of select="concat(
                  'Number of Subjects: ', number-of-subjects, $LINEBREAK,
                  'Number of Studies: ', number-of-studies, $LINEBREAK,
                  'Number of Datasets: ', number-of-datasets, $LINEBREAK,
                  'Size of Content: ', size-of-content, ' ', size-of-content/@units)"/>
               <xsl:if test="subject-details">
                  <xsl:value-of select="concat($LINEBREAK, $LINEBREAK, 'Subject Details:')"></xsl:value-of>
                  <xsl:for-each select="subject-details/*">
                     <xsl:value-of select="concat($LINEBREAK, '  ', name(.), ': ')"/>
                     <xsl:for-each select="./*">
                        <xsl:if test="position() > 1">
                           <xsl:value-of select="', '"></xsl:value-of>
                        </xsl:if>
                        <xsl:value-of select="concat(
                           ., ' ', name(.))"/>
                     </xsl:for-each>
                  </xsl:for-each>
               </xsl:if>               
               <xsl:if test="study-details">
                  <xsl:value-of select="concat($LINEBREAK, $LINEBREAK, 'Number of studies:')"></xsl:value-of>
                  <xsl:for-each select="study-details/type">
                     <xsl:value-of select="concat($LINEBREAK, '  ', ., ': ', @number)"/>
                  </xsl:for-each>
               </xsl:if>
            </description>

            <!--
               provide funding & ethics IDs as notes
               (previously as local identifiers, but ANDS prefers them as notes)
               - expected input format:
               <funding-id type="Derek Denton Endowment">Unknown</funding-id>
               <ethics-id type="Melbourne Health Human Research Ethics Committee">2007.015</ethics-id>
            -->
            <xsl:for-each select="ethics-id, funding-id">
               <description type="note">
                  <xsl:value-of select="concat(
                     @type, ', ',
                     name(.), ': ',
                     text()
                     )"/>
               </description>
            </xsl:for-each>
            
            <description type="rights">
               <xsl:value-of select="$REPOSITORY/rights/description"/>
            </description>
            
            <xsl:apply-templates select="field-of-research"/>
            
            <xsl:for-each select="keyword">
               <subject type="local">
                  <xsl:value-of select="text()"/>
               </subject>
            </xsl:for-each>

            <!-- each project collection has 0+ owners -->
            <xsl:for-each select="project-owner">
               <relatedObject>
                  <key><xsl:value-of select="d2r:rifcsKeyValue(.)"/></key>
                  <relation type="isOwnedBy"/>
               </relatedObject>
            </xsl:for-each>

            <!-- each project collection is located in DaRIS -->
            <relatedObject>
               <key>
                  <xsl:value-of select="d2r:rifcsKeyValue(//repository-harvest/repository)"/>
               </key>
               <relation type="isLocatedIn"/>
            </relatedObject>
            
            <!-- each project collection has 0+ funding ID -->
            <xsl:for-each select="funding-id">
               <relatedObject>
                  <key><xsl:value-of select="."/></key>
                  <relation type="hasAssociationWith"/>
               </relatedObject>
            </xsl:for-each>
            
            <!-- each project collection has 0+ related service -->
            <xsl:for-each select="related-services/service">
               <relatedObject>
                  <key><xsl:value-of select="identifier"/></key>
                  <relation type="{relation-type}"/>
               </relatedObject>
            </xsl:for-each>
            
            <!-- each project collection has 0+ publication -->
            <xsl:for-each select="publications/publication">
               <relatedInfo type="publication">
                  <identifier type="{identifier/@type}"><xsl:value-of select="identifier"/></identifier>
                  <title><xsl:value-of select="title"/></title>
                  <notes><xsl:value-of select="citation"/></notes>
               </relatedInfo>
            </xsl:for-each>
         </collection>
      </registryObject>
   </xsl:template>


   <!-- create rifcs:party object: use this only when missing NLA-ID -->
   <xsl:template match="repository/custodian | projects/project/project-owner">
      <registryObject group="{$registryObjectGroup}">
         <key>
            <xsl:value-of select="d2r:rifcsKeyValue(.)"/>
         </key>
         <originatingSource>
            <xsl:value-of select="$originatingSource"/>
         </originatingSource>

         <party type="person">
            <!-- duplicate registryObject's key for muRIFCS -->
            <identifier type="local">
               <xsl:value-of select="d2r:rifcsKeyValue(.)"/>
            </identifier>

            <name type="primary">
               <xsl:apply-templates select="prefix"/>               
               <namePart type="given">
                  <xsl:value-of select="first"/>
               </namePart>
               <namePart type="family">
                  <xsl:value-of select="last"/>
               </namePart>
            </name>
            
            <xsl:apply-templates select="email"/>
            <xsl:call-template name="default-field-of-research"/>            
         </party>
         
         <!--
            <xsl:element name="description">
            <xsl:attribute name="type">note</xsl:attribute> owner institution: <xsl:value-of
            select="institution/name"/>
            </xsl:element>
         -->
      </registryObject>
   </xsl:template>
   
   <xsl:template match="prefix">
      <namePart type="title">
         <xsl:value-of select="."/>
      </namePart>
   </xsl:template>
   
   <xsl:template match="email">
      <location>
           <address>
            <electronic type="email">
               <value><xsl:value-of select="."/></value>
            </electronic>
         </address>
      </location>      
   </xsl:template>

    <xsl:template match="custodian/address">
        <location>
            <address>
            <physical type="streetAddress">
              <addressPart type="addressLine">
                 <xsl:value-of select="concat(department, ', ', institution)"/>
              </addressPart>
               <xsl:for-each select="physical-address">
                  <addressPart type="addressLine">
                     <xsl:value-of select="."/>
                  </addressPart>
               </xsl:for-each>
            </physical>
         </address>
        </location>
    </xsl:template>    

   <!--<xsl:template match="location">
      <location>
         <address>
            <physical type="streetAddress">
              <addressPart type="addressLine">
                 <xsl:value-of select="institution"/>
              </addressPart>
               <xsl:for-each select="precinct">
                  <addressPart type="addressLine">
                     <xsl:value-of select="."/>
                  </addressPart>
               </xsl:for-each>
            </physical>
         </address>
      </location>
   </xsl:template>-->

   <xsl:template match="start-date | first-acquisition-date">
      <date type="dateFrom" dateFormat="W3CDTF">
         <xsl:value-of select="."/>
      </date>
   </xsl:template>

   <xsl:template match="end-date | last-acquisition-date">
      <date type="dateTo" dateFormat="W3CDTF">
         <xsl:value-of select="."/>
      </date>
   </xsl:template>

   <xsl:template match="field-of-research">
      <!--
         expected element format:
         <field-of-research
            source="Australian and New Zealand Standard Research Classifications"
            >1109 - Neurosciences</field-of-research>
      -->
      <xsl:if test="@source = 'Australian and New Zealand Standard Research Classifications'
                     or
                    @source = 'anzsrc-for'">
         <subject type="anzsrc-for">
            <xsl:value-of select="substring-before(text(), ' ')"/>
         </subject>
      </xsl:if>      
   </xsl:template>

   <xsl:template name="default-field-of-research">
      <subject type="anzsrc-for">
         <xsl:value-of select="1109"/>
      </subject>            
   </xsl:template>
   
   
   <!-- ======================================================================
      Function for generating RIFCS Key strings out of a DaRIS element.
      
      paramemters
       - record:
         the DaRIS element to create an identifier from
      ====================================================================== -->
   <xsl:function name="d2r:rifcsKeyValue" as="xs:string">
      <xsl:param name="record"/>
      <xsl:variable name="recordType" select="name($record)"/>

      <xsl:choose>
         <!-- create custodian & project-owner identifiers -->
         <xsl:when test="$recordType = 'custodian' or $recordType = 'project-owner'">
            <xsl:choose>
               <xsl:when test="$record/NLA-ID">
                  <xsl:value-of select="$record/NLA-ID"/>                  
               </xsl:when>
               <xsl:otherwise>
                  <xsl:value-of select="concat('daris:party/email:', $record/email)"/>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:when>
         
         <!-- role-specific person identifiers no longer correct:
              a person can have both the custodian & project-owner roles -->
         <!--<!-\-create custodian identifier-\-> 
         <xsl:when test="$recordType = 'custodian'">
            <xsl:value-of select="concat(
               'daris:custodian/email:', $record/email)"/>
         </xsl:when>

         <!-\-create project-owner identifier-\-> 
         <xsl:when test="$recordType = 'project-owner'">
            <xsl:value-of select="concat(
               'daris:project-owner/email:', $record/email)"/>
         </xsl:when>-->

         <!-- create repository identifier -->
         <xsl:when test="$recordType = 'repository'">
            <xsl:value-of select="concat(
               'daris:repository/id:', $record/id)"/>
         </xsl:when>

         <!-- create collection (DaRIS project) identifier -->
         <xsl:when test="$recordType = 'project'">
            <xsl:value-of select="concat(
               'daris:project/id:', $record/id)"/>
         </xsl:when>


         <!--<xsl:variable name="DARIS_REPO_ID_PREFIX">DaRIS:Repository:</xsl:variable>
         <xsl:variable name="DARIS_PERSON_ID_PREFIX">DaRIS:person:</xsl:variable>
         <xsl:variable name="DARIS_COLLECTION_ID_PREFIX">DaRIS:collection:</xsl:variable>
         
         <xsl:choose>
            <!-\- create person identifier -\->
            <xsl:when test="$recordType = 'custodian' or
               $recordType = 'project-owner'">
               <xsl:value-of select="concat(
                  $DARIS_PERSON_ID_PREFIX, $record/first, ':', $record/last)"/>
            </xsl:when>
            
            <!-\- create repository identifier -\->
            <xsl:when test="$recordType = 'repository'">
               <xsl:value-of select="concat(
                  $DARIS_REPO_ID_PREFIX, $record/id)"/>
            </xsl:when>
            
            <!-\- create collection (DaRIS project) identifier -\->
            <xsl:when test="$recordType = 'project'">
               <xsl:value-of select="concat(
                  $DARIS_COLLECTION_ID_PREFIX, $record/id)"/>
            </xsl:when>-->


         <!-- Invalid identifier type given -->
         <xsl:otherwise>
            <xsl:variable name="msg">
               <xsl:text>Error: rifcsKeyValue(record) failed, no such recordType: </xsl:text>
               <xsl:value-of select="$recordType"/>
            </xsl:variable>
            <xsl:value-of select="error(
               QName('http://www.w3.org/2005/xqt-errors', 'err:FOER0000'), $msg)"
            />
<!--            <xsl:message terminate="yes">
               <xsl:text>Error: rifcsKey(record) failed, no such recordType: </xsl:text>
               <xsl:value-of select="$recordType"/>
            </xsl:message>-->
         </xsl:otherwise>
      </xsl:choose>
   </xsl:function>


   <!-- ======================================================================
      Function for generating RIFCS originatingSource URL for OAI-PMH.

      paramemters
      - repo:
        the /repository-harvest/repository element
      ====================================================================== -->
   <xsl:function name="d2r:originatingSource" as="xs:string">
      <xsl:param name="repo"/>
      <xsl:choose>
         <xsl:when test="$repo/originating-source">               
            <xsl:value-of select="$repo/originating-source"/>                  
         </xsl:when>

         <!-- missing originating-source -->
         <xsl:otherwise>
            <xsl:value-of select="error(
               QName('http://www.w3.org/2005/xqt-errors', 'err:FOER0000'),
               'Error: originatingSource(repo) failed, missing $repo/originating-source')"
            />
<!--            <xsl:message terminate="yes">
               <xsl:text>Error: originatingSource(repo) failed, missing $repo/originating-source</xsl:text>
            </xsl:message>-->
         </xsl:otherwise>
      </xsl:choose>
   </xsl:function>
   
   
</xsl:stylesheet>