/**
 * question-palette.js
 * 
 * Manages the question presentation, palette grid status, sequential navigation,
 * AJAX answer auto-saving, and exam submission confirmation.
 * 
 * Palette Color Rules:
 * - Green (answered): Student has selected an option.
 * - Amber (marked for review): Student flagged question for subsequent review.
 * - Grey (not visited / unvisited): Not yet answered or visited.
 */

const QuestionPalette = (function () {
    let config = {
        attemptId: null,
        totalQuestions: 0,
        saveUrl: '',
        submitUrl: '',
        submitFormId: 'examSubmitForm'
    };

    let currentIndex = 0; // 0-based question index
    let questionCards = [];
    let paletteButtons = [];
    let visitedSet = new Set();
    let saveTimeout = null;

    // CSRF credentials
    let csrfToken = '';
    let csrfHeader = '';

    /**
     * Retrieve CSRF meta headers injected by Thymeleaf layout.
     */
    function setupCsrf() {
        const tokenMeta = document.querySelector('meta[name="_csrf"]');
        const headerMeta = document.querySelector('meta[name="_csrf_header"]');
        if (tokenMeta && headerMeta) {
            csrfToken = tokenMeta.getAttribute('content');
            csrfHeader = headerMeta.getAttribute('content');
        }
    }

    /**
     * Display a specific question by index, hiding all other question cards.
     * @param {number} targetIndex - 0-based question index
     */
    function showQuestion(targetIndex) {
        if (targetIndex < 0 || targetIndex >= config.totalQuestions) return;

        currentIndex = targetIndex;
        visitedSet.add(currentIndex);

        // Toggle card visibility
        questionCards.forEach(function (card, idx) {
            if (idx === currentIndex) {
                card.classList.remove('d-none');
            } else {
                card.classList.add('d-none');
            }
        });

        // Update Prev / Next button states
        const prevBtn = document.getElementById('prevQuestionBtn');
        const nextBtn = document.getElementById('nextQuestionBtn');
        if (prevBtn) {
            prevBtn.disabled = (currentIndex === 0);
        }
        if (nextBtn) {
            if (currentIndex === config.totalQuestions - 1) {
                nextBtn.innerHTML = 'Review All <i class="bi bi-card-checklist ms-1"></i>';
            } else {
                nextBtn.innerHTML = 'Next Question <i class="bi bi-chevron-right ms-1"></i>';
            }
        }

        // Update active border highlight on current palette button
        paletteButtons.forEach(function (btn, idx) {
            if (idx === currentIndex) {
                btn.classList.add('active-palette-item');
            } else {
                btn.classList.remove('active-palette-item');
            }
        });

        // Update active review button text and state
        updateReviewButtonState();

        // Refresh palette visual colors
        updatePaletteColors();
    }

    /**
     * Determine the status of a question at a given index.
     * @param {number} idx - 0-based index
     * @returns {'review' | 'answered' | 'unvisited'}
     */
    function getQuestionStatus(idx) {
        const card = questionCards[idx];
        if (!card) return 'unvisited';

        const isMarked = card.getAttribute('data-marked-review') === 'true';
        const selectedRadio = card.querySelector('input[type="radio"]:checked');
        const isAnswered = (selectedRadio !== null);

        if (isMarked) {
            return 'review';
        } else if (isAnswered) {
            return 'answered';
        }
        return 'unvisited';
    }

    /**
     * Apply the color classes to all palette buttons.
     * Green = answered, Amber = marked for review, Grey = unvisited.
     */
    function updatePaletteColors() {
        paletteButtons.forEach(function (btn, idx) {
            const status = getQuestionStatus(idx);

            // Clean previous palette state classes
            btn.classList.remove(
                'palette-answered', 
                'palette-review', 
                'palette-unvisited',
                'bg-success', 
                'text-white', 
                'border-success',
                'bg-warning', 
                'text-dark', 
                'border-warning',
                'bg-light', 
                'text-secondary'
            );

            if (status === 'review') {
                // Amber = marked for review
                btn.classList.add('palette-review', 'bg-warning', 'text-dark', 'border-warning');
            } else if (status === 'answered') {
                // Green = answered
                btn.classList.add('palette-answered', 'bg-success', 'text-white', 'border-success');
            } else {
                // Grey = not visited or unanswered
                btn.classList.add('palette-unvisited', 'bg-light', 'text-secondary');
            }
        });

        // Recalculate summary counters
        updateSummaryCounters();
    }

    /**
     * Update the label and styling of the "Mark for Review" button for the current question.
     */
    function updateReviewButtonState() {
        const currentCard = questionCards[currentIndex];
        const reviewBtn = document.getElementById('markReviewBtn');
        if (!currentCard || !reviewBtn) return;

        const isMarked = currentCard.getAttribute('data-marked-review') === 'true';
        if (isMarked) {
            reviewBtn.classList.remove('btn-outline-warning');
            reviewBtn.classList.add('btn-warning', 'text-dark');
            reviewBtn.innerHTML = '<i class="bi bi-bookmark-check-fill me-1"></i> Marked for Review';
        } else {
            reviewBtn.classList.remove('btn-warning', 'text-dark');
            reviewBtn.classList.add('btn-outline-dark');
            reviewBtn.innerHTML = '<i class="bi bi-bookmark me-1"></i> Mark for Review';
        }
    }

    /**
     * Recalculate and display the answered, unanswered, and marked-for-review counts.
     */
    function updateSummaryCounters() {
        let answered = 0;
        let review = 0;
        let unanswered = 0;

        questionCards.forEach(function (card, idx) {
            const status = getQuestionStatus(idx);
            const isMarked = card.getAttribute('data-marked-review') === 'true';
            const selectedRadio = card.querySelector('input[type="radio"]:checked');

            if (selectedRadio !== null) {
                answered++;
            } else {
                unanswered++;
            }

            if (isMarked) {
                review++;
            }
        });

        // Update counts in palette header and submission modal
        setElementText('paletteAnsweredCount', answered);
        setElementText('paletteReviewCount', review);
        setElementText('paletteUnansweredCount', unanswered);

        setElementText('modalAnsweredCount', answered);
        setElementText('modalReviewCount', review);
        setElementText('modalUnansweredCount', unanswered);
        setElementText('modalTotalCount', config.totalQuestions);
    }

    function setElementText(id, text) {
        const el = document.getElementById(id);
        if (el) el.textContent = text;
    }

    /**
     * Display transient autosave indicator message.
     * @param {'saving' | 'saved' | 'error'} state 
     * @param {string} [customMsg]
     */
    function setAutosaveIndicator(state, customMsg) {
        const indicator = document.getElementById('autosaveIndicator');
        if (!indicator) return;

        indicator.classList.remove('text-secondary', 'text-success', 'text-danger');

        if (state === 'saving') {
            indicator.classList.add('text-secondary');
            indicator.innerHTML = '<span class="spinner-border spinner-border-sm me-1" role="status"></span> Saving...';
        } else if (state === 'saved') {
            indicator.classList.add('text-success');
            indicator.innerHTML = '<i class="bi bi-check2-circle me-1"></i> Saved';
        } else if (state === 'error') {
            indicator.classList.add('text-danger');
            indicator.innerHTML = `<i class="bi bi-exclamation-triangle-fill me-1"></i> ${customMsg || 'Save error'}`;
        }
    }

    /**
     * Dispatch an AJAX POST request to save or clear an answer on the server.
     * @param {number} questionId 
     * @param {number|null} selectedOptionId 
     * @param {boolean} markedForReview 
     */
    function sendAutosave(questionId, selectedOptionId, markedForReview) {
        setAutosaveIndicator('saving');

        const payload = {
            questionId: questionId,
            selectedOptionId: selectedOptionId,
            markedForReview: markedForReview
        };

        const headers = {
            'Content-Type': 'application/json'
        };
        if (csrfToken && csrfHeader) {
            headers[csrfHeader] = csrfToken;
        }

        fetch(config.saveUrl, {
            method: 'POST',
            headers: headers,
            body: JSON.stringify(payload)
        })
        .then(function (response) {
            return response.json();
        })
        .then(function (data) {
            if (data.success) {
                setAutosaveIndicator('saved');

                // Synchronize client countdown with remaining seconds from server
                if (typeof ExamTimer !== 'undefined' && typeof data.remainingSeconds === 'number') {
                    ExamTimer.syncServerTime(data.remainingSeconds);
                }
            } else if (data.timeExpired) {
                setAutosaveIndicator('error', 'Exam time expired');
                // Trigger auto-submit when server signals time expiration
                triggerSubmitForm();
            } else {
                setAutosaveIndicator('error', data.message || 'Error saving');
            }
        })
        .catch(function (err) {
            console.error('Autosave error:', err);
            setAutosaveIndicator('error', 'Network issue. Retrying...');
        });
    }

    /**
     * Handle user selection of a radio option.
     * @param {Event} event 
     */
    function handleOptionChange(event) {
        const radio = event.target;
        const currentCard = questionCards[currentIndex];
        if (!currentCard) return;

        const questionId = parseInt(currentCard.getAttribute('data-question-id'), 10);
        const selectedOptionId = parseInt(radio.value, 10);
        const isMarked = currentCard.getAttribute('data-marked-review') === 'true';

        updatePaletteColors();
        sendAutosave(questionId, selectedOptionId, isMarked);
    }

    /**
     * Clear selected radio button for the current question.
     */
    function clearCurrentAnswer() {
        const currentCard = questionCards[currentIndex];
        if (!currentCard) return;

        const radios = currentCard.querySelectorAll('input[type="radio"]');
        radios.forEach(r => r.checked = false);

        const questionId = parseInt(currentCard.getAttribute('data-question-id'), 10);
        const isMarked = currentCard.getAttribute('data-marked-review') === 'true';

        updatePaletteColors();
        sendAutosave(questionId, null, isMarked);
    }

    /**
     * Toggle the "Mark for Review" status of the current question.
     */
    function toggleMarkForReview() {
        const currentCard = questionCards[currentIndex];
        if (!currentCard) return;

        const wasMarked = currentCard.getAttribute('data-marked-review') === 'true';
        const willBeMarked = !wasMarked;
        currentCard.setAttribute('data-marked-review', willBeMarked ? 'true' : 'false');

        updateReviewButtonState();
        updatePaletteColors();

        const questionId = parseInt(currentCard.getAttribute('data-question-id'), 10);
        const selectedRadio = currentCard.querySelector('input[type="radio"]:checked');
        const selectedOptionId = selectedRadio ? parseInt(selectedRadio.value, 10) : null;

        sendAutosave(questionId, selectedOptionId, willBeMarked);
    }

    /**
     * Submit the exam form.
     */
    function triggerSubmitForm() {
        const form = document.getElementById(config.submitFormId);
        if (form) {
            form.submit();
        }
    }

    /**
     * Public Initialization API.
     * @param {Object} options
     */
    function init(options) {
        config = Object.assign(config, options);
        setupCsrf();

        questionCards = Array.from(document.querySelectorAll('.question-card'));
        paletteButtons = Array.from(document.querySelectorAll('.palette-btn'));
        config.totalQuestions = questionCards.length;

        // Attach radio button change handlers
        questionCards.forEach(function (card) {
            const radios = card.querySelectorAll('input[type="radio"]');
            radios.forEach(function (radio) {
                radio.addEventListener('change', handleOptionChange);
            });
        });

        // Attach palette button click listeners
        paletteButtons.forEach(function (btn, idx) {
            btn.addEventListener('click', function () {
                showQuestion(idx);
            });
        });

        // Previous button
        const prevBtn = document.getElementById('prevQuestionBtn');
        if (prevBtn) {
            prevBtn.addEventListener('click', function () {
                if (currentIndex > 0) showQuestion(currentIndex - 1);
            });
        }

        // Next button
        const nextBtn = document.getElementById('nextQuestionBtn');
        if (nextBtn) {
            nextBtn.addEventListener('click', function () {
                if (currentIndex < config.totalQuestions - 1) {
                    showQuestion(currentIndex + 1);
                } else {
                    // On last question, open submission confirmation modal
                    const modalEl = document.getElementById('submitConfirmModal');
                    if (modalEl && typeof bootstrap !== 'undefined') {
                        const modal = new bootstrap.Modal(modalEl);
                        modal.show();
                    }
                }
            });
        }

        // Clear Answer button
        const clearBtn = document.getElementById('clearAnswerBtn');
        if (clearBtn) {
            clearBtn.addEventListener('click', clearCurrentAnswer);
        }

        // Mark for Review button
        const reviewBtn = document.getElementById('markReviewBtn');
        if (reviewBtn) {
            reviewBtn.addEventListener('click', toggleMarkForReview);
        }

        // Final Confirm Submit button in modal
        const confirmSubmitBtn = document.getElementById('confirmSubmitModalBtn');
        if (confirmSubmitBtn) {
            confirmSubmitBtn.addEventListener('click', function () {
                confirmSubmitBtn.disabled = true;
                confirmSubmitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span> Submitting...';
                triggerSubmitForm();
            });
        }

        // Show initial question (Question 1)
        showQuestion(0);
    }

    return {
        init: init,
        showQuestion: showQuestion,
        triggerSubmitForm: triggerSubmitForm
    };
})();
