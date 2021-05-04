document.addEventListener('keyup', ev => {
    if (ev.metaKey ||
        (ev.key !== "Escape" && (ev.target.tagName === 'INPUT' || ev.target.tagName === 'SELECT' || ev.target.tagName === 'TEXTAREA'))) {
        return;
    }
    let modifiers = "";
    if (ev.ctrlKey) modifiers += "Ctrl+";
    if (ev.altKey) modifiers += "Alt+";
    if (ev.shiftKey) modifiers += "Shift+";
    const el = document.querySelector("[data-keybind='" + CSS.escape(modifiers + ev.key) + "']");
    if (el) {
        if (el.tagName === 'A' || el.tagName === 'BUTTON') {
            el.click();
        } else {
            el.focus();
        }
    }
});