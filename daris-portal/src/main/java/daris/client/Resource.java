package daris.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;

public interface Resource extends ClientBundle {

    Resource INSTANCE = GWT.create(Resource.class);

    @Source("resource/24abort.png")
    ImageResource abort24();

    @Source("resource/16about.png")
    ImageResource about16();

    @Source("resource/16action.png")
    ImageResource action16();

    @Source("resource/24action.png")
    ImageResource action24();

    @Source("resource/24active.png")
    ImageResource active24();

    @Source("resource/16add.png")
    ImageResource add16();

    @Source("resource/16add_disabled.png")
    ImageResource addDisabled16();

    @Source("resource/16admin.png")
    ImageResource admin16();

    @Source("resource/16announcement.png")
    ImageResource announcement16();

    @Source("resource/16arrow_down_blue.png")
    ImageResource arrowDownBlue16();

    @Source("resource/16arrow_left_blue.png")
    ImageResource arrowLeftBlue16();

    @Source("resource/24arrow_left_grey.png")
    ImageResource arrowLeftGrey24();

    @Source("resource/16arrow_right_blue.png")
    ImageResource arrowRightBlue16();

    @Source("resource/24assigned.gif")
    ImageResource assigned24();

    @Source("resource/16attachment.png")
    ImageResource attachment16();

    @Source("resource/16await.png")
    ImageResource await16();

    @Source("resource/24await_processing.gif")
    ImageResource awaitProcessing24();

    @Source("resource/24background.png")
    ImageResource background24();

    @Source("resource/16binary.png")
    ImageResource binary16();

    @Source("resource/16cancel.png")
    ImageResource cancel16();

    @Source("resource/16clear.png")
    ImageResource clear16();

    @Source("resource/16close.png")
    ImageResource close16();

    @Source("resource/16close_hover.png")
    ImageResource closeHover16();

    @Source("resource/16close_box_red.png")
    ImageResource closeBoxRed16();

    @Source("resource/16computer.png")
    ImageResource computer16();

    @Source("resource/16connect.png")
    ImageResource connect16();

    @Source("resource/24create.png")
    ImageResource create24();

    @Source("resource/12cross.png")
    ImageResource cross12();

    @Source("resource/16csv.png")
    ImageResource csv16();

    @Source("resource/16cycle_blue.png")
    ImageResource cycleBlue16();

    @Source("resource/16dataset_derived.png")
    ImageResource datasetDerived16();

    @Source("resource/16data_object.png")
    ImageResource datasetObject16();

    @Source("resource/16dataset_primary.png")
    ImageResource datasetPrimary16();

    @Source("resource/16dataset_processed.png")
    ImageResource datasetProcessed16();

    @Source("resource/16delete.png")
    ImageResource delete16();

    @Source("resource/16delete_red.png")
    ImageResource deleteRed16();

    @Source("resource/24deposit.gif")
    ImageResource deposit24();

    @Source("resource/16detail.png")
    ImageResource detail16();

    @Source("resource/24detail.png")
    ImageResource detail24();

    @Source("resource/24dcm.png")
    ImageResource dicom24();

    @Source("resource/16disconnect.png")
    ImageResource disconnect16();

    @Source("resource/16document.png")
    ImageResource document16();

    @Source("resource/16down.png")
    ImageResource down16();

    @Source("resource/16down_hover.png")
    ImageResource downHover16();

    @Source("resource/16download.png")
    ImageResource download16();

    @Source("resource/24download.png")
    ImageResource download24();

    @Source("resource/16download_big.png")
    ImageResource downloadBig16();

    @Source("resource/16download_black.png")
    ImageResource downloadBlack16();

    @Source("resource/16download_small.gif")
    ImageResource downloadSmall16();

    @Source("resource/10edit.png")
    ImageResource edit10();

    @Source("resource/16edit.png")
    ImageResource edit16();

    @Source("resource/24edit.png")
    ImageResource edit24();

    @Source("resource/16editable.gif")
    ImageResource editable16();

    @Source("resource/24editable.png")
    ImageResource editable24();

    @Source("resource/16edit_disabled.png")
    ImageResource editDisabled16();

    @Source("resource/16export.png")
    ImageResource export16();

    @Source("resource/16enter.png")
    ImageResource enter16();

    @Source("resource/16error.png")
    ImageResource error16();

    @Source("resource/16error_triangle.png")
    ImageResource error16t();

    @Source("resource/24error.png")
    ImageResource error24();

    @Source("resource/48error.png")
    ImageResource error48();

    @Source("resource/16exclamation.png")
    ImageResource exclamation16();

    @Source("resource/24fast_forward.png")
    ImageResource fastForward24();

    @Source("resource/24fast_rewind.png")
    ImageResource fastRewind24();

    @Source("resource/16file.png")
    ImageResource file16();

    @Source("resource/16file_explorer.png")
    ImageResource fileExplorer16();

    @Source("resource/24file_explorer.png")
    ImageResource fileExplorer24();

    @Source("resource/16first.png")
    ImageResource first16();

    @Source("resource/24first.png")
    ImageResource first24();

    @Source("resource/16first_hover.png")
    ImageResource firstHover16();

    @Source("resource/16folder_blue.png")
    ImageResource folderBlue16();

    @Source("resource/16folder_blue_open.png")
    ImageResource folderBlueOpen16();

    @Source("resource/16folder_green.png")
    ImageResource folderGreen16();

    @Source("resource/16folder_green_open.png")
    ImageResource folderGreenOpen16();

    @Source("resource/16folder_grey.png")
    ImageResource folderGrey16();

    @Source("resource/16folder_grey_open.png")
    ImageResource folderGreyOpen16();

    @Source("resource/16folder_options.png")
    ImageResource folderOptions16();

    @Source("resource/16folder_orange.png")
    ImageResource folderOrange16();

    @Source("resource/16folder_orange_open.png")
    ImageResource folderOrangeOpen16();

    @Source("resource/16folder_violet.png")
    ImageResource folderViolet16();

    @Source("resource/16folder_violet_open.png")
    ImageResource folderVioletOpen16();

    @Source("resource/16folder_yellow.png")
    ImageResource folderYellow16();

    @Source("resource/16folder_yellow_open.png")
    ImageResource folderYellowOpen16();

    @Source("resource/16forward.png")
    ImageResource forward16();

    @Source("resource/24forward.png")
    ImageResource forward24();

    @Source("resource/24fulfilled.gif")
    ImageResource fulfilled24();

    @Source("resource/16hard_drive.png")
    ImageResource hardDrive16();

    @Source("resource/16help.png")
    ImageResource help16();

    @Source("resource/16hide_left.png")
    ImageResource hideLeft16();

    @Source("resource/16hide_right.png")
    ImageResource hideRight16();

    @Source("resource/24import.png")
    ImageResource import24();

    @Source("resource/48info.png")
    ImageResource info48();

    @Source("resource/16key.png")
    ImageResource key16();

    @Source("resource/16keychain.png")
    ImageResource keychain16();

    @Source("resource/16last.png")
    ImageResource last16();

    @Source("resource/24last.png")
    ImageResource last24();

    @Source("resource/16last_hover.png")
    ImageResource lastHover16();

    @Source("resource/16left.png")
    ImageResource left16();

    @Source("resource/16left_green.gif")
    ImageResource leftGreen16();

    @Source("resource/16left_hover.png")
    ImageResource leftHover16();

    @Source("resource/16link.png")
    ImageResource link16();

    @Source("resource/16link_bold.png")
    ImageResource linkBold16();

    @Source("resource/16load.png")
    ImageResource load16();

    @Source("resource/16loaded.png")
    ImageResource loaded16();

    @Source("resource/16loading1.gif")
    ImageResource loading16a();

    @Source("resource/16loading2.gif")
    ImageResource loading16b();

    @Source("resource/100loading.gif")
    ImageResource loading100();

    @Source("resource/128x15loading.gif")
    ImageResource loading128x15();

    @Source("resource/16loading.gif")
    ImageResource loading16();

    @Source("resource/128x15loading_failed.png")
    ImageResource loadingFailed128x15();

    @Source("resource/128x15loading_finished.png")
    ImageResource loadingFinished128x15();

    @Source("resource/24logoff.png")
    ImageResource logoff24();

    @Source("resource/24logout.png")
    ImageResource logout24();

    @Source("resource/16manager.png")
    ImageResource manager16();

    @Source("resource/24manager.png")
    ImageResource manager24();

    @Source("resource/16next.png")
    ImageResource next16();

    @Source("resource/16next_hover.png")
    ImageResource nextHover16();

    @Source("resource/16object.png")
    ImageResource object16();

    @Source("resource/16ok.png")
    ImageResource ok16();

    @Source("resource/16pause.png")
    ImageResource pause16();

    @Source("resource/16pdf.png")
    ImageResource pdf16();

    @Source("resource/16picker.png")
    ImageResource picker16();

    @Source("resource/24plus.png")
    ImageResource plus24();

    @Source("resource/16preferences.png")
    ImageResource preferences16();

    @Source("resource/16prev.png")
    ImageResource prev16();

    @Source("resource/16prev_hover.png")
    ImageResource prevHover16();

    @Source("resource/16processing.gif")
    ImageResource processing16();

    @Source("resource/24processing.gif")
    ImageResource processing24();

    @Source("resource/16question_mark.png")
    ImageResource question16();

    @Source("resource/32question.png")
    ImageResource question32();

    @Source("resource/16radio_deselected.gif")
    ImageResource radioDeselected16();

    @Source("resource/16radio_selected.gif")
    ImageResource radioSelected16();

    @Source("resource/16refresh_blue.png")
    ImageResource refreshBlue16();

    @Source("resource/16refresh_green.png")
    ImageResource refreshGreen16();

    @Source("resource/16rejected.gif")
    ImageResource rejected16();

    @Source("resource/24rejected.png")
    ImageResource rejected24();

    @Source("resource/24reload.png")
    ImageResource reload24();

    @Source("resource/16remove.png")
    ImageResource remove16();

    @Source("resource/16remove_disabled.png")
    ImageResource removeDisabled16();

    @Source("resource/24rewind.png")
    ImageResource rewind24();

    @Source("resource/16right.png")
    ImageResource right16();

    @Source("resource/16right_hover.png")
    ImageResource rightHover16();

    @Source("resource/12x32ruler.png")
    ImageResource ruler12x32();

    @Source("resource/16save_disk.png")
    ImageResource save16();

    @Source("resource/16search.png")
    ImageResource search16();

    @Source("resource/24search.png")
    ImageResource search24();

    @Source("resource/32search.png")
    ImageResource search32();

    @Source("resource/16send.png")
    ImageResource send16();

    @Source("resource/16settings.gif")
    ImageResource settings16();

    @Source("resource/16share.png")
    ImageResource share16();

    @Source("resource/24shoppingcart.png")
    ImageResource shoppingcart24();

    @Source("resource/16shoppingcart_green.png")
    ImageResource shoppingcartGreen16();

    @Source("resource/16shoppingcart_grey.png")
    ImageResource shoppingcartGrey16();

    @Source("resource/16start.gif")
    ImageResource start16();

    @Source("resource/16stop.gif")
    ImageResource stop16();

    @Source("resource/16stop.png")
    ImageResource stop16a();

    @Source("resource/16submit.png")
    ImageResource submit16();

    @Source("resource/16tab_close.png")
    ImageResource tabClose16();

    @Source("resource/16tab_close_disabled.png")
    ImageResource tabCloseDisabled16();

    @Source("resource/16tab_close_hover.png")
    ImageResource tabCloseHover16();

    @Source("resource/16thumbnail.png")
    ImageResource thumbnail16();

    @Source("resource/16tag.png")
    ImageResource tag16();

    @Source("resource/16tag_disabled.png")
    ImageResource tagDisabled16();

    @Source("resource/12tick.png")
    ImageResource tick12();

    @Source("resource/16tick_blue.png")
    ImageResource tickBlue16();

    @Source("resource/16tick_green.png")
    ImageResource tickGreen16();

    @Source("resource/16transform.png")
    ImageResource transform16();

    @Source("resource/16unknown.png")
    ImageResource unknown16();

    @Source("resource/16upload.png")
    ImageResource upload16();

    @Source("resource/16user.png")
    ImageResource user16();

    @Source("resource/24user.png")
    ImageResource user24();

    @Source("resource/16view.png")
    ImageResource view16();

    @Source("resource/16view_list.png")
    ImageResource viewList16();

    @Source("resource/24withdrawn.gif")
    ImageResource withdrawn24();

    @Source("resource/48warning.png")
    ImageResource warning48();

    @Source("resource/16xml.png")
    ImageResource xml16();

    @Source("resource/16zoom_in.png")
    ImageResource zoomIn16();

    @Source("resource/16zoom_out.png")
    ImageResource zoomOut16();

}