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

    updateClocks();
    setInterval(updateClocks, 1000);
})();
