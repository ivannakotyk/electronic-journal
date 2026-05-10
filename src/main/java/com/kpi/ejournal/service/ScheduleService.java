package com.kpi.ejournal.service;

import com.kpi.ejournal.dto.schedule.DayScheduleDTO;
import com.kpi.ejournal.dto.schedule.ScheduleItemResponse;
import com.kpi.ejournal.dto.schedule.WeeklyScheduleResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class ScheduleService {

    public WeeklyScheduleResponse getFullSchedule(Long groupId) {
        if (groupId != null && groupId == 2L) {
            return new WeeklyScheduleResponse(generateMockDaysForIS(1),
                    generateMockDaysForIS(2));
        }
        return new WeeklyScheduleResponse(generateMockDays(1),
                generateMockDays(2));
    }

    public List<ScheduleItemResponse> getSessionSchedule(Long groupId) {
        if (groupId != null && groupId == 2L) {
            return List.of(
                    createSessionItem(3001, "Комп'ютерні мережі (Екзамен)", "Харченко В.С.", "2026-06-08", "09:00", "501-18"),
                    createSessionItem(3002, "Бази даних (Залік)", "Ковальчук А.П.", "2026-06-12", "12:00", "Лаб. 4"),
                    createSessionItem(3003, "Системне ПЗ", "Мельник А.О.", "2026-06-18", "10:00", "202-18")
            );
        }
        return List.of(
                createSessionItem(1001, "ООП (Екзамен)", "Іванов О.М.", "2026-06-15", "09:00", "405-19"),
                createSessionItem(1002, "Вища математика", "Петров І.І.", "2026-06-19", "11:00", "101-18"),
                createSessionItem(1003, "Фізика (Залік)", "Сидоров О.К.", "2026-06-23", "10:00", "302-18")
        );
    }

    // --- РОЗКЛАД ДЛЯ ІА-33 ---
    private List<DayScheduleDTO> generateMockDays(int weekNum) {
        if (weekNum == 1) {
            return List.of(
                    new DayScheduleDTO(1, "Понеділок", List.of(
                            createLesson(1, "Вища математика (лек)", "Петров І.І.", "08:30", "101-18"),
                            createLesson(2, "Фізика (лек)", "Сидоров О.К.", "10:25", "302-18"),
                            createLesson(3, "ООП (практ)", "Іванов О.М.", "12:20", "405-19")
                    )),
                    new DayScheduleDTO(2, "Вівторок", List.of(
                            createLesson(4, "Іноземна мова", "English Center", "10:25", "Online"),
                            createLesson(5, "Дискретна математика", "Бондар В.В.", "12:20", "222-7")
                    )),
                    new DayScheduleDTO(3, "Середа", List.of(
                            createLesson(6, "Алгоритми та структури даних", "Коваль А.П.", "08:30", "305-18"),
                            createLesson(7, "Філософія", "Бондар В.В.", "10:25", "222-7")
                    )),
                    new DayScheduleDTO(5, "П'ятниця", List.of(
                            createLesson(8, "Фізичне виховання", "Спорткомплекс", "08:30", "Зал 1")
                    ))
            );
        } else {
            return List.of(
                    new DayScheduleDTO(1, "Понеділок", List.of(
                            createLesson(9, "Чисельні методи", "Сидоров О.К.", "10:25", "302-18"),
                            createLesson(10, "ООП (лекція)", "Іванов О.М.", "12:20", "405-19")
                    )),
                    new DayScheduleDTO(4, "Четвер", List.of(
                            createLesson(11, "Теорія ймовірностей", "Петров І.І.", "08:30", "101-18"),
                            createLesson(12, "Архітектура комп'ютера", "Мельник А.О.", "10:25", "202-18")
                    ))
            );
        }
    }

    private List<DayScheduleDTO> generateMockDaysForIS(int weekNum) {
        if (weekNum == 1) {
            return List.of(
                    new DayScheduleDTO(1, "Понеділок", List.of(
                            createLesson(201, "Комп'ютерні мережі", "Харченко В.С.", "10:25", "501-18"),
                            createLesson(202, "Веб-технології (практ)", "Данилов І.М.", "12:20", "Online")
                    )),
                    new DayScheduleDTO(3, "Середа", List.of(
                            createLesson(203, "Бази даних (лек)", "Ковальчук А.П.", "08:30", "401-18"),
                            createLesson(204, "Системне програмування", "Мельник А.О.", "10:25", "202-18"),
                            createLesson(205, "Економіка", "Зайцева Т.В.", "12:20", "105-1")
                    ))
            );
        } else {
            return List.of(
                    new DayScheduleDTO(2, "Вівторок", List.of(
                            createLesson(206, "Операційні системи", "Ковальчук А.П.", "10:25", "Лаб. 4"),
                            createLesson(207, "М'які обчислення", "Харченко В.С.", "12:20", "501-18")
                    )),
                    new DayScheduleDTO(5, "П'ятниця", List.of(
                            createLesson(208, "Бази даних (лаб)", "Ковальчук А.П.", "14:15", "Лаб. 4")
                    ))
            );
        }
    }

    private ScheduleItemResponse createLesson(long id, String disc, String teacher, String time, String room) {
        return new ScheduleItemResponse(
                id, "CLASS", null, LocalTime.parse(time), room,
                1L, "GROUP", id, disc, id, teacher
        );
    }

    private ScheduleItemResponse createSessionItem(long id, String disc, String teacher, String date, String time, String room) {
        return new ScheduleItemResponse(
                id, "SESSION", LocalDate.parse(date), LocalTime.parse(time), room,
                1L, "GROUP", id, disc, id, teacher
        );
    }
}