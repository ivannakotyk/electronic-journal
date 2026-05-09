package com.kpi.ejournal.config;

import com.kpi.ejournal.entity.*;
import com.kpi.ejournal.repository.*;
import com.kpi.ejournal.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalTime;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seed(UserRepository userRepository,
                           GroupRepository groupRepository,
                           DisciplineRepository disciplineRepository,
                           TeacherRepository teacherRepository,
                           StudentRepository studentRepository,
                           ScheduleRepository scheduleRepository,
                           GradeRepository gradeRepository,
                           AuthService authService) {
        return args -> {
            if (userRepository.count() > 0) {
                return;
            }


            GroupEntity group = new GroupEntity();
            group.setCode("IА-33");
            group.setCourse(3);
            group.setSpecialty("Інформаційні системи та технології");
            groupRepository.save(group);


            Discipline discipline = new Discipline();
            discipline.setName("Теорія систем та системний аналіз");
            discipline.setSemester(6);
            disciplineRepository.save(discipline);


            Administrator admin = new Administrator();
            admin.setFullName("Головний адміністратор");
            admin.setLogin("admin");
            admin.setEmail("admin@kpi.ua");
            admin.setPasswordHash(authService.hash("admin123"));
            admin.setRole(UserRole.ADMINISTRATOR);
            userRepository.save(admin);


            Teacher teacher = new Teacher();
            teacher.setFullName("Трофименко Олег Сергійович");
            teacher.setLogin("teacher1");
            teacher.setEmail("teacher1@kpi.ua");
            teacher.setPasswordHash(authService.hash("teacher123"));
            teacher.setRole(UserRole.TEACHER);
            teacher.setPosition("Доцент");
            teacherRepository.save(teacher);


            Student student = new Student();
            student.setFullName("Котик Іванна Володимирівна");
            student.setLogin("student1");
            student.setEmail("student1@kpi.ua");
            student.setPasswordHash(authService.hash("student123"));
            student.setRole(UserRole.STUDENT);
            student.setStudentCardNumber("IA33001");
            student.setGroup(group);
            studentRepository.save(student);



            Grade grade = new Grade();
            grade.setValue(95.0);
            grade.setGradeDate(LocalDate.now());
            grade.setControlType(ControlType.CURRENT);
            grade.setComment("Відмінна робота");
            grade.setStudent(student);
            grade.setTeacher(teacher);
            grade.setDiscipline(discipline);
            gradeRepository.save(grade);

            System.out.println(">>> Тестові дані (користувачі та групи) успішно додані.");
        };
    }
}