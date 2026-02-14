let hasChanged = false;
let skipDetectViewPort;

function clearButtonOnComplete(widgetVar, listContext) {
    // Clear the autocomplete component itself.
    if (widgetVar && PF(widgetVar)) {
        PF(widgetVar).clear();
    } else if (listContext && PF(listContext)) {
        PF(listContext).clear();
    }

    // Clear dependent components.
    if (listContext === 'organizationrequired') {
        if (PF('departmentrequired')) {
            PF('departmentrequired').clear();
        }
        if (PF('instituterequired')) {
            PF('instituterequired').clear();
        }
    }
    if (listContext === 'departmentrequired' && PF('instituterequired')) {
        PF('instituterequired').clear();
    }
}

function disableAllButtonsAndTabs() {
    const elements = document.getElementsByTagName("input");
    for (let i = 0; i < elements.length; i++) {
        if (elements[i].type === "button" || elements[i].type === "submit") {
            elements[i].disabled = true;
        }
    }
}

function disableAutocompleteItems(autocompleteClientId, uuids) {
    if (autocompleteClientId && uuids && uuids.length > 0) {
        const uuidsJoined = uuids.join(",");
        jQuery("div[id='".concat(autocompleteClientId).concat("'].disableItems ul.ui-autocomplete-multiple-container.ui-widget.ui-inputfield li[data-token-value]")).each(
            function () {
                const dataTokenValue = jQuery(this).attr("data-token-value");
                if (dataTokenValue) {
                    if (uuidsJoined.includes(dataTokenValue)) {
                        this.classList.add("ui-state-disabled");
                    }
                }
            }
        );
    }
}

function disableBackSpaceAndDeleteKeyboardInput(autoCompleteId, event) {
    const autoCompleteWidgetVar = PrimeFaces.getWidgetById(autoCompleteId);
    if (autoCompleteWidgetVar) {
        if (autoCompleteWidgetVar && autoCompleteWidgetVar.input && autoCompleteWidgetVar.input.val().length < 1 && (event.code === 'Backspace' || event.key === 'Backspace' || event.code === 'Delete' || event.key === 'Delete')) {
            event.preventDefault();
            event.stopPropagation();
            event.stopImmediatePropagation();
        }
    }
}

function enableAllButtonsAndTabs() {
    const elements = document.getElementsByTagName("input");
    for (let i = 0; i < elements.length; i++) {
        if (elements[i].type === "button" || elements[i].type === "submit") {
            elements[i].disabled = false;
        }
    }
}

function repositionDialog() {
    // Fix the default primefaces dialog position and size.
    jQuery(".ui-dialog").each(
        function () {
            try {
                jQuery(this).css("overflow", "auto");
                jQuery(this).css("top", "10%");
                jQuery(this).css("left", "10%");
                const innerHeight = window.innerHeight;
                if (innerHeight) {
                    jQuery(this).css("max-height", 0.9 * innerHeight);
                }
                const innerWidth = window.innerWidth;
                if (innerWidth) {
                    jQuery(this).css("max-width", 0.9 * innerWidth);
                }
            } catch (err) {
                // not interested in this.
            }
        });
}

function hideDialogOnSuccess(args, widgetName) {
    if (args && !args.validationFailed && PF(widgetName)) {
        PF(widgetName).hide();
    }
}

function fixOversize() {
    // Minimal page width 1000px.
    let width, availableWidth = 1000;
    const innerWidth = document.documentElement.clientWidth, activeElement = document.activeElement;
    let element;
    if (availableWidth < innerWidth) {
        availableWidth = innerWidth;
    }

    jQuery("body").css("width", availableWidth + "px");

    jQuery(".ui-datatable-tablewrapper, .ui-datatable-scrollable, .ui-datagrid-content, .ui-treetable, .oversize").each(
        function () {
            try {
                if (jQuery(this).hasClass("ui-treetable") && jQuery(this).hasClass("tableStyleClass")) {
                    // Creating a wrapper for the tree table content (analogously to datatable).
                    const originalId = this.id;
                    const newElementId = originalId.concat("_ui-treetable-wrapper-id");
                    let newElement = document.getElementById(newElementId);
                    if (newElement == null) {
                        jQuery(this).wrap("<div id=".concat(newElementId).concat(" ").concat("class='ui-treetable-wrapper'></div>"));
                        newElement = document.getElementById(newElementId);

                        if (newElement) {
                            let toMove = null;
                            jQuery(this).children("div[id^=\"".concat(originalId).concat("\"].ui-paginator.ui-paginator-bottom")).each(function () {
                                toMove = this;
                                this.remove();
                                return false;
                            });

                            if (toMove) {
                                jQuery(toMove).appendTo(newElement);
                                jQuery(newElement).css("width", jQuery(this).css("width"));
                                jQuery(this).css("overflow-x", "auto");
                                jQuery(this).children("table[role=\"treegrid\"].treeTableStyleClass").each(function () {
                                    jQuery(this).css("width", "auto");
                                    return false;
                                });
                            }
                        }
                    } else {
                        width = availableWidth - jQuery(this).offset().left - jQuery("#contentBody").css("padding-right").replace("px", "") - 2;
                        jQuery(newElement).css("width", width);
                    }
                } else {
                    // Important: reset width to auto to enforce its computation on the original component's width
                    jQuery(this).css("width", "auto");
                    // - 2 to be safe in case of different rounding of browsers
                    width = availableWidth - jQuery(this).offset().left - jQuery("#contentBody").css("padding-right").replace("px", "") - 2;
                    if (jQuery(this).width() > width) {
                        jQuery(this).css("width", width + "px");
                        jQuery(this).css("overflow-x", "auto");
                    }
                }
            } catch (err) {
                // not interested in this.
            }
        });

    if (activeElement != null && activeElement.id != null) {
        element = document.getElementById(activeElement.id);
        if (element != null) {
            element.blur();
            element.focus();
        }
    }
}

/* Adapt page title to window size. */
function fixPageTitle(availableWidth) {
    let pageTitle, cell, width;
    try {
        pageTitle = pageTitle = jQuery(".page-title");
        const padLeft = parseInt(pageTitle.css("padding-left")) || 0;
        const padRight = parseInt(pageTitle.css("padding-right")) || 0;
        const suffixW = jQuery("#pageTitleSuffix").outerWidth(true) || 0;
        width = availableWidth - padLeft - padRight - suffixW;
        width = Math.max(0, Math.round(width));
        cell = jQuery(".pageTitleEllipsis");
        cell.css("width", width + "px");
    } catch (err) {
        // not interested in this.
    }
}

/* Adapt container name to window size. */
function fixSearchContainer(availableWidth) {
    let cell, width;
    try {
        cell = jQuery("#contextContainerTitle");
        // 20 = left-padding + 160 = containerContext + 180 = search box plus buttons around + 30 = right padding + space required on the left side
        width = availableWidth - 390;
        cell.css("width", width + "px");
    } catch (err) {
        // not interested in this.
    }
}

/* Triggers all methods to fix oversize screens. */
function fixAll() {
    // Minimal page width 1000px
    let availableWidth = 1000;
    const innerWidth = document.documentElement.clientWidth;

    if (availableWidth < innerWidth) {
        availableWidth = innerWidth;
    }

    fixOversize();
    fixPageTitle(availableWidth);
    fixSearchContainer(availableWidth);
}

/* Jump to the specified page of the given widgetVar */
function jumpToPage(widgetVar, jumpToPage) {
    if (widgetVar && PF(widgetVar) && PF(widgetVar).paginator !== 'undefined' && jumpToPage && jumpToPage !== "") {
        PF(widgetVar).paginator.setPage(jumpToPage);
    }
}

/* Call blur when enter is pressed on any input text element */
function replaceEnterWithBlur(evt) {
    const keyboardEvent = (evt) ? evt : ((event) ? event : null), node = (keyboardEvent.target) ? keyboardEvent.target : null;
    // if 'Enter' key is pressed inside an input text element, just call the blur function.
    if ((keyboardEvent.key === 'Enter') && (node.type === "text")) {
        if (node.id !== 'searchForm:term') {
            node.blur();
        }
        return false;
    }
}

function resizeTreeTable(treeTable) {
    let maxCellWidth = 0;
    const treeTableDiv = document.getElementById(treeTable);
    if (treeTableDiv) {
        jQuery(treeTableDiv).find("table[role=treegrid] > tbody > tr[class*=ui-node-level] > td[role=gridcell]:first-child").each(function () {
            let totalCellWidth = 0;
            jQuery(this).children().each(function () {
                try {
                    const elementWidth = jQuery(this).width();
                    if (elementWidth) {
                        totalCellWidth += parseInt(elementWidth);
                    }
                } catch (err) {
                    // Skip as the width could not be retrieved.
                }
            });

            // Take left and right padding into account.
            totalCellWidth += parseInt(jQuery(this).css("paddingLeft")) + parseInt(jQuery(this).css("paddingRight"));
            if (totalCellWidth > maxCellWidth) {
                maxCellWidth = totalCellWidth;
            }
        });

        jQuery(treeTableDiv).find("table[role=treegrid] > thead > tr > th > span[class=ui-column-title]:first").each(function () {
            // Reset the values to their default.
            jQuery(this).css("white-space", "nowrap");
            this.textContent = this.textContent.trim();

            // Take left and right padding into account.
            const parentWidth = parseInt(jQuery(this).parent().css("width")) + parseInt(jQuery(this).parent().css("paddingLeft")) + parseInt(jQuery(this).parent().css("paddingRight"));
            if (maxCellWidth > parentWidth) {
                jQuery(this).css("white-space", "pre");
                let originalSpanWidth = parseInt(jQuery(this).width());
                const difference = Math.ceil(maxCellWidth - parentWidth);
                let updatedSpanWith = originalSpanWidth;
                let counter = 0;

                // To ensure the loop will not be endless.
                if (difference > 0 && !((updatedSpanWith - originalSpanWidth) < 0)) {
                    // Note: Adding whitespaces to the span content since setting the column's width has no effect.
                    const spacer = "                ";
                    while (difference > (updatedSpanWith - originalSpanWidth)) {
                        if (counter % 2 === 0) {
                            this.textContent = this.textContent.concat(spacer);
                        } else {
                            this.textContent = spacer.concat(this.textContent);
                        }
                        updatedSpanWith = parseInt(jQuery(this).width());
                        counter++;
                    }
                    this.textContent = this.textContent.concat(spacer);
                }
            }
            return false;
        });
    }
}

function setNavigateAway(classParameter) {
    jQuery(classParameter).each(function () {

        window.onbeforeunload = function (e) {
            if (hasChanged) {
                // If we haven't been passed the event get the window.event
                e = e || window.event;

                const message = "";

                // For IE6-8 and Firefox prior to version 4
                if (e) {
                    e.returnValue = message;
                }

                // For Chrome, Safari, IE8+ and Opera 12+
                return message;
            }
        };

        jQuery(this).change(function () {
            hasChanged = true;
            try {
                PF('projectSuggest').disable();
            } catch (err) {
                // not interested in this.
            }

            try {
                PF('orderSuggest').disable();
            } catch (err) {
                // not interested in this.
            }
        });
    });
}

function fixOversizeAndNavigateAway() {
    fixOversize();
    setNavigateAway(".navigate-away-class");
}

/**
 * Scrolls to the datascroller item for the given string.
 * The method returns the following tuple:
 * - [true, -1] if the datascroller item for the given string is found
 * - [false, allItems.length - 1] if the datascroller item for the given string was not found
 */
function scrollToDataScrollItem(startIndex, textString) {
    const allItems = $("li.ui-datascroller-item>div>div>span>span");
    let toScrollTo = null;
    const returnValue = [false, -1];

    for (let j = startIndex; j < allItems.length; ++j) {
        const tc = String(allItems.get(j).textContent);
        if (tc.includes(textString)) {
            toScrollTo = allItems.get(j);
            returnValue[0] = true;
            break;
        }
    }

    if (returnValue[0] && toScrollTo != null) {
        toScrollTo.scrollIntoView();
    } else {
        returnValue[1] = allItems.length - 1
    }

    return returnValue;
}

/**
 * Detect the datascroller items in the viewport and call the loadListener.
 */
function detectViewPort() {
    if (skipDetectViewPort == null || skipDetectViewPort === false) {
        let dataScrollerItems = document.getElementsByClassName("ui-datascroller-item");
        let indices = [];
        let indicesPinned = [];
        let indexCounter = 0;
        let indexCounterPinned = 0;
        let windowHeight = window.innerHeight;
        let windowWidth = window.innerWidth;
        let documentHeight = document.documentElement.clientHeight;
        let documentWidth = document.documentElement.clientWidth;
        for (let j = 0; j < dataScrollerItems.length; ++j) {
            const boundary = dataScrollerItems[j].getBoundingClientRect();
            const closest = jQuery(dataScrollerItems[j]).closest(".pinned");
            if (closest && closest.length === 1) {
                indexCounterPinned++;
                if (boundary.top + 10 >= 0 && boundary.left >= 0 && boundary.bottom <= (windowHeight || documentHeight) && boundary.right <= (windowWidth || documentWidth)) {
                    indicesPinned.push(j);
                }
            } else {
                indexCounter++;
                if (boundary.top + 10 >= 0 && boundary.left >= 0 && boundary.bottom <= (windowHeight || documentHeight) && boundary.right <= (windowWidth || documentWidth)) {
                    indices.push(j - indexCounterPinned);
                }
            }
        }
        loadListener([{name: 'viewedCommentIndices', value: indices}, {name: 'viewedCommentIndicesPinned', value: indicesPinned}]);
    }
}

/**
 * Register a scroll event handler for the comment datascroller.
 */
function scrollEventHandler() {
    $(window).scroll(function () {
        clearTimeout($.data(this, 'scrollTimer'));
        $.data(this, 'scrollTimer', setTimeout(function () {
            detectViewPort();
        }, 250));
    });
}

/**
 * Filter the datascroller items for the given widget and string.
 *
 * @param widgetVarName the widgetVarName
 * @param textString the textString
 */
function filterDataScrollItems(widgetVarName, textString) {
    skipDetectViewPort = true;
    PF('mpWait').show();
    const total = PF(widgetVarName).cfg.totalSize;
    let startIndex = 0;
    let startIndices = [startIndex];
    const delay = 100;
    const maxDelay = 8000;
    const highlightTimer = setInterval(function () {
        if (!PF(widgetVarName).allLoaded) {
            const returnValue = scrollToDataScrollItem(startIndex, textString);
            if (!returnValue[0] && returnValue[1] < total - 1) {
                // No match.
                startIndex = returnValue[1];
                if (!startIndices.includes(startIndex)) {
                    startIndices.push(startIndex);
                    PF(widgetVarName).load();
                }
            } else if (returnValue[0] || !(returnValue[1] < total - 1)) {
                // Either a match or all items loaded, but no match. In both cases, clear the interval.
                skipDetectViewPort = false;
                PF('mpWait').hide();
                clearInterval(highlightTimer);
            }
        } else {
            // All items already loaded.
            scrollToDataScrollItem(startIndex, textString);
            skipDetectViewPort = false;
            PF('mpWait').hide();
            clearInterval(highlightTimer);
        }
    }, delay);

    setTimeout(function () {
        skipDetectViewPort = false;
        PF('mpWait').hide();
        clearInterval(highlightTimer);
    }, maxDelay);
}

/* Called when the document is ready */
jQuery(document).ready(function () {
    fixAll();
    document.onkeydown = replaceEnterWithBlur;
    setNavigateAway(".navigate-away-class");
});

/* Called when the window is resized */
jQuery(window).resize(function () {
    fixAll();
});