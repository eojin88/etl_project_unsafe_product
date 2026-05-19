document.addEventListener('DOMContentLoaded', () => {
    const btnStart = document.getElementById('btn-start');
    const etlMode = document.getElementById('etl-mode');
    const logContainer = document.getElementById('log-container');
    const progressInner = document.getElementById('progress-inner');
    
    const summaryStatus = document.getElementById('summary-status');
    const summaryCount = document.getElementById('summary-count');
    const summaryTime = document.getElementById('summary-time');
    const summaryError = document.getElementById('summary-error');

    const btnSaveSchedule = document.getElementById('btn-save-schedule');
    const btnCancelSchedule = document.getElementById('btn-cancel-schedule');
    const scheduleTime = document.getElementById('schedule-time');

    let pollingInterval = null;

    // Add log to container
    function addLog(message) {
        const time = new Date().toLocaleTimeString();
        const logEntry = document.createElement('div');
        logEntry.textContent = `[${time}] ${message}`;
        logContainer.appendChild(logEntry);
        logContainer.scrollTop = logContainer.scrollHeight;
    }

    // Update summary section
    async function updateSummary() {
        try {
            const response = await fetch('/api/etl/summary');
            if (response.ok) {
                const data = await response.json();
                summaryStatus.textContent = data.status || '-';
                summaryCount.textContent = data.migratedCount || '0';
                summaryTime.textContent = data.endTime || '-';
                summaryError.textContent = data.errorMessage || 'None';
            }
        } catch (err) {
            console.error('Failed to fetch summary:', err);
        }
    }

    // Poll status from Node.js Gateway
    async function pollStatus() {
        try {
            const response = await fetch('/api/etl/status');
            if (response.ok) {
                const data = await response.json();
                
                // Update progress bar (mock logic or real if backend provides it)
                if (data.progress !== undefined) {
                    progressInner.style.width = `${data.progress}%`;
                }

                if (data.log) {
                    addLog(data.log);
                }

                if (data.status === 'SUCCESS' || data.status === 'FAIL') {
                    btnStart.disabled = false;
                    clearInterval(pollingInterval);
                    addLog(`Job finished with status: ${data.status}`);
                    updateSummary();
                }
            }
        } catch (err) {
            console.error('Polling error:', err);
        }
    }

    // Start ETL
    btnStart.addEventListener('click', async () => {
        const mode = etlMode.value;
        btnStart.disabled = true;
        progressInner.style.width = '0%';
        logContainer.innerHTML = '';
        addLog(`Starting ${mode} migration...`);

        try {
            const response = await fetch('/api/etl/run', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ mode })
            });

            if (response.ok) {
                addLog('Request sent successfully. Polling for updates...');
                pollingInterval = setInterval(pollStatus, 2000);
            } else {
                addLog('Failed to start ETL. Check backend logs.');
                btnStart.disabled = false;
            }
        } catch (err) {
            addLog(`Error: ${err.message}`);
            btnStart.disabled = false;
        }
    });

    // Save Schedule
    btnSaveSchedule.addEventListener('click', async () => {
        const time = scheduleTime.value;
        try {
            const response = await fetch('/api/etl/schedule', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ time })
            });
            if (response.ok) {
                alert(`Schedule saved: Daily at ${time}`);
            } else {
                alert('Failed to save schedule.');
            }
        } catch (err) {
            alert(`Error: ${err.message}`);
        }
    });

    // Cancel Schedule
    btnCancelSchedule.addEventListener('click', async () => {
        try {
            const response = await fetch('/api/etl/schedule', {
                method: 'DELETE'
            });
            if (response.ok) {
                alert('Schedule cancelled successfully.');
            } else {
                alert('Failed to cancel schedule.');
            }
        } catch (err) {
            alert(`Error: ${err.message}`);
        }
    });

    // Initial load
    updateSummary();
});
