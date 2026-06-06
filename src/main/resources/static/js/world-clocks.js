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

    function updateCountdown() {
        const element = document.getElementById("tournament-countdown");
        if (!element || !element.dataset.kickoff) {
            return;
        }

        const kickoff = new Date(element.dataset.kickoff);
        let remainingMs = kickoff.getTime() - Date.now();

        if (Number.isNaN(kickoff.getTime())) {
            element.textContent = "—";
            return;
        }

        if (remainingMs <= 0) {
            element.textContent = "Tournament started!";
            return;
        }

        const days = Math.floor(remainingMs / 86400000);
        remainingMs %= 86400000;
        const hours = Math.floor(remainingMs / 3600000);
        remainingMs %= 3600000;
        const minutes = Math.floor(remainingMs / 60000);

        element.textContent = formatCountdownUnit(days, "day") + " "
            + formatCountdownUnit(hours, "hour") + " "
            + formatCountdownUnit(minutes, "minute");
    }

    updateClocks();
    updateCountdown();
    setInterval(updateClocks, 1000);
    setInterval(updateCountdown, 60000);
})();
