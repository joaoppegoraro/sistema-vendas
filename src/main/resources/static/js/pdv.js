(function () {
  'use strict';

  var currencyFormatter = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' });

  function parseDecimal(value) {
    var parsed = parseFloat(value);
    return isNaN(parsed) ? 0 : parsed;
  }

  function recomputeTotal() {
    var cartSection = document.getElementById('cart-section');
    var totalDisplay = document.getElementById('totalDisplay');
    if (!cartSection || !totalDisplay) {
      return;
    }
    var subtotal = parseDecimal(cartSection.getAttribute('data-subtotal'));
    var discount = parseDecimal(document.getElementById('discount').value);
    var surcharge = parseDecimal(document.getElementById('surcharge').value);
    var total = Math.max(0, subtotal - discount + surcharge);
    totalDisplay.textContent = currencyFormatter.format(total);
  }

  function filterVariants() {
    var search = document.getElementById('variantSearch');
    var select = document.getElementById('variantSelect');
    if (!search || !select) {
      return;
    }
    var term = search.value.trim().toLowerCase();
    Array.prototype.forEach.call(select.options, function (option) {
      var matches = !term || (option.getAttribute('data-search') || '').indexOf(term) !== -1;
      option.style.display = matches ? '' : 'none';
    });
  }

  function replaceCartSection(html) {
    var wrapper = document.createElement('div');
    wrapper.innerHTML = html.trim();
    var newSection = wrapper.querySelector('#cart-section');
    var currentSection = document.getElementById('cart-section');
    if (newSection && currentSection) {
      currentSection.replaceWith(newSection);
    }
    recomputeTotal();
  }

  function addToCart() {
    var select = document.getElementById('variantSelect');
    var quantityInput = document.getElementById('variantQuantity');
    var errorEl = document.getElementById('cartActionError');
    if (!select || !select.value) {
      errorEl.textContent = 'Selecione um produto para adicionar';
      return;
    }
    errorEl.textContent = '';

    var params = new URLSearchParams();
    params.append('variantId', select.value);
    params.append('quantity', quantityInput.value || '1');

    fetch('/sales/cart/items', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: params.toString()
    })
      .then(function (res) { return res.text(); })
      .then(replaceCartSection)
      .catch(function () { errorEl.textContent = 'Não foi possível adicionar o item, tente novamente'; });
  }

  function removeFromCart(variantId) {
    fetch('/sales/cart/items/' + variantId + '/remove', { method: 'POST' })
      .then(function (res) { return res.text(); })
      .then(replaceCartSection);
  }

  document.addEventListener('DOMContentLoaded', function () {
    var addButton = document.getElementById('addToCartButton');
    if (addButton) {
      addButton.addEventListener('click', addToCart);
    }

    var search = document.getElementById('variantSearch');
    if (search) {
      search.addEventListener('input', filterVariants);
    }

    var discount = document.getElementById('discount');
    var surcharge = document.getElementById('surcharge');
    if (discount) {
      discount.addEventListener('input', recomputeTotal);
    }
    if (surcharge) {
      surcharge.addEventListener('input', recomputeTotal);
    }

    document.addEventListener('click', function (event) {
      var button = event.target.closest('.remove-cart-item');
      if (button) {
        removeFromCart(button.getAttribute('data-variant-id'));
      }
    });

    recomputeTotal();
  });
})();
