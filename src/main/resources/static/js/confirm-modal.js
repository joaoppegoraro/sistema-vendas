(function () {
  'use strict';

  var pendingForm = null;

  function openModal(message, form) {
    document.getElementById('confirm-modal-message').textContent = message;
    document.getElementById('confirm-modal-overlay').hidden = false;
    pendingForm = form;
  }

  function closeModal() {
    document.getElementById('confirm-modal-overlay').hidden = true;
    pendingForm = null;
  }

  document.addEventListener('DOMContentLoaded', function () {
    var overlay = document.getElementById('confirm-modal-overlay');
    var cancelButton = document.getElementById('confirm-modal-cancel');
    var confirmButton = document.getElementById('confirm-modal-confirm');
    if (!overlay || !cancelButton || !confirmButton) {
      return;
    }

    cancelButton.addEventListener('click', closeModal);
    overlay.addEventListener('click', function (event) {
      if (event.target === overlay) {
        closeModal();
      }
    });
    document.addEventListener('keydown', function (event) {
      if (event.key === 'Escape' && !overlay.hidden) {
        closeModal();
      }
    });
    confirmButton.addEventListener('click', function () {
      var form = pendingForm;
      closeModal();
      if (form) {
        form.submit();
      }
    });

    document.addEventListener('submit', function (event) {
      var form = event.target;
      if (form.hasAttribute('data-confirm') && !form.dataset.confirmed) {
        event.preventDefault();
        openModal(form.getAttribute('data-confirm'), form);
      }
    });
  });
})();
