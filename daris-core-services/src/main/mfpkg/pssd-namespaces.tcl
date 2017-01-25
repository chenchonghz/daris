proc create_daris_namespace { store } {
    if { [xvalue exists [application.property.exists :property -app daris  daris.namespace.default]] == "false" } {
        if { [xvalue exists [asset.namespace.exists :namespace "daris"]]=="true" } {
            # namespace 'daris' pre-exists, just set it as default
            application.property.create :property -app daris -name daris.namespace.default < :value "daris" >
            return "daris"
        } elseif { [xvalue exists [asset.namespace.exists :namespace "pssd"]]=="true" } {
            # namespace 'pssd' pre-exists, just set it as default
            application.property.create :property -app daris -name daris.namespace.default < :value "pssd" >
            return "pssd"
        } else {
            # create namespace 'daris'
            asset.namespace.create :namespace "daris" :store ${store} :description "Namespace for DaRIS (PSSD) projects."
            application.property.create :property -app daris -name daris.namespace.default < :value "daris" >
            return "daris"
        }
    } else {
        set ns [xvalue property [application.property.get :property -app daris daris.namespace.default]]
        if { [xvalue exists [asset.namespace.exists :namespace "${ns}"]]=="false" } {
            # create namespace
            asset.namespace.create :namespace "${ns}" :store ${store} :description "Namespace for DaRIS (PSSD) projects."
        }
        return ${ns}
    }
}

proc create_namespace_if_not_exist { ns { desc "" } } {
    if { [xvalue exists [asset.namespace.exists :namespace "${ns}"]]=="false" } {
        if { ${desc} == "" } {
            asset.namespace.create :namespace "${ns}"
        } else {
            asset.namespace.create :namespace "${ns}" :description "${desc}"
        }
    }
}

proc string_ends_with { str substr } {
    set len1 [string length $str]
    set len2 [string length $substr]
    set idx [string last $substr $str]
    if { $idx != -1 && [expr $len1 - $len2] == $idx } {
        return 1
    } else {
        return 0
    }
}

proc get_daris_namespace { } {
    return [xvalue property [application.property.get :property -app daris daris.namespace.default]]
}

proc get_daris_sub_namespace { name } {
    set ns [xvalue property [application.property.get :property -app daris daris.namespace.default]]
    if { ${ns} == "pssd" || [string_ends_with ${ns} "pssd"] == 1 } {
        return "${ns}/${name}"
    } else {
        return "${ns}/pssd/${name}"
    }
}

proc get_daris_fcp_namespace { } {
    return [get_daris_sub_namespace "fcp"]
}

proc get_daris_method_namespace { } {
    return [get_daris_sub_namespace "methods"]
}

proc create_daris_sub_namespaces { daris_namespace } {
    if { ${daris_namespace} == "pssd" || [string_ends_with ${daris_namespace} "pssd"] == 1 } {
        create_namespace_if_not_exist "${daris_namespace}/methods"
        create_namespace_if_not_exist "${daris_namespace}/fcp"
    } else {
        create_namespace_if_not_exist "${daris_namespace}/pssd"
        create_namespace_if_not_exist "${daris_namespace}/pssd/methods"
        create_namespace_if_not_exist "${daris_namespace}/pssd/fcp"
        #create_namespace_if_not_exist "${daris_namespace}/dicom"
    }
}

proc create_daris_all_namespaces { store } {
    set daris_namespace [create_daris_namespace ${store}]
    create_daris_sub_namespaces ${daris_namespace} 
}


if { [xvalue exists [application.property.exists :property -app daris  daris.namespace.default]]=="true" } {
    # property daris.namespace.default has been set, which indicates the 
    # namespaces have been created by the previous installation.
    set daris_namespace [xvalue property [application.property.get :property -app daris daris.namespace.default]]
    create_daris_sub_namespaces ${daris_namespace} 
} else {
    if { [xvalue exists [asset.namespace.exists :namespace "pssd"]]=="true" } {
        # namespace /pssd pre-exists, just set it as default
        application.property.create :property -app daris -name daris.namespace.default < :value "pssd" >
        create_daris_sub_namespaces "pssd"
    } else {
        # now we try to create the namespace, but first check if the store 'daris' or 'pssd' has been created:
        set daris_store ""
        if { [xvalue exists [asset.store.exists :name "daris"]]=="true" } {
            set daris_store "daris"
        } elseif { [xvalue exists [asset.store.exists :name "pssd"]]=="true" } {
            set daris_store "pssd"
        } else {
            error "The DaRIS asset store (Defaults to 'daris') must pre-exist before install this package."
        }
        create_daris_all_namespaces ${daris_store}
    }
}
set default_ns [xvalue property [application.property.get :property -app daris daris.namespace.default]]
set default_project_ns "${default_ns}/pssd"
if { [xvalue exists [dictionary.exists :name daris:pssd.project.asset.namespaces]] == "true" } {
    if { [xvalue exists [dictionary.entry.exists :dictionary daris:pssd.project.asset.namespaces :term ${default_project_ns}]] == "false" } {
        dictionary.entry.add :dictionary daris:pssd.project.asset.namespaces :term ${default_project_ns}
    }
}
