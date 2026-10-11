package com.eprtracker.config;

import com.eprtracker.entity.Company;
import com.eprtracker.entity.ComplianceLog;
import com.eprtracker.entity.Product;
import com.eprtracker.entity.Recycler;
import com.eprtracker.repository.CompanyRepository;
import com.eprtracker.repository.ComplianceLogRepository;
import com.eprtracker.repository.ProductRepository;
import com.eprtracker.repository.RecyclerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Configuration
public class SeedDataLoader {

    private static final Logger log = LoggerFactory.getLogger(SeedDataLoader.class);

    @Bean
    public CommandLineRunner seedDatabase(
            CompanyRepository companyRepository,
            ProductRepository productRepository,
            ComplianceLogRepository complianceLogRepository,
            RecyclerRepository recyclerRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            log.info("Checking database state for CPCB EPR demo companies and recyclers...");

            // 1. Seed Recyclers (30 recyclers across 10 cities, 3 per city)
            List<Recycler> existingRecyclers = recyclerRepository.findAll();
            if (existingRecyclers.isEmpty()) {
                log.info("Seeding 30 CPCB Certified Recyclers across 10 Indian cities...");
                List<Recycler> recyclers = createSeedRecyclers();
                for (Recycler r : recyclers) {
                    recyclerRepository.save(r);
                }
            }

            // 2. Do not seed demo enterprises - real users register their own enterprise
            log.info("CPCB EPR compliance database initialization complete (ready for real enterprise registrations).");
        };
    }

    private void seedDemoEnterprises(
            CompanyRepository companyRepo,
            ProductRepository productRepo,
            ComplianceLogRepository logRepo,
            RecyclerRepository recyclerRepo,
            PasswordEncoder encoder) {

        String encodedPw = encoder.encode("password123");

        // 1. boAt Lifestyle — earphones (10,00,000 units) + phones (5,00,000) -> AT RISK
        String c1Id = "c1-boat-lifestyle";
        Company boat = Company.builder()
                .companyId(c1Id)
                .companyName("boAt Lifestyle (Imagine Marketing Ltd)")
                .email("compliance@boat-lifestyle.com")
                .passwordHash(encodedPw)
                .state("Maharashtra")
                .cpcbRegistrationNumber("CPCB/EPR/BOAT/2023/001")
                .registeredOn(Instant.now().toString())
                .alertsEnabled(true)
                .alertEmail("compliance@boat-lifestyle.com")
                .alertPhone("+91 98200 11223")
                .build();
        companyRepo.save(boat);

        // Products for boAt:
        // Earphones: 10,00,000 * 0.333 * 0.80 = 2,66,400 units
        // Phones: 5,00,000 * 0.333 * 0.80 = 1,33,200 units
        // Total obligation = 3,99,600 units
        productRepo.save(Product.builder()
                .productId(UUID.randomUUID().toString())
                .companyId(c1Id)
                .productName("boAt Airdopes 141 True Wireless")
                .productCategory("EARPHONE_HEADPHONE")
                .unitsSold(1000000L)
                .conversionFactor(0.333)
                .financialYear("2026-27")
                .eprObligationUnits(266400L)
                .createdAt(Instant.now().toString())
                .build());
        productRepo.save(Product.builder()
                .productId(UUID.randomUUID().toString())
                .companyId(c1Id)
                .productName("boAt Smart Calling Feature Phone")
                .productCategory("MOBILE_PHONE")
                .unitsSold(500000L)
                .conversionFactor(0.333)
                .financialYear("2026-27")
                .eprObligationUnits(133200L)
                .createdAt(Instant.now().toString())
                .build());

        // boAt Collection (~50% -> AT RISK): 2,00,000 kg collected
        logRepo.save(ComplianceLog.builder()
                .logId(UUID.randomUUID().toString())
                .companyId(c1Id)
                .recyclerId("rec-mum-1")
                .recyclerName("EcoRecycle India Ltd (Mumbai)")
                .quantityCollectedKg(200000.0)
                .eprCertificateNumber("CPCB/REC/MAH/2026/8841")
                .dateLogged("2026-06-15")
                .financialYear("2026-27")
                .build());

        // 2. Vivo India — phones (8,00,000) + tablets (2,00,000) -> ON TRACK (>70%)
        String c2Id = "c2-vivo-india";
        Company vivo = Company.builder()
                .companyId(c2Id)
                .companyName("Vivo Mobile India Pvt Ltd")
                .email("compliance@vivo.com")
                .passwordHash(encodedPw)
                .state("Uttar Pradesh")
                .cpcbRegistrationNumber("CPCB/EPR/VIVO/2023/002")
                .registeredOn(Instant.now().toString())
                .alertsEnabled(true)
                .alertEmail("compliance@vivo.com")
                .alertPhone("+91 98111 22334")
                .build();
        companyRepo.save(vivo);

        // Phones: 8,00,000 * 0.333 * 0.80 = 2,13,120 units
        // Tablets: 2,00,000 * 0.250 * 0.80 = 40,000 units
        // Total = 2,53,120 units
        productRepo.save(Product.builder()
                .productId(UUID.randomUUID().toString())
                .companyId(c2Id)
                .productName("Vivo V30 Pro 5G")
                .productCategory("MOBILE_PHONE")
                .unitsSold(800000L)
                .conversionFactor(0.333)
                .financialYear("2026-27")
                .eprObligationUnits(213120L)
                .createdAt(Instant.now().toString())
                .build());
        productRepo.save(Product.builder()
                .productId(UUID.randomUUID().toString())
                .companyId(c2Id)
                .productName("Vivo Pad Air")
                .productCategory("TABLET")
                .unitsSold(200000L)
                .conversionFactor(0.250)
                .financialYear("2026-27")
                .eprObligationUnits(40000L)
                .createdAt(Instant.now().toString())
                .build());
        // 2,10,000 collected (83% -> ON TRACK)
        logRepo.save(ComplianceLog.builder()
                .logId(UUID.randomUUID().toString())
                .companyId(c2Id)
                .recyclerId("rec-del-1")
                .recyclerName("Greentec E-Waste Solutions (Delhi)")
                .quantityCollectedKg(210000.0)
                .eprCertificateNumber("CPCB/REC/DL/2026/1029")
                .dateLogged("2026-08-20")
                .financialYear("2026-27")
                .build());

        // 3. BlueStar Limited — ACs (3,00,000) -> NON-COMPLIANT (<40%)
        String c3Id = "c3-bluestar-limited";
        Company bluestar = Company.builder()
                .companyId(c3Id)
                .companyName("Blue Star Limited")
                .email("compliance@bluestarindia.com")
                .passwordHash(encodedPw)
                .state("Maharashtra")
                .cpcbRegistrationNumber("CPCB/EPR/BLUE/2023/003")
                .registeredOn(Instant.now().toString())
                .alertsEnabled(true)
                .alertEmail("compliance@bluestarindia.com")
                .alertPhone("+91 98222 33445")
                .build();
        companyRepo.save(bluestar);

        // ACs: 3,00,000 * 0.100 * 0.80 = 24,000 units
        productRepo.save(Product.builder()
                .productId(UUID.randomUUID().toString())
                .companyId(c3Id)
                .productName("Blue Star Inverter Split AC 1.5T")
                .productCategory("AIR_CONDITIONER")
                .unitsSold(300000L)
                .conversionFactor(0.100)
                .financialYear("2026-27")
                .eprObligationUnits(24000L)
                .createdAt(Instant.now().toString())
                .build());
        // Only 5,000 kg collected (20.8% -> NON-COMPLIANT)
        logRepo.save(ComplianceLog.builder()
                .logId(UUID.randomUUID().toString())
                .companyId(c3Id)
                .recyclerId("rec-pun-1")
                .recyclerName("Maharashtra Eco-Recyclers (Pune)")
                .quantityCollectedKg(5000.0)
                .eprCertificateNumber("CPCB/REC/PUN/2026/4102")
                .dateLogged("2026-05-10")
                .financialYear("2026-27")
                .build());

        // 4. Dell India — laptops (4,00,000) + printers (1,00,000) -> ON TRACK
        String c4Id = "c4-dell-india";
        Company dell = Company.builder()
                .companyId(c4Id)
                .companyName("Dell International Services India Pvt Ltd")
                .email("compliance@dell.com")
                .passwordHash(encodedPw)
                .state("Karnataka")
                .cpcbRegistrationNumber("CPCB/EPR/DELL/2023/004")
                .registeredOn(Instant.now().toString())
                .alertsEnabled(true)
                .alertEmail("compliance@dell.com")
                .alertPhone("+91 98333 44556")
                .build();
        companyRepo.save(dell);

        // Laptops: 4,00,000 * 0.200 * 0.80 = 64,000 units
        // Printers: 1,00,000 * 0.200 * 0.80 = 16,000 units
        // Total = 80,000 units
        productRepo.save(Product.builder()
                .productId(UUID.randomUUID().toString())
                .companyId(c4Id)
                .productName("Dell Latitude 5440 Enterprise")
                .productCategory("LAPTOP_COMPUTER")
                .unitsSold(400000L)
                .conversionFactor(0.200)
                .financialYear("2026-27")
                .eprObligationUnits(64000L)
                .createdAt(Instant.now().toString())
                .build());
        productRepo.save(Product.builder()
                .productId(UUID.randomUUID().toString())
                .companyId(c4Id)
                .productName("Dell Laser Multifunction Scanner")
                .productCategory("PRINTER_SCANNER")
                .unitsSold(100000L)
                .conversionFactor(0.200)
                .financialYear("2026-27")
                .eprObligationUnits(16000L)
                .createdAt(Instant.now().toString())
                .build());
        // 65,000 kg collected (81.2% -> ON TRACK)
        logRepo.save(ComplianceLog.builder()
                .logId(UUID.randomUUID().toString())
                .companyId(c4Id)
                .recyclerId("rec-blr-1")
                .recyclerName("Bengaluru Green E-Waste Recovery (Bangalore)")
                .quantityCollectedKg(65000.0)
                .eprCertificateNumber("CPCB/REC/KA/2026/7781")
                .dateLogged("2026-07-22")
                .financialYear("2026-27")
                .build());

        // 5. Whirlpool India — washing machines (2,50,000) + refrigerators (1,50,000) -> AT RISK
        String c5Id = "c5-whirlpool-india";
        Company whirlpool = Company.builder()
                .companyId(c5Id)
                .companyName("Whirlpool of India Limited")
                .email("compliance@whirlpool.com")
                .passwordHash(encodedPw)
                .state("Haryana")
                .cpcbRegistrationNumber("CPCB/EPR/WHIRL/2023/005")
                .registeredOn(Instant.now().toString())
                .alertsEnabled(true)
                .alertEmail("compliance@whirlpool.com")
                .alertPhone("+91 98444 55667")
                .build();
        companyRepo.save(whirlpool);

        // Washing Machines: 2,50,000 * 0.100 * 0.80 = 20,000 units
        // Refrigerators: 1,50,000 * 0.083 * 0.80 = 9,960 units
        // Total = 29,960 units
        productRepo.save(Product.builder()
                .productId(UUID.randomUUID().toString())
                .companyId(c5Id)
                .productName("Whirlpool 8kg Front Load Washing Machine")
                .productCategory("WASHING_MACHINE")
                .unitsSold(250000L)
                .conversionFactor(0.100)
                .financialYear("2026-27")
                .eprObligationUnits(20000L)
                .createdAt(Instant.now().toString())
                .build());
        productRepo.save(Product.builder()
                .productId(UUID.randomUUID().toString())
                .companyId(c5Id)
                .productName("Whirlpool Intellifresh Inverter Refrigerator")
                .productCategory("REFRIGERATOR")
                .unitsSold(150000L)
                .conversionFactor(0.083)
                .financialYear("2026-27")
                .eprObligationUnits(9960L)
                .createdAt(Instant.now().toString())
                .build());
        // 14,000 collected (46.7% -> AT RISK)
        logRepo.save(ComplianceLog.builder()
                .logId(UUID.randomUUID().toString())
                .companyId(c5Id)
                .recyclerId("rec-del-2")
                .recyclerName("Apex Clean Enviro Services (Delhi)")
                .quantityCollectedKg(14000.0)
                .eprCertificateNumber("CPCB/REC/HR/2026/3310")
                .dateLogged("2026-06-30")
                .financialYear("2026-27")
                .build());
    }

    private List<Recycler> createSeedRecyclers() {
        List<Recycler> list = new ArrayList<>();

        // Delhi (3)
        list.add(Recycler.builder()
                .recyclerId("rec-del-1")
                .name("Greentec E-Waste Solutions Pvt Ltd")
                .state("Delhi")
                .city("Delhi")
                .latitude(28.6139)
                .longitude(77.2090)
                .categoriesHandled(List.of("MOBILE_PHONE", "LAPTOP_COMPUTER", "TABLET"))
                .cpcbCertified(true)
                .contactEmail("contact@greentec-delhi.com")
                .contactPhone("+91 11 2345 6789")
                .capacityTonnesPerYear(4500)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-del-2")
                .name("Apex Clean Enviro Services")
                .state("Delhi")
                .city("Delhi")
                .latitude(28.6500)
                .longitude(77.2300)
                .categoriesHandled(List.of("WASHING_MACHINE", "REFRIGERATOR", "AIR_CONDITIONER"))
                .cpcbCertified(true)
                .contactEmail("ops@apexclean.in")
                .contactPhone("+91 11 2876 5432")
                .capacityTonnesPerYear(3200)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-del-3")
                .name("Capital E-Recovery India")
                .state("Delhi")
                .city("Delhi")
                .latitude(28.5800)
                .longitude(77.1800)
                .categoriesHandled(List.of("TELEVISION", "COMPUTER_MONITOR", "EARPHONE_HEADPHONE"))
                .cpcbCertified(true)
                .contactEmail("info@capitale-recovery.org")
                .contactPhone("+91 11 2981 1200")
                .capacityTonnesPerYear(2800)
                .build());

        // Mumbai (3)
        list.add(Recycler.builder()
                .recyclerId("rec-mum-1")
                .name("EcoRecycle India Ltd")
                .state("Maharashtra")
                .city("Mumbai")
                .latitude(19.0760)
                .longitude(72.8777)
                .categoriesHandled(List.of("EARPHONE_HEADPHONE", "MOBILE_PHONE", "SMARTWATCH_WEARABLE"))
                .cpcbCertified(true)
                .contactEmail("compliance@ecorecycle-mumbai.com")
                .contactPhone("+91 22 2654 3210")
                .capacityTonnesPerYear(5000)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-mum-2")
                .name("Western India Green Tech")
                .state("Maharashtra")
                .city("Mumbai")
                .latitude(19.1100)
                .longitude(72.9000)
                .categoriesHandled(List.of("LAPTOP_COMPUTER", "ROUTER_MODEM", "HARD_DISK_SSD"))
                .cpcbCertified(true)
                .contactEmail("contact@westerngreen.in")
                .contactPhone("+91 22 4123 9900")
                .capacityTonnesPerYear(3800)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-mum-3")
                .name("Navi E-Waste Dismantlers")
                .state("Maharashtra")
                .city("Mumbai")
                .latitude(19.0330)
                .longitude(73.0297)
                .categoriesHandled(List.of("AIR_CONDITIONER", "REFRIGERATOR", "MICROWAVE_OVEN"))
                .cpcbCertified(true)
                .contactEmail("info@naviewaste.com")
                .contactPhone("+91 22 2780 1144")
                .capacityTonnesPerYear(2400)
                .build());

        // Bangalore (3)
        list.add(Recycler.builder()
                .recyclerId("rec-blr-1")
                .name("Bengaluru Green E-Waste Recovery")
                .state("Karnataka")
                .city("Bangalore")
                .latitude(12.9716)
                .longitude(77.5946)
                .categoriesHandled(List.of("LAPTOP_COMPUTER", "PRINTER_SCANNER", "COMPUTER_MONITOR"))
                .cpcbCertified(true)
                .contactEmail("info@bengalurugreen.in")
                .contactPhone("+91 80 4112 3344")
                .capacityTonnesPerYear(4800)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-blr-2")
                .name("Silicon City Eco Refiners")
                .state("Karnataka")
                .city("Bangalore")
                .latitude(12.8500)
                .longitude(77.6600)
                .categoriesHandled(List.of("MOBILE_PHONE", "TABLET", "HARD_DISK_SSD"))
                .cpcbCertified(true)
                .contactEmail("connect@siliconcityrefiners.com")
                .contactPhone("+91 80 6789 0123")
                .capacityTonnesPerYear(3100)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-blr-3")
                .name("Whitefield Clean Electronics")
                .state("Karnataka")
                .city("Bangalore")
                .latitude(12.9698)
                .longitude(77.7500)
                .categoriesHandled(List.of("ROUTER_MODEM", "UPS_INVERTER", "SOLAR_PANEL_INVERTER"))
                .cpcbCertified(true)
                .contactEmail("support@whitefieldclean.org")
                .contactPhone("+91 80 2845 6711")
                .capacityTonnesPerYear(1900)
                .build());

        // Chennai (3)
        list.add(Recycler.builder()
                .recyclerId("rec-chn-1")
                .name("Coromandel E-Recycling Unit")
                .state("Tamil Nadu")
                .city("Chennai")
                .latitude(13.0827)
                .longitude(80.2707)
                .categoriesHandled(List.of("TELEVISION", "WATER_PURIFIER", "AIR_CONDITIONER"))
                .cpcbCertified(true)
                .contactEmail("ops@coromandel-erecycle.com")
                .contactPhone("+91 44 2434 5678")
                .capacityTonnesPerYear(3500)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-chn-2")
                .name("Madras Green Metals & E-Waste")
                .state("Tamil Nadu")
                .city("Chennai")
                .latitude(13.0400)
                .longitude(80.2200)
                .categoriesHandled(List.of("LAPTOP_COMPUTER", "CAMERA", "MOBILE_PHONE"))
                .cpcbCertified(true)
                .contactEmail("contact@madrasgreenmetals.in")
                .contactPhone("+91 44 2815 4321")
                .capacityTonnesPerYear(2900)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-chn-3")
                .name("Southern Safe Disposals")
                .state("Tamil Nadu")
                .city("Chennai")
                .latitude(13.1200)
                .longitude(80.2000)
                .categoriesHandled(List.of("EV_CHARGER", "UPS_INVERTER", "OTHER_EEE"))
                .cpcbCertified(true)
                .contactEmail("info@southernsafe.org")
                .contactPhone("+91 44 2661 7890")
                .capacityTonnesPerYear(2100)
                .build());

        // Hyderabad (3)
        list.add(Recycler.builder()
                .recyclerId("rec-hyd-1")
                .name("Deccan Circular Solutions")
                .state("Telangana")
                .city("Hyderabad")
                .latitude(17.3850)
                .longitude(78.4867)
                .categoriesHandled(List.of("MOBILE_PHONE", "LAPTOP_COMPUTER", "ROUTER_MODEM"))
                .cpcbCertified(true)
                .contactEmail("support@deccancircular.com")
                .contactPhone("+91 40 2345 6789")
                .capacityTonnesPerYear(4200)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-hyd-2")
                .name("Cyberabad Eco-Dismantling Park")
                .state("Telangana")
                .city("Hyderabad")
                .latitude(17.4400)
                .longitude(78.3800)
                .categoriesHandled(List.of("PRINTER_SCANNER", "HARD_DISK_SSD", "COMPUTER_MONITOR"))
                .cpcbCertified(true)
                .contactEmail("ops@cyberabadeco.in")
                .contactPhone("+91 40 4012 3344")
                .capacityTonnesPerYear(3300)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-hyd-3")
                .name("Telangana State Authorized Recyclers")
                .state("Telangana")
                .city("Hyderabad")
                .latitude(17.3200)
                .longitude(78.5200)
                .categoriesHandled(List.of("WASHING_MACHINE", "REFRIGERATOR", "MICROWAVE_OVEN"))
                .cpcbCertified(true)
                .contactEmail("info@ts-recyclers.org")
                .contactPhone("+91 40 2761 9876")
                .capacityTonnesPerYear(2500)
                .build());

        // Pune (3)
        list.add(Recycler.builder()
                .recyclerId("rec-pun-1")
                .name("Maharashtra Eco-Recyclers")
                .state("Maharashtra")
                .city("Pune")
                .latitude(18.5204)
                .longitude(73.8567)
                .categoriesHandled(List.of("AIR_CONDITIONER", "REFRIGERATOR", "WASHING_MACHINE"))
                .cpcbCertified(true)
                .contactEmail("contact@maheco-pune.com")
                .contactPhone("+91 20 2553 4411")
                .capacityTonnesPerYear(4500)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-pun-2")
                .name("Chakan Green Industrial Recovery")
                .state("Maharashtra")
                .city("Pune")
                .latitude(18.7500)
                .longitude(73.8500)
                .categoriesHandled(List.of("EV_CHARGER", "UPS_INVERTER", "SOLAR_PANEL_INVERTER"))
                .cpcbCertified(true)
                .contactEmail("info@chakangreen.in")
                .contactPhone("+91 20 6601 2233")
                .capacityTonnesPerYear(3700)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-pun-3")
                .name("Hinjawadi Tech-Waste Solutions")
                .state("Maharashtra")
                .city("Pune")
                .latitude(18.5900)
                .longitude(73.7400)
                .categoriesHandled(List.of("LAPTOP_COMPUTER", "MOBILE_PHONE", "EARPHONE_HEADPHONE"))
                .cpcbCertified(true)
                .contactEmail("service@hinjawadi-ewaste.com")
                .contactPhone("+91 20 4911 8800")
                .capacityTonnesPerYear(2600)
                .build());

        // Kolkata (3)
        list.add(Recycler.builder()
                .recyclerId("rec-kol-1")
                .name("Bengal Clean Earth Recyclers")
                .state("West Bengal")
                .city("Kolkata")
                .latitude(22.5726)
                .longitude(88.3639)
                .categoriesHandled(List.of("TELEVISION", "WATER_PURIFIER", "MICROWAVE_OVEN"))
                .cpcbCertified(true)
                .contactEmail("admin@bengalcleanearth.in")
                .contactPhone("+91 33 2289 1234")
                .capacityTonnesPerYear(3100)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-kol-2")
                .name("Salt Lake Safe Electronic Disposal")
                .state("West Bengal")
                .city("Kolkata")
                .latitude(22.5800)
                .longitude(88.4200)
                .categoriesHandled(List.of("LAPTOP_COMPUTER", "PRINTER_SCANNER", "ROUTER_MODEM"))
                .cpcbCertified(true)
                .contactEmail("saltlake@safedisposal.com")
                .contactPhone("+91 33 4005 6700")
                .capacityTonnesPerYear(2300)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-kol-3")
                .name("Howrah Industrial Waste Treatment")
                .state("West Bengal")
                .city("Kolkata")
                .latitude(22.6000)
                .longitude(88.3200)
                .categoriesHandled(List.of("UPS_INVERTER", "EV_CHARGER", "OTHER_EEE"))
                .cpcbCertified(true)
                .contactEmail("info@howrahwaste.org")
                .contactPhone("+91 33 2650 4433")
                .capacityTonnesPerYear(1800)
                .build());

        // Ahmedabad (3)
        list.add(Recycler.builder()
                .recyclerId("rec-ahm-1")
                .name("Gujarat Green Environment Ltd")
                .state("Gujarat")
                .city("Ahmedabad")
                .latitude(23.0225)
                .longitude(72.5714)
                .categoriesHandled(List.of("REFRIGERATOR", "AIR_CONDITIONER", "WASHING_MACHINE"))
                .cpcbCertified(true)
                .contactEmail("compliance@gujaratgreen.in")
                .contactPhone("+91 79 2640 1122")
                .capacityTonnesPerYear(4900)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-ahm-2")
                .name("Sabarmati Clean Metals & Plastics")
                .state("Gujarat")
                .city("Ahmedabad")
                .latitude(23.0800)
                .longitude(72.5900)
                .categoriesHandled(List.of("MOBILE_PHONE", "CAMERA", "SMARTWATCH_WEARABLE"))
                .cpcbCertified(true)
                .contactEmail("support@sabarmaticlean.com")
                .contactPhone("+91 79 4022 5566")
                .capacityTonnesPerYear(3600)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-ahm-3")
                .name("Sanand Electronic Recyclers Hub")
                .state("Gujarat")
                .city("Ahmedabad")
                .latitude(22.9800)
                .longitude(72.3800)
                .categoriesHandled(List.of("SOLAR_PANEL_INVERTER", "EV_CHARGER", "UPS_INVERTER"))
                .cpcbCertified(true)
                .contactEmail("ops@sanand-hub.org")
                .contactPhone("+91 79 6190 2200")
                .capacityTonnesPerYear(2700)
                .build());

        // Jaipur (3)
        list.add(Recycler.builder()
                .recyclerId("rec-jpr-1")
                .name("Rajasthan Eco-Care Processing")
                .state("Rajasthan")
                .city("Jaipur")
                .latitude(26.9124)
                .longitude(75.7873)
                .categoriesHandled(List.of("SOLAR_PANEL_INVERTER", "WATER_PURIFIER", "TELEVISION"))
                .cpcbCertified(true)
                .contactEmail("info@rajasthan-ecocare.in")
                .contactPhone("+91 141 2367 890")
                .capacityTonnesPerYear(3300)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-jpr-2")
                .name("Pink City E-Recovery Plant")
                .state("Rajasthan")
                .city("Jaipur")
                .latitude(26.8500)
                .longitude(75.8200)
                .categoriesHandled(List.of("MOBILE_PHONE", "EARPHONE_HEADPHONE", "TABLET"))
                .cpcbCertified(true)
                .contactEmail("contact@pinkcityrecovery.com")
                .contactPhone("+91 141 4088 123")
                .capacityTonnesPerYear(2500)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-jpr-3")
                .name("Sitapura Clean Electronics Facility")
                .state("Rajasthan")
                .city("Jaipur")
                .latitude(26.7800)
                .longitude(75.8500)
                .categoriesHandled(List.of("LAPTOP_COMPUTER", "ROUTER_MODEM", "OTHER_EEE"))
                .cpcbCertified(true)
                .contactEmail("ops@sitapuraclean.org")
                .contactPhone("+91 141 2770 456")
                .capacityTonnesPerYear(1900)
                .build());

        // Lucknow (3)
        list.add(Recycler.builder()
                .recyclerId("rec-lko-1")
                .name("Awadh Green E-Waste Solutions")
                .state("Uttar Pradesh")
                .city("Lucknow")
                .latitude(26.8467)
                .longitude(80.9462)
                .categoriesHandled(List.of("MOBILE_PHONE", "TELEVISION", "LAPTOP_COMPUTER"))
                .cpcbCertified(true)
                .contactEmail("compliance@awadhgreen.in")
                .contactPhone("+91 522 2345 678")
                .capacityTonnesPerYear(3400)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-lko-2")
                .name("Gomti Eco-Recycling Complex")
                .state("Uttar Pradesh")
                .city("Lucknow")
                .latitude(26.8900)
                .longitude(81.0100)
                .categoriesHandled(List.of("WASHING_MACHINE", "REFRIGERATOR", "MICROWAVE_OVEN"))
                .cpcbCertified(true)
                .contactEmail("contact@gomtieco.com")
                .contactPhone("+91 522 4012 900")
                .capacityTonnesPerYear(2800)
                .build());
        list.add(Recycler.builder()
                .recyclerId("rec-lko-3")
                .name("UP Central Electronics Recovery Hub")
                .state("Uttar Pradesh")
                .city("Lucknow")
                .latitude(26.7900)
                .longitude(80.9100)
                .categoriesHandled(List.of("UPS_INVERTER", "SOLAR_PANEL_INVERTER", "AIR_CONDITIONER"))
                .cpcbCertified(true)
                .contactEmail("info@upcentral-recovery.org")
                .contactPhone("+91 522 2611 345")
                .capacityTonnesPerYear(2200)
                .build());

        return list;
    }
}
