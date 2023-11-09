export {};
import SlimSelect from "slim-select";

declare var collectionId: number | null;
declare var collectionsEndpoint: string;

const inheritSubjectsCheckbox = document.getElementById('inheritSubjects') as HTMLInputElement;
const parentSelect = document.getElementById('parent') as HTMLSelectElement;
const subjectsSelect = document.getElementById('subjects') as HTMLSelectElement;

const parentSlimSelect = new SlimSelect({
    select: '#parent',
    settings: {
        allowDeselect: true,
        hideSelected: true,
        placeholderText: ''
    },
    events: {
        searchFilter: function (option, search) {
            return true; // leave filtering to backend
        },
        search: function (search: string, _) {
            return new Promise((resolve, reject) => {
                if (!search) return reject(false);
                fetch(collectionsEndpoint + "?q=" + encodeURIComponent(search) + "&size=100")
                    .then(response => response.json())
                    .then(results => resolve(results.filter(c => c.id !== collectionId)
                        .map(collection => ({
                            value: collection.id,
                            text: collection.fullName,
                            data: {
                                subjects: collection.inheritedSubjects.map(subject => typeof subject === 'number' ? subject : subject.id),
                            }
                        }))))
                    .catch(reject);
            });
        },
    }
});

let subjectsSlimSelect = new SlimSelect({
    select: '#subjects',
    settings: {
        hideSelected: true,
        placeholderText: ''
    },
    events: {
        searchFilter: function (option, search) {
            return option.data['fullname'].toLowerCase().indexOf(search.toLowerCase()) !== -1;
        },
    }
});

parentSelect.addEventListener('change', function () {
    if (parentSelect.value) {
        inheritSubjectsCheckbox.parentElement.classList.remove('hidden-input');
        if (subjectsSelect.selectedOptions.length === 0) {
            inheritSubjectsCheckbox.checked = true;
        }
        inheritSubjectsCheckbox.dispatchEvent(new Event('change'));
    } else {
        inheritSubjectsCheckbox.parentElement.classList.add('hidden-input');
        subjectsSlimSelect.enable();
    }
});

// Disable the subjects select if the inherit checkbox is checked
inheritSubjectsCheckbox.addEventListener('change', function () {
    subjectsSelect.disabled = this.checked;
    if (this.checked) {
        subjectsSlimSelect.disable();
        let selectedParentOptions = parentSelect.selectedOptions;
        if (selectedParentOptions.length > 0) {
            let selectedParentOption = selectedParentOptions[0];
            let parentSubjectIds = selectedParentOption.dataset['subjects'].split(',');
            subjectsSlimSelect.setSelected(parentSubjectIds);
        }
    } else {
        subjectsSlimSelect.enable();
    }
});

parentSelect.dispatchEvent(new Event('change'));