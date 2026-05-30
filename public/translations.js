// NOIR — translations.js
// Usage: toggleLang() switches between 'en' and 'fr'

const TRANSLATIONS = {
  en: {
    // Nav
    navMenu: "Menu",
    navReserve: "Reserve",
    navLoyalty: "Loyalty",
    navOffers: "Offers",
    navChef: "Chef",
    navAbout: "About us",
    navLogin: "Login",
    navLogout: "Logout",
    navOrder: "Order",
    navReserveTable: "Reserve Table",
    navLang: "FR",

    // Hero
    heroEyebrow: "Est. 2015 · Michelin ⭐⭐",
    heroTitle: "Where <em>Darkness</em><br>Meets Light",
    heroSub: "A culinary journey beyond the ordinary",
    heroCta1: "Explore Menu",
    heroCta2: "Book a Table",
    heroCta3: "Order Now",
    heroStat1Label: "Years of Excellence",
    heroStat2Label: "Michelin Stars",
    heroStat3Label: "Guest Rating",

    // Menu section
    menuEyebrow: "Culinary Creations",
    menuTitle: "Our <em>Signature</em> Menu",
    menuSub: "Each dish crafted with seasonal ingredients, locally sourced and prepared with classical French technique.",
    filterAll: "All",
    filterStarters: "Starters",
    filterMains: "Mains",
    filterDesserts: "Desserts",
    filterDrinks: "Drinks",
    filterVeg: "Vegetarian",

    // Reservation section
    resEyebrow: "Book Your Evening",
    resTitle: "Reserve <em>Your</em><br>Table",
    resSub: "Secure your spot for an unforgettable evening. Private dining rooms available for special occasions.",
    resFeature1Title: "Candlelit Atmosphere",
    resFeature1Desc: "Every table thoughtfully arranged",
    resFeature2Title: "Sommelier on Call",
    resFeature2Desc: "Expert cocktail pairing available",
    resFeature3Title: "Special Occasions",
    resFeature3Desc: "We take care of every detail",
    resFeature4Title: "Valet Parking",
    resFeature4Desc: "Complimentary for all guests",
    resCtaEyebrow: "Reserve with ease",
    resCtaTitle: "Book <em>Your</em> Table",
    resCtaSub: "Your complete reservation form — including dish pre-selection, allergy notes, and your booking history — opens in a popup.",
    resCtaFeat1: "Choose date, time & dining experience",
    resCtaFeat2: "Pre-select dishes from the menu",
    resCtaFeat3: "Declare allergies & special requests",
    resCtaFeat4: "View & cancel your existing reservations",
    resCtaBtn: "Open Reservation Popup",

    // Loyalty
    loyaltyEyebrow: "Member Rewards",
    loyaltyTitle: "The NOIR <em>Circle</em>",
    loyaltySub: "Earn points with every visit, unlock exclusive perks, and enjoy privileges reserved for our most valued guests.",
    loyaltyPerk1: "Birthday Surprise",
    loyaltyPerk1Desc: "Complimentary dessert & champagne on your birthday",
    loyaltyPerk2: "Priority Booking",
    loyaltyPerk2Desc: "First access to reservations & special events",
    loyaltyPerk3: "Cocktail Bar Access",
    loyaltyPerk3Desc: "Exclusive tastings in our private cocktail bar",
    loyaltyPerk4: "Chef's Table",
    loyaltyPerk4Desc: "Complimentary upgrade at 5,000 points",
    loyaltyPerk5: "Valet Parking",
    loyaltyPerk5Desc: "Complimentary valet for all Gold+ members",
    loyaltyPerk6: "Monthly Gift",
    loyaltyPerk6Desc: "Curated artisan products delivered monthly",

    // Offers
    offersEyebrow: "Exclusive Deals",
    offersTitle: "Offers & <em>Promotions</em>",

    // Chef
    chefEyebrow: "The Master Chef",
    chefTitle: "Chef <em>Karim Bennani</em>",
    chefBio: "With over 20 years of culinary expertise across Paris, Lyon, and Tunis, Chef Karim Bennani merges French elegance with Mediterranean flavors. His passion for excellence has made NOIR a destination for discerning diners.",
    chefQuote: '"Cuisine is the art of bringing people together through taste, tradition, and innovation."',
    award1Name: "Michelin Star 2019",
    award1Text: "Awarded for exceptional cuisine",
    award2Name: "Best Chef, Tunisia 2022",
    award2Text: "International culinary excellence award",
    award3Name: "Published Author",
    award3Text: '"Saveurs Méditerranéennes" — cookbook',
    award4Name: "World's 50 Best",
    award4Text: "Listed 2021, 2023, 2024",

    // About
    aboutEyebrow: "About NOIR",
    aboutTitle: "Our <em>Story</em>",
    aboutStory: "Since 2015, NOIR has served refined dining experiences in Tunis for more than 12 years, combining French precision, Mediterranean ingredients, and hospitality-first service.",
    mile1: "NOIR opened its first dining room in Tunis.",
    mile2: "Recognized with Michelin-level standards in execution and service.",
    mile3: "Expanded private dining and chef's table experiences.",
    mile4: "12+ years of continuous culinary excellence and innovation.",
    aboutAward1: "Quality Ingredients",
    aboutAward1Desc: "Seasonal sourcing with traceability.",
    aboutAward2: "Hospitality First",
    aboutAward2Desc: "Warm and attentive service for every guest.",
    aboutAward3: "Craftsmanship",
    aboutAward3Desc: "Precision in every plate and detail.",

    // Reviews
    reviewsEyebrow: "Guest Voices",
    reviewsTitle: "What Our <em>Guests</em> Say",
    reviewsShareEyebrow: "Share Your Experience",
    reviewsShareTitle: "Leave a <em>Review</em>",
    reviewsShareSub: "Your feedback helps us grow and serve you better.",
    reviewVisitDate: "Visit Date",
    reviewAccount: "Account",
    reviewPlaceholder: "Describe your experience...",
    reviewSubmit: "Submit Review",

    // Footer
    footerTagline: "A sanctuary of taste. Where every meal is a memory crafted with care, passion, and the finest ingredients.",
    footerNav: "Navigate",
    footerExperience: "Experience",
    footerContact: "Contact",
    footerPrivate: "Private Dining",
    footerChefTable: "Chef's Table",
    footerBar: "Cocktail Bar",
    footerCooking: "Cooking Classes",
    footerGift: "Gift Cards",
    footerPrivacy: "Privacy Policy",
    footerTerms: "Terms of Service",
    footerAllergen: "Allergen Info",
    footerCopy: "© 2024 NOIR Fine Dining. All rights reserved.",

    // Tracker
    trackerTitle: "Track Your Order",
    trackerSub: "Enter your order number for real-time status updates.",
    trackerLabel: "Order Number",
    trackerBtn: "Track Order",
    trackerEmpty: "Enter your order number to load the live tracking timeline.",

    // Search
    searchPlaceholder: "Search dishes, ingredients…",
    searchHint: "Press ESC to close",

    // Chat
    chatName: "NOIR Chatbot",
    chatStatus: "● Online now",
    chatPlaceholder: "Ask anything…",
    chatGreeting: "Bonsoir! I'm your personal dining chatbot. How can I help you this evening? 🥂",

    // Auth modal
    authLogin: "Login",
    authSignup: "Sign Up",
    authEmail: "Email",
    authPassword: "Password",
    authFirstName: "First Name",
    authLastName: "Last Name",
    authPhone: "Phone",
    authBirthDate: "Birth Date",
    authLoginBtn: "Login",
    authCreateBtn: "Create Account",

    // Reservation modal
    resMTitle: "Reserve Your Table",
    resMSub: "Secure your spot for an unforgettable evening",
    resMTabReserve: "Reserve",
    resMTabList: "My Reservations",
    resMConfirm: "Confirm Reservation",

    // Order modal
    orderMTitle: "Order From NOIR",
    orderMSub: "Choose your plates, enter delivery details, and place your order directly.",
    orderMTabOrder: "Order",
    orderMTabList: "My Orders",
    orderMName: "Your Name",
    orderMAddress: "Delivery Address",
    orderMTime: "Delivery Time",
    orderMPhone: "Phone",
    orderMNotes: "Special Instructions",
    orderMPlatesSub: "Select plates & quantities",
    orderMPlace: "Place Order",

    // Account modal
    accountTitle: "My Account",
    accountSub: "Update your personal information and password.",
    accountTabProfile: "Profile",
    accountTabPassword: "Password",
    accountPhoto: "Profile Photo",
    accountPro: "NOIR Pro",
    accountProLabel: "Enable Pro features",
    accountUpdateBtn: "Update Profile",
    accountOldPass: "Current Password",
    accountNewPass: "New Password",
    accountChangePassBtn: "Change Password",
    closeBtn: "Close",
  },

  fr: {
    // Nav
    navMenu: "Menu",
    navReserve: "Réserver",
    navLoyalty: "Fidélité",
    navOffers: "Offres",
    navChef: "Chef",
    navAbout: "À propos",
    navLogin: "Connexion",
    navLogout: "Déconnexion",
    navOrder: "Commander",
    navReserveTable: "Réserver",
    navLang: "EN",

    // Hero
    heroEyebrow: "Fondé en 2015 · Michelin ⭐⭐",
    heroTitle: "Là où l'<em>Obscurité</em><br>Rencontre la Lumière",
    heroSub: "Un voyage culinaire hors du commun",
    heroCta1: "Explorer le Menu",
    heroCta2: "Réserver",
    heroCta3: "Commander",
    heroStat1Label: "Années d'Excellence",
    heroStat2Label: "Étoiles Michelin",
    heroStat3Label: "Note des Clients",

    // Menu section
    menuEyebrow: "Créations Culinaires",
    menuTitle: "Notre Menu <em>Signature</em>",
    menuSub: "Chaque plat élaboré avec des ingrédients de saison, sourcés localement et préparés selon la technique française classique.",
    filterAll: "Tout",
    filterStarters: "Entrées",
    filterMains: "Plats",
    filterDesserts: "Desserts",
    filterDrinks: "Boissons",
    filterVeg: "Végétarien",

    // Reservation section
    resEyebrow: "Réservez votre Soirée",
    resTitle: "Réservez <em>Votre</em><br>Table",
    resSub: "Assurez votre place pour une soirée inoubliable. Salles privées disponibles pour les occasions spéciales.",
    resFeature1Title: "Ambiance aux Bougies",
    resFeature1Desc: "Chaque table soigneusement arrangée",
    resFeature2Title: "Sommelier Disponible",
    resFeature2Desc: "Accord cocktail expert disponible",
    resFeature3Title: "Occasions Spéciales",
    resFeature3Desc: "Nous prenons soin de chaque détail",
    resFeature4Title: "Voiturier",
    resFeature4Desc: "Offert pour tous les clients",
    resCtaEyebrow: "Réservez facilement",
    resCtaTitle: "Réservez <em>Votre</em> Table",
    resCtaSub: "Votre formulaire de réservation complet — avec présélection des plats, allergies et historique — s'ouvre dans un popup.",
    resCtaFeat1: "Choisissez date, heure & expérience",
    resCtaFeat2: "Présélectionnez des plats du menu",
    resCtaFeat3: "Déclarez allergies & demandes spéciales",
    resCtaFeat4: "Consultez & annulez vos réservations",
    resCtaBtn: "Ouvrir le Formulaire de Réservation",

    // Loyalty
    loyaltyEyebrow: "Récompenses Membres",
    loyaltyTitle: "Le Cercle <em>NOIR</em>",
    loyaltySub: "Gagnez des points à chaque visite, débloquez des avantages exclusifs et profitez de privilèges réservés à nos clients les plus fidèles.",
    loyaltyPerk1: "Surprise d'Anniversaire",
    loyaltyPerk1Desc: "Dessert & champagne offerts pour votre anniversaire",
    loyaltyPerk2: "Réservation Prioritaire",
    loyaltyPerk2Desc: "Accès en avant-première aux réservations & événements",
    loyaltyPerk3: "Accès au Bar Cocktail",
    loyaltyPerk3Desc: "Dégustations exclusives dans notre bar privé",
    loyaltyPerk4: "Table du Chef",
    loyaltyPerk4Desc: "Upgrade offert à 5 000 points",
    loyaltyPerk5: "Voiturier",
    loyaltyPerk5Desc: "Voiturier offert pour tous les membres Gold+",
    loyaltyPerk6: "Cadeau Mensuel",
    loyaltyPerk6Desc: "Produits artisanaux sélectionnés livrés chaque mois",

    // Offers
    offersEyebrow: "Deals Exclusifs",
    offersTitle: "Offres & <em>Promotions</em>",

    // Chef
    chefEyebrow: "Le Chef Maître",
    chefTitle: "Chef <em>Karim Bennani</em>",
    chefBio: "Avec plus de 20 ans d'expertise culinaire entre Paris, Lyon et Tunis, le Chef Karim Bennani marie l'élégance française aux saveurs méditerranéennes. Sa passion pour l'excellence a fait de NOIR une destination incontournable.",
    chefQuote: '"La cuisine est l\'art de rassembler les gens à travers le goût, la tradition et l\'innovation."',
    award1Name: "Étoile Michelin 2019",
    award1Text: "Décernée pour une cuisine exceptionnelle",
    award2Name: "Meilleur Chef, Tunisie 2022",
    award2Text: "Prix international d'excellence culinaire",
    award3Name: "Auteur Publié",
    award3Text: '"Saveurs Méditerranéennes" — livre de cuisine',
    award4Name: "World's 50 Best",
    award4Text: "Classé en 2021, 2023, 2024",

    // About
    aboutEyebrow: "À propos de NOIR",
    aboutTitle: "Notre <em>Histoire</em>",
    aboutStory: "Depuis 2015, NOIR propose des expériences gastronomiques raffinées à Tunis depuis plus de 12 ans, alliant précision française, ingrédients méditerranéens et service axé sur l'hospitalité.",
    mile1: "NOIR a ouvert sa première salle à manger à Tunis.",
    mile2: "Reconnu pour des standards d'exécution et de service de niveau Michelin.",
    mile3: "Développement de la salle privée et de la table du chef.",
    mile4: "Plus de 12 ans d'excellence et d'innovation culinaire continue.",
    aboutAward1: "Ingrédients de Qualité",
    aboutAward1Desc: "Approvisionnement saisonnier avec traçabilité.",
    aboutAward2: "Hospitalité Avant Tout",
    aboutAward2Desc: "Service chaleureux et attentionné pour chaque client.",
    aboutAward3: "Savoir-Faire",
    aboutAward3Desc: "Précision dans chaque plat et chaque détail.",

    // Reviews
    reviewsEyebrow: "Avis des Clients",
    reviewsTitle: "Ce que disent nos <em>Clients</em>",
    reviewsShareEyebrow: "Partagez votre Expérience",
    reviewsShareTitle: "Laisser un <em>Avis</em>",
    reviewsShareSub: "Vos retours nous aident à progresser et à mieux vous servir.",
    reviewVisitDate: "Date de Visite",
    reviewAccount: "Compte",
    reviewPlaceholder: "Décrivez votre expérience...",
    reviewSubmit: "Soumettre l'Avis",

    // Footer
    footerTagline: "Un sanctuaire du goût. Chaque repas est un souvenir façonné avec soin, passion et les meilleurs ingrédients.",
    footerNav: "Navigation",
    footerExperience: "Expérience",
    footerContact: "Contact",
    footerPrivate: "Salle Privée",
    footerChefTable: "Table du Chef",
    footerBar: "Bar Cocktail",
    footerCooking: "Cours de Cuisine",
    footerGift: "Cartes Cadeaux",
    footerPrivacy: "Politique de Confidentialité",
    footerTerms: "Conditions d'Utilisation",
    footerAllergen: "Info Allergènes",
    footerCopy: "© 2024 NOIR Fine Dining. Tous droits réservés.",

    // Tracker
    trackerTitle: "Suivre votre Commande",
    trackerSub: "Entrez votre numéro de commande pour un suivi en temps réel.",
    trackerLabel: "Numéro de Commande",
    trackerBtn: "Suivre la Commande",
    trackerEmpty: "Entrez votre numéro de commande pour charger le suivi en direct.",

    // Search
    searchPlaceholder: "Rechercher des plats, ingrédients…",
    searchHint: "Appuyez sur ÉCHAP pour fermer",

    // Chat
    chatName: "Chatbot NOIR",
    chatStatus: "● En ligne",
    chatPlaceholder: "Posez une question…",
    chatGreeting: "Bonsoir ! Je suis votre assistant personnel. Comment puis-je vous aider ce soir ? 🥂",

    // Auth modal
    authLogin: "Connexion",
    authSignup: "Inscription",
    authEmail: "Email",
    authPassword: "Mot de passe",
    authFirstName: "Prénom",
    authLastName: "Nom",
    authPhone: "Téléphone",
    authBirthDate: "Date de naissance",
    authLoginBtn: "Se connecter",
    authCreateBtn: "Créer un compte",

    // Reservation modal
    resMTitle: "Réserver votre Table",
    resMSub: "Assurez votre place pour une soirée inoubliable",
    resMTabReserve: "Réserver",
    resMTabList: "Mes Réservations",
    resMConfirm: "Confirmer la Réservation",

    // Order modal
    orderMTitle: "Commander chez NOIR",
    orderMSub: "Choisissez vos plats, entrez vos coordonnées de livraison et passez votre commande.",
    orderMTabOrder: "Commander",
    orderMTabList: "Mes Commandes",
    orderMName: "Votre Nom",
    orderMAddress: "Adresse de Livraison",
    orderMTime: "Heure de Livraison",
    orderMPhone: "Téléphone",
    orderMNotes: "Instructions Spéciales",
    orderMPlatesSub: "Sélectionnez plats & quantités",
    orderMPlace: "Passer la Commande",

    // Account modal
    accountTitle: "Mon Compte",
    accountSub: "Mettez à jour vos informations personnelles et votre mot de passe.",
    accountTabProfile: "Profil",
    accountTabPassword: "Mot de passe",
    accountPhoto: "Photo de Profil",
    accountPro: "NOIR Pro",
    accountProLabel: "Activer les fonctionnalités Pro",
    accountUpdateBtn: "Mettre à jour le Profil",
    accountOldPass: "Mot de passe actuel",
    accountNewPass: "Nouveau mot de passe",
    accountChangePassBtn: "Changer le mot de passe",
    closeBtn: "Fermer",
  }
};

let currentLang = 'en';

function t(key) {
  return TRANSLATIONS[currentLang][key] || TRANSLATIONS['en'][key] || key;
}

function toggleLang() {
  currentLang = currentLang === 'en' ? 'fr' : 'en';
  applyTranslations();
}

function applyTranslations() {
  const T = TRANSLATIONS[currentLang];

  // Lang button
  document.getElementById('langBtn').textContent = T.navLang;

  // Nav links
  const navLinks = document.querySelectorAll('.nav-links a');
  const navKeys = ['navMenu','navReserve','navLoyalty','navOffers','navChef','navAbout'];
  navLinks.forEach((el, i) => { if (navKeys[i]) el.textContent = T[navKeys[i]]; });

  // Nav actions
  const authBtn = document.getElementById('authBtn');
  if (authBtn) authBtn.innerHTML = `<i class="fas fa-user"></i> ${T.navLogin}`;
  const logoutBtn = document.getElementById('clientLogoutBtn');
  if (logoutBtn) logoutBtn.textContent = T.navLogout;

  const navBtns = document.querySelectorAll('.nav-actions .nav-btn:not(#authBtn):not(#clientLogoutBtn):not(#langBtn)');
  if (navBtns[0]) navBtns[0].textContent = T.navOrder;
  if (navBtns[1]) navBtns[1].textContent = T.navReserveTable;

  // Hero
  setHTML('heroEyebrowEl', T.heroEyebrow);
  setHTML('heroTitleEl', T.heroTitle);
  setText('heroSubEl', T.heroSub);
  const heroCtas = document.querySelectorAll('.hero-ctas button');
  if (heroCtas[0]) heroCtas[0].textContent = T.heroCta1;
  if (heroCtas[1]) heroCtas[1].textContent = T.heroCta2;
  if (heroCtas[2]) heroCtas[2].textContent = T.heroCta3;
  const heroStatLabels = document.querySelectorAll('.hero-stat-label');
  if (heroStatLabels[0]) heroStatLabels[0].textContent = T.heroStat1Label;
  if (heroStatLabels[1]) heroStatLabels[1].textContent = T.heroStat2Label;
  if (heroStatLabels[2]) heroStatLabels[2].textContent = T.heroStat3Label;

  // Menu section
  setText('menuEyebrowEl', T.menuEyebrow);
  setHTML('menuTitleEl', T.menuTitle);
  setText('menuSubEl', T.menuSub);
  const filterBtns = document.querySelectorAll('.filter-btn');
  const filterKeys = ['filterAll','filterStarters','filterMains','filterDesserts','filterDrinks','filterVeg'];
  filterBtns.forEach((el, i) => { if (filterKeys[i]) el.textContent = T[filterKeys[i]]; });

  // Reservation section
  setText('resEyebrowEl', T.resEyebrow);
  setHTML('resTitleEl', T.resTitle);
  setText('resSubEl', T.resSub);
  const resFeatures = document.querySelectorAll('.res-feature-text');
  if (resFeatures[0]) resFeatures[0].innerHTML = `<strong>${T.resFeature1Title}</strong><br>${T.resFeature1Desc}`;
  if (resFeatures[1]) resFeatures[1].innerHTML = `<strong>${T.resFeature2Title}</strong><br>${T.resFeature2Desc}`;
  if (resFeatures[2]) resFeatures[2].innerHTML = `<strong>${T.resFeature3Title}</strong><br>${T.resFeature3Desc}`;
  if (resFeatures[3]) resFeatures[3].innerHTML = `<strong>${T.resFeature4Title}</strong><br>${T.resFeature4Desc}`;
  setText('resCtaEyebrowEl', T.resCtaEyebrow);
  setHTML('resCtaTitleEl', T.resCtaTitle);
  setText('resCtaSubEl', T.resCtaSub);
  const resCtaFeats = document.querySelectorAll('.res-cta-feat');
  if (resCtaFeats[0]) resCtaFeats[0].innerHTML = `<i class="fas fa-calendar-check"></i>${T.resCtaFeat1}`;
  if (resCtaFeats[1]) resCtaFeats[1].innerHTML = `<i class="fas fa-utensils"></i>${T.resCtaFeat2}`;
  if (resCtaFeats[2]) resCtaFeats[2].innerHTML = `<i class="fas fa-triangle-exclamation"></i>${T.resCtaFeat3}`;
  if (resCtaFeats[3]) resCtaFeats[3].innerHTML = `<i class="fas fa-list"></i>${T.resCtaFeat4}`;
  setText('resCtaBtnEl', T.resCtaBtn);

  // Loyalty
  setText('loyaltyEyebrowEl', T.loyaltyEyebrow);
  setHTML('loyaltyTitleEl', T.loyaltyTitle);
  setText('loyaltySubEl', T.loyaltySub);
  const perkNames = document.querySelectorAll('.perk-name');
  const perkDescs = document.querySelectorAll('.perk-desc');
  const perkNameKeys = ['loyaltyPerk1','loyaltyPerk2','loyaltyPerk3','loyaltyPerk4','loyaltyPerk5','loyaltyPerk6'];
  const perkDescKeys = ['loyaltyPerk1Desc','loyaltyPerk2Desc','loyaltyPerk3Desc','loyaltyPerk4Desc','loyaltyPerk5Desc','loyaltyPerk6Desc'];
  perkNames.forEach((el, i) => { if (perkNameKeys[i]) el.textContent = T[perkNameKeys[i]]; });
  perkDescs.forEach((el, i) => { if (perkDescKeys[i]) el.textContent = T[perkDescKeys[i]]; });

  // Offers
  setText('offersEyebrowEl', T.offersEyebrow);
  setHTML('offersTitleEl', T.offersTitle);

  // Chef
  setText('chefEyebrowEl', T.chefEyebrow);
  setHTML('chefTitleEl', T.chefTitle);
  setText('chefBioEl', T.chefBio);
  setText('chefQuoteEl', T.chefQuote);
  const awardNames = document.querySelectorAll('.award-name');
  const awardTexts = document.querySelectorAll('.award-text');
  const awardNameKeys = ['award1Name','award2Name','award3Name','award4Name'];
  const awardTextKeys = ['award1Text','award2Text','award3Text','award4Text'];
  awardNames.forEach((el, i) => { if (awardNameKeys[i]) el.textContent = T[awardNameKeys[i]]; });
  awardTexts.forEach((el, i) => { if (awardTextKeys[i]) el.textContent = T[awardTextKeys[i]]; });

  // About
  setText('aboutEyebrowEl', T.aboutEyebrow);
  setHTML('aboutTitleEl', T.aboutTitle);
  setText('aboutStory', T.aboutStory);
  const miles = document.querySelectorAll('.about-mile span');
  if (miles[0]) miles[0].textContent = T.mile1;
  if (miles[1]) miles[1].textContent = T.mile2;
  if (miles[2]) miles[2].textContent = T.mile3;
  if (miles[3]) miles[3].textContent = T.mile4;

  // Reviews
  setText('reviewsEyebrowEl', T.reviewsEyebrow);
  setHTML('reviewsTitleEl', T.reviewsTitle);
  setText('reviewsShareEyebrowEl', T.reviewsShareEyebrow);
  setHTML('reviewsShareTitleEl', T.reviewsShareTitle);
  setText('reviewsShareSubEl', T.reviewsShareSub);

  // Footer
  setText('footerTaglineEl', T.footerTagline);
  setText('footerNavTitleEl', T.footerNav);
  setText('footerCopyEl', T.footerCopy);

  // Tracker
  setText('trackerTitleEl', T.trackerTitle);
  setText('trackerSubEl', T.trackerSub);
  setText('trackerEmptyEl', T.trackerEmpty);
  setText('trackerBtnEl', T.trackerBtn);
  const trackerLabel = document.querySelector('#tracker-sidebar .form-label');
  if (trackerLabel) trackerLabel.textContent = T.trackerLabel;

  // Search
  const searchInput = document.getElementById('searchInput');
  if (searchInput) searchInput.placeholder = T.searchPlaceholder;
  const searchHint = document.querySelector('.search-hint');
  if (searchHint) searchHint.textContent = T.searchHint;

  // Chat
  setText('chatHeadNameEl', T.chatName);
  setText('chatHeadStatusEl', T.chatStatus);
  const chatInput = document.getElementById('chatInput');
  if (chatInput) chatInput.placeholder = T.chatPlaceholder;

  // Modals - close buttons
  document.querySelectorAll('.btn-ghost').forEach(el => {
    if (el.textContent.trim() === 'Close' || el.textContent.trim() === 'Fermer') {
      el.textContent = T.closeBtn;
    }
  });

  // Update html lang attribute
  document.documentElement.lang = currentLang;
}

function setText(id, value) {
  const el = document.getElementById(id);
  if (el) el.textContent = value;
}

function setHTML(id, value) {
  const el = document.getElementById(id);
  if (el) el.innerHTML = value;
}