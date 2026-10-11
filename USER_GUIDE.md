# EPR Compliance Tracker — User & Operational Guide

**System Name:** EPR Compliance Tracker ("ClearTax for e-waste")  
**Regulatory Basis:** India's E-Waste (Management) Rules, 2022 (CPCB)  
**Release:** 1.0.0 Production-Ready Architecture

---

## 1. Quick Start & Access URLs

- **Frontend Application UI:** [http://localhost:3000](http://localhost:3000)
- **Backend REST API:** [http://localhost:8080](http://localhost:8080)
- **Health / Actuator:** [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## 2. Seeded Enterprise Demo Accounts

All demo accounts use password: `password123`

| Brand Name | Login Email | State | Obligation Status |
|---|---|---|---|
| **boAt Lifestyle** | `compliance@boat-lifestyle.com` | Maharashtra | **AT RISK** (~50.1% fulfilled) |
| **Vivo India** | `compliance@vivo.com` | Uttar Pradesh | **ON TRACK** (~83% fulfilled) |
| **Blue Star Limited** | `compliance@bluestarindia.com` | Maharashtra | **NON-COMPLIANT** (~20.8% fulfilled) |
| **Dell India** | `compliance@dell.com` | Karnataka | **ON TRACK** (~81.2% fulfilled) |
| **Whirlpool India** | `compliance@whirlpool.com` | Haryana | **AT RISK** (~46.7% fulfilled) |

*You can also click any of the 4 demo shortcut buttons directly on the login screen to sign in instantly.*

---

## 3. Mathematical Formula Implemented

$$\text{EPR Obligation } (Q_{EPR}) = Q_p \times C_f \times \text{Target \%}$$

- **Target \% Schedule:**
  - FY2023-24: 60%
  - FY2024-25: 65%
  - FY2025-26: 70%
  - **FY2026-27: 80%**
  - FY2027-28: 90%
- **Worked Example (boAt Earphones):**
  $$10,00,000 \times 0.333 \times 80\% = 2,66,400\text{ units}$$

---

## 4. Key Endpoints Reference

### Authentication
- `POST /api/auth/register` — Register new brand
- `POST /api/auth/login` — Sign in and receive JWT token
- `POST /api/auth/refresh` — Refresh active token

### Dashboard & Profile
- `GET /api/company/dashboard` — 4 KPI counters, fulfillment percentage, status badge, and monthly chart data
- `GET /api/company/profile` — Enterprise profile
- `PUT /api/company/profile` — Update CPCB number and state
- `DELETE /api/company` — Safety-confirmed entity deletion

### Products & EPR Calculator
- `GET /api/products/categories` — List all 20 CPCB categories and conversion factors ($C_f$)
- `POST /api/epr/calculate` — Immediate breakdown of units sold, factor, and target obligation
- `GET /api/products` — Current product portfolio
- `POST /api/products` — Add product to portfolio
- `PUT /api/products/{id}` — Update sales volume
- `DELETE /api/products/{id}` — Remove product from portfolio

### Recycler Directory & Geo-locator
- `GET /api/recyclers?state=&category=` — Filter 30 certified facilities
- `GET /api/recyclers/nearby?lat=&lng=&radiusKm=` — Proximity locator using Haversine formula

### Compliance Logs & Reports
- `GET /api/compliance-log` — List collection certificates
- `POST /api/compliance-log` — Record new recycler certificate
- `GET /api/compliance-log/export` — Download CSV ledger
- `POST /api/reports/generate` — Generate 7-section iText 7 PDF and upload to S3
- `GET /api/reports/history` — List archived reports with signed download URLs

### AI Assistant (AWS Bedrock Claude)
- `POST /api/ai/chat` — Queries CPCB rules with regulatory citations and Indian numeral formatting

### Alert Settings (Amazon SNS)
- `GET /api/alerts/settings` — Active email/SMS triggers
- `PUT /api/alerts/settings` — Update preferences
- `GET /api/alerts/history` — Audit trail of dispatched alerts
