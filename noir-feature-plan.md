# NOIR — Kitchen & Delivery API Feature Plan

## Overview of Changes

### New data files
- `src/main/resources/data/staff.json` — kitchen/delivery staff accounts
- `src/main/resources/data/special_plates.json` — chef special/temporary plates

### New config properties
- `noir.kitchen.token-secret` in `application.properties`
- `noir.delivery.token-secret` in `application.properties`

### Order status lifecycle
```
waiting      → (admin confirms)  → confirmed
confirmed    → (kitchen marks)   → being_prepared
being_prepared → (kitchen marks) → ready
ready        → (delivery picks)  → out_for_delivery
out_for_delivery → (delivery)    → delivered
```

### New files to CREATE
| File | Purpose |
|---|---|
| `model/StaffUser.java` | Kitchen/delivery staff model |
| `model/SpecialPlate.java` | Chef special plate model |
| `dto/request/StaffLoginRequest.java` | Staff login DTO |
| `dto/request/SpecialPlateRequest.java` | Create/update special plate |
| `dto/request/OrderStatusUpdateRequest.java` | Mark order status |
| `security/KitchenAuthFilter.java` | Protects `/api/kitchen/*` |
| `security/DeliveryAuthFilter.java` | Protects `/api/delivery/*` |
| `service/StaffAuthService.java` | Login, token sign/verify for staff |
| `service/KitchenService.java` | Kitchen business logic |
| `service/DeliveryService.java` | Delivery business logic |
| `controller/KitchenController.java` | Kitchen API endpoints |
| `controller/DeliveryController.java` | Delivery API endpoints |

### Files to EDIT (full file provided below)
| File | What changes |
|---|---|
| `application.properties` | Add kitchen/delivery token secrets |
| `AppConfig.java` | Add Kitchen and Delivery inner classes |
| `NoirApplication.java` | Register 2 new filters |
| `model/Order.java` | Add `kitchenNotes` field |
| `model/MenuItem.java` | Add `isSpecial`, `availableFrom`, `availableTo`, `availableDays` |
| `service/AdminService.java` | Add kitchen/delivery actions + staff mgmt + special plates |
| `controller/AdminController.java` | Add new admin endpoints |

---

## API Reference

### Kitchen API — `/api/kitchen/*`
All routes require `Authorization: Bearer <kitchen-token>`

| Method | Path | Who | Description |
|---|---|---|---|
| POST | `/api/kitchen/login` | Anyone | Staff login |
| GET | `/api/kitchen/orders` | All staff | Orders with status `confirmed`, `being_prepared`, `ready` |
| GET | `/api/kitchen/orders/{orderNumber}` | All staff | Order detail with allergy info |
| GET | `/api/kitchen/reservations` | All staff | Today's reservations with allergy info |
| PATCH | `/api/kitchen/orders/{orderNumber}/status` | All staff | Mark `being_prepared` or `ready` |
| GET | `/api/kitchen/special-plates` | All staff | List all special plates |
| POST | `/api/kitchen/special-plates` | Chef only | Add a special plate |
| PUT | `/api/kitchen/special-plates/{id}` | Chef only | Update a special plate |
| DELETE | `/api/kitchen/special-plates/{id}` | Chef only | Remove a special plate |
| POST | `/api/kitchen/recommend/{menuItemId}` | Chef only | Recommend an existing menu item |

### Delivery API — `/api/delivery/*`
All routes require `Authorization: Bearer <delivery-token>`

| Method | Path | Description |
|---|---|---|
| POST | `/api/delivery/login` | Staff login |
| GET | `/api/delivery/orders` | Orders with status `ready` or `out_for_delivery` |
| GET | `/api/delivery/orders/{orderNumber}` | Order detail |
| PATCH | `/api/delivery/orders/{orderNumber}/pickup` | Mark `out_for_delivery` |
| PATCH | `/api/delivery/orders/{orderNumber}/delivered` | Mark `delivered` |

### Admin additions — `/api/admin/*`
| Method | Path | Description |
|---|---|---|
| GET | `/api/admin/staff` | List all staff |
| POST | `/api/admin/staff` | Create staff account |
| DELETE | `/api/admin/staff/{id}` | Remove staff |
| GET | `/api/admin/special-plates` | List special plates |
| POST | `/api/admin/special-plates` | Add special plate |
| PUT | `/api/admin/special-plates/{id}` | Update special plate |
| DELETE | `/api/admin/special-plates/{id}` | Delete special plate |
| PATCH | `/api/admin/orders/{orderNumber}/status` | Update any order status |
| POST | `/api/admin/recommend/{menuItemId}` | Recommend menu item |

---
