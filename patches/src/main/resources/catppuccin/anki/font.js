// SPDX-License-Identifier: GPL-3.0-or-later
// Only included when an AnkiCat font override is selected. Saved templates are untouched.
(() => {
    const family = "AnkiCatCustom";
    function apply(element) {
        if (!element || element.nodeType !== Node.ELEMENT_NODE || !element.style) return;
        if (element.style.getPropertyValue("font-family") !== family ||
            element.style.getPropertyPriority("font-family") !== "important") {
            element.style.setProperty("font-family", family, "important");
        }
    }
    function applyTree(element) {
        apply(element);
        if (element.querySelectorAll) element.querySelectorAll("*").forEach(apply);
    }
    function start() {
        applyTree(document.body);
        const observer = new MutationObserver(records => {
            for (const record of records) {
                if (record.type === "attributes") apply(record.target);
                else record.addedNodes.forEach(applyTree);
            }
        });
        observer.observe(document.body, {subtree: true, childList: true,
            attributes: true, attributeFilter: ["style"]});
        window.addEventListener("pagehide", () => observer.disconnect(), {once: true});
    }
    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", start, {once: true});
    } else start();
})();
