package com.kpi.ejournal.service;

import com.kpi.ejournal.dto.report.GenerateReportRequest;
import com.kpi.ejournal.dto.report.ReportChartItemResponse;
import com.kpi.ejournal.dto.report.ReportDetailsResponse;
import com.kpi.ejournal.dto.report.ReportResponse;
import com.kpi.ejournal.dto.report.ReportStudentRowResponse;
import com.kpi.ejournal.entity.academic.ControlType;
import com.kpi.ejournal.entity.academic.Discipline;
import com.kpi.ejournal.entity.academic.Grade;
import com.kpi.ejournal.entity.academic.GroupEntity;
import com.kpi.ejournal.entity.user.Methodologist;
import com.kpi.ejournal.entity.report.Report;
import com.kpi.ejournal.entity.user.Student;
import com.kpi.ejournal.entity.user.Teacher;
import com.kpi.ejournal.entity.user.User;
import com.kpi.ejournal.exception.NotFoundException;
import com.kpi.ejournal.repository.academic.DisciplineRepository;
import com.kpi.ejournal.repository.academic.GradeRepository;
import com.kpi.ejournal.repository.academic.GroupRepository;
import com.kpi.ejournal.repository.report.ReportRepository;
import com.kpi.ejournal.repository.user.TeacherRepository;
import com.kpi.ejournal.repository.user.UserRepository;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final ReportRepository reportRepository;
    private final GradeRepository gradeRepository;
    private final GroupRepository groupRepository;
    private final DisciplineRepository disciplineRepository;
    private final TeacherRepository teacherRepository;
    private final UserRepository userRepository;
    private final MapperService mapperService;

    public ReportService(
            ReportRepository reportRepository,
            GradeRepository gradeRepository,
            GroupRepository groupRepository,
            DisciplineRepository disciplineRepository,
            TeacherRepository teacherRepository,
            UserRepository userRepository,
            MapperService mapperService
    ) {
        this.reportRepository = reportRepository;
        this.gradeRepository = gradeRepository;
        this.groupRepository = groupRepository;
        this.disciplineRepository = disciplineRepository;
        this.teacherRepository = teacherRepository;
        this.userRepository = userRepository;
        this.mapperService = mapperService;
    }

    public ReportResponse generate(GenerateReportRequest request) {
        GroupEntity group = getGroup(request.groupId());
        Discipline discipline = getDiscipline(request.disciplineId());
        Teacher teacher = request.teacherId() != null ? getTeacher(request.teacherId()) : null;
        Methodologist methodologist = getMethodologistOrNull(request.methodologistId());

        ReportPreview preview = buildPreview(group, discipline, teacher);

        Report report = new Report();
        report.setCreatedAt(LocalDateTime.now());
        report.setPeriod(request.period());
        report.setAverageScore(preview.averageScore());
        report.setGroup(group);
        report.setDiscipline(discipline);
        report.setTeacher(teacher);
        report.setMethodologist(methodologist);

        return mapperService.toReportResponse(reportRepository.save(report));
    }

    public List<ReportResponse> getAll() {
        return reportRepository.findAll().stream()
                .sorted(Comparator.comparing(Report::getCreatedAt).reversed())
                .map(mapperService::toReportResponse)
                .toList();
    }

    public ReportDetailsResponse getDetails(Long reportId) {
        Report report = getReport(reportId);
        ReportPreview preview = buildPreview(report.getGroup(), report.getDiscipline(), report.getTeacher());
        return toDetailsResponse(report, preview);
    }

    public byte[] exportPdf(Long reportId) {
        Report report = getReport(reportId);
        ReportPreview preview = buildPreview(report.getGroup(), report.getDiscipline(), report.getTeacher());

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = pdfFont(18, Font.BOLD);
            Font subtitleFont = pdfFont(12, Font.BOLD);
            Font normalFont = pdfFont(10, Font.NORMAL);
            Font smallFont = pdfFont(9, Font.NORMAL);

            Paragraph title = new Paragraph("Звіт успішності студентів", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(12);
            document.add(title);

            PdfPTable meta = new PdfPTable(new float[]{1.2f, 2.8f});
            meta.setWidthPercentage(100);
            addMetaRow(meta, "Період", report.getPeriod(), normalFont);
            addMetaRow(meta, "Група", safe(report.getGroup().getCode()), normalFont);
            addMetaRow(meta, "Дисципліна", report.getDiscipline() != null ? safe(report.getDiscipline().getName()) : "Усі дисципліни", normalFont);
            addMetaRow(meta, "Тип контролю", "Семестровий контроль", normalFont);
            addMetaRow(meta, "Викладач", report.getTeacher() != null ? report.getTeacher().getFullName() : "Усі / не зазначено", normalFont);
            addMetaRow(meta, "Дата формування", formatDateTime(report.getCreatedAt()), normalFont);
            document.add(meta);

            Paragraph stats = new Paragraph(
                    "\nСередній бал серед студентів із семестровою оцінкою: " + formatScore(preview.averageScore())
                            + "\nСтудентів у групі: " + preview.rows().size()
                            + " | з оцінкою: " + preview.studentsWithScore()
                            + " | без семестрової оцінки: " + preview.studentsWithoutScore(),
                    subtitleFont
            );
            stats.setSpacingAfter(10);
            document.add(stats);

            PdfPTable table = new PdfPTable(new float[]{0.5f, 3.2f, 1.2f, 2.4f, 1.1f});
            table.setWidthPercentage(100);
            addHeaderCell(table, "№", subtitleFont);
            addHeaderCell(table, "Студент", subtitleFont);
            addHeaderCell(table, "Група", subtitleFont);
            addHeaderCell(table, "Дисципліна", subtitleFont);
            addHeaderCell(table, "Бал", subtitleFont);

            int i = 1;
            for (ReportStudentRowResponse row : preview.rows()) {
                addBodyCell(table, String.valueOf(i++), normalFont, Element.ALIGN_CENTER);
                addBodyCell(table, row.studentName(), normalFont, Element.ALIGN_LEFT);
                addBodyCell(table, row.groupCode(), normalFont, Element.ALIGN_CENTER);
                addBodyCell(table, row.disciplineName(), normalFont, Element.ALIGN_LEFT);
                addBodyCell(table, row.semesterScore() == null ? "—" : formatScore(row.semesterScore()), normalFont, Element.ALIGN_CENTER);
            }
            document.add(table);

            Paragraph chartTitle = new Paragraph("\nРозподіл семестрових результатів", subtitleFont);
            chartTitle.setSpacingBefore(10);
            chartTitle.setSpacingAfter(6);
            document.add(chartTitle);

            PdfPTable chart = new PdfPTable(new float[]{1.8f, 1.0f, 4.0f});
            chart.setWidthPercentage(100);
            addHeaderCell(chart, "Діапазон", subtitleFont);
            addHeaderCell(chart, "К-сть", subtitleFont);
            addHeaderCell(chart, "Діаграма", subtitleFont);
            for (ReportChartItemResponse item : preview.distribution()) {
                addBodyCell(chart, item.label(), smallFont, Element.ALIGN_LEFT);
                addBodyCell(chart, String.valueOf(item.count()), smallFont, Element.ALIGN_CENTER);
                addBodyCell(chart, bar(item.percent()) + " " + String.format(java.util.Locale.US, "%.1f%%", item.percent()), smallFont, Element.ALIGN_LEFT);
            }
            document.add(chart);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Не вдалося сформувати PDF", e);
        }
    }

    public byte[] exportExcel(Long reportId) {
        Report report = getReport(reportId);
        ReportPreview preview = buildPreview(report.getGroup(), report.getDiscipline(), report.getTeacher());

        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            var sheet = workbook.createSheet("Звіт");
            var chartSheet = workbook.createSheet("Діаграма");

            CellStyle title = titleStyle(workbook);
            CellStyle header = headerStyle(workbook);
            CellStyle body = bodyStyle(workbook);
            CellStyle number = numberStyle(workbook);

            Row titleRow = sheet.createRow(0);
            titleRow.createCell(0).setCellValue("Звіт успішності студентів");
            titleRow.getCell(0).setCellStyle(title);

            writePair(sheet, 2, "Період", report.getPeriod(), body);
            writePair(sheet, 3, "Група", safe(report.getGroup().getCode()), body);
            writePair(sheet, 4, "Дисципліна", report.getDiscipline() != null ? safe(report.getDiscipline().getName()) : "Усі дисципліни", body);
            writePair(sheet, 5, "Тип контролю", "Семестровий контроль", body);
            writePair(sheet, 6, "Викладач", report.getTeacher() != null ? report.getTeacher().getFullName() : "Усі / не зазначено", body);
            writePair(sheet, 7, "Дата формування", formatDateTime(report.getCreatedAt()), body);
            writePair(sheet, 8, "Середній бал", formatScore(preview.averageScore()), body);
            writePair(sheet, 9, "Студентів / з оцінкою / без оцінки", preview.rows().size() + " / " + preview.studentsWithScore() + " / " + preview.studentsWithoutScore(), body);

            Row headerRow = sheet.createRow(11);
            String[] headers = {"№", "Студент", "Група", "Дисципліна", "Семестровий бал"};
            for (int c = 0; c < headers.length; c++) {
                headerRow.createCell(c).setCellValue(headers[c]);
                headerRow.getCell(c).setCellStyle(header);
            }

            int rowIndex = 12;
            int n = 1;
            for (ReportStudentRowResponse item : preview.rows()) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(n++);
                row.createCell(1).setCellValue(item.studentName());
                row.createCell(2).setCellValue(item.groupCode());
                row.createCell(3).setCellValue(item.disciplineName());
                if (item.semesterScore() == null) {
                    row.createCell(4).setCellValue("—");
                    row.getCell(4).setCellStyle(body);
                } else {
                    row.createCell(4).setCellValue(item.semesterScore());
                    row.getCell(4).setCellStyle(number);
                }
                for (int c = 0; c < 4; c++) row.getCell(c).setCellStyle(body);
            }

            Row distHeader = chartSheet.createRow(0);
            String[] distHeaders = {"Діапазон", "Кількість", "Відсоток", "Візуалізація"};
            for (int c = 0; c < distHeaders.length; c++) {
                distHeader.createCell(c).setCellValue(distHeaders[c]);
                distHeader.getCell(c).setCellStyle(header);
            }
            int distRowIndex = 1;
            for (ReportChartItemResponse item : preview.distribution()) {
                Row row = chartSheet.createRow(distRowIndex++);
                row.createCell(0).setCellValue(item.label());
                row.createCell(1).setCellValue(item.count());
                row.createCell(2).setCellValue(item.percent() / 100.0);
                row.createCell(3).setCellValue(bar(item.percent()));
                row.getCell(2).setCellStyle(percentStyle(workbook));
                row.getCell(0).setCellStyle(body);
                row.getCell(1).setCellStyle(body);
                row.getCell(3).setCellStyle(body);
            }

            for (int c = 0; c <= 4; c++) sheet.autoSizeColumn(c);
            for (int c = 0; c <= 3; c++) chartSheet.autoSizeColumn(c);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Не вдалося сформувати Excel", e);
        }
    }

    private ReportPreview buildPreview(GroupEntity group, Discipline discipline, Teacher teacher) {
        List<Student> students = group.getStudents().stream()
                .sorted(Comparator.comparing(Student::getFullName))
                .toList();

        List<Grade> grades;
        if (discipline == null && teacher == null) {
            grades = gradeRepository.findByStudentGroupIdAndControlType(group.getId(), ControlType.SEMESTER);
        } else if (discipline == null) {
            grades = gradeRepository.findByStudentGroupIdAndTeacherIdAndControlType(group.getId(), teacher.getId(), ControlType.SEMESTER);
        } else if (teacher == null) {
            grades = gradeRepository.findByStudentGroupIdAndDisciplineIdAndControlType(group.getId(), discipline.getId(), ControlType.SEMESTER);
        } else {
            grades = gradeRepository.findByStudentGroupIdAndDisciplineIdAndTeacherIdAndControlType(group.getId(), discipline.getId(), teacher.getId(), ControlType.SEMESTER);
        }

        Map<Long, Double> scoreByStudent = grades.stream()
                .filter(g -> g.getStudent() != null)
                .collect(Collectors.groupingBy(
                        g -> g.getStudent().getId(),
                        Collectors.averagingDouble(Grade::getValue)
                ));

        List<ReportStudentRowResponse> rows = new ArrayList<>();
        for (Student student : students) {
            rows.add(new ReportStudentRowResponse(
                    student.getId(),
                    student.getFullName(),
                    group.getCode(),
                    discipline != null ? discipline.getName() : "Усі дисципліни",
                    scoreByStudent.get(student.getId())
            ));
        }

        double average = rows.stream()
                .map(ReportStudentRowResponse::semesterScore)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        int withScore = (int) rows.stream().filter(r -> r.semesterScore() != null).count();
        int withoutScore = rows.size() - withScore;

        return new ReportPreview(rows, average, withScore, withoutScore, buildDistribution(rows));
    }

    private List<ReportChartItemResponse> buildDistribution(List<ReportStudentRowResponse> rows) {
        Map<String, Long> counts = new LinkedHashMap<>();
        counts.put("90–100", 0L);
        counts.put("75–89", 0L);
        counts.put("60–74", 0L);
        counts.put("1–59", 0L);
        counts.put("Немає оцінки", 0L);

        for (ReportStudentRowResponse row : rows) {
            Double score = row.semesterScore();
            String key;
            if (score == null) key = "Немає оцінки";
            else if (score >= 90) key = "90–100";
            else if (score >= 75) key = "75–89";
            else if (score >= 60) key = "60–74";
            else key = "1–59";
            counts.put(key, counts.get(key) + 1);
        }

        int total = Math.max(rows.size(), 1);
        return counts.entrySet().stream()
                .map(e -> new ReportChartItemResponse(e.getKey(), e.getValue(), e.getValue() * 100.0 / total))
                .toList();
    }

    private ReportDetailsResponse toDetailsResponse(Report report, ReportPreview preview) {
        return new ReportDetailsResponse(
                report.getId(),
                report.getCreatedAt(),
                report.getPeriod(),
                report.getGroup().getId(),
                report.getGroup().getCode(),
                report.getDiscipline() != null ? report.getDiscipline().getId() : null,
                report.getDiscipline() != null ? report.getDiscipline().getName() : "Усі дисципліни",
                report.getTeacher() != null ? report.getTeacher().getId() : null,
                report.getTeacher() != null ? report.getTeacher().getFullName() : null,
                report.getMethodologist() != null ? report.getMethodologist().getId() : null,
                report.getMethodologist() != null ? report.getMethodologist().getFullName() : null,
                preview.averageScore(),
                preview.rows().size(),
                preview.studentsWithScore(),
                preview.studentsWithoutScore(),
                preview.rows(),
                preview.distribution()
        );
    }

    private GroupEntity getGroup(Long id) {
        return groupRepository.findById(id).orElseThrow(() -> new NotFoundException("Групу не знайдено"));
    }

    private Discipline getDiscipline(Long id) {
        return disciplineRepository.findById(id).orElseThrow(() -> new NotFoundException("Дисципліну не знайдено"));
    }

    private Teacher getTeacher(Long id) {
        return teacherRepository.findById(id).orElseThrow(() -> new NotFoundException("Викладача не знайдено"));
    }

    private Report getReport(Long id) {
        return reportRepository.findById(id).orElseThrow(() -> new NotFoundException("Звіт не знайдено"));
    }

    private Methodologist getMethodologistOrNull(Long id) {
        if (id == null) return null;
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("Методиста не знайдено"));
        if (user instanceof Methodologist methodologist) return methodologist;
        throw new NotFoundException("Користувач не є методистом");
    }

    private Font pdfFont(int size, int style) {
        try {
            BaseFont baseFont = BaseFont.createFont("C:/Windows/Fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            return new Font(baseFont, size, style);
        } catch (Exception ignored) {
            return new Font(Font.HELVETICA, size, style);
        }
    }

    private void addMetaRow(PdfPTable table, String label, String value, Font font) {
        addBodyCell(table, label, font, Element.ALIGN_LEFT);
        addBodyCell(table, value, font, Element.ALIGN_LEFT);
    }

    private void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(230, 230, 230));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text, Font font, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        table.addCell(cell);
    }

    private String bar(double percent) {
        int count = (int) Math.round(percent / 5.0);
        return "█".repeat(Math.max(0, count));
    }

    private String safe(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private String formatScore(Double value) {
        return value == null ? "—" : String.format(java.util.Locale.US, "%.2f", value);
    }

    private String formatDateTime(LocalDateTime value) {
        if (value == null) return "—";
        return value.format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
    }

    private void writePair(org.apache.poi.ss.usermodel.Sheet sheet, int rowIndex, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowIndex);
        row.createCell(0).setCellValue(label);
        row.createCell(1).setCellValue(value);
        row.getCell(0).setCellStyle(style);
        row.getCell(1).setCellStyle(style);
    }

    private CellStyle titleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        var font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        return style;
    }

    private CellStyle headerStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        var font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        addBorders(style);
        return style;
    }

    private CellStyle bodyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        addBorders(style);
        return style;
    }

    private CellStyle numberStyle(Workbook workbook) {
        CellStyle style = bodyStyle(workbook);
        style.setDataFormat(workbook.createDataFormat().getFormat("0.00"));
        return style;
    }

    private CellStyle percentStyle(Workbook workbook) {
        CellStyle style = bodyStyle(workbook);
        style.setDataFormat(workbook.createDataFormat().getFormat("0.0%"));
        return style;
    }

    private void addBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
    }

    private record ReportPreview(
            List<ReportStudentRowResponse> rows,
            double averageScore,
            int studentsWithScore,
            int studentsWithoutScore,
            List<ReportChartItemResponse> distribution
    ) {}
}
