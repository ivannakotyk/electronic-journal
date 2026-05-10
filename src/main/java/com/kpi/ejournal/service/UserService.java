package com.kpi.ejournal.service;

import com.kpi.ejournal.dto.user.CreateUserRequest;
import com.kpi.ejournal.dto.user.UpdateUserRequest;
import com.kpi.ejournal.dto.user.UserResponse;
import com.kpi.ejournal.entity.academic.GroupEntity;
import com.kpi.ejournal.entity.user.*;
import com.kpi.ejournal.exception.*;
import com.kpi.ejournal.repository.academic.GroupRepository;
import com.kpi.ejournal.repository.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final AuthService authService;
    private final MapperService mapperService;
    private final JdbcTemplate jdbcTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    public UserService(UserRepository userRepository,
                       GroupRepository groupRepository,
                       AuthService authService,
                       MapperService mapperService,
                       JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.authService = authService;
        this.mapperService = mapperService;
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<UserResponse> getUsers() {
        return userRepository.findAll()
                .stream()
                .map(mapperService::toUserResponse)
                .toList();
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByLogin(request.login())) {
            throw new BadRequestException("Користувач з таким логіном уже існує");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Користувач з таким email уже існує");
        }

        UserRole role = UserRole.valueOf(request.role().toUpperCase());

        User user = switch (role) {
            case STUDENT -> buildStudent(request);
            case TEACHER -> buildTeacher(request);
            case ADMINISTRATOR -> new Administrator();
            case METHODOLOGIST -> new Methodologist();
        };

        user.setFullName(request.fullName());
        user.setLogin(request.login());
        user.setEmail(request.email());
        user.setPasswordHash(authService.hash(request.password()));
        user.setRole(role);

        return mapperService.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Користувача не знайдено"));

        user.setFullName(request.fullName());
        user.setEmail(request.email());

        if (user instanceof Teacher teacher) {
            teacher.setPosition(request.position());
        }

        if (user instanceof Student student) {
            student.setStudentCardNumber(request.studentCardNumber());

            if (request.groupId() != null) {
                GroupEntity group = groupRepository.findById(request.groupId())
                        .orElseThrow(() -> new NotFoundException("Групу не знайдено"));
                student.setGroup(group);
            }
        }

        return mapperService.toUserResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Користувача не знайдено"));

        if (user.getRole() == UserRole.ADMINISTRATOR) {
            throw new BadRequestException("Адміністратора видаляти не можна");
        }

        entityManager.flush();
        entityManager.clear();

        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");

        try {
            jdbcTemplate.update("DELETE FROM GRADES WHERE STUDENT_ID = ? OR TEACHER_ID = ?", id, id);
            jdbcTemplate.update("DELETE FROM TEACHING_ASSIGNMENTS WHERE TEACHER_ID = ?", id);
            jdbcTemplate.update("DELETE FROM SCHEDULE_ITEMS WHERE TEACHER_ID = ?", id);
            jdbcTemplate.update("DELETE FROM REPORTS WHERE TEACHER_ID = ? OR METHODOLOGIST_ID = ?", id, id);

            jdbcTemplate.update("DELETE FROM STUDENTS WHERE ID = ?", id);
            jdbcTemplate.update("DELETE FROM TEACHERS WHERE ID = ?", id);
            jdbcTemplate.update("DELETE FROM METHODOLOGISTS WHERE ID = ?", id);

            jdbcTemplate.update("DELETE FROM USERS WHERE ID = ?", id);
        } finally {
            jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");
        }

        entityManager.clear();
    }

    private Student buildStudent(CreateUserRequest request) {
        Student student = new Student();
        student.setStudentCardNumber(request.studentCardNumber());

        if (request.groupId() != null) {
            GroupEntity group = groupRepository.findById(request.groupId())
                    .orElseThrow(() -> new NotFoundException("Групу не знайдено"));
            student.setGroup(group);
        }

        return student;
    }

    private Teacher buildTeacher(CreateUserRequest request) {
        Teacher teacher = new Teacher();
        teacher.setPosition(request.position());
        return teacher;
    }
}