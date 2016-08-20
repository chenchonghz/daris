asset.doc.type.update :create true :type daris:mytardis-experiment :index all :definition < \
    :element -name id -type long -min-occurs 1 -max-occurs 1 \
    :element -name title -type string -min-occurs 0 -max-occurs 1 \
    :element -name description -type string -min-occurs 0 -max-occurs 1 >

asset.doc.type.update :create true :type daris:mytardis-dataset :index all :definition < \
    :element -name id -type long -min-occurs 1 -max-occurs 1 \
    :element -name description -type string -min-occurs 0 -max-occurs 1 \
    :element -name instrument -type string -min-occurs 0 -max-occurs 1 \
    :element -name uri -type url -min-occurs 1 -max-occurs 1 >
