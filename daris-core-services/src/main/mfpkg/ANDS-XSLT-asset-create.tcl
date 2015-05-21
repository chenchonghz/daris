#Create an asset holding the XSLT to convert the result of  om.pssd.project.metadata-harvest
# into the Melbourne University RIF CS schema.  EVentually, this will be scraped for the ANDS
# national data commons registry
#
# Although the meta-data are populated by domain independent and research domain dependent steps, the meta-data
# schema that is ultimately to be transformed is not research domain dependent. Therefore this XSLT
# is appropriate to the pssd package
################################################################################################################################################
proc install-XSLT-asset { } {

# Remove legacy versions with older names managed through old approach 
    set oldID [xvalue id [asset.query :where name='DarisToRIFCS.xsl']]
    if { $oldID != "" } {
	   asset.hard.destroy :id $oldID
    }
    set oldID [xvalue id [asset.query :where name='daris2rifcs.xsl']]
    if { $oldID != "" } {
	   asset.hard.destroy :id $oldID
    }    
    
    # Set details
    set SCRIPT        daris2rifcs.xsl
    set NAMESPACE     system/transform/xslt
    set PROFILE       PSSD-TO-RIF-CS

    # If already exists destroy and re-create. This re-creates what the wrapper
    # service asset.meta.transform.profile.xslt.add does but allows specification
    # via the :url argument (server side)
    if { [xvalue exists [asset.meta.transform.profile.exists :profile $PROFILE]] == "true" } {
    
       # Save asset ID
       set assetId [xvalue profile/@assetId [asset.meta.transform.profile.describe :profile PSSD-TO-RIF-CS]]
    
       asset.meta.transform.profile.xslt.remove :profile $PROFILE
       
       # Clean up asset (if soft destroy enabled above does not remove)
       set assetExists [xvalue exists [asset.exists :id $assetId]]
       if { $assetExists == "true" } {
          asset.hard.destroy :id $assetId
       }
       
    }
    asset.create :name $PROFILE \
        :namespace -create true $NAMESPACE \
        :type "application/arc-transform-xslt" \
        :description "Metadata transformation from PSSD metadata to RIF-CS." \
        :meta < \
                :mf-transform-profile < \
                        :output "text/xml" \
                        :namespace "http://ands.org.au/standards/rif-cs/registryObjects" \
                        :schema "http://services.ands.org.au/documentation/rifcs/schema/registryObjects.xsd" \
                > \
        > \
        :url archive:$SCRIPT
}

#################################################################################################################################

install-XSLT-asset
