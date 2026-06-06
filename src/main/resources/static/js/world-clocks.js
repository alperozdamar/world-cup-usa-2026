(function () {
    const clocks = [
        { id: "ankara", zone: "Europe/Istanbul" },
        { id: "amsterdam", zone: "Europe/Amsterdam" },
        { id: "new-york", zone: "America/New_York" },
        { id: "los-angeles", zone: "America/Los_Angeles" }
    ];

    function updateClocks() {
        const now = new Date();
        for (const clock of clocks) {
            const element = document.getElementById("clock-" + clock.id);
            if (!element) {
                continue;
            }
            element.textContent = new Intl.DateTimeFormat("en-US", {
                hour: "2-digit",
                minute: "2-digit",
                second: "2-digit",
                hour12: false,
                timeZone: clock.zone
            }).format(now);
        }
    }

    function formatCountdownUnit(value, singular) {
        return value + " " + (value === 1 ? singular : singular + "s");
    }

    function formatRemainingTime(remainingMs) {
        if (remainingMs <= 0) {
            return null;
        }

        let ms = remainingMs;
        const days = Math.floor(ms / 86400000);
        ms %= 86400000;
        const hours = Math.floor(ms / 3600000);
        ms %= 3600000;
        const minutes = Math.floor(ms / 60000);

        return formatCountdownUnit(days, "day") + " "
            + formatCountdownUnit(hours, "hour") + " "
            + formatCountdownUnit(minutes, "minute");
    }

    function updateCountdownElement(element, startedLabel) {
        if (!element || !element.dataset.kickoff) {
            return;
        }

        const kickoff = new Date(element.dataset.kickoff);
        if (Number.isNaN(kickoff.getTime())) {
            element.textContent = "—";
            return;
        }

        const label = formatRemainingTime(kickoff.getTime() - Date.now());
        element.textContent = label != null ? label : (startedLabel || "Started");
    }

    function updateCountdown() {
        updateCountdownElement(document.getElementById("tournament-countdown"), "Tournament started!");
    }

    function updateMatchLockCountdowns() {
        document.querySelectorAll(".match-lock-countdown").forEach(function (element) {
            updateCountdownElement(element, "Started");
        });
    }

    function refreshAll() {
        updateClocks();
        updateCountdown();
        updateMatchLockCountdowns();
    }

    refreshAll();
    setInterval(updateClocks, 1000);
    setInterval(function () {
        updateCountdown();
        updateMatchLockCountdowns();
    }, 60000);
})();
