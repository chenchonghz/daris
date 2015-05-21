package daris.client.ui.object;

import java.util.ArrayList;

import arc.gui.InterfaceCreateHandler;
import arc.gui.object.action.ActionInterface;
import arc.gui.object.action.precondition.ActionPrecondition;
import arc.gui.object.action.precondition.ActionPreconditionListener;
import arc.gui.object.action.precondition.ActionPreconditionOutcome;
import arc.gui.object.action.precondition.EvaluatePrecondition;
import arc.gui.window.Window;
import arc.mf.model.dictionary.DictionaryRef;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.TagDictionary;
import daris.client.model.project.Project;
import daris.client.model.user.messages.ActorSelfHaveRole;
import daris.client.model.user.messages.Self;

public class TagAction extends ActionInterface<DObject> {

    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 480;

    private DObjectRef _o;
    private boolean _projectAdmin;

    public TagAction(DObjectRef o, Window owner) {
        super(o.referentTypeName(), new ArrayList<ActionPrecondition>(), owner, DEFAULT_WIDTH, DEFAULT_HEIGHT);
        _o = o;
        preconditions().add(new ActionPrecondition() {

            @Override
            public EvaluatePrecondition evaluate() {
                return EvaluatePrecondition.BEFORE_INTERACTION;
            }

            @Override
            public String description() {
                return "Check if the tag dictionary has tags available to select and if the tag dictionary is empty, check if the user has project administrator role to create new tags.";
            }

            @Override
            public void execute(final ActionPreconditionListener l) {
                Self.canWrite(_o, new ObjectMessageResponse<Boolean>() {

                    @Override
                    public void responded(Boolean r) {
                        if (r) {
                            /*
                             * check if the user is project admin if there is no tags in the dictionary. The interface
                             * will let project admin create tags in the dictionary, then apply the tag to the object.
                             */
                            String projectAdminRole = Project.adminRoleFromId(_o.id());
                            if (projectAdminRole == null) {
                                _projectAdmin = false;
                                l.executed(ActionPreconditionOutcome.FAIL, "Failed to resolve project admin role name.");
                                return;
                            }
                            new ActorSelfHaveRole(projectAdminRole).send(new ObjectMessageResponse<Boolean>() {

                                @Override
                                public void responded(Boolean projectAdmin) {
                                    _projectAdmin = projectAdmin;
                                    final DictionaryRef dict = TagDictionary.tagDictionaryFor(_o);
                                    if (_projectAdmin) {
                                        TagDictionary.createIfNotExists(dict,
                                                new ObjectMessageResponse<DictionaryRef>() {

                                                    @Override
                                                    public void responded(DictionaryRef d) {
                                                        if (d != null) {
                                                            l.executed(
                                                                    ActionPreconditionOutcome.PASS,
                                                                    "You are a project administrator of the project. You can create tags (in the tag dictionary) then apply them on the object.");
                                                        } else {
                                                            l.executed(ActionPreconditionOutcome.FAIL,
                                                                    "Failed to create tag dictionary: " + dict.name());
                                                        }
                                                    }
                                                });
                                    } else {
                                        TagDictionary.isEmpty(dict, new ObjectMessageResponse<Boolean>() {
                                            @Override
                                            public void responded(Boolean isEmpty) {
                                                /*
                                                 * pass if tag dictionary has entires.
                                                 */
                                                if (!isEmpty) {
                                                    l.executed(ActionPreconditionOutcome.PASS,
                                                            "There are tags available in the tag dictionary.");
                                                } else {
                                                    l.executed(
                                                            ActionPreconditionOutcome.FAIL,
                                                            "The tag dictionary is empty and you do not have suffient privilege to create new tags. To create new tags, you need project administrator privilege.");
                                                }
                                            }
                                        });
                                    }
                                }
                            });
                        } else {
                            l.executed(ActionPreconditionOutcome.FAIL, "You do not have suffient privilege to modify "
                                    + _o.referentTypeName() + " " + _o.id() + ".");
                        }
                    }
                });

            }
        });
    }

    @Override
    public void createInterface(InterfaceCreateHandler ch) {
        ch.created(new TagForm(_o, _projectAdmin));
    }

    @Override
    public String actionName() {
        return "Manage tags";
    }

    public String title() {
        return "Manage tags for " + _o.referentTypeName() + " " + _o.id();
    }

    public String actionButtonName() {
        return "OK";
    }

}
