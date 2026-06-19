(function () {
    function isDraw(homeInput, awayInput) {
        const home = homeInput.value.trim();
        const away = awayInput.value.trim();
        if (home === '' || away === '') {
            return false;
        }
        return Number(home) === Number(away);
    }

    function leadingTeamId(form, homeInput, awayInput) {
        const home = Number(homeInput.value);
        const away = Number(awayInput.value);
        if (home > away) {
            return form.dataset.homeTeamId;
        }
        if (away > home) {
            return form.dataset.awayTeamId;
        }
        return null;
    }

    function leadingTeamName(form, homeInput, awayInput) {
        const home = Number(homeInput.value);
        const away = Number(awayInput.value);
        if (home > away) {
            return form.dataset.homeTeamName;
        }
        if (away > home) {
            return form.dataset.awayTeamName;
        }
        return null;
    }

    function setSectionEnabled(section, enabled) {
        if (!section) {
            return;
        }
        section.querySelectorAll('input, select, textarea, button').forEach(function (element) {
            element.disabled = !enabled;
        });
    }

    function syncForm(form) {
        const homeInput = form.querySelector('.knockout-score-home');
        const awayInput = form.querySelector('.knockout-score-away');
        const drawFields = form.querySelector('.knockout-draw-fields');
        const autoAdvance = form.querySelector('.knockout-auto-advance');
        const autoAdvanceId = form.querySelector('.knockout-auto-advance-id');
        const autoAdvanceLabel = form.querySelector('.knockout-auto-advance-label');
        const saveButton = form.querySelector('button[type="submit"]');

        if (!homeInput || !awayInput || !drawFields || !autoAdvance) {
            return;
        }

        if (isDraw(homeInput, awayInput)) {
            drawFields.classList.remove('d-none');
            autoAdvance.classList.add('d-none');
            setSectionEnabled(drawFields, true);
            setSectionEnabled(autoAdvance, false);
            if (autoAdvanceId) {
                autoAdvanceId.value = '';
            }
            if (saveButton) {
                saveButton.disabled = false;
            }
            return;
        }

        drawFields.classList.add('d-none');
        autoAdvance.classList.remove('d-none');
        setSectionEnabled(drawFields, false);
        setSectionEnabled(autoAdvance, true);

        const winnerId = leadingTeamId(form, homeInput, awayInput);
        const winnerName = leadingTeamName(form, homeInput, awayInput);
        if (autoAdvanceId && winnerId) {
            autoAdvanceId.value = winnerId;
        }
        if (autoAdvanceLabel && winnerName) {
            autoAdvanceLabel.textContent = winnerName;
        }
        if (saveButton) {
            saveButton.disabled = !winnerId;
        }
    }

    function init() {
        document.querySelectorAll('.knockout-pick-form').forEach(function (form) {
            const homeInput = form.querySelector('.knockout-score-home');
            const awayInput = form.querySelector('.knockout-score-away');
            syncForm(form);

            [homeInput, awayInput].forEach(function (input) {
                if (!input) {
                    return;
                }
                input.addEventListener('input', function () {
                    syncForm(form);
                });
            });
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }
})();
