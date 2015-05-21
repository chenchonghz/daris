source __uninstall.tcl

# ============================================================================
# Install Plugins
# ============================================================================
set plugin_label           [string toupper PACKAGE_$package]
set plugin_namespace       /mflux/plugins
set plugin_zip             sinks-plugin.zip
set plugin_jar             sinks-plugin.jar
set module_class           nig.mf.plugin.sink.SinkPluginModule

# extract transform-plugin.jar to /mflux/plugins
asset.import :url archive:${plugin_zip} \
        :namespace -create yes ${plugin_namespace} \
        :label -create yes ${plugin_label} :label PUBLISHED \
        :update true

# install the plugin module
if { [xvalue exists [plugin.module.exists :path ${plugin_namespace}/${plugin_jar} :class ${module_class}]] == "false" } {
    plugin.module.add :path ${plugin_namespace}/${plugin_jar} \
                      :class ${module_class} \
                      :lib bcprov-jdk16-140.jar \
                      :lib commons-codec-1.8.jar \
                      :lib commons-logging-1.1.3.jar \
                      :lib ganymed-ssh2-261.jar \
                      :lib hamcrest-core-1.3.jar \
                      :lib httpclient-4.3.2.jar \
                      :lib httpcore-4.3.1.jar \
                      :lib sardine-5.1.jar \
                      :lib slf4j-api-1.7.5.jar
}

# reload the services     
system.service.reload

# refresh the enclosing shell
srefresh

# ============================================================================
# Define roles and service permissions
# ============================================================================
source service-perms.tcl
