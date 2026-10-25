/**
 * exam-timer.js
 * 
 * Manages the client-side countdown timer for active student examination sessions.
 * 
 * Features:
 * - Formats remaining time into HH:MM:SS or MM:SS.
 * - Dynamic color transitions:
 *     * Standard: Charcoal/dark (#212529)
 *     * < 5 minutes (300s): Amber warning (#b58105)
 *     * < 1 minute (60s): Red danger (#dc3545)
 * - Synchronizes with server-reported remaining time on AJAX autosave.
 * - Automatically locks the exam interface and triggers form submission when time expires.
 */

const ExamTimer = (function () {
    let timerInterval = null;
    let remainingSeconds = 0;
    let displayElement = null;
    let timerBadgeElement = null;
    let onTimeExpireCallback = null;
    let isExpired = false;

    /**
     * Format raw seconds into a zero-padded string (HH:MM:SS or MM:SS).
     * @param {number} totalSecs 
     * @returns {string} Formatted duration string
     */
    function formatTime(totalSecs) {
        if (totalSecs < 0) totalSecs = 0;
        const hours = Math.floor(totalSecs / 3600);
        const minutes = Math.floor((totalSecs % 3600) / 60);
        const seconds = totalSecs % 60;

        const pad = (num) => String(num).padStart(2, '0');

        if (hours > 0) {
            return `${pad(hours)}:${pad(minutes)}:${pad(seconds)}`;
        }
        return `${pad(minutes)}:${pad(seconds)}`;
    }

    /**
     * Update the visual styling of the timer based on remaining seconds.
     * Thresholds:
     * - Under 5 minutes (300s): Amber warning state
     * - Under 1 minute (60s): Red danger state
     */
    function updateVisualClasses() {
        if (!displayElement) return;

        // Reset threshold styling classes
        displayElement.classList.remove('text-warning', 'text-danger');
        if (timerBadgeElement) {
            timerBadgeElement.classList.remove('border-warning', 'border-danger', 'bg-warning-subtle', 'bg-danger-subtle');
        }

        if (remainingSeconds <= 60) {
            // Critical warning: under 1 minute
            displayElement.classList.add('text-danger');
            if (timerBadgeElement) {
                timerBadgeElement.classList.add('border-danger', 'bg-danger-subtle');
            }
        } else if (remainingSeconds <= 300) {
            // Early warning: under 5 minutes
            displayElement.classList.add('text-warning');
            if (timerBadgeElement) {
                timerBadgeElement.classList.add('border-warning', 'bg-warning-subtle');
            }
        }
    }

    /**
     * Decrement the timer by 1 second and handle expiration.
     */
    function tick() {
        if (remainingSeconds <= 0) {
            handleExpiration();
            return;
        }

        remainingSeconds--;
        displayElement.textContent = formatTime(remainingSeconds);
        updateVisualClasses();

        if (remainingSeconds <= 0) {
            handleExpiration();
        }
    }

    /**
     * Handle the time expiration event when the timer reaches 0.
     */
    function handleExpiration() {
        if (isExpired) return;
        isExpired = true;

        clearInterval(timerInterval);
        displayElement.textContent = "00:00";
        updateVisualClasses();

        // Notify user and execute expiration callback (auto-submission)
        if (typeof onTimeExpireCallback === 'function') {
            onTimeExpireCallback();
        }
    }

    /**
     * Public Initialization API.
     * @param {Object} config
     * @param {number} config.initialSeconds - Starting duration in seconds from server
     * @param {string} config.displayId - ID of element rendering countdown text
     * @param {string} [config.badgeId] - Optional ID of wrapping badge container
     * @param {Function} [config.onExpire] - Callback to execute when time hits zero
     */
    function init(config) {
        remainingSeconds = Math.max(0, parseInt(config.initialSeconds, 10) || 0);
        displayElement = document.getElementById(config.displayId);
        timerBadgeElement = config.badgeId ? document.getElementById(config.badgeId) : null;
        onTimeExpireCallback = config.onExpire || null;
        isExpired = false;

        if (!displayElement) {
            console.error('ExamTimer: Display element not found for id', config.displayId);
            return;
        }

        // Render initial state
        displayElement.textContent = formatTime(remainingSeconds);
        updateVisualClasses();

        if (remainingSeconds <= 0) {
            handleExpiration();
            return;
        }

        // Start 1-second interval ticker
        if (timerInterval) clearInterval(timerInterval);
        timerInterval = setInterval(tick, 1000);
    }

    /**
     * Synchronize remaining time with server response from AJAX autosave.
     * Prevents client drift while preventing accidental extensions.
     * @param {number} serverSeconds 
     */
    function syncServerTime(serverSeconds) {
        if (typeof serverSeconds === 'number' && serverSeconds >= 0) {
            remainingSeconds = serverSeconds;
            if (displayElement) {
                displayElement.textContent = formatTime(remainingSeconds);
                updateVisualClasses();
            }
            if (remainingSeconds <= 0) {
                handleExpiration();
            }
        }
    }

    /**
     * Stop the timer interval.
     */
    function stop() {
        if (timerInterval) {
            clearInterval(timerInterval);
            timerInterval = null;
        }
    }

    return {
        init: init,
        syncServerTime: syncServerTime,
        stop: stop,
        getRemainingSeconds: function () { return remainingSeconds; }
    };
})();
