package me.yokkkoso.tiktokkk;

public final class Countries {

    public static final String[][] LIST = {
            {"", "Off (use real region)", "", "", ""},
            {"US", "United States", "310", "260", "T-Mobile"},
            {"GB", "United Kingdom", "234", "15", "Vodafone"},
            {"KZ", "Kazakhstan", "401", "02", "Kcell"},
            {"RU", "Russia", "250", "01", "MTS"},
            {"AF", "Afghanistan", "412", "01", "AWCC"},
            {"UA", "Ukraine", "255", "01", "Vodafone UA"},
            {"DE", "Germany", "262", "01", "Telekom.de"},
            {"FR", "France", "208", "01", "Orange F"},
            {"ES", "Spain", "214", "07", "Movistar"},
            {"IT", "Italy", "222", "01", "TIM"},
            {"NL", "Netherlands", "204", "08", "KPN"},
            {"PL", "Poland", "260", "03", "Orange PL"},
            {"TR", "Turkey", "286", "01", "Turkcell"},
            {"JP", "Japan", "440", "10", "docomo"},
            {"KR", "South Korea", "450", "05", "SKTelecom"},
            {"CN", "China", "460", "00", "China Mobile"},
            {"IN", "India", "404", "10", "Airtel"},
            {"ID", "Indonesia", "510", "10", "Telkomsel"},
            {"VN", "Vietnam", "452", "04", "Viettel"},
            {"TH", "Thailand", "520", "03", "AIS"},
            {"PH", "Philippines", "515", "02", "Globe"},
            {"BR", "Brazil", "724", "10", "Vivo"},
            {"MX", "Mexico", "334", "020", "Telcel"},
            {"CA", "Canada", "302", "720", "Rogers"},
            {"AU", "Australia", "505", "01", "Telstra"},
            {"SA", "Saudi Arabia", "420", "01", "STC"},
            {"AE", "UAE", "424", "02", "Etisalat"},
            {"EG", "Egypt", "602", "02", "Vodafone EG"},
            {"NG", "Nigeria", "621", "30", "MTN"},
    };

    private static final java.util.Set<String> BLOCKED =
            new java.util.HashSet<>(java.util.Arrays.asList("RU", "AF"));

    public static boolean isBlocked(String code) {
        return code != null && BLOCKED.contains(code.trim().toUpperCase(java.util.Locale.ROOT));
    }

    public static String[] find(String code) {
        String c = code == null ? "" : code.trim();
        for (String[] r : LIST) {
            if (r[0].equalsIgnoreCase(c)) return r;
        }
        return null;
    }

    public static String mcc(String code) {
        String[] r = find(code);
        return r != null && r.length > 2 ? r[2] : "";
    }

    public static String mnc(String code) {
        String[] r = find(code);
        return r != null && r.length > 3 ? r[3] : "";
    }

    public static String operator(String code) {
        String[] r = find(code);
        return r != null && r.length > 4 ? r[4] : "";
    }

    public static String flag(String code) {
        if (code == null || code.length() != 2) return "🏳️";
        code = code.toUpperCase(java.util.Locale.ROOT);
        int a = Character.codePointAt("" + code.charAt(0), 0) - 'A' + 0x1F1E6;
        int b = Character.codePointAt("" + code.charAt(1), 0) - 'A' + 0x1F1E6;
        return new String(Character.toChars(a)) + new String(Character.toChars(b));
    }

    public static String label(String code) {
        for (String[] c : LIST) {
            if (c[0].equalsIgnoreCase(code == null ? "" : code)) {
                return c[0].isEmpty() ? c[1] : flag(c[0]) + "  " + c[1];
            }
        }
        return code;
    }

    private Countries() {}
}
