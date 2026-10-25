/**
 * Online Examination Management System - Client Scripts
 * Handles UX enhancements, button loading states, and responsive alerts.
 */
document.addEventListener('DOMContentLoaded', function () {
    // Attach loading spinners to submit buttons on form submission
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function (e) {
            if (form.checkValidity && !form.checkValidity()) {
                return; // Let standard HTML5 validation display errors
            }

            const submitBtn = form.querySelector('button[type="submit"]');
            if (submitBtn && !submitBtn.classList.contains('no-loading')) {
                submitBtn.classList.add('btn-loading');
                const originalText = submitBtn.innerHTML;
                submitBtn.setAttribute('data-original-text', originalText);
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status" aria-hidden="true"></span> Processing...';
            }
        });
    });

    // Auto-dismissible alert fade out (optional dismissal after 6s)
    const alerts = document.querySelectorAll('.alert-dismissible');
    alerts.forEach(alertEl => {
        setTimeout(() => {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alertEl);
            if (bsAlert) {
                bsAlert.close();
            }
        }, 6000);
    });
});

