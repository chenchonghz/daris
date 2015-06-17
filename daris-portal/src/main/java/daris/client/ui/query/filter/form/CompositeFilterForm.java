package daris.client.ui.query.filter.form;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import arc.gui.gwt.colour.RGB;
import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.dnd.DropCheck;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.dnd.DropListener;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.button.Button;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.format.WidgetFormatter;
import arc.gui.gwt.widget.image.Image;
import arc.gui.gwt.widget.image.LinearGradient;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridColumn;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.list.ListGridHeader;
import arc.gui.gwt.widget.panel.AbsolutePanel;
import arc.gui.gwt.widget.panel.HorizontalPanel;
import arc.gui.gwt.widget.panel.HorizontalSplitPanel;
import arc.gui.gwt.widget.panel.VerticalPanel;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tip.ToolTip;
import arc.gui.gwt.widget.tip.ToolTipHandler;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.MustBeValid;
import arc.mf.client.util.Validity;

import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Widget;

import daris.client.Resource;
import daris.client.model.query.filter.CompositeFilter;
import daris.client.model.query.filter.CompositeFilter.Member;
import daris.client.model.query.filter.Filter;
import daris.client.model.query.filter.FilterTree;
import daris.client.model.query.filter.LogicOperator;
import daris.client.model.query.filter.pssd.ObjectCompositeFilter;
import daris.client.ui.query.filter.FilterTreeGUI;
import daris.client.ui.query.filter.item.FilterItem;
import daris.client.ui.query.filter.item.FilterItemFactory;
import daris.client.ui.query.filter.item.FilterItem.HasComposite;

public class CompositeFilterForm extends FilterForm<CompositeFilter> {

	public static final arc.gui.image.Image ICON_DELETE = new arc.gui.image.Image(
			Resource.INSTANCE.closeBoxRed16().getSafeUri().asString(), 12, 12);

	public static final arc.gui.image.Image ICON_VALID = new arc.gui.image.Image(
			Resource.INSTANCE.tick12().getSafeUri().asString(), 12, 12);

	public static final arc.gui.image.Image ICON_INVALID = new arc.gui.image.Image(
			Resource.INSTANCE.cross12().getSafeUri().asString(), 12, 12);

	private List<MustBeValid> _mbvs;
	private List<OperatorItem> _operatorItems;
	private List<NegationItem> _negationItems;
	private List<FilterItem<?>> _filterItems;

	private VerticalPanel _vp;

	private Button _removeButton;

	private ListGrid<CompositeFilter.Member> _grid;

	private Image _statusIcon;
	private HTML _statusHTML;
	private HTML _summaryHTML;

	private FilterTree _filterTree;

	public CompositeFilterForm(CompositeFilter filter, boolean editable) {
		this(filter, editable, FilterTree.DEFAULT);
	}

	public CompositeFilterForm(CompositeFilter filter, boolean editable,
			FilterTree filterTree) {
		super(filter, editable);
		_filterTree = filterTree == null ? FilterTree.DEFAULT : filterTree;
		_mbvs = new ArrayList<MustBeValid>();
		_operatorItems = new ArrayList<OperatorItem>();
		_negationItems = new ArrayList<NegationItem>();
		_filterItems = new ArrayList<FilterItem<?>>();

		DataSource<ListGridEntry<CompositeFilter.Member>> ds = new DataSource<ListGridEntry<Member>>() {

			@Override
			public boolean isRemote() {
				return false;
			}

			@Override
			public boolean supportCursor() {
				return false;
			}

			@Override
			public void load(arc.gui.gwt.data.filter.Filter f, long start,
					long end,
					DataLoadHandler<ListGridEntry<CompositeFilter.Member>> lh) {
				List<CompositeFilter.Member> members = filter().members();
				if (members == null || members.isEmpty()) {
					lh.loaded(0, 0, 0, null, DataLoadAction.REPLACE);
					return;
				}

				List<CompositeFilter.Member> fmembers = new ArrayList<CompositeFilter.Member>(
						members);
				if (f != null) {
					for (Iterator<CompositeFilter.Member> it = fmembers
							.iterator(); it.hasNext();) {
						CompositeFilter.Member ff = it.next();
						if (!f.matches(ff)) {
							it.remove();
						}
					}
				}
				if (fmembers.isEmpty()) {
					lh.loaded(0, 0, 0, null, DataLoadAction.REPLACE);
					return;
				}

				int total = fmembers.size();
				int start0 = (int) start;
				int end0 = end < total ? (int) end : total;

				fmembers = start0 < total ? fmembers.subList(start0, end0)
						: null;
				if (fmembers == null || fmembers.isEmpty()) {
					lh.loaded(start0, end0, 0, null, DataLoadAction.REPLACE);
					return;
				}

				List<ListGridEntry<CompositeFilter.Member>> entries = new ArrayList<ListGridEntry<CompositeFilter.Member>>(
						fmembers.size());
				for (int i = 0; i < fmembers.size(); i++) {
					CompositeFilter.Member filter = fmembers.get(i);
					ListGridEntry<CompositeFilter.Member> entry = new ListGridEntry<CompositeFilter.Member>(
							filter);
					entry.set("index", i);
					entry.set("operator", entry.data().operator());
					entry.set("negated", entry.data().negated());
					entry.set("filter", entry.data().filter());
					entries.add(entry);
				}
				lh.loaded(start0, end0, entries.size(), entries,
						DataLoadAction.REPLACE);
			}
		};
		_grid = new ListGrid<CompositeFilter.Member>(ds, ScrollPolicy.AUTO) {
			@Override
			protected void preLoad() {
				beforeLoad();
			}

			@Override
			protected void postLoad(long start, long end, long total,
					List<ListGridEntry<CompositeFilter.Member>> entries) {
				afterLoad();
			}
		};

		if (editable()) {
			_grid.addColumnDefn("index", "", "delete this filter",
					new WidgetFormatter<Member, Integer>() {

						@Override
						public BaseWidget format(Member f, final Integer idx) {
							final Image deleteIcon = new Image(ICON_DELETE, 12,
									12);
							deleteIcon.addClickHandler(new ClickHandler() {

								@Override
								public void onClick(ClickEvent event) {

									removeMember(idx);
								}
							});
							deleteIcon.setToolTip("Delete this filter " + idx
									+ ".");
							deleteIcon.setOpacity(0.8);
							deleteIcon
									.addMouseOverHandler(new MouseOverHandler() {

										@Override
										public void onMouseOver(
												MouseOverEvent event) {
											deleteIcon.setOpacity(1.0);
										}
									});
							deleteIcon
									.addMouseOutHandler(new MouseOutHandler() {

										@Override
										public void onMouseOut(
												MouseOutEvent event) {
											deleteIcon.setOpacity(0.8);
										}
									});
							return deleteIcon;
						}
					}).setWidth(20);
		}

		_grid.addColumnDefn("index", "index").setWidth(40);

		_grid.addColumnDefn("operator", "operator", null,
				new WidgetFormatter<Member, LogicOperator>() {

					@Override
					public BaseWidget format(Member member, LogicOperator op) {
						boolean first = filter().indexOf(member) == 0;
						if (!first) {
							OperatorItem c = new OperatorItem(member,
									editable());
							CompositeFilterForm.this.addMustBeValid(c);
							return c.widget();
						}
						return null;
					}
				}).setWidth(75);

		_grid.addColumnDefn("negated", "negation", null,
				new WidgetFormatter<Member, Boolean>() {

					@Override
					public BaseWidget format(Member member, Boolean negated) {
						NegationItem c = new NegationItem(member, editable());
						CompositeFilterForm.this.addMustBeValid(c);
						return c.widget();
					}
				}).setWidth(75);

		@SuppressWarnings("unchecked")
		ListGridColumn<Filter> filterCol = _grid.addColumnDefn("filter",
				"filter", null, new WidgetFormatter<Member, Filter>() {

					@Override
					public BaseWidget format(Member context, Filter filter) {
						FilterItem<?> item = null;
						item = FilterItemFactory.createItem(
								CompositeFilterForm.this, filter, editable());
						if (item != null) {
							CompositeFilterForm.this.addMustBeValid(item);
							/*
							 * Widget in list grid cell does not have its parent
							 * (ContainerWidget) set. Therefore, its window()
							 * can not be resolved. Here we manually set its
							 * window.
							 */
							item.setWindow(window());
						}
						return item == null ? null : item.widget();
					}
				});
		filterCol.setWidth(800);
		filterCol.setVerticalAlign(VerticalAlign.MIDDLE);

		_grid.setSelectionHandler(new SelectionHandler<Member>() {

			@Override
			public void selected(Member o) {
				enableMenuButtons();
			}

			@Override
			public void deselected(Member o) {
				disableMenuButtons();
			}
		});
		_grid.fitToParent();
		_grid.setShowHeader(true);
		_grid.setShowRowSeparators(true);
		_grid.setMultiSelect(false);
		_grid.setFontSize(10);
		_grid.setCellSpacing(0);
		_grid.setCellPadding(1);
		_grid.setEmptyMessage("");
		_grid.setLoadingMessage("Load filters ...");
		_grid.setCursorSize(Integer.MAX_VALUE);
		_grid.setFontSize(12);

		_grid.setRowToolTip(new ToolTip<Member>() {

			@Override
			public void generate(Member m, ToolTipHandler th) {

				Filter f = m.filter();
				if (f != null) {
					th.setTip(new HTML(f.toString()));
				}
			}
		});

		_grid.enableDropTarget(false);
		_grid.setDropHandler(new DropHandler() {

			@Override
			public DropCheck checkCanDrop(Object data) {
				if (data != null && (data instanceof Filter)) {
					return DropCheck.CAN;
				}
				return DropCheck.CANNOT;
			}

			@Override
			public void drop(BaseWidget target, List<Object> objs,
					DropListener dl) {
				if (objs == null || objs.isEmpty()) {
					dl.dropped(DropCheck.CANNOT);
					return;
				}
				dl.dropped(DropCheck.CAN);
				for (Object obj : objs) {
					if (obj instanceof Filter) {
						Filter f = ((Filter) obj).copy();
						addMember(LogicOperator.and, false, f);
					}
				}
			}
		});

		_vp = new VerticalPanel();
		_vp.fitToParent();

		if (editable()) {
			HorizontalSplitPanel hsp = new HorizontalSplitPanel(5);
			hsp.fitToParent();
			hsp.add(_grid);

			VerticalPanel treeVP = new VerticalPanel();
			treeVP.setPreferredWidth(0.3);
			treeVP.setHeight100();

			HTML treeLabel = new HTML(
					"<div text-align=\"center\"><div style=\"width: 50%; margin: 0 auto; text-align: left;\">Filter Selector</div></div>");
			treeLabel.setFontSize(11);
			treeLabel.setWidth100();
			treeLabel.setHeight(ListGridHeader.HEIGHT);
			treeLabel.setBackgroundImage(new LinearGradient(
					LinearGradient.Orientation.TOP_TO_BOTTOM,
					ListGridHeader.HEADER_COLOUR_LIGHT,
					ListGridHeader.HEADER_COLOUR_DARK));

			treeVP.add(treeLabel);

			FilterTreeGUI treeGUI = new FilterTreeGUI(filterTree());
			treeGUI.fitToParent();
			treeVP.add(treeGUI);

			hsp.add(treeVP);

			_vp.add(hsp);

		} else {
			_vp.add(_grid);
		}

		HorizontalPanel footerHP = new HorizontalPanel();
		footerHP.setHeight(20);
		footerHP.setWidth100();

		_statusIcon = new Image(ICON_VALID, 12, 12);
		_statusIcon.setDisabledImage(ICON_INVALID);
		_statusIcon.setMarginTop(3);
		_statusIcon.hide();
		footerHP.setSpacing(3);
		footerHP.add(_statusIcon);
		footerHP.setBorderTop(1, BorderStyle.SOLID, new RGB(0xaa, 0xaa, 0xaa));

		AbsolutePanel statusAP = new AbsolutePanel();
		statusAP.setHeight100();
		statusAP.setWidth100();
		footerHP.setSpacing(3);
		footerHP.add(statusAP);

		_statusHTML = new HTML();
		_statusHTML.setMarginTop(1);
		_statusHTML.setVerticalAlign(VerticalAlign.MIDDLE);
		_statusHTML.setFontSize(11);
		_statusHTML.setPosition(Position.ABSOLUTE);
		_statusHTML.setLeft(0);
		statusAP.add(_statusHTML);

		_summaryHTML = new HTML();
		_summaryHTML.setMarginTop(1);
		_summaryHTML.setFontSize(11);
		_summaryHTML.setWidth(60);
		footerHP.add(_summaryHTML);

		_vp.add(footerHP);

		_grid.refresh();
	}

	@Override
	public Validity valid() {
		List<Member> members = filter().members();
		int total = members == null ? 0 : members.size();
		int nbInvalids = 0;
		Validity v = IsValid.INSTANCE;
		if (members == null || members.isEmpty()) {
			v = new Validity() {
				@Override
				public boolean valid() {
					return false;
				}

				@Override
				public String reasonForIssue() {
					return "At least one sub-filter is required.";
				}
			};
		} else {
			for (int i = 0; i < total; i++) {
				final int idx = i;
				Filter f = members.get(i).filter();
				final Validity iv = f.valid();
				if (!iv.valid()) {
					nbInvalids++;
					if (v.valid()) {
						v = new Validity() {
							@Override
							public boolean valid() {
								return false;
							}

							@Override
							public String reasonForIssue() {
								return "sub-filter row " + idx
										+ " is invalid: " + iv.reasonForIssue();
							}
						};
					}
				}
			}
		}
		_statusIcon.setEnabled(v.valid());
		_statusIcon.show();
		_statusHTML.setHTML(v.valid() ? filter().toString() : v
				.reasonForIssue());
		_summaryHTML.setHTML("valid: " + (total - nbInvalids) + " / " + total);
		return v;
	}

	@Override
	public Widget gui() {
		return _vp;
	}

	public void addMember(LogicOperator op, boolean negated, Filter filter) {
		addMember(new Member(op, negated, filter));
	}

	public void addMember(Member m) {
		filter().addMember(m);
		notifyOfChangeInState();
		_grid.refresh();
	}

	public void removeMember(int index) {
		filter().removeMember(index);
		_grid.refresh();
		notifyOfChangeInState();
	}

	public void clearMembers() {
		filter().clearMembers();
		_grid.refresh();
		notifyOfChangeInState();
	}

	@Override
	protected void addMustBeValid(MustBeValid mbv, boolean notify) {
		addMustBeValid(mbv, notify, true);
	}

	protected void addMustBeValid(MustBeValid mbv, boolean notify,
			boolean saveRef) {
		if (saveRef) {
			_mbvs.add(mbv);
			if (mbv instanceof FilterItem) {
				_filterItems.add((FilterItem<?>) mbv);
			} else if (mbv instanceof OperatorItem) {
				_operatorItems.add((OperatorItem) mbv);
			} else if (mbv instanceof NegationItem) {
				_negationItems.add((NegationItem) mbv);
			}
		}
		super.addMustBeValid(mbv, notify);
	}

	protected void removeMustBeValid(MustBeValid mbv) {
		if (mbv instanceof FilterItem) {
			_filterItems.remove((FilterItem<?>) mbv);
		} else if (mbv instanceof OperatorItem) {
			_operatorItems.remove((OperatorItem) mbv);
		} else if (mbv instanceof NegationItem) {
			_negationItems.remove((NegationItem) mbv);
		}
		_mbvs.remove(mbv);
		super.removeMustBeValid(mbv);
	}

	protected void beforeLoad() {
		clearMustBeValids();
		disableMenuButtons();
	}

	protected void afterLoad() {
		enableMenuButtons();
	}

	private void disableMenuButtons() {
		if (_removeButton != null) {
			_removeButton.disable();
		}
	}

	private void enableMenuButtons() {
		if (editable()) {
			if (_removeButton != null) {
				_removeButton.enable();
			}
		}
	}

	private void clearMustBeValids() {
		for (MustBeValid mbv : _mbvs) {
			super.removeMustBeValid(mbv);
		}
		_mbvs.clear();
		_filterItems.clear();
		_operatorItems.clear();
		_negationItems.clear();
	}

	public int indexOf(FilterItem<?> item) {
		return _filterItems.indexOf(item);
	}

	public FilterTree filterTree() {
		return _filterTree;
	}

	public void setFilterTree(FilterTree filterTree) {
		_filterTree = filterTree;
	}

	public static CompositeFilterForm create(Filter filter, boolean editable) {
		return create(filter, editable, null);
	}

	public static CompositeFilterForm create(Filter filter, boolean editable,
			FilterTree filterTree) {
		if (filter instanceof ObjectCompositeFilter) {
			return new ObjectCompositeFilterForm(
					(ObjectCompositeFilter) filter, editable, filterTree);
		} else {
			return new CompositeFilterForm(
					CompositeFilter.wrapIfItIsNotComposite(filter), editable,
					filterTree);
		}
	}

	public static CompositeFilterForm create(final HasComposite hasComposite) {
		FilterItem<?> item = hasComposite.hadBy();
		CompositeFilter composite = hasComposite.composite();
		CompositeFilterForm form;
		if (composite instanceof ObjectCompositeFilter) {
			form = new ObjectCompositeFilterForm(
					(ObjectCompositeFilter) composite, item.editable(),
					item.form() == null ? null : item.form().filterTree(),
					false);
		} else {
			form = new CompositeFilterForm(composite, item.editable(),
					item.form() == null ? null : item.form().filterTree());
		}
		form.addFilterChangeListener(new FilterChangeListener<CompositeFilter>() {

			@Override
			public void filterChanged(CompositeFilter filter) {
				hasComposite.setComposite(filter);
			}
		});
		return form;
	}
}
