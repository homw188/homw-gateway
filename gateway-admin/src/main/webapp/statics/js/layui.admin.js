function sideFlexible() {
	var app = layui.$("#LAY_app");
	var flex = layui.$("#LAY_app_flexible");
	
	var left = "layui-icon-spread-left";
	var right = "layui-icon-shrink-right";
	var shrink = "layadmin-side-shrink";
	var spread = "layadmin-side-spread-sm";
	
	var size = screenSize();
	
	if (layui.$("#LAY_app_flexible").hasClass("layui-icon-spread-left")) {
		flex.removeClass(left).addClass(right);
		if (size < 2) {
			app.addClass(spread);
		} else {
			app.removeClass(spread);
		}
		app.removeClass(shrink);
	} else {
		flex.removeClass(right).addClass(left);
		if (size < 2) {
			app.removeClass(shrink);
		} else {
			app.addClass(shrink);
		}
		app.removeClass(spread);
	}
}

function screenSize() {
	var width = layui.$(window).width();
	return width > 1200 ? 3 : width > 992 ? 2 : width > 768 ? 1 : 0;
}

var tabsPage = {};
function tabsBody(tabIndex) {
	return layui.$("#LAY_app_body").find(".layadmin-tabsbody-item").eq(tabIndex || 0);
}

function changeTab(tab) {
	tabsBodyChange(tab.index(), {
		url: (tab.attr("lay-id"), tab.attr("lay-attr"))
	});
}

function tabsBodyChange(tabIndex, url) {
	tabsBody(tabIndex).addClass("layui-show").siblings().removeClass("layui-show");
	rollPage("auto", tabIndex);
}

function rollPage(type, tabIndex) {
	var tabs = layui.$("#LAY_app_tabsheader");
	var children = tabs.children("li");
	var tabsWidth = (tabs.prop("scrollWidth"), tabs.outerWidth());
	var tabsLeft = parseFloat(tabs.css("left"));
	
	if ("left" === type) {
		if (!tabsLeft && tabsLeft <= 0) return;
		var r = -tabsLeft - tabsWidth;
		children.each(function(idx, tab) {
			var $this = layui.$(tab);
			var left = $this.position().left;
			if (left >= r) {
				tabs.css("left", -tabsWidth);
				return false;
			}
		});
	} else "auto" === type ? !
	function() {
		var temp;
		var tab = children.eq(tabIndex);
		if (tab[0]) {
			temp = tab.position().left;
			if (temp < -tabsLeft) {
				return tabs.css("left", -temp);
			}
			if (temp + tab.outerWidth() >= tabsWidth - tabsLeft) {
				var diff = temp + tab.outerWidth() - (tabsWidth - tabsLeft);
				children.each(function(idx, t) {
					var $this = layui.$(tab);
					var left = $this.position().left;
					if (left + tabsLeft > 0 && tabsWidth - tabsLeft > diff) {
						tabs.css("left", -tabsWidth);
						return false;
					}
				});
			}
		}
	}() : children.each(function(idx, tab) {
		var $this = layui.$(tab);
		var left = $this.position().left;
		if (left + $this.outerWidth() >= tabsWidth - tabsLeft) {
			tabs.css("left", -left);
			return false;
		}
	});
}

var resizeFn = {};
function resize(action) {
	var path = layui.router().path.join("-");
	if (resizeFn[path]) {
		layui.$(window).off("resize", resizeFn[path]);
		delete resizeFn[path];
	}
	if ("off" !== action) {
		action();
		resizeFn[path] = action;
		layui.$(window).on("resize", resizeFn[path]);
	}
}

function refreshTab() {
	var len = layui.$(".layadmin-tabsbody-item").length;
	tabsPage.index >= len && (tabsPage.index = len - 1);
	var iframe = tabsBody(tabsPage.index).find(".layadmin-iframe");
	iframe[0].contentWindow.location.reload(!0);
}

function leftPage() {
	rollPage("left");
}

function rightPage() {
	rollPage();
}

function closeThisTabs() {
	if (tabsPage.index) {
		layui.$("#LAY_app_tabsheader>li").eq(tabsPage.index).find(".layui-tab-close").trigger("click");
	}
}

function closeOtherTabs(type) {
	if ("all" === type) {
		layui.$("#LAY_app_tabsheader>li:gt(0)").remove();
		layui.$("#LAY_app_body").find(".layadmin-tabsbody-item:gt(0)").remove();
		layui.$("#LAY_app_tabsheader>li").eq(0).trigger("click");
	} else {
		layui.$("#LAY_app_tabsheader>li").each(function(idx, tab) {
			if (idx && idx != tabsPage.index) {
				layui.$(tab).addClass("LAY-system-pagetabs-remove");
				tabsBody(idx).addClass("LAY-system-pagetabs-remove");
			}
		});
		layui.$(".LAY-system-pagetabs-remove").remove();
	}
}

function closeAllTabs() {
	closeOtherTabs("all")
}

layui.$("body").on("click", "*[lay-href]", function() {
	var $this = layui.$(this);
	var href = $this.attr("lay-href");
	var txt = $this.attr("lay-text");
	
	layui.router();
	tabsPage.elem = $this;
	
	openTabsPage(href, txt || $this.text());
	
	if (href === tabsBody(tabsPage.index).find("iframe").attr("src")) {
		refreshTab();
	}
});

function openTabsPage(href, txt) {
	var exist = false;
	var tabs = layui.$("#LAY_app_tabsheader>li");
	var attr = href.replace(/(^http(s*):)|(\?[\s\S]*$)/g, "");
	tabs.each(function(idx) {
		var $this = layui.$(this);
		var layId = $this.attr("lay-id");
		if (layId === href) {
			exist = true;
			tabsPage.index = idx;
		}
	});
	txt = txt || "新标签页";
	var _tabChange = function() {
		layui.element.tabChange("layadmin-layout-tabs", href);
		tabsBodyChange(tabsPage.index, {
			url: href,
			text: txt
		});
	};
	var pageTabs = true;
	if (pageTabs) {
		if (!exist) {
			setTimeout(function() {
				layui.$("#LAY_app_body").append(['<div class="layadmin-tabsbody-item layui-show">', 
					'<iframe src="' + href + '" frameborder="0" class="layadmin-iframe"></iframe>', "</div>"].join(""));
				_tabChange();
			}, 10);
			tabsPage.index = tabs.length;
			layui.element.tabAdd("layadmin-layout-tabs", {
				title: "<span>" + txt + "</span>",
				id: href,
				attr: attr
			});
		}
	} else {
		var iframe = tabsBody(tabsPage.index).find(".layadmin-iframe");
		iframe[0].contentWindow.location.href = href;
	}
	_tabChange();
}

layui.element.on("tab(layadmin-layout-tabs)", function(tab) {
	tabsPage.index = tab.index;
});

layui.$("body").on("click", "#LAY_app_tabsheader>li", function() {
	var $this = layui.$(this);
	tabsPage.type = "tab";
	tabsPage.index = $this.index();
	changeTab($this);
});

layui.element.on("tabDelete(layadmin-layout-tabs)", function(tab) {
	var selectedTab = layui.$("#LAY_app_tabsheader>li.layui-this");
	if (tab.index) {
		tabsBody(tab.index).remove();
		changeTab(selectedTab);
		resize("off");
	}
});

layui.element.on("nav(layadmin-pagetabs-nav)", function(nav) {
	var navParent = nav.parent();
	navParent.removeClass("layui-this");
	navParent.parent().removeClass("layui-show");
});