package com.wayble.server.wayblezone.importer;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BusinessHourParser {

    private static final String RANGE = "(\\d{1,2})[:시](\\d{2})\\s*[-~]\\s*(\\d{1,2})[:시](\\d{2})";
    private static final Pattern P_DAILY   = Pattern.compile("매일\\s*" + RANGE);
    private static final Pattern P_WEEKDAY = Pattern.compile("평일\\s*" + RANGE);
    private static final Pattern P_WEEKEND = Pattern.compile("(주말(?:,\\s*공휴일)?)\\s*" + RANGE);
    private static final Pattern P_SAT     = Pattern.compile("(토|토요일)\\s*" + RANGE);
    private static final Pattern P_SUN     = Pattern.compile("(일|일요일)\\s*" + RANGE);
    private static final Pattern P_DAY_KO  = Pattern.compile("(월|화|수|목|금|토|일)(?:요일)?\\s*" + RANGE);

    public record TimeRange(LocalTime open, LocalTime close) {}

    public static Map<DayOfWeek, TimeRange> parse(String raw) {
        Map<DayOfWeek, TimeRange> map = new EnumMap<>(DayOfWeek.class);
        if (raw == null) return map;

        String s = raw.replaceAll("\\s+", " ").trim();
        if (s.isEmpty() || s.contains("점포별") || s.contains("상이") || s.contains("휴무")) return map;

        if (s.contains("24시간") || s.contains("24시")) {
            TimeRange tr = new TimeRange(LocalTime.MIDNIGHT, LocalTime.of(23,59));
            for (DayOfWeek d : DayOfWeek.values()) map.put(d, tr);
            return map;
        }

        // 매일
        matchAndFill(P_DAILY, s, map, List.of(DayOfWeek.values()));

        // 평일
        matchAndFill(P_WEEKDAY, s, map, List.of(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY));

        // 주말, 공휴일
        matchAndFill(P_WEEKEND, s, map, List.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY));

        // 토/일 개별
        matchAndFill(P_SAT, s, map, List.of(DayOfWeek.SATURDAY));
        matchAndFill(P_SUN, s, map, List.of(DayOfWeek.SUNDAY));

        // 개별 요일 지정 혼합
        Matcher md = P_DAY_KO.matcher(s);
        while (md.find()) {
            DayOfWeek d = toDay(md.group(1));
            TimeRange tr = toRange(md, md.groupCount()-3);
            if (d != null && tr != null) map.put(d, tr);
        }

        return map;
    }

    private static void matchAndFill(Pattern p, String s, Map<DayOfWeek, TimeRange> map, List<DayOfWeek> days) {
        Matcher m = p.matcher(s);
        if (m.find()) {
            int base = m.groupCount() - 3;
            TimeRange tr = toRange(m, base);
            for (DayOfWeek d : days) map.put(d, tr);
        }
    }

    private static TimeRange toRange(Matcher m, int base) {
        int sH = toInt(m.group(base));
        int sM = toInt(m.group(base+1));
        int eH = toInt(m.group(base+2));
        int eM = toInt(m.group(base+3));
        LocalTime open = LocalTime.of(normalizeHour(sH), sM);
        LocalTime close = LocalTime.of(normalizeHour(eH), eM);
        if (eH == 24 && eM == 0) close = LocalTime.of(23,59); // 24:00 처리
        return new TimeRange(open, close);
    }

    private static int toInt(String s) { return Integer.parseInt(s.replaceAll("[^0-9]", "")); }
    private static int normalizeHour(int h) { return h == 24 ? 23 : h; }

    private static DayOfWeek toDay(String ko) {
        return switch (ko) {
            case "월" -> DayOfWeek.MONDAY;
            case "화" -> DayOfWeek.TUESDAY;
            case "수" -> DayOfWeek.WEDNESDAY;
            case "목" -> DayOfWeek.THURSDAY;
            case "금" -> DayOfWeek.FRIDAY;
            case "토", "토요일" -> DayOfWeek.SATURDAY;
            case "일", "일요일" -> DayOfWeek.SUNDAY;
            default -> null;
        };
    }
}