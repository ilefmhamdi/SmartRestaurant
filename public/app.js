// ─────────────────────────────────────────────────────────────────────────────
// NOIR — app.js  (complete fixed version)
//
// FIXES applied:
//  1. Order modal: tab switching (form ↔ my orders) instead of split layout
//  2. Reservation modal: tab switching bug (inverted logic) fixed
//  3. submitOrderModal: sends orderName field (backend requires it)
//  4. submitReservationModal: robust phone/date validation
//  5. Session restore: no logout on menu failures
//  6. Chat: real AI-powered chatbot via Anthropic API
// ─────────────────────────────────────────────────────────────────────────────

const apiBase = '/api';
const defaultAvatarSrc = 'data:image/svg+xml,%3Csvg xmlns=%22http://www.w3.org/2000/svg%22 viewBox=%220 0 64 64%22%3E%3Crect width=%2264%22 height=%2264%22 fill=%23212121/%3E%3Ccircle cx=%2232%22 cy=%2220%22 r=%2212%22 fill=%23fff/%3E%3Cpath d=%22M16 54c0-12 16-16 16-16s16 4 16 16H16z%22 fill=%23fff/%3E%3C/svg%3E';
const clientTokenKey = 'noir-client-token';
const clientUserKey = 'noir-client-user';

let menuData = [];
let reviewsData = [];
let activeFilter = 'all';
let userRating = 4;
let clientOrders = [];
let orderCart = [];

const selectedReservationMenuIds = new Set();

let clientSession = {
    token: localStorage.getItem(clientTokenKey) || '',
    user: null,
};
let selectedAccountPhotoDataUrl = null;

try {
    const raw = localStorage.getItem(clientUserKey);
    clientSession.user = raw ? JSON.parse(raw) : null;
} catch {
    clientSession.user = null;
}

const accountPictureInput = document.getElementById('accountPicture');
if (accountPictureInput) {
    accountPictureInput.addEventListener('change', handleAccountPictureChange);
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
function esc(v) {
    return String(v ?? '').replace(/[&<>"']/g, c => ({
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#39;',
    }[c]));
}

function isLoggedIn() {
    return Boolean(clientSession.token && clientSession.user);
}

async function apiFetch(path, options={}) {
    const cfg = { ...options };
    cfg.headers = { ...(options.headers || {})};
    if (clientSession.token) cfg.headers.Authorization = `Bearer ${clientSession.token}`;
    if (cfg.body && !cfg.headers['Content-Type']) cfg.headers['Content-Type'] = 'application/json';
    const res = await fetch(`${apiBase}${path}`, cfg);
    const json = await res.json().catch( () => ({}));
    if (!res.ok) throw new Error(json.message || 'Unexpected server error');
    return json.data;
}

function fmtPrice(v) { return `${Number(v ?? 0).toFixed(0)} TND`;}
function fmtStatus(v) { return String(v || '').replaceAll('_', ' ').replace(/\b\w/g, c => c.toUpperCase());}
function fmtDate(v) {
    const d = new Date(v);
    return isNaN(d) ? v : d.toLocaleDateString('fr-FR', { year: 'numeric', month: 'short', day: 'numeric'});
}

function showToast(msg, type='', icon='<i class="fas fa-info-circle"></i>') {
    const c = document.getElementById('toastContainer');
    const t = document.createElement('div');
    t.className = `toast ${type}`;
    t.innerHTML = `<span class="toast-icon">${icon}</span>${esc(msg)}`;
    c.appendChild(t);
    setTimeout( () => t.classList.add('show'), 50);
    setTimeout( () => { t.classList.remove('show'); setTimeout( () => t.remove(), 400);}, 3200);
}

function handleAccountPictureChange(event) {
    const file = event.target.files?.[0];
    if (!file) return;
    if (!file.type.startsWith('image/')) {
        showToast('Please select a valid image file', '', '<i class="fas fa-exclamation-triangle"></i>');
        return;
    }
    const reader = new FileReader();
    reader.onload = () => {
        selectedAccountPhotoDataUrl = reader.result;
        const preview = document.getElementById('accountPhotoPreview');
        if (preview) preview.src = selectedAccountPhotoDataUrl;
    };
    reader.readAsDataURL(file);
}

const obs = new IntersectionObserver(entries => {
    entries.forEach(e => { if (e.isIntersecting) e.target.classList.add('visible'); });
},{ threshold: 0.1 });

function observeFadeIns() {
    document.querySelectorAll('.fade-in').forEach(el => obs.observe(el));
}

// ─── Custom cursor & scroll ────────────────────────────────────────────────────
document.addEventListener('mousemove', e => {
    const c = document.getElementById('cursor');
    const d = document.getElementById('cursorDot');
    if (c) { c.style.left = `${e.clientX - 10}px`; c.style.top = `${e.clientY - 10}px`;}
    if (d) { d.style.left = `${e.clientX - 2}px`; d.style.top = `${e.clientY - 2}px`; }
});

window.addEventListener('scroll', () => {
    const n = document.getElementById('navbar');
    if (n) n.style.background = window.scrollY > 50 ? 'rgba(5,5,5,0.97)' : 'rgba(5,5,5,0.85)';
});

function scrollSec(id) { document.getElementById(id)?.scrollIntoView({ behavior: 'smooth' });}

function addToCart(itemId) {
    const item = menuData.find(i => i.id === itemId);
    if (!item)
        return;
    const existing = orderCart.find(c => c.id === itemId);
    if (existing) {
        existing.qty += 1;
    } else {
        orderCart.push({
            ...item,
            qty: 1
        });
    }
    updateOrderCartUI();
    updateCartInputs();
    showToast(`Added ${item.name} to order`, '', '<i class="fas fa-shopping-cart"></i>');
}

function removeFromCart(itemId) {
    orderCart = orderCart.filter(c => c.id !== itemId);
    updateOrderCartUI();
    updateCartInputs();
}

function syncCartFromInput(inp) {
    const itemId = parseInt(inp.dataset.orderItemId, 10);
    const quantity = Math.max(0, parseInt(inp.value, 10) || 0);
    const item = menuData.find(i => i.id === itemId);
    if (!item)
        return;
    const existing = orderCart.find(c => c.id === itemId);
    if (quantity === 0) {
        if (existing)
            orderCart = orderCart.filter(c => c.id !== itemId);
    } else if (existing) {
        existing.qty = quantity;
    } else {
        orderCart.push({
            ...item,
            qty: quantity
        });
    }
    updateOrderCartUI();
}

function changeOrderQty(itemId, delta) {
    const inp = document.querySelector(`#orderItemsList input[data-order-item-id="${itemId}"]`);
    if (!inp)
        return;
    inp.value = Math.max(0, (parseInt(inp.value, 10) || 0) + delta);
    syncCartFromInput(inp);
}

function updateCartInputs() {
    document.querySelectorAll('#orderItemsList input[data-order-item-id]').forEach(inp => {
        const itemId = parseInt(inp.dataset.orderItemId, 10);
        const cartItem = orderCart.find(c => c.id === itemId);
        inp.value = cartItem ? cartItem.qty : 0;
    }
    );
}

function updateOrderCartUI() {
    const container = document.getElementById('orderCartSummary');
    if (!container)
        return;
    if (!orderCart.length) {
        container.innerHTML = '<div style="color:var(--text-muted);font-size:13px">Your cart is empty. Add plates from the menu.</div>';
        return;
    }

    const total = orderCart.reduce( (sum, c) => sum + (c.price * c.qty), 0);
    container.innerHTML = `
    <div style="display:flex;justify-content:space-between;align-items:center;gap:12px;margin-bottom:10px">
      <div><strong>Your Cart</strong><div style="font-size:11px;color:var(--text-muted)">Selected plates for this order</div></div>
      <button class="btn-ghost btn-small" type="button" onclick="clearOrderCart()">Clear</button>
    </div>
    ${orderCart.map(item => `
      <div style="display:flex;justify-content:space-between;align-items:center;gap:10px;padding:10px;border:0.5px solid var(--border);border-radius:10px;background:var(--surface2);">
        <div style="flex:1;min-width:0">
          <div style="font-weight:600;font-size:13px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">${esc(item.name)}</div>
          <div style="font-size:11px;color:var(--text-muted)">${item.qty} × ${fmtPrice(item.price)}</div>
        </div>
        <div style="display:flex;align-items:center;gap:6px">
          <button class="btn-ghost btn-small" type="button" onclick="changeOrderQty(${item.id},-1)">−</button>
          <span style="min-width:24px;text-align:center">${item.qty}</span>
          <button class="btn-ghost btn-small" type="button" onclick="changeOrderQty(${item.id},1)">+</button>
          <button class="btn-ghost btn-small" type="button" onclick="removeFromCart(${item.id})"><i class="fas fa-trash"></i></button>
        </div>
      </div>
    `).join('')}
    <div style="display:flex;justify-content:space-between;align-items:center;padding:12px 0 0;margin-top:12px;border-top:0.5px solid var(--border);font-weight:600;color:var(--gold)">Total:<span>${fmtPrice(total)}</span></div>
  `;

    // Update nav cart count
    const cartCountEl = document.getElementById('cartCount');
    if (cartCountEl)
        cartCountEl.textContent = orderCart.reduce( (sum, c) => sum + c.qty, 0);
}

function clearOrderCart() {
    orderCart = [];
    updateOrderCartUI();
    updateCartInputs();
}

function renderMenu() {
    const grid = document.getElementById('menuGrid');
    const filtered = activeFilter === 'all' ? menuData : activeFilter === 'veg' ? menuData.filter(i => i.veg) : menuData.filter(i => i.cat === activeFilter);

    if (!filtered.length) {
        grid.innerHTML = `<div class="fade-in" style="grid-column:1/-1;padding:2rem;border:0.5px solid var(--border);border-radius:var(--radius-lg);color:var(--text-muted)">No dishes found for this filter.</div>`;
        observeFadeIns();
        return;
    }

    grid.innerHTML = filtered.map(item => `
    <div class="menu-card fade-in" onclick="openItemModal(${item.id})">
      <div class="menu-card-img">
        ${item.image ? `<img class="menu-card-img-photo" src="${esc(item.image)}" alt="${esc(item.name)}">` : `<div class="menu-card-img-fallback">${esc((item.name || '?').charAt(0))}</div>`}
        ${(item.badges || []).length ? `<div class="menu-badges">${(item.badges || []).map(b => `<span class="menu-badge badge-${b === 'popular' ? 'popular' : b === 'new' ? 'new' : b === 'veg' ? 'veg' : 'spicy'}">${b === 'popular' ? 'Popular' : b === 'new' ? 'New' : b === 'veg' ? 'Vegetarian' : b === 'recommended' ? '★ Chef Pick' : 'Spicy'}</span>`).join('')}</div>` : ''}
      </div>
      <div class="menu-card-body">
        <div class="menu-card-category">${esc(item.cat)}</div>
        <div class="menu-card-name">${esc(item.name)}</div>
        <div class="menu-card-desc">${esc((item.desc || '').substring(0, 80))}...</div>
        <div class="menu-card-allergens">${(item.allergens || []).map(a => `<span class="allergen">${esc(a)}</span>`).join('')}</div>
        <div class="menu-card-footer">
          <div class="menu-price">${fmtPrice(item.price)}</div>
          <div style="display:flex;align-items:center;gap:8px">
            <div class="menu-rating"><span class="star">★</span> ${item.rating} <span>(${item.reviews})</span></div>
            <button class="add-btn" onclick="event.stopPropagation(); addToCart(${item.id})">+</button>
          </div>
        </div>
      </div>
    </div>`).join('');
    observeFadeIns();
}

function filterMenu(cat, btn) {
    activeFilter = cat;
    document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    renderMenu();
}

async function loadMenu() {
    try {
        menuData = await apiFetch('/menu');
    } catch {
        try {
            menuData = await apiFetch('/client/menu');
        } catch {
            menuData = [];
        }
    }
    renderMenu();
}

// ─── Reviews ───────────────────────────────────────────────────────────────────
function renderReviews() {
    document.getElementById('reviewsGrid').innerHTML = reviewsData.map(r => `
    <div class="review-card fade-in">
      <div class="review-stars">
        ${'★'.repeat(r.rating).split('').map( () => `<span style="color:var(--gold);font-size:14px">★</span>`).join('')}
        ${'★'.repeat(5 - r.rating).split('').map( () => `<span style="color:var(--text-dim);font-size:14px">★</span>`).join('')}
      </div>
      <div class="review-text">"${esc(r.text)}"</div>
      <div class="review-author">
        <div class="review-avatar">${esc(r.avatar)}</div>
        <div><div class="review-name">${esc(r.name)}</div><div class="review-date">${fmtDate(r.date)}</div></div>
        <div style="margin-left:auto"><div class="review-platform">${esc(r.platform)}</div></div>
      </div>
      <div class="ai-reply"><div class="ai-reply-label">NOIR Response</div>Merci beaucoup, ${esc(r.name.split(' ')[0])}. We look forward to welcoming you again soon.</div>
    </div>`).join('');
    observeFadeIns();
}

async function loadReviews() {
    try {
        reviewsData = await apiFetch('/reviews');
        renderReviews();
    } catch {
        reviewsData = [];
    }
}

function setRating(n) {
    userRating = n;
    document.querySelectorAll('.star-btn').forEach( (s, i) => s.classList.toggle('active', i < n));
}

async function submitReview() {
    if (!isLoggedIn()) {
        openAuthModal('login');
        showToast('Login required to review', '', '<i class="fas fa-user-lock"></i>');
        return;
    }
    const payload = {
        date: document.getElementById('reviewDate').value,
        rating: userRating,
        text: document.getElementById('reviewText').value.trim(),
    };
    try {
        const review = await apiFetch('/client/reviews', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        reviewsData.unshift(review);
        renderReviews();
        document.getElementById('reviewText').value = '';
        showToast('Thank you for your review!', 'gold', '<i class="fas fa-check-circle"></i>');
    } catch (err) {
        showToast(err.message, '', '<i class="fas fa-exclamation-triangle"></i>');
    }
}

// ─── Auth modal ────────────────────────────────────────────────────────────────
function openAuthModal(mode='login') {
    switchAuthTab(mode);
    document.getElementById('authOverlay').classList.add('open');
}

function closeAuthModal(event) {
    if (!event || event.target.id === 'authOverlay')
        document.getElementById('authOverlay').classList.remove('open');
}

function switchAuthTab(mode) {
    const login = mode !== 'signup';
    document.getElementById('authLoginTab').classList.toggle('active', login);
    document.getElementById('authSignupTab').classList.toggle('active', !login);
    document.getElementById('clientLoginForm').classList.toggle('hidden', !login);
    document.getElementById('clientSignupForm').classList.toggle('hidden', login);
}

document.getElementById('clientLoginForm').addEventListener('submit', async e => {
    e.preventDefault();
    try {
        const data = await apiFetch('/auth/login', {
            method: 'POST',
            body: JSON.stringify({
                email: document.getElementById('clientLoginEmail').value.trim(),
                password: document.getElementById('clientLoginPassword').value,
            }),
        });
        setClientSession(data);
        closeAuthModal();
        e.target.reset();
        showToast(`Welcome back, ${data.user.firstName}`, 'gold', '<i class="fas fa-user-check"></i>');
    } catch (err) {
        showToast(err.message, '', '<i class="fas fa-exclamation-triangle"></i>');
    }
}
);

document.getElementById('clientSignupForm').addEventListener('submit', async e => {
    e.preventDefault();
    try {
        const data = await apiFetch('/auth/signup', {
            method: 'POST',
            body: JSON.stringify({
                firstName: document.getElementById('clientFirstName').value.trim(),
                lastName: document.getElementById('clientLastName').value.trim(),
                email: document.getElementById('clientSignupEmail').value.trim(),
                password: document.getElementById('clientSignupPassword').value,
            }),
        });
        setClientSession(data);
        closeAuthModal();
        e.target.reset();
        showToast(`Welcome to NOIR, ${data.user.firstName}`, 'gold', '<i class="fas fa-user-plus"></i>');
    } catch (err) {
        showToast(err.message, '', '<i class="fas fa-exclamation-triangle"></i>');
    }
}
);

// ─── Session management ────────────────────────────────────────────────────────
function setClientSession(data) {
    if (!data) {
        clientSession = { token: '', user: null };
    } else {
        clientSession = {
            token: data.token != null ? data.token : clientSession.token,
            user: data.user != null ? data.user : clientSession.user
        };
    }
    if (clientSession.token)
        localStorage.setItem(clientTokenKey, clientSession.token);
    else
        localStorage.removeItem(clientTokenKey);
    if (clientSession.user)
        localStorage.setItem(clientUserKey, JSON.stringify(clientSession.user));
    else
        localStorage.removeItem(clientUserKey);
    updateClientUiState();
}

function updateClientUiState() {
    const loggedIn = isLoggedIn();
    document.getElementById('authBtn').classList.toggle('hidden', loggedIn);
    document.getElementById('clientLogoutBtn').classList.toggle('hidden', !loggedIn);
    document.getElementById('accountBtn').classList.toggle('hidden', !loggedIn);
    document.querySelectorAll('.client-only').forEach(el => el.classList.toggle('hidden', !loggedIn));
    renderClientAvatar();
    if (loggedIn) {
        prefillClientFields();
        loadClientPortalData().catch( () => {}
        );
        loadClientOrders().catch( () => {}
        );
    } else {
        selectedReservationMenuIds.clear();
        clientOrders = [];
    }
}

function getClientAvatarHtml() {
    const user = clientSession.user;
    if (user?.profilePicture) {
        return `<img src="${esc(user.profilePicture)}" alt="Profile" style="width:100%;height:100%;object-fit:cover;border-radius:50%;">`;
    }
    return '<i class="fa-solid fa-circle-user"></i>';
}

function renderClientAvatar() {
    const accountBtn = document.getElementById('accountBtn');
    if (accountBtn)
        accountBtn.innerHTML = getClientAvatarHtml();
}

function prefillClientFields() {
    if (!clientSession.user)
        return;
    const u = clientSession.user;
    const fields = [['resFirstNameModal', u.firstName], ['resLastNameModal', u.lastName], ['resEmailModal', u.email], ['orderNameModal', `${u.firstName || ''} ${u.lastName || ''}`.trim()], ];
    fields.forEach( ([id,val]) => {
        const el = document.getElementById(id);
        if (el && !el.value)
            el.value = val || '';
    }
    );
}

async function logoutClient() {
    try {
        await apiFetch('/auth/logout', {
            method: 'POST'
        });
    } catch {}
    setClientSession(null);
    showToast('You have been logged out', 'gold', '<i class="fas fa-door-open"></i>');
}

async function restoreClientSession() {
    if (clientSession.token && clientSession.user)
        updateClientUiState();
    if (!clientSession.token)
        return;
    try {
        const user = await apiFetch('/auth/me');
        clientSession.user = user;
        localStorage.setItem(clientUserKey, JSON.stringify(user));
        updateClientUiState();
    } catch {
        setClientSession(null);
        showToast('Your session expired. Please login again.', '', '<i class="fas fa-clock"></i>');
    }
}

// ─── Client portal data ────────────────────────────────────────────────────────
async function loadClientPortalData() {
    if (!isLoggedIn())
        return;
    try {
        const portal = await apiFetch('/client/portal');
        renderClientOffers(portal.offers || []);
        renderClientLoyalty(portal.loyalty || {});
        if (portal.about?.story)
            document.getElementById('aboutStory').textContent = portal.about.story;
        if (portal.user)
            document.getElementById('loyaltyName').textContent = `${portal.user.firstName} ${portal.user.lastName}`;
    } catch {}
}

function renderClientOffers(offers) {
    const grid = document.getElementById('offersGrid');
    if (!grid)
        return;
    grid.innerHTML = offers.map( (o, i) => `
    <div class="offer-card ${i % 2 === 0 ? 'offer-gold' : 'offer-dark'} fade-in">
      <div class="offer-discount">${esc(o.discount)}</div>
      <div class="offer-tag">Member Offer</div>
      <div class="offer-title">${esc(o.title)}</div>
      <div class="offer-desc">${esc(o.description)}</div>
      <div class="offer-code" onclick="copyCode('${esc(o.code)}')">${esc(o.code)}</div>
      <div class="offer-expiry">Available for signed-in clients</div>
    </div>`).join('');
}

function renderClientLoyalty(l) {
    document.getElementById('loyaltyPoints').textContent = Number(l.points || 0).toLocaleString();
    document.getElementById('loyaltyTier').textContent = `${l.tier || 'Bronze'} Member`;
    document.getElementById('loyaltyCurrentTier').textContent = `Current: ${l.tier || 'Bronze'}`;
    document.getElementById('loyaltyToNextTier').textContent = `${l.pointsToNextTier || 0} pts to next tier`;
    const ratio = Math.max(0, Math.min(100, 100 - (((l.pointsToNextTier || 0) / Math.max((l.points || 0) + (l.pointsToNextTier || 0), 1)) * 100)));
    document.getElementById('loyaltyBar').style.width = `${ratio}%`;
    const act = document.getElementById('loyaltyActivity');
    act.innerHTML = l.activity?.length ? l.activity.map(a => `<div style="display:flex;justify-content:space-between;font-size:12px;color:var(--text-muted)"><span>${esc(a.label)}</span><span style="color:var(--green)">+${a.points} pts</span></div>`).join('') : '<div style="display:flex;justify-content:space-between;font-size:12px;color:var(--text-muted)"><span>No activity yet</span><span style="color:var(--text-dim)">0 pts</span></div>';
}

// ─── Item modal ─────────────────────────────────────────────────────────────────
function openItemModal(id) {
    const item = menuData.find(e => e.id === id);
    if (!item)
        return;
    document.getElementById('modalContent').innerHTML = `
    <div style="text-align:center;margin-bottom:1.5rem">
      <img src="${esc(item.image || '')}" alt="${esc(item.name)}" style="width:100%;max-height:220px;object-fit:cover;border-radius:12px;margin-bottom:0.75rem;border:0.5px solid var(--border)">
      <div style="font-size:10px;letter-spacing:3px;text-transform:uppercase;color:var(--gold)">${esc(item.cat)}</div>
      <h2 style="font-family:'Cormorant Garamond',serif;font-size:2rem;margin-top:0.25rem">${esc(item.name)}</h2>
    </div>
    <p style="font-size:13px;color:var(--text-muted);line-height:1.8;margin-bottom:1.5rem">${esc(item.desc)}</p>
    <div style="display:flex;gap:6px;flex-wrap:wrap;margin-bottom:1.5rem">${(item.allergens || []).map(a => `<span class="allergen">${esc(a)}</span>`).join('')}</div>
    <div style="display:flex;justify-content:space-between;align-items:center;padding-top:1rem;border-top:0.5px solid var(--border)">
      <div>
        <div style="font-family:'Cormorant Garamond',serif;font-size:2rem;color:var(--gold)">${fmtPrice(item.price)}</div>
        <div style="font-size:11px;color:var(--text-muted)">★ ${item.rating} · ${item.reviews} reviews</div>
      </div>
      <div style="display:flex;gap:10px;flex-wrap:wrap;justify-content:flex-end">
        <button class="btn-primary" onclick="event.stopPropagation(); addToCart(${item.id}); document.getElementById('itemModal').classList.remove('open')">+</button>
      </div>
    </div>`;
    document.getElementById('itemModal').classList.add('open');
}

function closeModal(e) {
    if (e.target.id === 'itemModal')
        document.getElementById('itemModal').classList.remove('open');
}

// ─── ORDER MODAL ───────────────────────────────────────────────────────────────
// Tab-based: "New Order" | "My Orders"  (no split layout — works on all screen sizes)

let orderModalTab = 'form';
// 'form' | 'list'

async function openOrderModal(menuItemId=null) {
    if (!isLoggedIn()) {
        openAuthModal('login');
        showToast('Please login to place an order', '', '<i class="fas fa-user-lock"></i>');
        return;
    }
    if (!menuData.length)
        await loadMenu();
    if (menuItemId !== null)
        addToCart(menuItemId);
    document.getElementById('orderOverlay').classList.add('open');
    switchOrderTab('order');
    // always open on form tab
    populateOrderForm();
    loadClientOrdersListModal();
    // pre-load list in background
}

function closeOrderModal(event) {
    if (!event || event.target.id === 'orderOverlay')
        document.getElementById('orderOverlay').classList.remove('open');
}

function switchOrderTab(mode) {
    const isOrder = mode === 'order';
    document.getElementById('OrderTab').classList.toggle('active', isOrder);
    document.getElementById('OrderList').classList.toggle('active', !isOrder);
    document.getElementById('orderFormCol').classList.toggle('hidden', !isOrder);
    document.getElementById('ordersListCol').classList.toggle('hidden', isOrder);
    if (!isOrder)
        loadClientOrdersListModal();
}

function populateOrderForm(menuItemId=null) {
    prefillClientFields();
    const timeInput = document.getElementById('orderTimeModal');
    if (timeInput && !timeInput.value) timeInput.value = '19:00';

    const container = document.getElementById('orderItemsList');
    if (!container) return;
    container.innerHTML = menuData.map(item => {
        const qty = orderCart.find(c => c.id === item.id)?.qty || 0;
        return `
      <div style="display:grid;grid-template-columns:1fr auto;gap:12px;align-items:center;padding:10px;border:0.5px solid var(--border);border-radius:10px;">
        <div>
          <div><img src="${esc(item.image)}" style="width:50px;height:50px;object-fit:cover;border-radius:8px;border:0.5px solid var(--border)"></div>
          <div style="font-weight:600;font-size:13px">${esc(item.name)}</div>
          <div style="font-size:11px;color:var(--text-muted)">${esc(item.cat)} · ${fmtPrice(item.price)}</div>
        </div>
        <div style="display:flex;align-items:center;gap:6px;">
          <button class="btn-ghost btn-small" type="button" onclick="changeOrderQty(${item.id},-1)">−</button>
          <input type="number" min="0" value="${qty}" data-order-item-id="${item.id}" class="order-qty-input" style="width:52px;text-align:center;padding:5px;border:0.5px solid var(--border);border-radius:8px;background:var(--surface2);color:var(--text);" oninput="syncCartFromInput(this)">
          <button class="btn-ghost btn-small" type="button" onclick="changeOrderQty(${item.id},1)">+</button>
        </div>
      </div>`;
    }
    ).join('');

    if (menuItemId !== null) {
        addToCart(menuItemId);
        updateCartInputs();
    }
    updateOrderCartUI();
}

async function submitOrderModal() {
    if (!isLoggedIn()) {
        openAuthModal('login');
        return;
    }

    const items = orderCart.map( ({id, qty}) => ({
        id,
        quantity: qty
    })).filter(i => i.quantity > 0);

    if (!items.length) {
        showToast('Select at least one plate', '', '<i class="fas fa-exclamation-triangle"></i>');
        return;
    }

    const name = document.getElementById('orderNameModal').value.trim();
    const addr = document.getElementById('orderPlaceModal').value.trim();
    const time = document.getElementById('orderTimeModal').value.trim() || 'ASAP';
    const phone = document.getElementById('orderPhoneModal').value.trim();

    if (!name) {
        showToast('Please enter your name', '', '<i class="fas fa-exclamation-triangle"></i>');
        return;
    }
    if (!addr) {
        showToast('Delivery address required', '', '<i class="fas fa-exclamation-triangle"></i>');
        return;
    }
    if (!phone) {
        showToast('Phone number required', '', '<i class="fas fa-exclamation-triangle"></i>');
        return;
    }
    if (phone.replace(/\D/g, '').length < 8) {
        showToast('Phone must be at least 8 digits', '', '<i class="fas fa-exclamation-triangle"></i>');
        return;
    }

    const payload = {
        orderName: name,
        items,
        deliveryAddress: addr,
        deliveryTime: time,
        contactPhone: phone,
        notes: document.getElementById('orderNotesModal')?.value.trim() || '',
    };

    try {
        const order = await apiFetch('/client/orders', {
            method: 'POST',
            body: JSON.stringify(payload)
        });
        showToast(`Order ${order.orderNumber} placed!`, 'gold', '<i class="fas fa-check-circle"></i>');
        clearOrderCart();
        document.querySelectorAll('#orderItemsList input[data-order-item-id]').forEach(inp => inp.value = '0');
        document.getElementById('orderPlaceModal').value = '';
        document.getElementById('orderPhoneModal').value = '';
        if (document.getElementById('orderNotesModal'))
            document.getElementById('orderNotesModal').value = '';
        await loadClientOrders();
        switchOrderTab('list');
    } catch (err) {
        showToast(err.message, '', '<i class="fas fa-exclamation-triangle"></i>');
    }
}

async function loadClientOrders() {
    if (!isLoggedIn()) {
        clientOrders = [];
        return;
    }
    try {
        clientOrders = await apiFetch('/client/orders');
    } catch {
        clientOrders = [];
    }
}

async function loadClientOrdersListModal() {
    const container = document.getElementById('modalOrdersList');
    if (!container)
        return;
    if (!isLoggedIn()) {
        container.innerHTML = '<div style="color:var(--text-muted);font-size:12px">Login to see your orders.</div>';
        return;
    }
    try {
        const orders = await apiFetch('/client/orders');
        clientOrders = orders;
        if (!orders.length) {
            container.innerHTML = '<div style="color:var(--text-muted);font-size:12px">No orders yet. Place your first order!</div>';
            return;
        }
        container.innerHTML = orders.map(o => `
      <div style="background:var(--surface2);border:0.5px solid var(--border);border-radius:var(--radius);padding:14px;font-size:12px;">
        <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:8px;gap:8px;flex-wrap:wrap;">
          <div>
            <strong style="color:var(--gold);font-size:13px">${esc(o.orderNumber)}</strong>
            <div style="color:var(--text-muted);font-size:11px;margin-top:2px">${esc(fmtStatus(o.status))} · ${fmtPrice(o.total)}</div>
          </div>
          <div style="display:flex;gap:6px;flex-wrap:wrap;align-items:center;">
            ${o.status === 'delivered' ? `<button class="btn-ghost btn-small" type="button" onclick="confirmDelivery('${esc(o.orderNumber)}')">Confirm</button>` : ''}
            ${!['cancelled', 'rejected', 'delivered', 'completed'].includes(o.status) ? `<button class="btn-danger btn-small" type="button" onclick="cancelOrderModal('${esc(o.orderNumber)}')">Cancel</button>` : ''}
          </div>
        </div>
        <div style="font-size:11px;color:var(--text-muted);">${esc(o.deliveryAddress || '')} · ${esc(o.contactPhone || '')}</div>
        <div style="font-size:11px;color:var(--text-muted);margin-top:4px"><strong>Track:</strong> ${esc(fmtStatus(o.status))}${o.deliveryTime ? ` · ETA ${esc(o.deliveryTime)}` : ''}</div>
        <div style="font-size:11px;color:var(--text-muted);margin-top:4px;">${(o.items || []).map(i => `${esc(i.name)} ×${i.quantity}`).join(', ')}</div>
      </div>`).join('');
    } catch {
        container.innerHTML = '<div style="color:var(--text-muted);font-size:12px">Unable to load orders.</div>';
    }
}

async function cancelOrderModal(orderNumber) {
    if (!confirm(`Cancel order ${orderNumber}?`))
        return;
    try {
        await apiFetch(`/client/orders/${encodeURIComponent(orderNumber)}`, {
            method: 'DELETE'
        });
        showToast(`Order ${orderNumber} cancelled`, '', '<i class="fas fa-ban"></i>');
        await loadClientOrdersListModal();
    } catch (err) {
        showToast(err.message, '', '<i class="fas fa-exclamation-triangle"></i>');
    }
}

async function confirmDelivery(orderNumber) {
    try {
        await apiFetch(`/client/orders/${encodeURIComponent(orderNumber)}/confirm-delivery`, {
            method: 'PATCH'
        });
        showToast('Delivery confirmed — thank you!', 'gold', '<i class="fas fa-check-circle"></i>');
        await loadClientOrdersListModal();
    } catch (err) {
        showToast(err.message, '', '<i class="fas fa-exclamation-triangle"></i>');
    }
}

// ─── ACCOUNT MODAL ─────────────────────────────────────────────────────────────

async function openAccountModal() {
    if (!isLoggedIn()) {
        openAuthModal('login');
        showToast('Please login to access your account', '', '<i class="fas fa-user-lock"></i>');
        return;
    }
    document.getElementById('accountOverlay').classList.add('open');
    switchAccountTab('profile');
    await loadAccountData();
}

function closeAccountModal(event) {
    if (!event || event.target.id === 'accountOverlay')
        document.getElementById('accountOverlay').classList.remove('open');
}

function switchAccountTab(mode) {
    const isProfile = mode === 'profile';
    document.getElementById('ProfileTab').classList.toggle('active', isProfile);
    document.getElementById('PasswordTab').classList.toggle('active', !isProfile);
    document.getElementById('ProfileForm').classList.toggle('hidden', !isProfile);
    document.getElementById('PasswordForm').classList.toggle('hidden', isProfile);
}

async function loadAccountData() {
    try {
        const user = await apiFetch('/auth/me');
        clientSession.user = user;
        localStorage.setItem(clientUserKey, JSON.stringify(user));
        document.getElementById('accountFirstName').value = user.firstName || '';
        document.getElementById('accountLastName').value = user.lastName || '';
        document.getElementById('accountEmail').value = user.email || '';
        document.getElementById('accountPhone').value = user.phone || '';
        document.getElementById('accountBirthDate').value = user.birthDate || '';
        document.getElementById('accountIsPro').checked = Boolean(user.isPro);
        document.getElementById('accountPhotoPreview').src = user.profilePicture || defaultAvatarSrc;
        document.getElementById('accountPicture').value = '';
        selectedAccountPhotoDataUrl = null;
        renderClientAvatar();
    } catch (err) {
        showToast('Failed to load account data', '', '<i class="fas fa-exclamation-triangle"></i>');
    }
}

async function updateProfile() {
    const firstName = document.getElementById('accountFirstName').value.trim();
    const lastName = document.getElementById('accountLastName').value.trim();
    const email = document.getElementById('accountEmail').value.trim();
    if (!firstName || !lastName || !email) {
        showToast('All fields are required', '', '<i class="fas fa-exclamation-triangle"></i>');
        return;
    }
    try {
        const payload = {
            firstName,
            lastName,
            email,
            phone: document.getElementById('accountPhone').value.trim(),
            birthDate: document.getElementById('accountBirthDate').value,
            isPro: document.getElementById('accountIsPro').checked
        };
        if (selectedAccountPhotoDataUrl) {
            payload.profilePicture = selectedAccountPhotoDataUrl;
        }

        const user = await apiFetch('/auth/me', {
            method: 'PUT',
            body: JSON.stringify(payload)
        });
        showToast('Profile updated successfully', 'gold', '<i class="fas fa-check-circle"></i>');
        selectedAccountPhotoDataUrl = null;
        setClientSession({ user });
        const preview = document.getElementById('accountPhotoPreview');
        if (preview) preview.src = user.profilePicture || defaultAvatarSrc;
        const accountPictureInput = document.getElementById('accountPicture');
        if (accountPictureInput) accountPictureInput.value = '';
    } catch (err) {
        showToast(err.message, '', '<i class="fas fa-exclamation-triangle"></i>');
    }
}

async function changePassword() {
    const oldPassword = document.getElementById('oldPassword').value;
    const newPassword = document.getElementById('newPassword').value;
    if (!oldPassword || !newPassword) {
        showToast('Both passwords are required', '', '<i class="fas fa-exclamation-triangle"></i>');
        return;
    }
    if (newPassword.length < 8) {
        showToast('New password must be at least 8 characters', '', '<i class="fas fa-exclamation-triangle"></i>');
        return;
    }
    try {
        await apiFetch('/auth/me/password', {
            method: 'PUT',
            body: JSON.stringify({
                oldPassword,
                newPassword
            })
        });
        showToast('Password changed successfully', 'gold', '<i class="fas fa-check-circle"></i>');
        document.getElementById('oldPassword').value = '';
        document.getElementById('newPassword').value = '';
    } catch (err) {
        showToast(err.message, '', '<i class="fas fa-exclamation-triangle"></i>');
    }
}

// ─── RESERVATION MODAL ─────────────────────────────────────────────────────────
// Tab-based: "New Reservation" | "My Reservations"

async function openReservationModal() {
    if (!isLoggedIn()) {
        openAuthModal('login');
        showToast('Please login to make a reservation', '', '<i class="fas fa-user-lock"></i>');
        return;
    }
    if (!menuData.length)
        await loadMenu();
    document.getElementById('reservationOverlay').classList.add('open');
    prefillClientFields();
    const d = new Date();
    d.setDate(d.getDate() + 3);
    const dateInput = document.getElementById('resDateModal');
    if (dateInput && !dateInput.value)
        dateInput.value = d.toISOString().split('T')[0];
    populateReservationMenuOptions();
    switchResTab('list');
    // open on list tab to show reservations
    loadClientReservationsList();
    // load list
}

function closeReservationModal(event) {
    if (!event || event.target.id === 'reservationOverlay')
        document.getElementById('reservationOverlay').classList.remove('open');
}

// FIX: The previous version had inverted logic (reserve === 'list' instead of === 'reserve')
function switchResTab(mode) {
    const isReserve = mode === 'reserve';
    document.getElementById('ResTab').classList.toggle('active', isReserve);
    document.getElementById('ResList').classList.toggle('active', !isReserve);
    document.getElementById('reservationFormCol').classList.toggle('hidden', !isReserve);
    document.getElementById('reservationsListCol').classList.toggle('hidden', isReserve);
    if (!isReserve)
        loadClientReservationsList();
}

function switchOrderTab(mode) {
    const isOrder = mode === 'order';
    document.getElementById('OrderTab').classList.toggle('active', isOrder);
    document.getElementById('OrderList').classList.toggle('active', !isOrder);
    document.getElementById('orderFormCol').classList.toggle('hidden', !isOrder);
    document.getElementById('ordersListCol').classList.toggle('hidden', isOrder);
    if (!isOrder)
        loadClientOrdersListModal();
}

function selectTimeModal(el) {
    document.querySelectorAll('#reservationOverlay .time-slot:not(.unavailable)').forEach(s => s.classList.remove('selected'));
    el.classList.add('selected');
}

function selectExpModal(el) {
    document.querySelectorAll('#reservationOverlay .exp-option').forEach(o => o.classList.remove('selected'));
    el.classList.add('selected');
}

function toggleAllergySelectionModal() {
    const cb = document.getElementById('mentionAllergiesModal');
    document.getElementById('allergySelectorWrapModal')?.classList.toggle('hidden', !cb.checked);
}

function populateReservationMenuOptions() {
    const container = document.getElementById('reservationMenuOptionsModal');
    if (!container || !menuData.length)
        return;
    container.innerHTML = menuData.map(item => `
    <div style="display:grid;grid-template-columns:1fr auto auto;gap:12px;align-items:center;padding:10px;border:0.5px solid var(--border);border-radius:10px;">
      <div>
        <div><img src="${esc(item.image)}" style="width:50px;height:50px;object-fit:cover;border-radius:8px;border:0.5px solid var(--border)"></div>  
        <div style="font-weight:600;font-size:13px">${esc(item.name)}</div>
        <div style="font-size:11px;color:var(--text-muted)">${esc(item.cat)} · ${fmtPrice(item.price)}</div>
      </div>
      <div style="display:flex;align-items:center;gap:6px;">
        <button type="button" onclick="changeResMenuQty('${item.id}', -1)" style="width:24px;height:24px;border-radius:50%;background:var(--surface2);border:1px solid var(--border);cursor:pointer">-</button>
        <span id="res-qty-${item.id}" style="min-width:20px;text-align:center">0</span>
        <button type="button" onclick="changeResMenuQty('${item.id}', 1)" style="width:24px;height:24px;border-radius:50%;background:var(--surface2);border:1px solid var(--border);cursor:pointer">+</button>
      </div>
    </div>`).join('');
}

function changeResMenuQty(id, delta) {
    const span = document.getElementById(`res-qty-${id}`);
    if (!span)
        return;
    let qty = parseInt(span.textContent, 10) || 0;
    qty = Math.max(0, qty + delta);
    span.textContent = qty;
    updateResMenuSummary();
}

function updateResMenuSummary() {
    const selected = Array.from(document.querySelectorAll('#reservationOverlay [id^="res-qty-"]')).filter(span => parseInt(span.textContent, 10) > 0).map(span => {
        const id = span.id.replace('res-qty-', '');
        const item = menuData.find(m => m.id === parseInt(id, 10));
        const qty = parseInt(span.textContent, 10);
        return {
            name: item?.name || `Item ${id}`,
            qty
        };
    }
    );
    const summary = document.getElementById('reservationMenuSummaryModal');
    if (!summary)
        return;
    if (!selected.length) {
        summary.textContent = 'No dishes selected yet.';
        return;
    }
    const total = selected.reduce( (sum, s) => sum + s.qty, 0);
    const names = selected.map(s => `${s.name} (${s.qty})`);
    summary.textContent = `${total} dish${total !== 1 ? 'es' : ''} selected: ${names.join(', ')}`;
}

function getSelectedReservationMenusModal() {
    return Array.from(document.querySelectorAll('#reservationOverlay [id^="res-qty-"]')).filter(span => parseInt(span.textContent, 10) > 0).map(span => {
        const id = span.id.replace('res-qty-', '');
        const item = menuData.find(m => m.id === parseInt(id, 10));
        const qty = parseInt(span.textContent, 10);
        return `${item?.name || `Item ${id}`} (${qty})`;
    }
    );
}

function getSelectedAllergiesModal() {
    const checked = Array.from(document.querySelectorAll('#reservationOverlay .allergy-option input:checked')).map(i => i.value);
    const other = document.getElementById('allergyOtherModal')?.value?.trim();
    if (other)
        checked.push(other);
    return checked;
}

async function submitReservationModal() {
    if (!isLoggedIn()) {
        openAuthModal('login');
        return;
    }

    const selectedTime = document.querySelector('#reservationOverlay .time-slot.selected')?.textContent?.trim();
    const selectedExp = document.querySelector('#reservationOverlay .exp-option.selected .exp-name')?.textContent?.trim();

    if (!selectedTime) {
        showToast('Please select a time', '', '<i class="fas fa-exclamation-triangle"></i>');
        return;
    }
    if (!selectedExp) {
        showToast('Please select a dining experience', '', '<i class="fas fa-exclamation-triangle"></i>');
        return;
    }

    const phone = document.getElementById('resPhoneModal').value.trim();
    const date = document.getElementById('resDateModal').value;
    const rawGuests = document.getElementById('resGuestsModal').value;
    const guests = parseInt(rawGuests, 10) || 1;

    if (!phone) {
        showToast('Phone number required', '', '<i class="fas fa-exclamation-triangle"></i>');
        return;
    }
    if (phone.replace(/\D/g, '').length < 8) {
        showToast('Phone must be at least 8 digits', '', '<i class="fas fa-exclamation-triangle"></i>');
        return;
    }
    if (!date) {
        showToast('Date required', '', '<i class="fas fa-exclamation-triangle"></i>');
        return;
    }

    const payload = {
        phone,
        date,
        guests,
        time: selectedTime,
        experience: selectedExp,
        menuSelections: getSelectedReservationMenusModal(),
        allergySelections: getSelectedAllergiesModal(),
        specialRequests: document.getElementById('resSpecialRequestsModal')?.value?.trim() || '',
    };

    try {
        const res = await apiFetch('/client/reservations', {
            method: 'POST',
            body: JSON.stringify(payload)
        });

        // Flash success inside the form col
        document.getElementById('reservationFormModal').style.display = 'none';
        const succ = document.getElementById('resSuccessModal');
        succ.classList.add('show');
        document.getElementById('resSuccessCodeModal').textContent = `Booking Ref: ${res.confirmationCode}`;
        showToast('Reservation confirmed!', 'gold', '<i class="fas fa-check-circle"></i>');

        setTimeout( () => {
            document.getElementById('reservationFormModal').style.display = 'block';
            succ.classList.remove('show');
            switchResTab('list');
            // auto-switch to list after confirmation
        }
        , 2000);

        await loadClientReservationsList();
    } catch (err) {
        showToast(err.message, '', '<i class="fas fa-exclamation-triangle"></i>');
    }
}

async function loadClientReservationsList() {
    const container = document.getElementById('modalReservationsList');
    if (!container)
        return;
    if (!isLoggedIn()) {
        container.innerHTML = '<div style="color:var(--text-muted);font-size:12px">Login to see your reservations.</div>';
        return;
    }
    try {
        const list = await apiFetch('/client/reservations');
        if (!list.length) {
            container.innerHTML = '<div style="color:var(--text-muted);font-size:12px">No reservations yet. Book your first table!</div>';
            return;
        }
        container.innerHTML = list.map(r => `
      <div style="background:var(--surface2);border:0.5px solid var(--border);border-radius:var(--radius);padding:14px;font-size:12px;">
        <div style="display:flex;justify-content:space-between;align-items:flex-start;margin-bottom:6px;gap:8px;flex-wrap:wrap;">
          <div>
            <strong style="color:var(--gold);font-size:13px">${esc(r.date)} at ${esc(r.time)}</strong>
            <div style="color:var(--text-muted);font-size:11px;margin-top:2px">${r.guests} guest${r.guests !== 1 ? 's' : ''} · ${esc(r.experience || 'À la Carte')}</div>
          </div>
          <span style="background:${r.status === 'confirmed' ? 'rgba(46,204,113,.15)' : r.status === 'cancelled' ? 'rgba(192,57,43,.15)' : 'rgba(201,168,76,.15)'};color:${r.status === 'confirmed' ? 'var(--green)' : r.status === 'cancelled' ? '#ff9a90' : 'var(--gold)'};padding:3px 8px;border-radius:4px;font-size:10px;letter-spacing:1px;text-transform:uppercase;">${esc(fmtStatus(r.status))}</span>
        </div>
        <div style="color:var(--text-dim);font-size:11px;margin-bottom:8px">Ref: ${esc(r.confirmationCode || r.id)}</div>
        ${!['cancelled', 'rejected'].includes(r.status) ? `<button class="btn-danger btn-small" type="button" onclick="cancelReservationModal('${esc(r.id)}')">Cancel Reservation</button>` : ''}
      </div>`).join('');
    } catch {
        container.innerHTML = '<div style="color:var(--text-muted);font-size:12px">Unable to load reservations.</div>';
    }
}

async function cancelReservationModal(id) {
    if (!confirm('Cancel this reservation?'))
        return;
    try {
        await apiFetch(`/client/reservations/${encodeURIComponent(id)}`, {
            method: 'DELETE'
        });
        showToast('Reservation cancelled', '', '<i class="fas fa-ban"></i>');
        await loadClientReservationsList();
    } catch (err) {
        showToast(err.message, '', '<i class="fas fa-exclamation-triangle"></i>');
    }
}

// ─── Order tracker sidebar ──────────────────────────────────────────────────────
function toggleTrackerSidebar(forceOpen) {
    const sidebar = document.getElementById('tracker-sidebar');
    const shouldOpen = typeof forceOpen === 'boolean' ? forceOpen : !sidebar.classList.contains('open');
    sidebar.classList.toggle('open', shouldOpen);
    document.body.classList.toggle('tracker-open', shouldOpen);
    if (shouldOpen)
        setTimeout( () => document.getElementById('trackInput')?.focus(), 120);
}

async function trackOrder(opts={}) {
    if (!isLoggedIn()) {
        openAuthModal('login');
        showToast('Login required', '', '<i class="fas fa-user-lock"></i>');
        return;
    }
    const num = document.getElementById('trackInput').value.trim();
    if (!num) {
        if (!opts.silent)
            showToast('Enter an order number', '', '<i class="fas fa-exclamation-triangle"></i>');
        return;
    }
    try {
        const order = await apiFetch(`/client/orders/${encodeURIComponent(num)}`);
        renderTracker(order);
        toggleTrackerSidebar(true);
        if (!opts.silent)
            showToast(`Order ${order.orderNumber} found`, 'gold', '<i class="fas fa-location-dot"></i>');
    } catch (err) {
        document.getElementById('trackerSteps').innerHTML = '';
        document.getElementById('trackerEmpty').style.display = 'block';
        if (!opts.silent)
            showToast(err.message, '', '<i class="fas fa-exclamation-triangle"></i>');
    }
}

function renderTracker(order) {
    document.getElementById('trackerEmpty').style.display = 'none';
    document.getElementById('trackerSteps').innerHTML = `
    <div class="tracker-steps">
      ${(order.steps || []).map(s => `
        <div class="tracker-step">
          <div class="step-circle ${s.state === 'done' ? 'done' : ''} ${s.state === 'active' ? 'active' : ''}">
            <i class="fas ${esc(s.icon)}"></i>
          </div>
          <div class="step-info">
            <div class="step-label">${esc(s.label)}</div>
            <div class="step-time">${esc(s.time)}</div>
          </div>
        </div>`).join('')}
    </div>
    <div style="margin-top:2rem;background:var(--surface);border:0.5px solid var(--border);border-radius:var(--radius);padding:1.25rem">
      <div style="font-size:10px;letter-spacing:2px;text-transform:uppercase;color:var(--gold);margin-bottom:0.75rem">Your Order</div>
      ${(order.items || []).map(i => `
        <div style="display:grid;grid-template-columns:34px 1fr auto;align-items:center;gap:8px;margin-bottom:6px">
          ${i.image ? `<img src="${esc(i.image)}" style="width:34px;height:34px;object-fit:cover;border-radius:6px;border:0.5px solid var(--border)">` : `<div style="width:34px;height:34px;border-radius:6px;border:0.5px solid var(--border);display:flex;align-items:center;justify-content:center;color:var(--gold);font-size:11px">${esc((i.name || '?').slice(0, 1))}</div>`}
          <span style="font-size:13px;color:var(--text-muted)">${esc(i.name)} × ${i.quantity}</span>
          <span style="font-size:13px">${fmtPrice(i.price * i.quantity)}</span>
        </div>`).join('')}
      <div style="border-top:0.5px solid var(--border);margin-top:0.75rem;padding-top:0.75rem;display:flex;justify-content:space-between;font-size:13px">
        <span style="color:var(--text-muted)">Total</span>
        <span style="color:var(--gold);font-family:'Cormorant Garamond',serif;font-size:1.1rem">${fmtPrice(order.total)}</span>
      </div>
    </div>`;
}

// ─── Search ────────────────────────────────────────────────────────────────────
function openSearch() {
    document.getElementById('searchOverlay').classList.add('open');
    setTimeout( () => document.getElementById('searchInput').focus(), 100);
}
function closeSearch(e) {
    if (!e || e.target.id === 'searchOverlay')
        document.getElementById('searchOverlay').classList.remove('open');
}
function searchFor(q) {
    document.getElementById('searchInput').value = q;
    handleSearch(q);
}
function handleSearch(q) {
    const res = document.getElementById('searchResults');
    if (!q) {
        res.innerHTML = '';
        return;
    }
    const matches = menuData.filter(i => i.name.toLowerCase().includes(q.toLowerCase()) || i.desc.toLowerCase().includes(q.toLowerCase()) || (i.allergens || []).some(a => a.toLowerCase().includes(q.toLowerCase())));
    res.innerHTML = matches.length ? matches.map(i => `<div onclick="closeSearch();openItemModal(${i.id})" style="display:flex;gap:1rem;align-items:center;padding:0.75rem;border-radius:var(--radius);cursor:pointer;transition:background .2s" onmouseover="this.style.background='var(--surface)'" onmouseout="this.style.background='transparent'"><img src="${esc(i.image || '')}" style="width:46px;height:46px;object-fit:cover;border-radius:8px;border:0.5px solid var(--border)"><div><div style="font-size:14px">${esc(i.name)}</div><div style="font-size:11px;color:var(--text-muted)">${esc(i.cat)} · ${fmtPrice(i.price)}</div></div></div>`).join('') : `<div style="font-size:13px;color:var(--text-dim)">No dishes found for "${esc(q)}"</div>`;
}

// ─── Keyboard shortcuts ─────────────────────────────────────────────────────────
document.addEventListener('keydown', e => {
    if (e.key === 'Escape') {
        closeSearch();
        closeAuthModal();
        toggleTrackerSidebar(false);
        document.getElementById('itemModal').classList.remove('open');
        closeOrderModal();
        closeReservationModal();
        closeAccountModal();
    }
    if (e.key === '/' && document.activeElement.tagName !== 'INPUT' && document.activeElement.tagName !== 'TEXTAREA') {
        e.preventDefault();
        openSearch();
    }
}
);

// ─── AI Chatbot ────────────────────────────────────────────────────────────────
// Uses the local restaurant backend for responses.
// Maintains a short conversation history for context.

const chatHistory = [];

function toggleChat() {
    document.getElementById('chatBubble').classList.toggle('open');
}

async function sendChat() {
    const inp = document.getElementById('chatInput');
    const msg = inp.value.trim();
    if (!msg)
        return;

    const msgs = document.getElementById('chatMessages');

    // Show user message
    msgs.innerHTML += `<div class="chat-msg user">${esc(msg)}</div>`;
    inp.value = '';
    msgs.scrollTop = msgs.scrollHeight;

    // Show typing indicator
    const typingId = 'typing-' + Date.now();
    msgs.innerHTML += `<div class="chat-msg bot" id="${typingId}" style="opacity:0.6;font-style:italic">…</div>`;
    msgs.scrollTop = msgs.scrollHeight;

    // Add to history
    chatHistory.push({
        role: 'user',
        content: msg
    });
    // Keep last 10 turns to avoid context explosion
    if (chatHistory.length > 20)
        chatHistory.splice(0, 2);

    try {
        const response = await fetch('/api/chat', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ message: msg, history: chatHistory }),
        });

        if (!response.ok) {
            throw new Error(`Chat service returned status ${response.status}`);
        }

        const data = await response.json();
        const reply = data.data?.reply || 'I apologize — I could not process your request right now. Please try again or call us at +216 71 240 240.';
        const suggestions = data.data?.suggestions || [];
        const items = data.data?.items || [];

        // Add assistant reply to history
        chatHistory.push({ role: 'assistant', content: reply });

        // Replace typing indicator with real response
        const typingEl = document.getElementById(typingId);
        if (typingEl) {
            typingEl.style.opacity = '1';
            typingEl.style.fontStyle = 'normal';
            typingEl.textContent = reply;

            // Append suggestions if present
            if (suggestions.length) {
                const suggWrap = document.createElement('div');
                suggWrap.className = 'chat-suggestions';
                suggestions.forEach(s => {
                    const btn = document.createElement('button');
                    btn.className = 'chat-suggestion';
                    btn.type = 'button';
                    btn.textContent = s;
                    btn.addEventListener('click', (ev) => {
                        ev.preventDefault();
                        inp.value = s;
                        setTimeout(() => sendChat(), 50);
                    });
                    suggWrap.appendChild(btn);
                });
                typingEl.appendChild(document.createElement('br'));
                typingEl.appendChild(suggWrap);
            }

            // If items are present, show a short list after reply
            if (items.length) {
                const list = document.createElement('div');
                list.className = 'chat-items';
                list.style.marginTop = '8px';
                list.textContent = items.join(', ');
                typingEl.appendChild(list);
            }
        }
    } catch (error) {
        console.error('Chat request failed:', error);
        const typingEl = document.getElementById(typingId);
        if (typingEl) {
            typingEl.textContent = 'My apologies — I could not reach the restaurant assistant right now. Please refresh the page or call us at +216 71 240 240.';
        }
    }

    msgs.scrollTop = msgs.scrollHeight;
}

// Allow Enter key to send
document.getElementById('chatInput').addEventListener('keydown', e => {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        sendChat();
    }
}
);

// ─── Misc ───────────────────────────────────────────────────────────────────────
function copyCode(code) {
    navigator.clipboard?.writeText(code).catch( () => {}
    );
    showToast(`Code "${code}" copied!`, 'gold', '<i class="fas fa-clipboard"></i>');
}

// ─── Init ───────────────────────────────────────────────────────────────────────
async function init() {
    document.getElementById('reviewDate').value = new Date().toISOString().split('T')[0];
    updateClientUiState();
    await restoreClientSession();
    updateOrderCartUI();
    // Initialize cart UI

    const tasks = await Promise.allSettled([loadMenu(), loadReviews()]);
    const failed = tasks.filter(t => t.status === 'rejected');
    if (failed.length)
        showToast('Some data failed to load', '', '<i class="fas fa-exclamation-triangle"></i>');

    setTimeout( () => observeFadeIns(), 100);
}

init().catch(err => {
    console.error('Init error:', err);
}
);
