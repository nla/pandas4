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

document.querySelectorAll('.title-flag').forEach(function (flagButton) {
   flagButton.addEventListener('click', function(event) {
       let formData = new FormData();
       formData.append("_csrf", document.querySelector("input[name='_csrf']").value);
       formData.append("redirect", false);
       let oldFormAction = flagButton.formAction;
       fetch(oldFormAction, {method: 'POST', body: formData})
           .then(r => console.log("Toggled " + oldFormAction));

       // toggle all the flags for this title (the title may be displayed in multiple places on the same screen)
       let active = !flagButton.classList.contains('active');
       let newFormAction = oldFormAction.replace(/[^/]*$/, '') + (active ? 'unflag' : 'flag');
       document.querySelectorAll('.title-flag[formaction="' + flagButton.getAttribute('formaction') + '"]')
           .forEach(button => {
               if (active) {
                   button.classList.add('active');
               } else {
                   button.classList.remove('active');
               }
               button.formAction = newFormAction;
           });

       event.preventDefault();
       return false;
   });
});