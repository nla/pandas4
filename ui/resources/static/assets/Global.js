document.addEventListener('keyup', ev => {
    if (ev.altKey || ev.ctrlKey || ev.metaKey || ev.shiftKey ||
        (ev.key !== "Escape" && (ev.target.tagName === 'INPUT' || ev.target.tagName === 'SELECT' || ev.target.tagName === 'TEXTAREA'))) {
        return;
    }
    const el = document.querySelector("[data-keybind='" + CSS.escape(ev.key) + "']");
    if (el) {
        if (el.tagName === 'A' || el.tagName === 'BUTTON') {
            el.click();
        } else {
            el.focus();
        }
    }
});