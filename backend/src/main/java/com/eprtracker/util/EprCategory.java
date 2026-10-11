package com.eprtracker.util;

import lombok.Getter;

@Getter
public enum EprCategory {
    MOBILE_PHONE("Mobile Phones", 3, 0.333),
    LAPTOP_COMPUTER("Laptop Computers", 5, 0.200),
    TELEVISION("Televisions", 7, 0.143),
    AIR_CONDITIONER("Air Conditioners", 10, 0.100),
    EARPHONE_HEADPHONE("Earphones & Headphones", 3, 0.333),
    WASHING_MACHINE("Washing Machines", 10, 0.100),
    REFRIGERATOR("Refrigerators", 12, 0.083),
    PRINTER_SCANNER("Printers & Scanners", 5, 0.200),
    TABLET("Tablets", 4, 0.250),
    CAMERA("Digital Cameras", 5, 0.200),
    MICROWAVE_OVEN("Microwave Ovens", 8, 0.125),
    WATER_PURIFIER("Water Purifiers", 7, 0.143),
    UPS_INVERTER("UPS & Inverters", 6, 0.167),
    COMPUTER_MONITOR("Computer Monitors", 6, 0.167),
    HARD_DISK_SSD("Hard Disks & SSDs", 4, 0.250),
    ROUTER_MODEM("Routers & Modems", 5, 0.200),
    SMARTWATCH_WEARABLE("Smartwatches & Wearables", 3, 0.333),
    EV_CHARGER("EV Chargers", 10, 0.100),
    SOLAR_PANEL_INVERTER("Solar Panel Inverters", 15, 0.067),
    OTHER_EEE("Other Electrical & Electronic Equipment", 5, 0.200);

    private final String displayName;
    private final int lifespanYears;
    private final double conversionFactor;

    EprCategory(String displayName, int lifespanYears, double conversionFactor) {
        this.displayName = displayName;
        this.lifespanYears = lifespanYears;
        this.conversionFactor = conversionFactor;
    }

    public static double getTargetPercentageForYear(String financialYear) {
        if (financialYear == null) {
            return 0.80; // Default current FY2026-27
        }
        return switch (financialYear.trim().toUpperCase()) {
            case "2023-24", "FY2023-24" -> 0.60;
            case "2024-25", "FY2024-25" -> 0.65;
            case "2025-26", "FY2025-26" -> 0.70;
            case "2026-27", "FY2026-27" -> 0.80;
            case "2027-28", "FY2027-28" -> 0.90;
            default -> 0.80;
        };
    }
}
