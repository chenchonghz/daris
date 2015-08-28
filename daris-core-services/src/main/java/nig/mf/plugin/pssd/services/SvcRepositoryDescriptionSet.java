package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.Application;
import nig.mf.plugin.pssd.RepositoryDescription;
import nig.mf.pssd.Role;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.DateType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.UrlType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcRepositoryDescriptionSet extends PluginService {

    public static final String SERVICE_NAME = "daris.repository.description.set";
    public static final String SERVICE_DESCRIPTION = "Set the description (asset) of daris repository.";
    private Interface _defn;

    public SvcRepositoryDescriptionSet() {

        _defn = new Interface();

        // name
        Interface.Element nameElement = new Interface.Element("name",
                StringType.DEFAULT, "The name of the repository.", 1, 1);
        nameElement.add(new Interface.Attribute("acronym", StringType.DEFAULT,
                "Acronym for the repository name.", 0));
        _defn.add(nameElement);

        // custodian
        Interface.Element custodianElement = new Interface.Element("custodian",
                XmlDocType.DEFAULT,
                "The person responsible for the management of the repository.",
                0, 1);
        custodianElement.add(new Interface.Element("email", StringType.DEFAULT,
                "The custodian's email address", 1, 1));
        custodianElement.add(new Interface.Element("prefix",
                StringType.DEFAULT, "Prefix for the custodian's name.", 0, 1));
        custodianElement.add(new Interface.Element("first", StringType.DEFAULT,
                "The custodian's first name.", 1, 1));
        custodianElement.add(new Interface.Element("middle",
                StringType.DEFAULT, "The custodian's middle name.", 0,
                Integer.MAX_VALUE));
        custodianElement.add(new Interface.Element("last", StringType.DEFAULT,
                "The custodian's last name.", 1, 1));
        Interface.Element custodianAddressElement = new Interface.Element(
                "address", XmlDocType.DEFAULT,
                "The institutional address of the custodian.", 0, 1);
        custodianAddressElement.add(new Interface.Element("department",
                StringType.DEFAULT, "The custodian's department.", 0, 1));
        custodianAddressElement.add(new Interface.Element("institution",
                StringType.DEFAULT, "The custodian's institution.", 0, 1));
        custodianAddressElement.add(new Interface.Element("physical-address",
                StringType.DEFAULT,
                "The custodian's address - use as many of these as you need.",
                0, Integer.MAX_VALUE));
        custodianElement.add(custodianAddressElement);
        custodianElement.add(new Interface.Element("NLA-ID",
                StringType.DEFAULT,
                "National Library of Australia identifier.", 0, 1));
        _defn.add(custodianElement);

        // location
        Interface.Element locationElement = new Interface.Element("location",
                XmlDocType.DEFAULT, "The physical location of the repository.",
                0, 1);
        locationElement.add(new Interface.Element("institution",
                StringType.DEFAULT, "The institution hosting the data.", 1, 1));
        locationElement.add(new Interface.Element("department",
                StringType.DEFAULT, "The department within the institution.",
                0, 1));
        locationElement.add(new Interface.Element("building",
                StringType.DEFAULT, "The building within the institution.", 0,
                1));
        locationElement.add(new Interface.Element("precinct",
                StringType.DEFAULT,
                "Can be a suburb or generic term describing an area.", 0, 1));
        _defn.add(locationElement);

        // rights
        Interface.Element rightsElement = new Interface.Element(
                "rights",
                XmlDocType.DEFAULT,
                "A description of the rights process to gain access to collections in the repository.",
                0, 1);
        rightsElement.add(new Interface.Element("description",
                StringType.DEFAULT, "The description.", 1, 1));
        _defn.add(rightsElement);

        // data holdings
        Interface.Element dataHoldingsElement = new Interface.Element(
                "data-holdings",
                XmlDocType.DEFAULT,
                "Describes broadly the data holdings in the repositoryo collections in the repository.",
                0, 1);
        dataHoldingsElement.add(new Interface.Element("description",
                StringType.DEFAULT, "The description.", 1, 1));
        dataHoldingsElement
                .add(new Interface.Element(
                        "start-date",
                        DateType.DEFAULT,
                        "The date on which the repository was activated and started managing data.",
                        0, 1));
        _defn.add(dataHoldingsElement);

        // originating-source
        _defn.add(new Interface.Element(
                "originating-source",
                UrlType.DEFAULT,
                "The originating source for any meta-data harvested from this repository",
                0, 1));

    }

    public String name() {
        return SERVICE_NAME;
    }

    public String description() {
        return SERVICE_DESCRIPTION;
    }

    public Interface definition() {
        return _defn;
    }

    public Access access() {
        return ACCESS_ADMINISTER;
    }

    public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
            throws Throwable {

        String assetId = RepositoryDescription.getAssetId(executor());
        XmlDocMaker dm = new XmlDocMaker("args");
        if (assetId == null) {
            dm.add("name", RepositoryDescription.ASSET_NAME);
            dm.add("namespace", Application.defaultNamespace(executor()));
            dm.push("acl");
            dm.add("actor", new String[] { "type", "role" },
                    Role.objectAdminRoleName());
            dm.add("access", "read-write");
            dm.pop();
            dm.push("acl");
            dm.add("actor", new String[] { "type", "role" },
                    Role.modelUserRoleName());
            dm.add("access", "read");
            dm.pop();
            dm.push("meta");
        } else {
            dm.add("id", assetId);
            dm.push("meta", new String[] { "action", "replace" });
        }
        dm.push(RepositoryDescription.DOC_TYPE);
        dm.add(args, false);
        dm.pop();
        dm.pop();

        if (assetId == null) {
            assetId = executor().execute("asset.create", dm.root()).value("id");
        } else {
            executor().execute("asset.set", dm.root());
        }
        w.add("id", assetId);
    }
}
