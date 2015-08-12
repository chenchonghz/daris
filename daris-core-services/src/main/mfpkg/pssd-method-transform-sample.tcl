set server_uuid [xvalue uuid [server.uuid]]
if { ( $server_uuid == "687" || $server_uuid == "755" )  && [xvalue exists [package.exists :package "daris-transform"]] == "true" } {
    set name "Transform - Clinical Pain"
    set description "Transform - Clinical Pain"
    set namespace "/pssd/methods"
    asset.query :where "namespace='$namespace' and (xpath(pssd-object/name)='$name') and (xpath(pssd-object/type)='method')" :action pipe :service -name asset.destroy
    om.pssd.method.for.subject.create \
        :name $name \
        :description $description \
        :namespace $namespace \
        :subject < :project < :public < :metadata < :definition -requirement optional mf-note > > > > \
        :step < :name MR :study < :dicom < :modality MR > :type "Magnetic Resonance Imaging" > > \
        :step < \
            :name Transform \
            :transform < \
                :definition -version 0 60 \
                :iterator < \
                    :scope ex-method \
                    :type citeable-id \
                    :query "model='om.pssd.study' and mf-dicom-study has value" \
                    :parameter pid \
                > \
            > \
        >
}
