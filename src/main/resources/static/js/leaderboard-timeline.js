(function () {
    const canvas = document.getElementById('leaderboard-timeline-chart');
    const payload = window.leaderboardTimeline;
    if (!canvas || typeof Chart === 'undefined' || !payload) {
        return;
    }

    const labels = payload.labels || [];
    const series = payload.series || [];
    if (!labels.length || !series.length) {
        return;
    }

    const datasets = series.map(function (entry) {
        return {
            label: entry.label,
            data: entry.data,
            borderColor: entry.color,
            backgroundColor: entry.color,
            pointRadius: labels.length > 20 ? 0 : 2,
            pointHoverRadius: 4,
            borderWidth: 2,
            tension: 0.2,
            fill: false
        };
    });

    new Chart(canvas, {
        type: 'line',
        data: {
            labels: labels,
            datasets: datasets
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            interaction: {
                mode: 'nearest',
                intersect: false
            },
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        boxWidth: 12,
                        padding: 14
                    }
                },
                tooltip: {
                    callbacks: {
                        label: function (context) {
                            return context.dataset.label + ': ' + context.parsed.y + ' pts';
                        }
                    }
                }
            },
            scales: {
                x: {
                    ticks: {
                        maxRotation: 0,
                        autoSkip: true,
                        maxTicksLimit: 12
                    },
                    grid: {
                        display: false
                    }
                },
                y: {
                    beginAtZero: true,
                    ticks: {
                        precision: 0
                    },
                    title: {
                        display: true,
                        text: 'Cumulative points'
                    }
                }
            }
        }
    });
})();
