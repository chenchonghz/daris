package daris.client.ui.repository;

import java.util.Date;

import arc.gui.form.Field;
import arc.gui.form.FieldDefinition;
import arc.gui.form.FieldGroup;
import arc.gui.form.FieldRenderOptions;
import arc.gui.form.Form;
import arc.gui.form.FormEditMode;
import arc.gui.form.FormItem;
import arc.gui.form.FormItem.Property;
import arc.gui.form.FormItemListener;
import arc.mf.dtype.ConstantType;
import arc.mf.dtype.DateType;
import arc.mf.dtype.StringType;
import arc.mf.dtype.TextType;
import daris.client.model.repository.Repository;
import daris.client.ui.object.DObjectDetails;

public class RepositoryDetails extends DObjectDetails {

    public RepositoryDetails(Repository o, FormEditMode mode) {

        super(null, o, mode);
    }

    @Override
    protected void addToInterfaceForm(Form interfaceForm) {

        final Repository ro = (Repository) object();

        if (ro.name() != null) {
            Field<String> nameField = new Field<String>(new FieldDefinition(
                    "name", StringType.DEFAULT, null, null, 0, 1));
            nameField.setValue(ro.name());
            nameField.addListener(new FormItemListener<String>() {
                @Override
                public void itemValueChanged(FormItem<String> f) {

                    ro.setName(f.value());
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f,
                        FormItem.Property p) {

                }
            });
            interfaceForm.add(nameField);
        }

        if (ro.acronym() != null) {
            Field<String> acronymField = new Field<String>(new FieldDefinition(
                    "acronym", StringType.DEFAULT, null, null, 0, 1));
            acronymField.setValue(ro.name());
            acronymField.addListener(new FormItemListener<String>() {
                @Override
                public void itemValueChanged(FormItem<String> f) {

                    ro.setAcronym(f.value());
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f,
                        FormItem.Property p) {

                }
            });
            interfaceForm.add(acronymField);
        }

        /*
         * Custodian
         */
        if (ro.custodian() != null || !mode().equals(FormEditMode.READ_ONLY)) {
            addCustodianFields(interfaceForm, ro.custodian(), mode());
        }

        /*
         * Location
         */
        if (ro.location() != null || !mode().equals(FormEditMode.READ_ONLY)) {
            addLocationFields(interfaceForm, ro.location(), mode());
        }

        /*
         * Data Holdings
         */
        if (ro.dataHoldings() != null || !mode().equals(FormEditMode.READ_ONLY)) {
            addDataHoldingsFields(interfaceForm, ro.dataHoldings(), mode());
        }

        /*
         * Rights
         */
        Field<String> rightsField = new Field<String>(new FieldDefinition(
                "Rights", TextType.DEFAULT, "Rights", null, 0, 1));
        FieldRenderOptions fro = new FieldRenderOptions();
        fro.setWidth(1.0);
        rightsField.setRenderOptions(fro);
        rightsField.setValue(ro.rights());
        if (!mode().equals(FormEditMode.READ_ONLY)) {
            rightsField.addListener(new FormItemListener<String>() {

                @Override
                public void itemValueChanged(FormItem<String> f) {

                    ro.setRights(f.value());
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f,
                        Property property) {

                }
            });
        }

        /*
         * Server and database information
         */
        if (!mode().equals(FormEditMode.CREATE)) {
            Repository.Server server = ro.server();
            if (server != null) {
                addServerFields(interfaceForm, server, mode());
            }
        }

        setActiveTab();
    }

    private void addCustodianFields(Form interfaceForm,
            final Repository.Custodian custodian, FormEditMode mode) {

        final Repository ro = (Repository) object();
        FieldGroup fg = new FieldGroup(new FieldDefinition("custodian",
                ConstantType.DEFAULT, "Custodian", null, 0, 1));
        final Field<String> prefixField = new Field<String>(
                new FieldDefinition("prefix", StringType.DEFAULT, "Prefix",
                        null, 0, 1));
        fg.add(prefixField);
        final Field<String> firstNameField = new Field<String>(
                new FieldDefinition("first name", StringType.DEFAULT,
                        "First name", null, 0, 1));
        fg.add(firstNameField);
        final Field<String> middleNameField = new Field<String>(
                new FieldDefinition("middle name", StringType.DEFAULT,
                        "Middle name", null, 0, 1));
        fg.add(middleNameField);
        final Field<String> lastNameField = new Field<String>(
                new FieldDefinition("last name", StringType.DEFAULT,
                        "Last name", null, 0, 1));
        fg.add(lastNameField);
        final Field<String> emailField = new Field<String>(new FieldDefinition(
                "email", StringType.DEFAULT, "E-mail", null, 1, 1));
        fg.add(emailField);
        final Field<String> addressField = new Field<String>(
                new FieldDefinition("address", TextType.DEFAULT, "Address",
                        null, 0, 1));
        FieldRenderOptions fro = new FieldRenderOptions();
        fro.setWidth(1.0);
        addressField.setRenderOptions(fro);
        fg.add(addressField);
        if (custodian != null) {
            prefixField.setValue(custodian.prefix());
            firstNameField.setValue(custodian.firstName());
            middleNameField.setValue(custodian.middleName());
            lastNameField.setValue(custodian.lastName());
            emailField.setValue(custodian.email());
            addressField.setValue(custodian.address());
        }
        if (!mode.equals(FormEditMode.READ_ONLY)) {
            FormItemListener<String> fl = new FormItemListener<String>() {

                @Override
                public void itemValueChanged(FormItem<String> f) {

                    String email = emailField.value();
                    if (email != null) {
                        ro.setCustodian(prefixField.value(),
                                firstNameField.value(),
                                middleNameField.value(), lastNameField.value(),
                                addressField.value(), email);
                    }
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f,
                        Property property) {

                }
            };
            prefixField.addListener(fl);
            firstNameField.addListener(fl);
            middleNameField.addListener(fl);
            lastNameField.addListener(fl);
            emailField.addListener(fl);
            addressField.addListener(fl);
        }
        interfaceForm.add(fg);
    }

    private void addLocationFields(Form interfaceForm,
            final Repository.Location location, FormEditMode mode) {

        final Repository ro = (Repository) object();
        FieldGroup fg = new FieldGroup(new FieldDefinition("location",
                ConstantType.DEFAULT, "Location", null, 0, 1));
        final Field<String> buildingField = new Field<String>(
                new FieldDefinition("building", StringType.DEFAULT, "Building",
                        null, 0, 1));
        fg.add(buildingField);
        final Field<String> departmentField = new Field<String>(
                new FieldDefinition("department", StringType.DEFAULT,
                        "Department", null, 0, 1));
        fg.add(departmentField);
        final Field<String> institutionField = new Field<String>(
                new FieldDefinition("institution", StringType.DEFAULT,
                        "Institution", null, 0, 1));
        fg.add(institutionField);
        final Field<String> precinctField = new Field<String>(
                new FieldDefinition("precinct", StringType.DEFAULT, "Precinct",
                        null, 0, 1));
        fg.add(precinctField);
        if (location != null) {
            buildingField.setValue(location.building());
            departmentField.setValue(location.department());
            institutionField.setValue(location.institution());
            precinctField.setValue(location.precinct());
        }
        if (!mode.equals(FormEditMode.READ_ONLY)) {
            FormItemListener<String> fl = new FormItemListener<String>() {

                @Override
                public void itemValueChanged(FormItem<String> f) {

                    String institution = institutionField.value();
                    if (institution != null) {
                        ro.setLocation(buildingField.value(),
                                departmentField.value(), institution,
                                precinctField.value());
                    }
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f,
                        Property property) {

                }
            };
            buildingField.addListener(fl);
            departmentField.addListener(fl);
            institutionField.addListener(fl);
            precinctField.addListener(fl);
        }
        interfaceForm.add(fg);
    }

    private void addDataHoldingsFields(Form interfaceForm,
            final Repository.DataHoldings dataHoldings, FormEditMode mode) {

        final Repository ro = (Repository) object();
        FieldGroup fg = new FieldGroup(new FieldDefinition("data holdings",
                ConstantType.DEFAULT, "Data holdings", null, 0, 1));
        final Field<String> descriptionField = new Field<String>(
                new FieldDefinition("description", StringType.DEFAULT,
                        "Description", null, 0, 1));
        fg.add(descriptionField);
        final Field<Date> startDateField = new Field<Date>(new FieldDefinition(
                "start date", DateType.DEFAULT, "Start date", null, 0, 1));
        fg.add(startDateField);
        if (dataHoldings != null) {
            descriptionField.setValue(dataHoldings.description());
            startDateField.setValue(dataHoldings.startDate());
        }
        if (!mode.equals(FormEditMode.READ_ONLY)) {
            descriptionField.addListener(new FormItemListener<String>() {

                @Override
                public void itemValueChanged(FormItem<String> f) {

                    if (f.value() != null) {
                        ro.setDataHoldings(startDateField.value(), f.value());
                    }
                }

                @Override
                public void itemPropertyChanged(FormItem<String> f,
                        Property property) {

                }
            });
            startDateField.addListener(new FormItemListener<Date>() {

                @Override
                public void itemValueChanged(FormItem<Date> f) {

                    if (descriptionField.value() != null) {
                        ro.setDataHoldings(f.value(), descriptionField.value());
                    }
                }

                @Override
                public void itemPropertyChanged(FormItem<Date> f,
                        Property property) {

                }
            });
        }
        interfaceForm.add(fg);
    }

    private void addServerFields(Form interfaceForm, Repository.Server server,
            FormEditMode mode) {

        FieldGroup fg = new FieldGroup(new FieldDefinition("server",
                ConstantType.DEFAULT, "Mediaflux server", null, 1, 1));
        Field<String> f = new Field<String>(new FieldDefinition("uuid",
                ConstantType.DEFAULT, "Mediaflux server UUID", null, 1, 1));
        f.setValue(server.uuid());
        fg.add(f);
        f = new Field<String>(new FieldDefinition("version",
                ConstantType.DEFAULT, "Mediaflux server version", null, 1, 1));
        f.setValue(server.version());
        fg.add(f);
        f = new Field<String>(new FieldDefinition("name", ConstantType.DEFAULT,
                "Mediaflux server name", null, 1, 1));
        f.setValue(server.name());
        fg.add(f);
        f = new Field<String>(new FieldDefinition("organization",
                ConstantType.DEFAULT, "Organization", null, 1, 1));
        f.setValue(server.organization());
        fg.add(f);
        interfaceForm.add(fg);
    }

}
