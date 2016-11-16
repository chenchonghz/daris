
asset.doc.type.update :create yes :type daris:LicenceUsage \
  :label "LicenceUsage" \
  :description "Used to track licence usage" \
   :definition < \
    :element -name "max-used" -type "integer" -index "true" -min-occurs "0" \
    < \
      :restriction -base "integer" \
      < \
        :minimum "0" \
      > \
      :attribute -name "date" -type "date" -index "true" \
      < \
        :restriction -base "date" \
        < \
          :time "false" \
        > \
      > \
    > \
    >