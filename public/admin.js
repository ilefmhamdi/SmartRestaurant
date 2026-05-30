const adminApiBase = '/api/admin';
const adminTokenKey = 'noir-admin-token';

const state = {
  token: localStorage.getItem(adminTokenKey) || '',
  menuEditId: null,
  orderEditId: null,
  promotionEditId: null,
  ingredientEditId: null,
  tableEditId: null,
  staffEditId: null,
  menu: [],
  ingredients: [],
  tables: [],
  staff: [],
  reservations: [],
  orders: [],
  reviews: [],
  promotions: [],
  subscribers: [],
  currentView: 'home',
};

function escapeHtml(value) {
  return String(value ?? '').replace(/[&<>"']/g, (char) => ({
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#39;',
  }[char]));
}

function showToast(message, type = 'success') {
  const stack = document.getElementById('toastStack');
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  toast.textContent = message;
  stack.appendChild(toast);
  setTimeout(() => toast.classList.add('show'), 10);
  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 250);
  }, 2600);
}

async function adminFetch(path, options = {}) {
  const config = { ...options };
  config.headers = {
    ...(options.headers || {}),
  };

  if (state.token) {
    config.headers.Authorization = `Bearer ${state.token}`;
  }
  if (config.body && !config.headers['Content-Type']) {
    config.headers['Content-Type'] = 'application/json';
  }

  const response = await fetch(`${adminApiBase}${path}`, config);
  const payload = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(payload.message || 'Admin request failed');
  }
  return payload.data;
}

function setLoggedIn(isLoggedIn) {
  document.getElementById('loginView').classList.toggle('hidden', isLoggedIn);
  document.getElementById('adminView').classList.toggle('hidden', !isLoggedIn);
}

function setActiveView(viewName) {
  state.currentView = viewName;
  document.querySelectorAll('.view').forEach((view) => {
    view.classList.toggle('active', view.id === `view-${viewName}`);
  });
  document.querySelectorAll('.nav-btn').forEach((button) => {
    button.classList.toggle('active', button.dataset.viewTarget === viewName);
  });
  // Re-fill allergen select when switching to allergens view
  if (viewName === 'allergens') {
    _fillAllergenSelect();
    setTimeout(_fillAllergenSelect, 100);
  }
  // Load admin profile when navigating to account view
  if (viewName === 'account') {
    loadAdminProfile();
  }
}

function renderStats(overview) {
  const entries = [
    ['Menu Items', overview.stats.menuItems],
    ['Reservations', overview.stats.reservations],
    ['Orders', overview.stats.orders],
    ['Reviews', overview.stats.reviews],
    ['Subscribers', overview.stats.subscribers],
    ['Promotions', overview.stats.promotions || 0],
  ];

  document.getElementById('statsGrid').innerHTML = entries.map(([label, value]) => `
    <div class="stat">
      <div class="num">${value}</div>
      <div class="label">${label}</div>
    </div>
  `).join('');
}

function renderHome(overview) {
  document.getElementById('homeReservations').innerHTML = overview.recentReservations.length
    ? overview.recentReservations.map((item) => `
      <div class="detail-card">
        <strong>${escapeHtml(item.firstName)} ${escapeHtml(item.lastName)}</strong>
        <div class="muted">${escapeHtml(item.date)} at ${escapeHtml(item.time)} · ${item.guests} guests</div>
        <div class="muted">${escapeHtml(item.emailMasked)} · ${escapeHtml(item.phoneMasked)}</div>
      </div>
    `).join('')
    : '<div class="empty">No recent reservations.</div>';

  document.getElementById('homeOrders').innerHTML = overview.recentOrders.length
    ? overview.recentOrders.map((item) => `
      <div class="detail-card">
        <strong>${escapeHtml(item.orderNumber)}</strong>
        <div class="muted">${escapeHtml(item.status)} · ${escapeHtml(item.eta)}</div>
        <div class="muted">Client: ${escapeHtml(item.customerName || 'Unknown')} (${escapeHtml(item.customerEmail || 'N/A')})</div>
        <div class="muted">${item.items.length} items · ${item.total} TND</div>
      </div>
    `).join('')
    : '<div class="empty">No recent orders.</div>';

  document.getElementById('homeReviews').innerHTML = overview.recentReviews.length
    ? overview.recentReviews.map((item) => `
      <div class="detail-card">
        <strong>${escapeHtml(item.name)}</strong>
        <div class="muted">${escapeHtml(item.platform)} · ${item.rating}/5</div>
        <div class="muted">Client: ${escapeHtml(item.clientName || 'Guest')} (${escapeHtml(item.clientEmail || 'N/A')})</div>
        <div class="sub" style="margin-top:8px">${escapeHtml(item.text).slice(0, 120)}${item.text.length > 120 ? '...' : ''}</div>
      </div>
    `).join('')
    : '<div class="empty">No recent reviews.</div>';
}

function renderMenu(items) {
  state.menu = items;
  renderMenuCards(items);
  const tbody = document.getElementById('menuTable');
  tbody.innerHTML = items.map((item) => `
    <tr>
      <td>
        <div style="display:flex;gap:10px;align-items:flex-start">
          <img src="${escapeHtml(item.image || '')}" alt="${escapeHtml(item.name)}" style="width:46px;height:46px;object-fit:cover;border-radius:8px;border:0.5px solid var(--border)">
          <div><strong>${escapeHtml(item.name)}</strong><br><span class="muted">${escapeHtml((item.badges || []).join(', ') || 'No badges')}</span></div>
        </div>
      </td>
      <td>${escapeHtml(item.cat)}</td>
      <td>${item.price} TND</td>
      <td>${item.rating} (${item.reviews})</td>
      <td>
        <div class="actions">
          <button class="btn-ghost btn-small" type="button" onclick="startMenuEditById(${item.id})">Edit</button>
          <button class="btn-danger btn-small" type="button" onclick="deleteMenuItem(${item.id})">Delete</button>
        </div>
      </td>
    </tr>
  `).join('') || `<tr><td colspan="5" class="muted">No dishes found.</td></tr>`;
}

function renderMenuCards(items) {
  const grid = document.getElementById('menuCardGrid');
  if (!grid) return;

  grid.innerHTML = items.length ? items.map((item) => `
    <div class="panel card plate-card" style="padding:18px;">
      <div style="display:flex;gap:12px;align-items:flex-start;">
        <img src="${escapeHtml(item.image || '')}" alt="${escapeHtml(item.name)}" style="width:120px;height:120px;object-fit:cover;border-radius:10px;border:0.5px solid var(--border)">
        <div style="flex:1;">
          <div style="display:flex;justify-content:space-between;align-items:start;gap:12px;">
            <div>
              <strong style="font-size:1.1rem;">${escapeHtml(item.name)}</strong>
              <div class="muted" style="margin-top:6px;">${escapeHtml(item.cat)} · ${item.price} TND</div>
            </div>
            <div style="text-align:right;">
              <div class="badge">${item.veg ? 'Vegetarian' : 'Main'}</div>
            </div>
          </div>
          <p class="muted" style="margin:12px 0 8px;line-height:1.5;">${escapeHtml(item.desc || 'No description available.')}</p>
          <div style="display:flex;gap:10px;flex-wrap:wrap;align-items:center;">
            ${(item.badges || []).map((badge) => `<span class="badge">${escapeHtml(badge)}</span>`).join('')}
          </div>
        </div>
      </div>
    </div>
  `).join('') : '<div class="empty">No dishes available to display.</div>';
}

function renderReservations(items) {
  state.reservations = items;
  const tbody = document.getElementById('reservationsTable');
  tbody.innerHTML = items.length ? items.map((item) => `
    <tr>
      <td>${escapeHtml(item.firstName)} ${escapeHtml(item.lastName)}</td>
      <td>${escapeHtml(item.date)}<br><span class="muted">${escapeHtml(item.time)}</span></td>
      <td>${item.guests}</td>
      <td>${escapeHtml(item.emailMasked)}<br>${escapeHtml(item.phoneMasked)}</td>
      <td>${escapeHtml(item.status || 'confirmed')}</td>
      <td>
        <div class="actions">
          <button class="btn-ghost btn-small" type="button" onclick="loadReservationDetail('${item.id}')">Inspect</button>
          <button class="btn-danger btn-small" type="button" onclick="deleteReservation('${item.id}')">Delete</button>
        </div>
      </td>
    </tr>
  `).join('') : `<tr><td colspan="6" class="muted">No reservations yet.</td></tr>`;
}

function renderReservationDetail(item) {
  document.getElementById('reservationDetail').innerHTML = `
    <div class="detail-card stack">
      <div><span class="badge">Client data revealed intentionally</span></div>
      <div><strong>${escapeHtml(item.firstName)} ${escapeHtml(item.lastName)}</strong><div class="muted">${escapeHtml(item.email)} - ${escapeHtml(item.phone)}</div></div>
      <div class="muted">Client ID: ${escapeHtml(item.clientId || 'Guest')}</div>
      <div class="muted">Date: ${escapeHtml(item.date)} at ${escapeHtml(item.time)} - Guests: ${item.guests}</div>
      <div class="muted">Experience: ${escapeHtml(item.experience)}</div>
      <div class="muted">Menu selections: ${escapeHtml((item.menuSelections || []).join(', ') || 'None')}</div>
      <div class="muted">Allergies: ${escapeHtml((item.allergySelections || []).join(', ') || 'None')}</div>
      <div class="muted">Special requests: ${escapeHtml(item.specialRequests || 'None')}</div>
      <div class="muted">Status: ${escapeHtml(item.status || 'confirmed')}</div>
      <div class="field">
        <label>Update Status</label>
        <input id="reservationStatusInput" class="input" value="${escapeHtml(item.status || 'confirmed')}">
      </div>
      <div class="field">
        <label>Admin Notes</label>
        <textarea id="reservationNotesInput" class="textarea">${escapeHtml(item.notes || '')}</textarea>
      </div>
      <button class="btn-primary" type="button" onclick="saveReservation('${item.id}')">Save Reservation</button>
    </div>
  `;
}

function renderOrders(items) {
  state.orders = items;
  const tbody = document.getElementById('ordersTable');
  tbody.innerHTML = items.length ? items.map((item) => `
    <tr>
      <td><strong>${escapeHtml(item.orderNumber)}</strong><br><span class="muted">${item.items.length} items</span></td>
      <td>${escapeHtml(item.customerName || 'Unknown')}<br><span class="muted">${escapeHtml(item.customerEmail || '-')}</span></td>
      <td>${escapeHtml(item.status)}</td>
      <td>${escapeHtml(item.eta)}</td>
      <td>${item.total} TND</td>
      <td>
        <div class="actions">
          <button class="btn-ghost btn-small" type="button" onclick="startOrderEditById('${item.orderNumber}')">Edit</button>
          <button class="btn-danger btn-small" type="button" onclick="deleteOrder('${item.orderNumber}')">Delete</button>
        </div>
      </td>
    </tr>
  `).join('') : `<tr><td colspan="6" class="muted">No orders found.</td></tr>`;
}

function renderReviews(items) {
  state.reviews = items;
  const container = document.getElementById('reviewsList');
  container.innerHTML = items.length ? items.map((item) => `
    <div class="detail-card">
      <div style="display:flex;justify-content:space-between;gap:14px;align-items:start">
        <div>
          <strong>${escapeHtml(item.name)}</strong> <span class="muted">(${escapeHtml(item.platform)})</span>
          <div class="muted">${escapeHtml(item.date)} - Rating ${item.rating}/5</div>
          <div class="muted">Client: ${escapeHtml(item.clientName || 'Guest')} (${escapeHtml(item.clientEmail || 'N/A')})</div>
          <div class="muted">Client ID: ${escapeHtml(item.clientId || 'N/A')}</div>
          <p class="sub" style="margin-top:8px">${escapeHtml(item.text)}</p>
        </div>
        <button class="btn-danger btn-small" type="button" onclick="deleteReview('${item.id}')">Delete</button>
      </div>
    </div>
  `).join('') : `<div class="empty">No reviews to moderate.</div>`;
}

function renderSubscribers(items) {
  state.subscribers = items;
  const container = document.getElementById('subscribersList');
  container.innerHTML = items.length ? items.map((item) => `
    <div class="detail-card">
      <div style="display:flex;justify-content:space-between;gap:14px;align-items:start">
        <div>
          <strong>${escapeHtml(item.emailMasked)}</strong>
          <div class="muted">${escapeHtml(item.subscribedAt || 'Unknown date')}</div>
          <div class="muted">Source: ${escapeHtml(item.source || 'newsletter')}</div>
        </div>
        <div class="actions">
          <button class="btn-ghost btn-small" type="button" onclick="revealSubscriber('${item.id}')">Reveal</button>
          <button class="btn-danger btn-small" type="button" onclick="deleteSubscriber('${item.id}')">Delete</button>
        </div>
      </div>
    </div>
  `).join('') : `<div class="empty">No subscribers yet.</div>`;
}

function renderPromotions(items) {
  state.promotions = items;
  const tbody = document.getElementById('promotionsTable');
  if (!tbody) return;

  tbody.innerHTML = items.length ? items.map((item) => `
    <tr>
      <td><strong>${escapeHtml(item.title)}</strong><br><span class="muted">${escapeHtml(item.description)}</span></td>
      <td>${escapeHtml(item.code)}</td>
      <td>${escapeHtml(item.discount)}</td>
      <td>${escapeHtml(item.status)}</td>
      <td>${escapeHtml(item.startsAt || 'Open')} → ${escapeHtml(item.endsAt || 'Open')}</td>
      <td>
        <div class="actions">
          <button class="btn-ghost btn-small" type="button" onclick="startPromotionEditById('${item.id}')">Edit</button>
          <button class="btn-ghost btn-small" type="button" onclick="togglePromotionStatus('${item.id}','${item.status}')">${item.status === 'active' ? 'Set Inactive' : 'Set Active'}</button>
        </div>
      </td>
    </tr>
  `).join('') : `<tr><td colspan="6" class="muted">No promotions found.</td></tr>`;
}

function renderIngredients(items) {
  state.ingredients = items;
  const tbody = document.getElementById('ingredientsTable');
  tbody.innerHTML = items.length ? items.map((item) => `
    <tr>
      <td>${escapeHtml(item.name)}</td>
      <td>${escapeHtml(item.stock)}</td>
      <td>${escapeHtml(item.unit)}</td>
      <td>${item.available ? 'Yes' : 'No'}</td>
      <td>
        <div class="actions">
          <button class="btn-ghost btn-small" type="button" onclick="startIngredientEditById('${item.id}')">Edit</button>
          <button class="btn-danger btn-small" type="button" onclick="deleteIngredient('${item.id}')">Delete</button>
        </div>
      </td>
    </tr>
  `).join('') : `<tr><td colspan="5" class="muted">No ingredients found.</td></tr>`;
}

function renderTables(items) {
  state.tables = items;
  const tbody = document.getElementById('tablesTable');
  tbody.innerHTML = items.length ? items.map((item) => `
    <tr>
      <td>${escapeHtml(item.id)}</td>
      <td>${escapeHtml(item.seats)}</td>
      <td>${item.available ? 'Yes' : 'No'}</td>
      <td>${escapeHtml(item.location || '')}</td>
      <td>
        <div class="actions">
          <button class="btn-ghost btn-small" type="button" onclick="startTableEditById('${item.id}')">Edit</button>
          <button class="btn-danger btn-small" type="button" onclick="deleteTable('${item.id}')">Delete</button>
        </div>
      </td>
    </tr>
  `).join('') : `<tr><td colspan="5" class="muted">No tables configured.</td></tr>`;
}

function renderStaff(items) {
  state.staff = items;
  const tbody = document.getElementById('staffTable');
  tbody.innerHTML = items.length ? items.map((item) => `
    <tr>
      <td>${escapeHtml(item.username)}</td>
      <td>${escapeHtml(item.role)}</td>
      <td>${escapeHtml(item.createdAt || '-')}</td>
      <td>${escapeHtml(item.lastLoginAt || '-')}</td>
      <td>
        <div class="actions">
          <button class="btn-ghost btn-small" type="button" onclick="startStaffEditById('${item.id}')">Edit</button>
          <button class="btn-danger btn-small" type="button" onclick="deleteStaff('${item.id}')">Delete</button>
        </div>
      </td>
    </tr>
  `).join('') : `<tr><td colspan="5" class="muted">No staff accounts available.</td></tr>`;
}

function resetMenuForm() {
  state.menuEditId = null;
  document.getElementById('menuForm').reset();
  document.getElementById('menuFormTitle').textContent = 'Create Dish';
  document.getElementById('menuCancel').classList.add('hidden');
  document.getElementById('menuVeg').value = 'false';
}

function resetOrderForm() {
  state.orderEditId = null;
  document.getElementById('orderForm').reset();
  document.getElementById('orderFormTitle').textContent = 'Create Order';
  document.getElementById('orderCancel').classList.add('hidden');
  document.getElementById('orderStatus').value = 'confirmed';
  document.getElementById('orderItemsText').value = '';
}

function resetPromotionForm() {
  state.promotionEditId = null;
  document.getElementById('promotionForm')?.reset();
  document.getElementById('promotionFormTitle').textContent = 'Create Promotion';
  document.getElementById('promotionCancel').classList.add('hidden');
  document.getElementById('promotionStatus').value = 'active';
}

function resetIngredientForm() {
  state.ingredientEditId = null;
  document.getElementById('ingredientForm').reset();
  document.getElementById('ingredientFormTitle').textContent = 'Add Ingredient';
  document.getElementById('ingredientCancel').classList.add('hidden');
  document.getElementById('ingredientAvailable').value = 'true';
}

function resetTableForm() {
  state.tableEditId = null;
  document.getElementById('tableForm').reset();
  document.getElementById('tableFormTitle').textContent = 'Add Table';
  document.getElementById('tableCancel').classList.add('hidden');
  document.getElementById('tableAvailable').value = 'true';
}

function resetStaffForm() {
  state.staffEditId = null;
  document.getElementById('staffForm').reset();
  document.getElementById('staffFormTitle').textContent = 'Add Staff';
  document.getElementById('staffCancel').classList.add('hidden');
  document.getElementById('staffRole').value = 'chef';
  document.getElementById('staffPhotoPreview').src = "data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 64 64%22%3E%3Crect width=%2264%22 height=%2264%22 fill=%23212121/%3E%3Ccircle cx=%2232%22 cy=%2220%22 r=%2212%22 fill=%23fff/%3E%3Cpath d=%22M16 54c0-12 16-16 16-16s16 4 16 16H16z%22 fill=%23fff/%3E%3C/svg%3E";
}

function exportValue(value) {
  if (value == null) return '';
  if (Array.isArray(value)) return value.join(' | ');
  if (typeof value === 'object') return JSON.stringify(value);
  return String(value);
}

function buildExportRows(kind) {
  if (kind === 'menu') {
    return state.menu.map((item) => ({
      id: item.id,
      name: item.name,
      category: item.cat,
      image_url: item.image || '',
      price_tnd: item.price,
      rating: item.rating,
      reviews: item.reviews,
      vegetarian: item.veg ? 'Yes' : 'No',
      badges: exportValue(item.badges),
      allergens: exportValue(item.allergens),
      description: item.desc,
    }));
  }

  if (kind === 'reservations') {
    return state.reservations.map((item) => ({
      id: item.id,
      first_name: item.firstName,
      last_name: item.lastName,
      date: item.date,
      time: item.time,
      guests: item.guests,
      email_masked: item.emailMasked,
      phone_masked: item.phoneMasked,
      status: item.status || 'confirmed',
      client_id: item.clientId || '',
      experience: item.experience,
      menu_selections: exportValue(item.menuSelections || []),
      allergy_selections: exportValue(item.allergySelections || []),
      special_requests: item.specialRequests || '',
      created_at: item.createdAt || '',
    }));
  }

  if (kind === 'orders') {
    return state.orders.map((item) => ({
      order_number: item.orderNumber,
      client_name: item.customerName || '',
      client_email: item.customerEmail || '',
      client_id: item.clientId || '',
      status: item.status,
      eta: item.eta,
      total_tnd: item.total,
      delivery_address: item.deliveryAddress || '',
      delivery_time: item.deliveryTime || '',
      contact_phone: item.contactPhone || '',
      items_count: item.items.length,
      items: exportValue((item.items || []).map((entry) => `${entry.name} x${entry.quantity}`)),
      steps: exportValue((item.steps || []).map((entry) => `${entry.label} (${entry.state})`)),
    }));
  }

  if (kind === 'reviews') {
    return state.reviews.map((item) => ({
      id: item.id,
      name: item.name,
      client_name: item.clientName || '',
      client_email: item.clientEmail || '',
      client_id: item.clientId || '',
      platform: item.platform,
      date: item.date,
      rating: item.rating,
      review: item.text,
    }));
  }

  if (kind === 'subscribers') {
    return state.subscribers.map((item) => ({
      id: item.id,
      email_masked: item.emailMasked,
      subscribed_at: item.subscribedAt || '',
      source: item.source || 'newsletter',
    }));
  }

  if (kind === 'promotions') {
    return state.promotions.map((item) => ({
      id: item.id,
      title: item.title,
      code: item.code,
      discount: item.discount,
      description: item.description,
      status: item.status,
      starts_at: item.startsAt || '',
      ends_at: item.endsAt || '',
    }));
  }

  return [];
}

function exportToExcel(filename, sheetName, rows) {
  if (!window.XLSX) {
    throw new Error('Excel exporter unavailable');
  }

  const worksheet = window.XLSX.utils.json_to_sheet(rows);
  const workbook = window.XLSX.utils.book_new();
  window.XLSX.utils.book_append_sheet(workbook, worksheet, sheetName.slice(0, 31));
  window.XLSX.writeFile(workbook, filename);
}

function exportToPdf(filename, title, rows) {
  if (!window.jspdf?.jsPDF) {
    throw new Error('PDF exporter unavailable');
  }

  const doc = new window.jspdf.jsPDF({ orientation: 'landscape' });
  const headers = Object.keys(rows[0]);
  const body = rows.map((row) => headers.map((header) => exportValue(row[header])));
  doc.setFontSize(14);
  doc.text(title, 14, 16);
  doc.autoTable({
    head: [headers],
    body,
    startY: 22,
    styles: { fontSize: 8 },
    headStyles: { fillColor: [22, 22, 22] },
  });
  doc.save(filename);
}

function exportData(kind, format) {
  try {
    const rows = buildExportRows(kind);
    if (!rows.length) {
      showToast(`No ${kind} data to export`, 'error');
      return;
    }

    const stamp = new Date().toISOString().slice(0, 10);
    const title = `NOIR ${kind.toUpperCase()} EXPORT`;
    const base = `noir-${kind}-${stamp}`;

    if (format === 'xlsx') {
      exportToExcel(`${base}.xlsx`, kind, rows);
      showToast(`${kind} exported as Excel`);
      return;
    }

    exportToPdf(`${base}.pdf`, title, rows);
    showToast(`${kind} exported as PDF`);
  } catch (error) {
    showToast(error.message, 'error');
  }
}

function startMenuEdit(item) {
  state.menuEditId = item.id;
  document.getElementById('menuFormTitle').textContent = `Edit Dish #${item.id}`;
  document.getElementById('menuCancel').classList.remove('hidden');
  document.getElementById('menuName').value = item.name;
  document.getElementById('menuCategory').value = item.cat;
  document.getElementById('menuPrice').value = item.price;
  document.getElementById('menuImage').value = item.image || '';
  document.getElementById('menuVeg').value = item.veg ? 'true' : 'false';
  document.getElementById('menuRating').value = item.rating;
  document.getElementById('menuReviews').value = item.reviews;
  document.getElementById('menuDesc').value = item.desc;
  document.getElementById('menuAllergens').value = (item.allergens || []).join(', ');
  document.getElementById('menuBadges').value = (item.badges || []).join(', ');
}

function startMenuEditById(id) {
  const item = state.menu.find((entry) => entry.id === id);
  if (item) startMenuEdit(item);
}

function buildOrderItemsText(items) {
  return (items || []).map((item) => (
    `${item.name} | ${item.quantity} | ${item.price} | ${item.image || ''}`
  )).join('\n');
}

function parseOrderItemsText(raw) {
  const lines = String(raw || '')
    .split('\n')
    .map((line) => line.trim())
    .filter(Boolean);

  const items = lines.map((line) => {
    const [name, qtyRaw, priceRaw, imageRaw] = line.split('|').map((part) => part.trim());
    const quantity = Number(qtyRaw);
    const price = Number(priceRaw);
    if (!name || !Number.isFinite(quantity) || quantity <= 0 || !Number.isFinite(price) || price <= 0) {
      throw new Error(`Invalid item row: "${line}". Use "Name | Quantity | Price | Image URL".`);
    }

    return {
      name,
      quantity,
      price,
      image: imageRaw || '',
    };
  });

  if (!items.length) {
    throw new Error('At least one order item is required');
  }

  return items;
}

function buildOrderSteps(status) {
  const normalized = String(status || '').toLowerCase();
  const stage = normalized === 'delivered'
    ? 3
    : normalized === 'out_for_delivery'
      ? 2
      : normalized === 'being_prepared'
        ? 1
        : 0;

  const states = ['pending', 'pending', 'pending', 'pending'];
  for (let index = 0; index < stage; index += 1) states[index] = 'done';
  states[stage] = stage === 3 ? 'done' : 'active';

  return [
    {
      label: 'Order Confirmed',
      time: 'Order has been confirmed',
      state: states[0],
      icon: 'fa-check',
    },
    {
      label: 'Being Prepared',
      time: 'Kitchen is preparing the order',
      state: states[1],
      icon: 'fa-check',
    },
    {
      label: 'Out for Delivery',
      time: 'Delivery rider is on the way',
      state: states[2],
      icon: 'fa-check',
    },
    {
      label: 'Delivered',
      time: 'Order delivered',
      state: states[3],
      icon: 'fa-check',
    },
  ];
}

function startOrderEdit(item) {
  state.orderEditId = item.orderNumber;
  document.getElementById('orderFormTitle').textContent = `Edit ${item.orderNumber}`;
  document.getElementById('orderCancel').classList.remove('hidden');
  document.getElementById('orderNumber').value = item.orderNumber;
  document.getElementById('orderCustomerName').value = item.customerName || '';
  document.getElementById('orderCustomerEmail').value = item.customerEmail || '';
  document.getElementById('orderStatus').value = item.status;
  document.getElementById('orderEta').value = item.eta;
  document.getElementById('orderTotal').value = item.total;
  document.getElementById('orderDeliveryAddress').value = item.deliveryAddress || '';
  document.getElementById('orderDeliveryTime').value = item.deliveryTime || '';
  document.getElementById('orderContactPhone').value = item.contactPhone || '';
  document.getElementById('orderItemsText').value = buildOrderItemsText(item.items);
  document.getElementById('orderNotes').value = item.notes || '';
}

function startOrderEditById(orderNumber) {
  const item = state.orders.find((entry) => entry.orderNumber === orderNumber);
  if (item) startOrderEdit(item);
}

function startPromotionEdit(item) {
  state.promotionEditId = item.id;
  document.getElementById('promotionFormTitle').textContent = `Edit ${item.code}`;
  document.getElementById('promotionCancel').classList.remove('hidden');
  document.getElementById('promotionTitle').value = item.title;
  document.getElementById('promotionCode').value = item.code;
  document.getElementById('promotionDiscount').value = item.discount;
  document.getElementById('promotionDescription').value = item.description;
  document.getElementById('promotionStatus').value = item.status;
  document.getElementById('promotionStartsAt').value = item.startsAt || '';
  document.getElementById('promotionEndsAt').value = item.endsAt || '';
}

function startPromotionEditById(id) {
  const item = state.promotions.find((entry) => entry.id === id);
  if (item) startPromotionEdit(item);
}

async function loadReservationDetail(id) {
  try {
    renderReservationDetail(await adminFetch(`/reservations/${id}`));
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function saveReservation(id) {
  try {
    await adminFetch(`/reservations/${id}`, {
      method: 'PATCH',
      body: JSON.stringify({
        status: document.getElementById('reservationStatusInput').value.trim(),
        notes: document.getElementById('reservationNotesInput').value.trim(),
      }),
    });
    showToast('Reservation updated');
    await loadAllData();
    await loadReservationDetail(id);
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function deleteReservation(id) {
  if (!confirm('Delete this reservation?')) return;
  try {
    await adminFetch(`/reservations/${id}`, { method: 'DELETE' });
    showToast('Reservation deleted', 'success');
    await loadAllData();
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function deleteMenuItem(id) {
  if (!confirm('Delete this dish?')) return;
  try {
    await adminFetch(`/menu/${id}`, { method: 'DELETE' });
    showToast('Dish deleted');
    await loadAllData();
    resetMenuForm();
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function deleteOrder(orderNumber) {
  if (!confirm(`Delete order ${orderNumber}?`)) return;
  try {
    await adminFetch(`/orders/${encodeURIComponent(orderNumber)}`, { method: 'DELETE' });
    showToast('Order deleted');
    await loadAllData();
    resetOrderForm();
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function deleteReview(id) {
  if (!confirm('Delete this review?')) return;
  try {
    await adminFetch(`/reviews/${id}`, { method: 'DELETE' });
    showToast('Review deleted');
    await loadAllData();
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function revealSubscriber(id) {
  try {
    const subscriber = await adminFetch(`/subscribers/${id}`);
    alert(`Subscriber email: ${subscriber.email}`);
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function deleteSubscriber(id) {
  if (!confirm('Delete this subscriber?')) return;
  try {
    await adminFetch(`/subscribers/${id}`, { method: 'DELETE' });
    showToast('Subscriber deleted');
    await loadAllData();
  } catch (error) {
    showToast(error.message, 'error');
  }
}

function startIngredientEditById(id) {
  const item = state.ingredients.find((entry) => String(entry.id) === String(id));
  if (!item) return;
  state.ingredientEditId = item.id;
  document.getElementById('ingredientFormTitle').textContent = 'Edit Ingredient';
  document.getElementById('ingredientName').value = item.name || '';
  document.getElementById('ingredientStock').value = item.stock ?? '';
  document.getElementById('ingredientUnit').value = item.unit || '';
  document.getElementById('ingredientAvailable').value = item.available ? 'true' : 'false';
  document.getElementById('ingredientNotes').value = item.notes || '';
  document.getElementById('ingredientCancel').classList.remove('hidden');
}

function startTableEditById(id) {
  const item = state.tables.find((entry) => String(entry.id) === String(id));
  if (!item) return;
  state.tableEditId = item.id;
  document.getElementById('tableFormTitle').textContent = 'Edit Table';
  document.getElementById('tableId').value = item.id || '';
  document.getElementById('tableSeats').value = item.seats ?? '';
  document.getElementById('tableAvailable').value = item.available ? 'true' : 'false';
  document.getElementById('tableLocation').value = item.location || '';
  document.getElementById('tableNotes').value = item.notes || '';
  document.getElementById('tableCancel').classList.remove('hidden');
}

function startStaffEditById(id) {
  const item = state.staff.find((entry) => String(entry.id) === String(id));
  if (!item) return;
  state.staffEditId = item.id;
  document.getElementById('staffFormTitle').textContent = 'Edit Staff';
  document.getElementById('staffUsername').value = item.username || '';
  document.getElementById('staffPassword').value = '';
  document.getElementById('staffRole').value = item.role || 'chef';
  document.getElementById('staffFirstName').value = item.firstName || '';
  document.getElementById('staffLastName').value = item.lastName || '';
  document.getElementById('staffEmail').value = item.email || '';
  document.getElementById('staffPhone').value = item.phone || '';
  document.getElementById('staffBirthDate').value = item.birthDate || '';
  if (item.profilePicture) document.getElementById('staffPhotoPreview').src = item.profilePicture;
  document.getElementById('staffCancel').classList.remove('hidden');
}

function previewStaffPhoto(input) {
  if (input.files && input.files[0]) {
    const reader = new FileReader();
    reader.onload = e => { document.getElementById('staffPhotoPreview').src = e.target.result; };
    reader.readAsDataURL(input.files[0]);
  }
}

async function deleteIngredient(id) {
  if (!confirm('Delete this ingredient?')) return;
  try {
    await adminFetch(`/ingredients/${encodeURIComponent(id)}`, { method: 'DELETE' });
    showToast('Ingredient deleted');
    await loadAllData();
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function deleteTable(id) {
  if (!confirm('Delete this table?')) return;
  try {
    await adminFetch(`/tables/${encodeURIComponent(id)}`, { method: 'DELETE' });
    showToast('Table deleted');
    await loadAllData();
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function deleteStaff(id) {
  if (!confirm('Delete this staff account?')) return;
  try {
    await adminFetch(`/staff/${encodeURIComponent(id)}`, { method: 'DELETE' });
    showToast('Staff deleted');
    await loadAllData();
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function togglePromotionStatus(id, currentStatus) {
  const nextStatus = currentStatus === 'active' ? 'inactive' : 'active';
  try {
    await adminFetch(`/promotions/${id}/status`, {
      method: 'PATCH',
      body: JSON.stringify({ status: nextStatus }),
    });
    showToast(`Promotion set to ${nextStatus}`);
    await loadAllData();
  } catch (error) {
    showToast(error.message, 'error');
  }
}

async function loadAllData() {
  const [overview, menu, reservations, orders, reviews, subscribers, promotions, ingredients, tables, staff] = await Promise.all([
    adminFetch('/overview'),
    adminFetch('/menu'),
    adminFetch('/reservations'),
    adminFetch('/orders'),
    adminFetch('/reviews'),
    adminFetch('/subscribers'),
    adminFetch('/promotions'),
    adminFetch('/ingredients'),
    adminFetch('/tables'),
    adminFetch('/staff'),
  ]);

  renderStats(overview);
  renderHome(overview);
  renderMenu(menu);
  renderReservations(reservations);
  renderOrders(orders);
  renderReviews(reviews);
  renderSubscribers(subscribers);
  renderPromotions(promotions);
  renderIngredients(ingredients);
  renderTables(tables);
  renderStaff(staff);
  renderAllergens(menu, reservations);
}

document.getElementById('loginForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const data = await adminFetch('/login', {
      method: 'POST',
      body: JSON.stringify({
        username: document.getElementById('username').value.trim(),
        password: document.getElementById('password').value,
      }),
    });

    state.token = data.token;
    localStorage.setItem(adminTokenKey, data.token);
    setLoggedIn(true);
    resetMenuForm();
    resetOrderForm();
    await loadAllData();
    showToast('Admin session opened');
  } catch (error) {
    showToast(error.message, 'error');
  }
});

document.getElementById('menuForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const payload = {
      name: document.getElementById('menuName').value.trim(),
      cat: document.getElementById('menuCategory').value,
      image: document.getElementById('menuImage').value.trim(),
      price: Number(document.getElementById('menuPrice').value),
      desc: document.getElementById('menuDesc').value.trim(),
      allergens: document.getElementById('menuAllergens').value.split(',').map((value) => value.trim()).filter(Boolean),
      rating: Number(document.getElementById('menuRating').value),
      reviews: Number(document.getElementById('menuReviews').value),
      badges: document.getElementById('menuBadges').value.split(',').map((value) => value.trim()).filter(Boolean),
      veg: document.getElementById('menuVeg').value === 'true',
    };

    if (state.menuEditId) {
      await adminFetch(`/menu/${state.menuEditId}`, { method: 'PUT', body: JSON.stringify(payload) });
      showToast('Dish updated');
    } else {
      await adminFetch('/menu', { method: 'POST', body: JSON.stringify(payload) });
      showToast('Dish created');
    }

    resetMenuForm();
    await loadAllData();
  } catch (error) {
    showToast(error.message, 'error');
  }
});

document.getElementById('orderForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const status = document.getElementById('orderStatus').value.trim();
    const items = parseOrderItemsText(document.getElementById('orderItemsText').value);
    const rawTotal = document.getElementById('orderTotal').value.trim();
    const computedTotal = items.reduce((sum, item) => sum + (Number(item.price) * Number(item.quantity)), 0);
    const payload = {
      orderNumber: document.getElementById('orderNumber').value.trim() || undefined,
      customerName: document.getElementById('orderCustomerName').value.trim() || undefined,
      customerEmail: document.getElementById('orderCustomerEmail').value.trim() || undefined,
      status,
      eta: document.getElementById('orderEta').value.trim(),
      deliveryAddress: document.getElementById('orderDeliveryAddress').value.trim() || undefined,
      deliveryTime: document.getElementById('orderDeliveryTime').value.trim() || undefined,
      contactPhone: document.getElementById('orderContactPhone').value.trim() || undefined,
      notes: document.getElementById('orderNotes').value.trim() || '',
      total: rawTotal ? Number(rawTotal) : Math.round(computedTotal * 100) / 100,
      items,
      steps: buildOrderSteps(status),
    };

    if (state.orderEditId) {
      await adminFetch(`/orders/${encodeURIComponent(state.orderEditId)}`, { method: 'PUT', body: JSON.stringify(payload) });
      showToast('Order updated');
    } else {
      await adminFetch('/orders', { method: 'POST', body: JSON.stringify(payload) });
      showToast('Order created');
    }

    resetOrderForm();
    await loadAllData();
  } catch (error) {
    showToast(error.message, 'error');
  }
});

document.getElementById('promotionForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const payload = {
      title: document.getElementById('promotionTitle').value.trim(),
      code: document.getElementById('promotionCode').value.trim().toUpperCase(),
      discount: document.getElementById('promotionDiscount').value.trim(),
      description: document.getElementById('promotionDescription').value.trim(),
      status: document.getElementById('promotionStatus').value,
      startsAt: document.getElementById('promotionStartsAt').value,
      endsAt: document.getElementById('promotionEndsAt').value,
    };

    if (state.promotionEditId) {
      await adminFetch(`/promotions/${state.promotionEditId}`, {
        method: 'PUT',
        body: JSON.stringify(payload),
      });
      showToast('Promotion updated');
    } else {
      await adminFetch('/promotions', {
        method: 'POST',
        body: JSON.stringify(payload),
      });
      showToast('Promotion created');
    }

    resetPromotionForm();
    await loadAllData();
  } catch (error) {
    showToast(error.message, 'error');
  }
});

document.getElementById('ingredientForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const payload = {
      name: document.getElementById('ingredientName').value.trim(),
      stock: Number(document.getElementById('ingredientStock').value),
      unit: document.getElementById('ingredientUnit').value.trim(),
      available: document.getElementById('ingredientAvailable').value === 'true',
      notes: document.getElementById('ingredientNotes').value.trim(),
    };

    if (state.ingredientEditId) {
      await adminFetch(`/ingredients/${encodeURIComponent(state.ingredientEditId)}`, {
        method: 'PUT', body: JSON.stringify(payload),
      });
      showToast('Ingredient updated');
    } else {
      await adminFetch('/ingredients', { method: 'POST', body: JSON.stringify(payload) });
      showToast('Ingredient created');
    }

    resetIngredientForm();
    await loadAllData();
  } catch (error) {
    showToast(error.message, 'error');
  }
});

document.getElementById('tableForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const payload = {
      id: document.getElementById('tableId').value.trim(),
      seats: Number(document.getElementById('tableSeats').value),
      available: document.getElementById('tableAvailable').value === 'true',
      location: document.getElementById('tableLocation').value.trim(),
      notes: document.getElementById('tableNotes').value.trim(),
    };

    if (state.tableEditId) {
      await adminFetch(`/tables/${encodeURIComponent(state.tableEditId)}`, {
        method: 'PUT', body: JSON.stringify(payload),
      });
      showToast('Table updated');
    } else {
      await adminFetch('/tables', { method: 'POST', body: JSON.stringify(payload) });
      showToast('Table added');
    }

    resetTableForm();
    await loadAllData();
  } catch (error) {
    showToast(error.message, 'error');
  }
});

document.getElementById('staffForm').addEventListener('submit', async (event) => {
  event.preventDefault();
  try {
    const fileInput = document.getElementById('staffPicture');
    let profilePicture = null;
    if (fileInput && fileInput.files && fileInput.files[0]) {
      profilePicture = await toDataURL(fileInput.files[0]);
    }
    const payload = {
      username: document.getElementById('staffUsername').value.trim(),
      password: document.getElementById('staffPassword').value,
      role: document.getElementById('staffRole').value,
      firstName: document.getElementById('staffFirstName').value.trim(),
      lastName: document.getElementById('staffLastName').value.trim(),
      email: document.getElementById('staffEmail').value.trim(),
      phone: document.getElementById('staffPhone').value.trim(),
      birthDate: document.getElementById('staffBirthDate').value,
      profilePicture,
    };

    if (state.staffEditId) {
      await adminFetch(`/staff/${encodeURIComponent(state.staffEditId)}`, {
        method: 'PUT', body: JSON.stringify(payload),
      });
      showToast('Staff account updated');
    } else {
      await adminFetch('/staff', { method: 'POST', body: JSON.stringify(payload) });
      showToast('Staff account created');
    }

    resetStaffForm();
    await loadAllData();
  } catch (error) {
    showToast(error.message, 'error');
  }
});

document.getElementById('menuCancel').addEventListener('click', resetMenuForm);
document.getElementById('orderCancel').addEventListener('click', resetOrderForm);
document.getElementById('promotionCancel').addEventListener('click', resetPromotionForm);
document.getElementById('ingredientCancel').addEventListener('click', resetIngredientForm);
document.getElementById('tableCancel').addEventListener('click', resetTableForm);
document.getElementById('staffCancel').addEventListener('click', resetStaffForm);
document.getElementById('refreshBtn').addEventListener('click', async () => {
  try {
    await loadAllData();
    showToast('Data refreshed');
  } catch (error) {
    showToast(error.message, 'error');
  }
});
document.getElementById('logoutBtn').addEventListener('click', () => {
  state.token = '';
  localStorage.removeItem(adminTokenKey);
  setLoggedIn(false);
  showToast('Logged out');
});

document.querySelectorAll('.nav-btn').forEach((button) => {
  button.addEventListener('click', () => setActiveView(button.dataset.viewTarget));
});

resetMenuForm();
resetOrderForm();
resetPromotionForm();
resetIngredientForm();
resetTableForm();
resetStaffForm();
setActiveView('home');

if (state.token) {
  setLoggedIn(true);
  loadAllData().catch((error) => {
    state.token = '';
    localStorage.removeItem(adminTokenKey);
    setLoggedIn(false);
    showToast(error.message, 'error');
  });
} else {
  setLoggedIn(false);
}

window.startMenuEdit = startMenuEdit;
window.startMenuEditById = startMenuEditById;
window.startOrderEdit = startOrderEdit;
window.startOrderEditById = startOrderEditById;
window.loadReservationDetail = loadReservationDetail;
window.saveReservation = saveReservation;
window.deleteReservation = deleteReservation;
window.deleteMenuItem = deleteMenuItem;
window.deleteOrder = deleteOrder;
window.deleteReview = deleteReview;
window.revealSubscriber = revealSubscriber;
window.deleteSubscriber = deleteSubscriber;
window.startPromotionEdit = startPromotionEdit;
window.startPromotionEditById = startPromotionEditById;
window.togglePromotionStatus = togglePromotionStatus;
window.startIngredientEditById = startIngredientEditById;
window.startTableEditById = startTableEditById;
window.startStaffEditById = startStaffEditById;
window.deleteIngredient = deleteIngredient;
window.deleteTable = deleteTable;
window.deleteStaff = deleteStaff;
window.exportData = exportData;

// Kitchen Menu Functions
function renderKitchenMenu(items, role) {
  const grid = document.getElementById('kitchenMenuGrid');
  if (!grid) return;

  const isChef = role === 'chef';
  
  grid.innerHTML = items.map((item) => `
    <div class="plate-card">
      <div style="margin-bottom: 10px;">
        ${item.image 
          ? `<img src="${escapeHtml(item.image)}" alt="${escapeHtml(item.name)}" style="width: 100%; height: 200px; object-fit: cover; border-radius: 6px; border: 0.5px solid var(--border);">` 
          : `<div style="width: 100%; height: 200px; background: var(--surface); border-radius: 6px; border: 0.5px solid var(--border); display: flex; align-items: center; justify-content: center; color: var(--gold); font-size: 14px;">${escapeHtml(item.name.charAt(0))}</div>`}
      </div>
      <div style="margin-bottom: 8px;">
        <strong style="font-family: 'Cormorant Garamond', serif; font-size: 1rem;">${escapeHtml(item.name)}</strong>
        <div style="font-size: 11px; color: var(--text-muted); text-transform: uppercase; letter-spacing: 1px; margin-top: 2px;">${escapeHtml(item.cat)} · ${item.price} TND</div>
      </div>
      <p style="font-size: 12px; color: var(--text-muted); margin: 0 0 10px 0; line-height: 1.5;">${escapeHtml(item.desc || 'No description')}</p>
      ${item.allergens ? `<div style="font-size: 10px; color: rgba(192,57,43,0.8); margin-bottom: 10px;"><strong>Allergens:</strong> ${escapeHtml(item.allergens.join(', '))}</div>` : ''}
      ${item.rating ? `<div style="font-size: 11px; color: var(--gold); margin-bottom: 10px;"><i class="fas fa-star"></i> ${item.rating}/5 (${item.reviews} reviews)</div>` : ''}
      ${isChef ? `<button class="btn-gold btn-small" type="button" onclick="quickRecommendDish(${item.id}, '${escapeHtml(item.name)}')" style="width: 100%; background: rgba(201,168,76,.2); color: var(--gold); border: 0.5px solid var(--gold);"><i class="fas fa-heart"></i> Recommend</button>` : ''}
    </div>
  `).join('');
}

function quickRecommendDish(dishId, dishName) {
  const modal = document.getElementById('recommendPanel');
  if (modal) {
    document.getElementById('recId').value = dishId;
    modal.style.display = 'block';
    showToast(`Ready to recommend: ${dishName}`, 'gold', '<i class="fas fa-heart"></i>');
  }
}

// Update setActiveView to handle menu tab
const originalSetActiveView = setActiveView;
setActiveView = function(viewName) {
  originalSetActiveView.call(this, viewName);
  
  // Load and render kitchen menu when viewing
  if (viewName === 'menu' && state.menu.length > 0) {
    const roleBadge = document.getElementById('roleBadge');
    const role = roleBadge?.textContent?.toLowerCase() || 'kitchen';
    renderKitchenMenu(state.menu, role);
    
    // Add tab filtering for menu
    document.querySelectorAll('#view-menu .tab-btn').forEach((btn) => {
      btn.classList.remove('active');
      btn.addEventListener('click', function() {
        const filter = this.dataset.filter;
        filterKitchenMenu(filter, role);
      });
    });
    document.querySelector('#view-menu .tab-btn')?.classList.add('active');
  }
};

function filterKitchenMenu(filter, role) {
  const items = filter === 'all' || !filter
    ? state.menu
    : filter === 'veg'
      ? state.menu.filter((item) => item.veg)
      : state.menu.filter((item) => item.cat === filter);
  
  renderKitchenMenu(items, role);
  
  // Update active tab
  document.querySelectorAll('#view-menu .tab-btn').forEach((btn) => {
    btn.classList.toggle('active', btn.dataset.filter === filter);
  });
}

// ─── ACCOUNT ────────────────────────────────────────────────────────────────

function toDataURL(file, maxSize = 800) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onerror = () => reject(new Error('Read failed'));
    reader.onload = (e) => {
      const img = new Image();
      img.onerror = () => reject(new Error('Image load failed'));
      img.onload = () => {
        const scale = Math.min(1, maxSize / Math.max(img.width, img.height));
        const canvas = document.createElement('canvas');
        canvas.width = Math.round(img.width * scale);
        canvas.height = Math.round(img.height * scale);
        canvas.getContext('2d').drawImage(img, 0, 0, canvas.width, canvas.height);
        resolve(canvas.toDataURL('image/jpeg', 0.82));
      };
      img.src = e.target.result;
    };
    reader.readAsDataURL(file);
  });
}

function switchAdminTab(mode) {
  const isProfile = mode === 'profile';
  document.querySelectorAll('#view-account .tab').forEach((tab, index) => {
    tab.classList.toggle('active', index === (isProfile ? 0 : 1));
  });
  document.getElementById('adminProfileForm').classList.toggle('hidden', !isProfile);
  document.getElementById('adminPasswordForm').classList.toggle('hidden', isProfile);
  if (isProfile) loadAdminProfile();
}

async function loadAdminProfile() {
  try {
    const user = await adminFetch('/me');
    document.getElementById('adminUsername').value = user.username || 'admin';
    document.getElementById('adminRole').value = user.role || 'admin';
    document.getElementById('adminFirstName').value = user.firstName || '';
    document.getElementById('adminLastName').value = user.lastName || '';
    document.getElementById('adminEmail').value = user.email || '';
    document.getElementById('adminPhone').value = user.phone || '';
    document.getElementById('adminBirthDate').value = user.birthDate || '';
    const photo = document.getElementById('adminPhotoPreview');
    if (user.profilePicture) photo.src = user.profilePicture;
  } catch (err) {
    showToast('Failed to load profile', 'error');
  }
}

async function updateAdminProfile() {
  try {
    const username = document.getElementById('adminUsername').value.trim();
    const role = document.getElementById('adminRole').value;
    const firstName = document.getElementById('adminFirstName').value.trim();
    const lastName = document.getElementById('adminLastName').value.trim();
    const email = document.getElementById('adminEmail').value.trim();
    const phone = document.getElementById('adminPhone').value.trim();
    const birthDate = document.getElementById('adminBirthDate').value;
    const fileInput = document.getElementById('adminPicture');
    let profilePicture = null;
    if (fileInput && fileInput.files && fileInput.files[0]) {
      profilePicture = await toDataURL(fileInput.files[0]);
    }
    await adminFetch('/me', {
      method: 'PUT',
      body: JSON.stringify({ username, role, firstName, lastName, email, phone, birthDate, profilePicture })
    });
    showToast('Profile updated', 'gold');
  } catch (err) {
    showToast(err.message || 'Failed to update profile', 'error');
  }
}

async function changeAdminPassword() {
  const oldPassword = document.getElementById('adminOldPassword').value;
  const newPassword = document.getElementById('adminNewPassword').value;
  if (!oldPassword || !newPassword) {
    showToast('Both passwords are required', 'error');
    return;
  }
  if (newPassword.length < 8) {
    showToast('New password must be at least 8 characters', 'error');
    return;
  }
  try {
    await adminFetch('/me/password', {
      method: 'PUT',
      body: JSON.stringify({ oldPassword, newPassword }),
    });
    document.getElementById('adminOldPassword').value = '';
    document.getElementById('adminNewPassword').value = '';
    showToast('Password updated successfully');
  } catch (err) {
    showToast(err.message, 'error');
  }
}



// ─── Allergens Dashboard ───────────────────────────────────────────────────

function renderAllergens(menu, reservations) {
  // Normalize API responses — backend wraps data in {success, data}
  const menuList = Array.isArray(menu) ? menu : (menu?.data || []);
  const resList = Array.isArray(reservations) ? reservations : (reservations?.data || reservations?.content || []);

  // 1. Menu allergens table
  populateAllergenDishSelect(menuList);
  const tbody = document.getElementById('allergensMenuTable');
  if (tbody) {
    if (menuList.length === 0) {
      tbody.innerHTML = '<tr><td colspan="5" style="color:var(--text-muted)">No dishes found.</td></tr>';
    } else {
      tbody.innerHTML = menuList.map(d => `
        <tr>
          <td><strong>${escA(d.name)}</strong></td>
          <td>${escA(d.cat || d.category || '')}</td>
          <td>${(d.allergens && d.allergens.length > 0) ? d.allergens.map(a => `<span class="badge">${escA(a)}</span>`).join('') : '<span style="color:var(--text-muted);font-size:12px">None</span>'}</td>
          <td>${d.veg ? 'X' : '—'}</td>
          <td><button class="btn-ghost btn-small" type="button" onclick="editDishAllergens('${d.id}')">Edit</button></td>
        </tr>`).join('');
    }
  }

  // 2. Allergen frequency stats cards
  const statsCards = document.getElementById('allergenStatsCards');
  if (statsCards) {
    const freq = {};
    menuList.forEach(d => (d.allergens || []).forEach(a => { freq[a] = (freq[a] || 0) + 1; }));
    const sorted = Object.entries(freq).sort((a, b) => b[1] - a[1]);
    const totalDishesWithAllergens = menuList.filter(d => d.allergens && d.allergens.length > 0).length;
    const summaryCards = [
      { label: 'Dishes with Allergens', value: totalDishesWithAllergens, color: '#c0392b' },
      { label: 'Allergen Types', value: sorted.length, color: 'var(--gold)' },
    ];
    const allergenCards = sorted.slice(0, 6).map(([name, count]) => ({ label: name, value: count + ' dish' + (count > 1 ? 'es' : ''), color: 'var(--border)' }));
    statsCards.innerHTML = [...summaryCards, ...allergenCards].map(c => `
      <div style="background:var(--dark);border:0.5px solid ${c.color};border-radius:8px;padding:14px 20px;min-width:140px;flex:1">
        <div style="font-size:22px;font-weight:600;color:${c.color}">${c.value}</div>
        <div style="font-size:11px;color:var(--text-muted);margin-top:4px;text-transform:uppercase;letter-spacing:1px">${c.label}</div>
      </div>`).join('');
  }

  // 3. Reservations with allergy declarations
  const resTbody = document.getElementById('allergensReservationsTable');
  if (resTbody) {
    const withAllergies = resList.filter(r =>
      (r.allergySelections && r.allergySelections.length > 0) || r.allergyOther
    );
    if (withAllergies.length === 0) {
      resTbody.innerHTML = '<tr><td colspan="5" style="color:var(--text-muted)">No allergy declarations found in reservations.</td></tr>';
    } else {
      resTbody.innerHTML = withAllergies.map(r => `
        <tr>
          <td><strong>${escA(r.firstName || '')} ${escA(r.lastName || '')}</strong></td>
          <td>${escA(r.date || '')}</td>
          <td>${escA(String(r.guests || ''))}</td>
          <td>
            ${(r.allergySelections || []).map(a => `<span style="background:#3a1a1a;border:0.5px solid #c0392b;color:#e74c3c;padding:2px 8px;border-radius:4px;font-size:11px;margin-right:4px">${escA(a)}</span>`).join('')}
            ${r.allergyOther ? `<span style="background:var(--dark);border:0.5px solid var(--border);color:var(--text-muted);padding:2px 8px;border-radius:4px;font-size:11px">${escA(r.allergyOther)}</span>` : ''}
          </td>
          <td style="font-size:12px;color:var(--text-muted)">${escA(r.specialRequests || '—')}</td>
        </tr>`).join('');
    }
  }
}

function exportAllergens(format) {
  const rows = [];
  document.querySelectorAll('#allergensMenuTable tr').forEach(tr => {
    const cells = [...tr.querySelectorAll('td,th')].map(td => td.innerText);
    if (cells.length) rows.push(cells);
  });
  if (format === 'xlsx') {
    const wb = XLSX.utils.book_new();
    const ws = XLSX.utils.aoa_to_sheet([['Dish','Category','Allergens','Vegetarian'], ...rows]);
    XLSX.utils.book_append_sheet(wb, ws, 'Allergens');
    XLSX.writeFile(wb, 'allergens.xlsx');
  } else {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();
    doc.text('NOIR — Allergen Report', 14, 16);
    doc.autoTable({ head: [['Dish','Category','Allergens','Vegetarian']], body: rows, startY: 22 });
    doc.save('allergens.pdf');
  }
}

// ─── Allergen Editor ───────────────────────────────────────────────────────

let allergenCurrentList = [];
let allergenAllMenu = [];

function populateAllergenDishSelect(menuList) {
  allergenAllMenu = menuList;
  // Try immediately, and also after a short delay in case the view is not yet visible
  _fillAllergenSelect();
}

function _fillAllergenSelect() {
  const sel = document.getElementById('allergenDishSelect');
  if (!sel) return;
  if (allergenAllMenu.length === 0) return; // don't overwrite with empty if data not loaded yet
  sel.innerHTML = '<option value="">— choose a dish —</option>' +
    allergenAllMenu.map(d => `<option value="${d.id}">${escA(d.name)}</option>`).join('');
  sel.onchange = () => {
    const dish = allergenAllMenu.find(d => String(d.id) === String(sel.value));
    allergenCurrentList = dish ? [...(dish.allergens || [])] : [];
    renderAllergenTags();
  };
}

function escA(v) { return String(v ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c])); }

function renderAllergenTags() {
  const container = document.getElementById('allergenCurrentTags');
  if (!container) return;
  if (allergenCurrentList.length === 0) {
    container.innerHTML = '<span style="color:var(--text-muted);font-size:12px">No allergens yet</span>';
    return;
  }
  container.innerHTML = allergenCurrentList.map((a, i) => `
    <span style="background:var(--dark);border:0.5px solid var(--gold);color:var(--gold);padding:3px 10px;border-radius:4px;font-size:12px;display:flex;align-items:center;gap:6px">
      ${escA(a)}
      <span onclick="removeAllergenTag(${i})" style="cursor:pointer;color:var(--text-muted);font-size:14px;line-height:1">&times;</span>
    </span>`).join('');
}

function removeAllergenTag(index) {
  allergenCurrentList.splice(index, 1);
  renderAllergenTags();
}

function addAllergenTag() {
  const sel = document.getElementById('allergenSelect');
  const val = sel.value.trim();
  if (!val) return;
  if (!allergenCurrentList.includes(val)) {
    allergenCurrentList.push(val);
    renderAllergenTags();
  }
  sel.value = '';
}

function addCustomAllergenTag() {
  const input = document.getElementById('allergenCustom');
  const val = input.value.trim();
  if (!val) return;
  if (!allergenCurrentList.includes(val)) {
    allergenCurrentList.push(val);
    renderAllergenTags();
  }
  input.value = '';
}

function editDishAllergens(dishId) {
  const sel = document.getElementById('allergenDishSelect');
  if (sel) {
    sel.value = String(dishId);
    sel.dispatchEvent(new Event('change'));
    document.getElementById('allergenForm').scrollIntoView({ behavior: 'smooth' });
  }
}

async function saveAllergens() {
  const sel = document.getElementById('allergenDishSelect');
  const dishId = sel.value;
  if (!dishId) { showToast('Please select a dish first', 'error'); return; }
  const dish = allergenAllMenu.find(d => String(d.id) === String(dishId));
  if (!dish) { showToast('Dish not found', 'error'); return; }
  try {
    const payload = {
      name: dish.name,
      cat: dish.cat || dish.category || '',
      image: dish.image || '',
      price: dish.price || 0,
      desc: dish.desc || dish.description || '',
      allergens: allergenCurrentList,
      rating: dish.rating || 0,
      reviews: dish.reviews || 0,
      badges: dish.badges || [],
      veg: dish.veg || false
    };
    await adminFetch(`/menu/${dishId}`, {
      method: 'PUT',
      body: JSON.stringify(payload)
    });
    showToast('Allergens saved!', 'gold');
    await loadAllData();
  } catch (err) {
    showToast('Failed to save allergens: ' + err.message, 'error');
  }
}