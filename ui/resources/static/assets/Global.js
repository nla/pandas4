try {
    const storedTheme = localStorage.getItem('pandas-theme');
    if (['light', 'light-warm', 'dark'].includes(storedTheme)) {
        document.documentElement.dataset.theme = storedTheme;
    } else if (storedTheme !== null) {
        console.warn('Ignoring invalid theme preference:', storedTheme);
    }
} catch (error) {
    console.warn('Theme preference could not be read:', error);
}

document.addEventListener('DOMContentLoaded', () => {
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

    const themeChoice = document.getElementById('theme-choice');
    if (themeChoice) {
        themeChoice.value = document.documentElement.dataset.theme || 'system';
        themeChoice.addEventListener('change', () => {
            if (themeChoice.value === 'system') {
                delete document.documentElement.dataset.theme;
            } else {
                document.documentElement.dataset.theme = themeChoice.value;
            }
            try {
                if (themeChoice.value === 'system') {
                    localStorage.removeItem('pandas-theme');
                } else {
                    localStorage.setItem('pandas-theme', themeChoice.value);
                }
            } catch (error) {
                console.warn('Theme preference could not be saved:', error);
            }
        });
    }

    // Split-button toggle menus
    document.querySelectorAll('.split-toggle').forEach(function (toggle) {
        let menu = toggle.nextElementSibling;
        if ((!menu || !menu.classList.contains('split-menu')) && toggle.parentElement) {
            menu = toggle.parentElement.nextElementSibling;
        }
        if (!menu || !menu.classList.contains('split-menu')) return;

        function closeMenu() {
            menu.style.display = "none";
            toggle.setAttribute("aria-expanded", "false");
        }

        toggle.addEventListener("click", function (e) {
            e.stopPropagation();
            const isOpen = menu.style.display === "block";
            if (isOpen) {
                closeMenu();
            } else {
                menu.style.display = "block";
                toggle.setAttribute("aria-expanded", "true");
            }
        });

        document.addEventListener("click", function () {
            closeMenu();
        });

        menu.addEventListener("click", function (e) {
            e.stopPropagation();
        });
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
});
