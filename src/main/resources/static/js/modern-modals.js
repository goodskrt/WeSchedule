/**
 * Modern Modals - WeSchedule
 * Système de modals modernes pour remplacer les alertes natives
 */

const ModernModal = {
    /**
     * Afficher un modal de succès
     */
    success: function(title, message, callback) {
        this.show({
            type: 'success',
            icon: 'fa-check-circle',
            title: title,
            message: message,
            buttons: [
                {
                    text: 'OK',
                    class: 'modal-btn-primary',
                    callback: callback
                }
            ]
        });
    },

    /**
     * Afficher un modal d'erreur
     */
    error: function(title, message, callback) {
        this.show({
            type: 'error',
            icon: 'fa-exclamation-circle',
            title: title,
            message: message,
            buttons: [
                {
                    text: 'OK',
                    class: 'modal-btn-danger',
                    callback: callback
                }
            ]
        });
    },

    /**
     * Afficher un modal d'avertissement
     */
    warning: function(title, message, callback) {
        this.show({
            type: 'warning',
            icon: 'fa-exclamation-triangle',
            title: title,
            message: message,
            buttons: [
                {
                    text: 'OK',
                    class: 'modal-btn-primary',
                    callback: callback
                }
            ]
        });
    },

    /**
     * Afficher un modal d'information
     */
    info: function(title, message, callback) {
        this.show({
            type: 'info',
            icon: 'fa-info-circle',
            title: title,
            message: message,
            buttons: [
                {
                    text: 'OK',
                    class: 'modal-btn-primary',
                    callback: callback
                }
            ]
        });
    },

    /**
     * Afficher un modal de confirmation
     */
    confirm: function(title, message, onConfirm, onCancel) {
        this.show({
            type: 'question',
            icon: 'fa-question-circle',
            title: title,
            message: message,
            buttons: [
                {
                    text: 'Annuler',
                    class: 'modal-btn-secondary',
                    callback: onCancel
                },
                {
                    text: 'Confirmer',
                    class: 'modal-btn-primary',
                    callback: onConfirm
                }
            ]
        });
    },

    /**
     * Afficher un modal de chargement
     */
    loading: function(message) {
        const html = `
            <div class="modal-overlay active" id="modernModalOverlay">
                <div class="modern-modal">
                    <div class="modern-modal-body">
                        <div class="modal-loading">
                            <div class="modal-spinner"></div>
                            <p class="modal-loading-text">${message || 'Chargement en cours...'}</p>
                        </div>
                    </div>
                </div>
            </div>
        `;
        
        document.body.insertAdjacentHTML('beforeend', html);
    },

    /**
     * Fermer le modal de chargement
     */
    closeLoading: function() {
        const overlay = document.getElementById('modernModalOverlay');
        if (overlay) {
            overlay.classList.remove('active');
            setTimeout(() => overlay.remove(), 300);
        }
    },

    /**
     * Afficher un modal personnalisé
     */
    show: function(options) {
        const {
            type = 'info',
            icon = 'fa-info-circle',
            title = 'Information',
            subtitle = '',
            message = '',
            buttons = [],
            closeOnOverlay = true
        } = options;

        // Créer le HTML du modal
        const buttonsHtml = buttons.map(btn => `
            <button class="modal-btn ${btn.class || 'modal-btn-secondary'}" data-action="${btn.text}">
                ${btn.icon ? `<i class="fas ${btn.icon}"></i>` : ''}
                ${btn.text}
            </button>
        `).join('');

        const html = `
            <div class="modal-overlay" id="modernModalOverlay">
                <div class="modern-modal">
                    <div class="modern-modal-header">
                        <div class="modal-icon ${type}">
                            <i class="fas ${icon}"></i>
                        </div>
                        <div class="modal-header-content">
                            <h3 class="modal-title">${title}</h3>
                            ${subtitle ? `<p class="modal-subtitle">${subtitle}</p>` : ''}
                        </div>
                    </div>
                    <div class="modern-modal-body">
                        <p class="modal-message">${message}</p>
                    </div>
                    <div class="modern-modal-footer">
                        ${buttonsHtml}
                    </div>
                </div>
            </div>
        `;

        // Ajouter au DOM
        document.body.insertAdjacentHTML('beforeend', html);

        // Récupérer les éléments
        const overlay = document.getElementById('modernModalOverlay');
        const modal = overlay.querySelector('.modern-modal');

        // Animer l'apparition
        setTimeout(() => overlay.classList.add('active'), 10);

        // Gérer les clics sur les boutons
        buttons.forEach((btn, index) => {
            const btnElement = overlay.querySelectorAll('.modal-btn')[index];
            btnElement.addEventListener('click', () => {
                this.close();
                if (btn.callback) {
                    btn.callback();
                }
            });
        });

        // Fermer en cliquant sur l'overlay
        if (closeOnOverlay) {
            overlay.addEventListener('click', (e) => {
                if (e.target === overlay) {
                    this.close();
                }
            });
        }

        // Fermer avec Escape
        const escapeHandler = (e) => {
            if (e.key === 'Escape') {
                this.close();
                document.removeEventListener('keydown', escapeHandler);
            }
        };
        document.addEventListener('keydown', escapeHandler);
    },

    /**
     * Fermer le modal actif
     */
    close: function() {
        const overlay = document.getElementById('modernModalOverlay');
        if (overlay) {
            overlay.classList.remove('active');
            setTimeout(() => overlay.remove(), 300);
        }
    }
};

/**
 * Remplacer les confirm() natifs par des modals modernes
 */
function modernConfirm(message, onConfirm, onCancel) {
    ModernModal.confirm(
        'Confirmation',
        message,
        onConfirm,
        onCancel
    );
    return false; // Empêcher l'action par défaut
}

/**
 * Remplacer les alert() natifs par des modals modernes
 */
function modernAlert(message, type = 'info') {
    const types = {
        'success': () => ModernModal.success('Succès', message),
        'error': () => ModernModal.error('Erreur', message),
        'warning': () => ModernModal.warning('Attention', message),
        'info': () => ModernModal.info('Information', message)
    };
    
    (types[type] || types['info'])();
}

// Exporter pour utilisation globale
window.ModernModal = ModernModal;
window.modernConfirm = modernConfirm;
window.modernAlert = modernAlert;
