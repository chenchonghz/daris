#
# Document: nig-nifti-1 [version 19]
#
asset.doc.type.update :create yes :type daris:nifti-1 \
  :label "nifti-1" \
  :description "NIFTI-1" \
  :definition < \
    :element -name "sizeof_hdr" -type "integer" -index "true" -max-occurs "1" \
    < \
      :description "Size of hdr. Must be 348." \
      :restriction -base "integer" \
      < \
	:minimum "348" \
	:maximum "348" \
      > \
    > \
    :element -name "data_type" -type "string" -max-occurs "1" \
    < \
      :description "Data type" \
      :restriction -base "string" \
      < \
	:max-length "10" \
      > \
    > \
    :element -name "db_name" -type "string" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Currently unused in nifti" \
      :restriction -base "string" \
      < \
	:max-length "18" \
      > \
    > \
    :element -name "extents" -type "integer" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Currently unused" \
    > \
    :element -name "session_error" -type "integer" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Currently unused." \
      :restriction -base "integer" \
      < \
	:minimum " -32768" \
	:maximum "32767" \
      > \
    > \
    :element -name "regular" -type "string" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Currently unused" \
    > \
    :element -name "dim_info" -type "string" -max-occurs "1" \
    < \
      :description "MRI Slice ordering" \
    > \
    :element -name "dim_0" -type "integer" -max-occurs "1" \
    < \
      :description "Number of dimensions" \
      :restriction -base "integer" \
      < \
	:minimum "1" \
	:maximum "7" \
      > \
    > \
    :element -name "dim_1" -type "integer" -max-occurs "1" \
    < \
      :description "Size of first axis" \
      :restriction -base "integer" \
      < \
	:minimum " -32768" \
	:maximum "32767" \
      > \
    > \
    :element -name "dim_2" -type "integer" -max-occurs "1" \
    < \
      :description "Size of second axis" \
      :restriction -base "integer" \
      < \
	:minimum " -32768" \
	:maximum "32767" \
      > \
    > \
    :element -name "dim_3" -type "integer" -max-occurs "1" \
    < \
      :description "Size of third axis" \
      :restriction -base "integer" \
      < \
	:minimum " -32767" \
	:maximum "32768" \
      > \
    > \
    :element -name "dim_4" -type "integer" -max-occurs "1" \
    < \
      :description "Size of fourth axis" \
      :restriction -base "integer" \
      < \
	:minimum " -32767" \
	:maximum "32768" \
      > \
    > \
    :element -name "dim_5" -type "integer" -max-occurs "1" \
    < \
      :description "Size of fifth axis" \
      :restriction -base "integer" \
      < \
	:minimum " -32767" \
	:maximum "32768" \
      > \
    > \
    :element -name "dim_6" -type "integer" -max-occurs "1" \
    < \
      :description "Size of sixth axis" \
      :restriction -base "integer" \
      < \
	:minimum " -32767" \
	:maximum "32768" \
      > \
    > \
    :element -name "dim_7" -type "integer" -max-occurs "1" \
    < \
      :description "Size of seventh axis" \
      :restriction -base "integer" \
      < \
	:minimum " -32767" \
	:maximum "32768" \
      > \
    > \
    :element -name "intent_p1" -type "float" -max-occurs "1" \
    < \
      :description "1st intent parameter" \
    > \
    :element -name "intent_p2" -type "float" -max-occurs "1" \
    < \
      :description "2nd intent parameter" \
    > \
    :element -name "intent_p3" -type "float" -max-occurs "1" \
    < \
      :description "3rd intent parameter" \
    > \
    :element -name "intent_code" -type "enumeration" -max-occurs "1" \
    < \
      :description "NIFTI_INTENT Code" \
      :restriction -base "enumeration" \
      < \
	:value "0" \
	:value "2" \
	:value "3" \
	:value "4" \
	:value "5" \
	:value "6" \
	:value "7" \
	:value "8" \
	:value "9" \
	:value "10" \
	:value "11" \
	:value "12" \
	:value "13" \
	:value "14" \
	:value "15" \
	:value "16" \
	:value "17" \
	:value "18" \
	:value "19" \
	:value "20" \
	:value "21" \
	:value "22" \
	:value "1001" \
	:value "1002" \
	:value "1003" \
	:value "1004" \
	:value "1005" \
	:value "1006" \
	:value "1007" \
	:value "1008" \
	:value "1009" \
	:value "1010" \
	:case-sensitive "true" \
      > \
    > \
    :element -name "datatype" -type "enumeration" -max-occurs "1" \
    < \
      :description "Defines data type" \
      :restriction -base "enumeration" \
      < \
	:value "0" \
	:value "1" \
	:value "2" \
	:value "4" \
	:value "8" \
	:value "16" \
	:value "32" \
	:value "64" \
	:value "128" \
	:value "255" \
	:value "256" \
	:value "512" \
	:value "768" \
	:value "1024" \
	:value "1280" \
	:value "1536" \
	:value "1792" \
	:value "2048" \
	:case-sensitive "true" \
      > \
    > \
    :element -name "bitpix" -type "integer" -max-occurs "1" \
    < \
      :description "Number bits/voxel" \
      :restriction -base "integer" \
      < \
	:minimum " -32767" \
	:maximum "32768" \
      > \
    > \
    :element -name "slice_start" -type "integer" -max-occurs "1" \
    < \
      :description "First slice index" \
      :restriction -base "integer" \
      < \
	:minimum " -32768" \
	:maximum "32767" \
      > \
    > \
    :element -name "pixdim_0" -type "float" -max-occurs "1" \
    < \
      :description "Unused" \
    > \
    :element -name "pixdim_1" -type "float" -max-occurs "1" \
    < \
      :description "Voxel width along first dimension" \
    > \
    :element -name "pixdim_2" -type "float" -max-occurs "1" \
    < \
      :description "Voxel width along second dimension" \
    > \
    :element -name "pixdim_3" -type "float" -max-occurs "1" \
    < \
      :description "Voxel width along third dimension" \
    > \
    :element -name "pixdim_4" -type "float" -max-occurs "1" \
    < \
      :description "Voxel width along fourth dimension" \
    > \
    :element -name "pixdim_5" -type "float" -max-occurs "1" \
    < \
      :description "Voxel width along fifth dimension" \
    > \
    :element -name "pixdim_6" -type "float" -max-occurs "1" \
    < \
      :description "Voxel width along sixth dimension" \
    > \
    :element -name "pixdim_7" -type "float" -max-occurs "1" \
    < \
      :description "Voxel width along seventh dimension" \
    > \
    :element -name "vox_offset" -type "float" -max-occurs "1" \
    < \
      :description "Byte offset into the nii file." \
    > \
    :element -name "scl_slope" -type "float" -max-occurs "1" \
    < \
      :description "Data scaling: slope" \
    > \
    :element -name "scl_inter" -type "float" -max-occurs "1" \
    < \
      :description "Data scaling: offset" \
    > \
    :element -name "slice_end" -type "integer" -max-occurs "1" \
    < \
      :description "Last slice index" \
      :restriction -base "integer" \
      < \
	:minimum " -32768" \
	:maximum "32767" \
      > \
    > \
    :element -name "slice_code" -type "string" -max-occurs "1" \
    < \
      :description "Slice timing order" \
    > \
    :element -name "xyzt_units" -type "string" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Units of pixdim\[1..4\]; contains spatial and temporal units" \
    > \
    :element -name "cal_max" -type "float" -max-occurs "1" \
    < \
      :description "Maximum display intensity" \
    > \
    :element -name "cal_min" -type "float" -max-occurs "1" \
    < \
      :description "Min display intensity" \
    > \
    :element -name "slice_duration" -type "float" -max-occurs "1" \
    < \
      :description "Time for 1 slice" \
    > \
    :element -name "toffset" -type "float" -max-occurs "1" \
    < \
      :description "Time axis shift" \
    > \
    :element -name "glmax" -type "integer" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Unused" \
    > \
    :element -name "glmin" -type "integer" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Unused" \
    > \
    :element -name "descrip" -type "string" -max-occurs "1" \
    < \
      :description "Any text you like" \
      :restriction -base "string" \
      < \
	:max-length "80" \
      > \
    > \
    :element -name "aux_file" -type "string" -max-occurs "1" \
    < \
      :description "Auxiliary filename" \
      :restriction -base "string" \
      < \
	:max-length "24" \
      > \
    > \
    :element -name "qform_code" -type "enumeration" -max-occurs "1" \
    < \
      :description "NIFTIXFORM code" \
      :restriction -base "enumeration" \
      < \
	:value "0" \
	:value "1" \
	:value "2" \
	:value "3" \
	:value "4" \
	:case-sensitive "true" \
      > \
    > \
    :element -name "sform_code" -type "enumeration" -max-occurs "1" \
    < \
      :description "NIFTIXFORM code" \
      :restriction -base "enumeration" \
      < \
	:value "0" \
	:value "1" \
	:value "2" \
	:value "3" \
	:value "4" \
	:case-sensitive "true" \
      > \
    > \
    :element -name "quatern_b" -type "float" -max-occurs "1" \
    < \
      :description "Quaternion b param" \
    > \
    :element -name "quatern_c" -type "float" -max-occurs "1" \
    < \
      :description "Quaternion c param" \
    > \
    :element -name "quatern_d" -type "float" -max-occurs "1" \
    < \
      :description "Quaternion d param" \
    > \
    :element -name "qoffset_x" -type "float" -max-occurs "1" \
    < \
      :description "Quaternion x shift" \
    > \
    :element -name "qoffset_y" -type "float" -max-occurs "1" \
    < \
      :description "Quaternion y shift" \
    > \
    :element -name "qoffset_z" -type "float" -max-occurs "1" \
    < \
      :description "Quaternion z shift" \
    > \
    :element -name "srow_x_1" -type "float" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Element in the first row and first column of the affine transform." \
    > \
    :element -name "srow_x_2" -type "float" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Element in the first row and second column of the affine transform" \
    > \
    :element -name "srow_x_3" -type "float" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Element in the first row and third column of the affine transform" \
    > \
    :element -name "srow_x_4" -type "float" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Element in the first row and fourth column of the affine transform" \
    > \
    :element -name "srow_y_1" -type "float" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Element in the second row and first column of the affine transform" \
    > \
    :element -name "srow_y_2" -type "float" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Element in the second row and second column of the affine transform" \
    > \
    :element -name "srow_y_3" -type "float" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Element in the second row and third column of the affine transform" \
    > \
    :element -name "srow_y_4" -type "float" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Element in the second row and fourth column of the affine transform" \
    > \
    :element -name "srow_z_1" -type "float" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Element in the third row and first column of the affine transform" \
    > \
    :element -name "srow_z_2" -type "float" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Element in the third row and second column of the affine transform" \
    > \
    :element -name "srow_z_3" -type "float" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Element in the third row and third column of the affine transform" \
    > \
    :element -name "srow_z_4" -type "float" -min-occurs "0" -max-occurs "1" \
    < \
      :description "Element in the third row and fourth column of the affine transform" \
    > \
    :element -name "intent_name" -type "string" -max-occurs "1" \
    < \
      :description "Name of meaning of the voxel data" \
      :restriction -base "string" \
      < \
	:max-length "16" \
      > \
    > \
    :element -name "magic" -type "string" -max-occurs "1" \
    < \
      :description "Magic number. MUST be \\\"ni1\\0\\\" or \\\"n+1\\0\\\"" \
      :restriction -base "string" \
      < \
	:max-length "4" \
      > \
    > \
   >


