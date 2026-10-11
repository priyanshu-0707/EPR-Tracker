# EPR Compliance Tracker ("ClearTax for E-Waste")

> **An enterprise-grade, full-stack compliance management & financial risk engine built for India's E-Waste (Management) Rules, 2022 (CPCB).**

[![GitHub Repository](https://img.shields.io/badge/GitHub-priyanshu--0707%2FEPR--Tracker-181717?logo=github)](https://github.com/priyanshu-0707/EPR-Tracker)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18%20%2B%20TypeScript-blue.svg)](https://reactjs.org/)
[![Vite](https://img.shields.io/badge/Vite-Bundler-purple.svg)](https://vitejs.dev/)
[![Tailwind CSS](https://img.shields.io/badge/TailwindCSS-Custom%20Tokens-38B2AC.svg)](https://tailwindcss.com/)
[![AWS SDK](https://img.shields.io/badge/AWS-DynamoDB%20%7C%20S3%20%7C%20Bedrock-232F3E.svg)](https://aws.amazon.com/)

**Repository:** [https://github.com/priyanshu-0707/EPR-Tracker](https://github.com/priyanshu-0707/EPR-Tracker)

---

## 📌 Executive Summary

Under India's **E-Waste (Management) Rules, 2022**, electronic producers, brand owners, and importers face strict legal obligations:
- Producers must collect and recycle up to **80% (FY 2026-27)** of their historical sales volume.
- Failure to comply triggers **non-compoundable Environmental Compensation (EC)** penalties under **Rule 22** assessed at **₹18,000 / Metric Ton** plus compounding severity surcharges.
- The market suffers from **Ghost Credits**—fraudulent recyclers reselling the same recycling certificate across multiple companies, exposing legal teams to regulatory clawbacks.

**EPR Compliance Tracker** solves this by moving beyond passive tracking to **Predictive Financial Shielding**, **Anti-Fraud Ledger Verification**, **Automated Credit Procurement**, and **Direct Government Filing Automation**.

---

## 🚀 Key Enterprise Features

### 1. ⚠️ The "What-If" Environmental Compensation (EC) Simulator
- **Regulatory Formula (CPCB Rule 22 & Schedule VI):**
  $$\text{EC Liability (INR)} = \text{Shortfall in MT} \times ₹18,000 \times \text{Severity} \times (1 + \text{Delay})$$
- Computes exact statutory penalty liabilities in Indian Rupees.
- Compares fine exposure vs. live credit procurement rates to provide an immediate **ROI justification for the CFO** (*e.g., "Spending ₹2.25 Lakhs on credits today averts a ₹10.80 Lakh CPCB penalty next month — 4.8x ROI"*).
- Executive summary export ready for CFO/Board review.

### 2. 🛡️ Multi-Tiered "Chain of Custody" Ledger (Anti-Fraud Gate)
- Solves the Indian EPR market's **Ghost Credit** fraud crisis.
- Checks certificate uploads against the recycler’s legal CPCB annual capacity limit and cross-enterprise logs.
- Flags duplicate certificates across all companies to prevent double-selling.
- Issues an **"Audit-Ready Guarantee"** cryptographic hash token (`CPCB-AUTH-...`).

### 3. 🛒 Automated "Gap-Filling" Marketplace Procurement
- Analyzes brand inventories and detects compliance deficits per EEE category code (e.g., `ITEW1`, `CEEW2`).
- Auto-recommends verified CPCB-registered recyclers with live surplus quotas and transparent spot pricing (₹/MT).
- 1-click credit purchase automatically logs the issued certificate into the company's compliance ledger to close the target gap.

### 4. ⚡ Smart Data Ingestion & Schema Mapper (Zero Manual Entry)
- Eliminates manual spreadsheet cleaning for enterprise ERP exports (SAP, Oracle, NetSuite).
- Dynamic CSV/Excel inspection with automated column header detection.
- Visual mapping interface to align custom ERP fields (`Material_Desc`, `Billing_Qty_EA`) with legal CPCB fields.
- Saves schema templates per enterprise for recurring monthly imports.

### 5. 📄 Official CPCB Form 2 (Annual Returns) Exporter & PDF Generator
- **Excel Exporter (Apache POI):** Generates official multi-sheet CPCB Form 2 spreadsheets:
  - **Part A:** Producer Details, CPCB Registration, and Compliance Officer.
  - **Part B:** EEE codes, sales volume, lifespan conversion factors, and statutory collection quotas.
  - **Part C:** Authenticated recycler collection certificates, quantities, and custody status.
- **Legal PDF Generator (iText 7):** Formats 7-section compliance documents with direct streaming download and archive management.

### 6. 🧠 AI Regulatory Assistant (AWS Bedrock / Claude 3 Sonnet)
- In-app compliance copilot answering complex Indian e-waste regulations, Rule 4(1) collection quotas, hazardous fractions, and legal filing deadlines.

---

## 🏗️ Architecture & Technology Stack

```
                          ┌─────────────────────────────────────┐
                          │     React 18 + TypeScript + Vite    │
                          │   (Tailwind CSS, Recharts, Leaflet) │
                          └──────────────────┬──────────────────┘
                                             │ HTTP / REST (/api)
                                             ▼
                          ┌─────────────────────────────────────┐
                          │      Spring Boot 3.2.4 (Java 21)    │
                          │  Controller ➔ Service ➔ Repository  │
                          └──────────┬───────────────┬──────────┘
                                     │               │
                 ┌───────────────────┴──┐     ┌──────┴────────────────────┐
                 ▼                      ▼     ▼                           ▼
       AWS DynamoDB / LocalStore   AWS S3   AWS Bedrock       Apache POI & iText 7
      (Enterprises, Products,     (Reports) (Claude 3 Sonnet) (Form 2 .xlsx & PDFs)
       Recyclers, Custody Logs)
```

- **Backend:**
  - Java 21 LTS
  - Spring Boot 3.2.4 (Spring Security, Spring Web, Spring Validation)
  - Nimbus JOSE + JWT (Stateless authentication)
  - AWS SDK v2 (DynamoDB Enhanced Client, S3, Bedrock Runtime)
  - Apache POI 5.2.5 (`poi-ooxml` for multi-sheet Excel generation)
  - iText 7.2.5 (PDF report generation)
  - Thread-safe local fallbacks for seamless offline testing without live AWS credentials
- **Frontend:**
  - React 18 with TypeScript (Strict mode, zero `any` types)
  - Vite build tool & reverse proxy
  - Tailwind CSS with custom design tokens (`#0D1117`, `#161B22`, `#58A6FF`, `#3FB950`, `#F85149`)
  - Recharts for executive compliance analytics
  - Leaflet / OpenStreetMap for geographic recycler locator
  - Indian numeral formatting (`10,00,000`) across all currency and unit displays

---

## 📦 Project Structure

```
Hacks/
├── backend/
│   ├── src/main/java/com/eprtracker/
│   │   ├── config/          # SecurityConfig, SeedDataLoader, AwsConfig
│   │   ├── controller/      # REST API Controllers (Simulator, Marketplace, Ingestion, Reports, etc.)
│   │   ├── dto/             # Request & Response DTOs
│   │   ├── entity/          # Company, Product, Recycler, ComplianceLog
│   │   ├── repository/      # DynamoDb repositories with concurrent in-memory fallbacks
│   │   ├── service/         # Business logic layer (Rule 22 simulation, Form 2, Anti-Fraud)
│   │   └── util/            # EprCategory enum, IndianNumberFormatter
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── components/      # UI components, layout, Navigation, AI Drawer
│   │   ├── context/         # AuthContext with token management
│   │   ├── pages/           # EcSimulatorPage, MarketplacePage, DataIngestionPage,
│   │   │                    # DashboardPage, ComplianceLogPage, ReportsPage, etc.
│   │   ├── services/        # Axios API client methods
│   │   ├── types/           # TypeScript interfaces
│   │   └── utils/           # Number formatters & calculations
│   ├── package.json
│   └── vite.config.ts
│
├── ENTERPRISE_FEATURES_PLAN.md
├── REAL_DATA_TESTING_GUIDE.md
└── USER_GUIDE.md
```

---

## ⚡ Getting Started

### Clone the Repository
```bash
git clone https://github.com/priyanshu-0707/EPR-Tracker.git
cd EPR-Tracker
```

### Prerequisites
- **Java:** JDK 21+ ([Eclipse Temurin 21](https://adoptium.net/) recommended)
- **Node.js:** Node 18+ and npm
- **Maven:** 3.8+

---

### Backend Setup

1. Open a terminal and navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Compile and run the Spring Boot application:
   ```bash
   export JAVA_HOME="/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home"  # Set your JDK 21 path
   mvn spring-boot:run
   ```
   *The backend starts on `http://localhost:8080` and pre-seeds 30 verified CPCB recyclers across 10 Indian cities.*

---

### Frontend Setup

1. Open a second terminal and navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Launch the Vite development server:
   ```bash
   npm run dev -- --port 3000
   ```
   *The frontend starts on `http://localhost:3000` with automated reverse-proxying of `/api` requests to port `8080`.*

---

## 🧪 Testing the Workflow

1. Open **[http://localhost:3000](http://localhost:3000)** in your browser.
2. Click **"Register Brand"** and enter your enterprise details (*e.g., Tata Electronics, CPCB/EPR/TATA/2026/001*).
3. **EPR Calculator (`/calculator`):** Enter sales volume for electronic products to calculate mandatory legal quotas.
4. **⚠️ EC Simulator (`/ec-simulator`):** Adjust the shortfall slider to compute Rule 22 penalties and review the CFO ROI report.
5. **🛡️ Compliance Log (`/compliance-log`):** Enter certificate details and run the **Anti-Fraud Validation Gate** to obtain an audit-ready guarantee token.
6. **🛒 Marketplace (`/marketplace`):** View category shortfalls and procure verified surplus credits in 1 click.
7. **⚡ ERP Data Ingestion (`/data-ingestion`):** Upload an ERP sales spreadsheet to map custom columns directly to CPCB fields.
8. **📄 Reports (`/reports`):** Click **Download CPCB Form 2 (.xlsx)** for the official Excel return or **Generate CPCB PDF Report** to download/delete legal PDF certificates.

---

## ⚖️ Regulatory Compliance Reference

- **Statute:** Ministry of Environment, Forest and Climate Change (MoEFCC) Notification S.O. 5125(E), *E-Waste (Management) Rules, 2022*.
- **Collection Targets:** Rule 4(1), Schedule III:
  - FY 2023-24: 60%
  - FY 2024-25: 65%
  - FY 2025-26: 70%
  - **FY 2026-27: 80% (Current Target)**
  - FY 2027-28 onwards: 90%
- **Penalty Regime:** Rule 22 (Environmental Compensation) assessed at ₹18,000 / MT.
- **Reporting Forms:** Form 2 (Annual Returns under Rule 13(1)).

---

## 📄 License
This project is proprietary and intended for enterprise compliance management under Indian CPCB regulations.
