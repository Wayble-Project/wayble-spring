package com.wayble.server.wayblezone.importer;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BusinessHourParser {

    private static final Pattern RANGE = Pattern.compile("(\\d{1,2}):(\\d{2})\\s*[-~]\\s*(\\d{1,2}):(\\d{2})");
    private static final Pattern DAILY = Pattern.compile("매일\\s*" + RANGE.pattern());
    private static final Pattern WEEKDAY = Pattern.compile("평일\\s*" + RANGE.pattern());
    private static final Pattern WEEKEND = Pattern.compile("주말\\s*" + RANGE.pattern());
    private static final Pattern DAY_KO = Pattern.compile("(월|화|수|목|금|토|일)\\s*" + RANGE.pattern());

    public record TimeRange(LocalTime open, LocalTime close) {}

    public static Map<DayOfWeek, TimeRange> parse(String raw) {
        Map<DayOfWeek, TimeRange> map = new EnumMap<>(DayOfWeek.class);
        if (raw == null) { return map; }

        String s = raw.replaceAll("\\s+", " ").trim();
        if (s.isEmpty()) { return map; }

        // 예외 or 무시하는 케이스
        if (s.contains("점포별") || s.contains("상이") || s.contains("휴무")) { return map; }

        if (s.contains("24시간") || s.contains("24시")) {
            TimeRange tr = new TimeRange(LocalTime.MIDNIGHT, LocalTime.of(23,59));
            for (DayOfWeek d : DayOfWeek.values()) map.put(d, tr);
            return map;
        }

        // 매일
        Matcher mDaily = DAILY.matcher(s);
        if (mDaily.find()) {
            TimeRange tr = toRange(mDaily);
            for (DayOfWeek d : DayOfWeek.values()) map.put(d, tr);
        }

        // 평일/주말
        Matcher mWeek = WEEKDAY.matcher(s);
        if (mWeek.find()) {
            TimeRange tr = toRange(mWeek);
            for (DayOfWeek d : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                    DayOfWeek.THURSDAY, DayOfWeek.FRIDAY)) {
                map.put(d, tr);
            }
        }
        Matcher mWeekend = WEEKEND.matcher(s);
        if (mWeekend.find()) {
            TimeRange tr = toRange(mWeekend);
            map.put(DayOfWeek.SATURDAY, tr);
            map.put(DayOfWeek.SUNDAY, tr);
        }

        // 개별 요일 패턴도 있어야함 (예시: 토 10:00-21:00 | 일 11:00-20:00)
        Matcher md = DAY_KO.matcher(s);
        while (md.find()) {
            DayOfWeek d = toDay(md.group(1));
            TimeRange tr = toRange(md);
            if (d != null && tr != null) map.put(d, tr);
        }

        return map;
    }

    private static TimeRange toRange(Matcher m) {
        int sH = Integer.parseInt(m.group(m.groupCount()-3));
        int sM = Integer.parseInt(m.group(m.groupCount()-2));
        int eH = Integer.parseInt(m.group(m.groupCount()-1));
        int eM = Integer.parseInt(m.group(m.groupCount()));
        LocalTime open = LocalTime.of(sH, sM);
        LocalTime close = LocalTime.of(eH, eM);
        if (close.equals(LocalTime.MIDNIGHT)) close = LocalTime.of(23,59);
        return new TimeRange(open, close);
    }

    private static DayOfWeek toDay(String ko) {
        return switch (ko) {
            case "월" -> DayOfWeek.MONDAY;
            case "화" -> DayOfWeek.TUESDAY;
            case "수" -> DayOfWeek.WEDNESDAY;
            case "목" -> DayOfWeek.THURSDAY;
            case "금" -> DayOfWeek.FRIDAY;
            case "토" -> DayOfWeek.SATURDAY;
            case "일" -> DayOfWeek.SUNDAY;
            default -> null;
        };
    }
}
