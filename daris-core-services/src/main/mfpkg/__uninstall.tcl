# ============================================================================
# Destroy the default shopping cart template (name: pssd)
# ============================================================================
foreach cartId [xvalues cart/@id [shopping.cart.describe  :list-all true :size infinity]] { shopping.cart.destroy :sid $cartId }
shopping.cart.template.destroy :name pssd
# om.pssd.shoppingcart.template.destroy :force true

# ============================================================================
# Include utils.tcl functions
# ============================================================================
source utils.tcl

# ============================================================================
# Remove plugin module
# ============================================================================
source plugin-module-remove.tcl

# ============================================================================
# Uninstall the trigger for /dicom namespace, which monitoring the arrivals of
# NON-PSSD style DICOM data and send notifications.
# ============================================================================
source triggers-uninstall.tcl




