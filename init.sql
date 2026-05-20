-- =====================================================
-- 1. ТАБЛИЦЫ
-- =====================================================

-- Пользователи
CREATE TABLE IF NOT EXISTS users (
                                     id SERIAL PRIMARY KEY,
                                     login VARCHAR(50) UNIQUE,
    password VARCHAR(100),
    role VARCHAR(20)
    );

-- Группы
CREATE TABLE IF NOT EXISTS groups (
                                      id SERIAL PRIMARY KEY,
                                      name VARCHAR(50) UNIQUE
    );

-- Студенты
CREATE TABLE IF NOT EXISTS students (
                                        id SERIAL PRIMARY KEY,
                                        user_id INTEGER UNIQUE,
                                        full_name VARCHAR(150),
    group_id INTEGER,
    status VARCHAR(20)
    );

-- Абитуриенты
CREATE TABLE IF NOT EXISTS applicants (
                                          id SERIAL PRIMARY KEY,
                                          user_id INTEGER UNIQUE,
                                          full_name VARCHAR(150)
    );

-- Заявления
CREATE TABLE IF NOT EXISTS applications (
                                            id SERIAL PRIMARY KEY,
                                            applicant_id INTEGER,
                                            program_id INTEGER,
                                            status VARCHAR(20)
    );

-- Секретные вопросы
CREATE TABLE IF NOT EXISTS security_questions (
                                                  id SERIAL PRIMARY KEY,
                                                  user_id INTEGER NOT NULL,
                                                  question TEXT NOT NULL,
                                                  answer_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Приказы
CREATE TABLE IF NOT EXISTS orders (
                                      id SERIAL PRIMARY KEY,
                                      type VARCHAR(50),
    status VARCHAR(50),
    created_by INTEGER
    );

-- Элементы приказов
CREATE TABLE IF NOT EXISTS order_items (
                                           id SERIAL PRIMARY KEY,
                                           order_id INTEGER,
                                           student_id INTEGER,
                                           action VARCHAR(50),
    from_group_id INTEGER,
    to_group_id INTEGER
    );

-- Преподаватели
CREATE TABLE IF NOT EXISTS teachers (
                                        id SERIAL PRIMARY KEY,
                                        user_id INTEGER,
                                        full_name VARCHAR(150),
    position VARCHAR(80),
    department_id INTEGER,
    head_id INTEGER,
    phone VARCHAR(20),
    academic_degree VARCHAR(50)
    );

-- Специальности
CREATE TABLE IF NOT EXISTS specializations (
                                               id SERIAL PRIMARY KEY,
                                               code VARCHAR(20) UNIQUE,
    name VARCHAR(200) NOT NULL
    );

-- Уровни образования
CREATE TABLE IF NOT EXISTS degree_levels (
                                             id SERIAL PRIMARY KEY,
                                             name VARCHAR(50) UNIQUE
    );

-- Дисциплины
CREATE TABLE IF NOT EXISTS courses (
                                       id SERIAL PRIMARY KEY,
                                       name VARCHAR(150) NOT NULL,
    hours INT
    );

-- Связь преподаватель-дисциплина
CREATE TABLE IF NOT EXISTS teacher_course (
                                              teacher_id INT NOT NULL,
                                              course_id INT NOT NULL,
                                              PRIMARY KEY (teacher_id, course_id)
    );

-- Учебный план (тернарная связь)
CREATE TABLE IF NOT EXISTS curriculum (
                                          id SERIAL PRIMARY KEY,
                                          group_id INT NOT NULL,
                                          course_id INT NOT NULL,
                                          teacher_id INT NOT NULL,
                                          semester INT
);

-- История переводов студентов
CREATE TABLE IF NOT EXISTS student_group_history (
                                                     id SERIAL PRIMARY KEY,
                                                     student_id INT NOT NULL,
                                                     from_group_id INT,
                                                     to_group_id INT NOT NULL,
                                                     changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                                     order_id INT
);

-- Академические отпуска
CREATE TABLE IF NOT EXISTS academic_leave (
                                              id SERIAL PRIMARY KEY,
                                              student_id INT NOT NULL,
                                              start_date DATE NOT NULL,
                                              end_date DATE,
                                              order_id INT
);

-- Журнал действий
CREATE TABLE IF NOT EXISTS journal (
                                       id SERIAL PRIMARY KEY,
                                       user_id INT NOT NULL,
                                       action VARCHAR(100),
    entity_type VARCHAR(50),
    entity_id INT,
    old_value TEXT,
    new_value TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Контакты студентов
CREATE TABLE IF NOT EXISTS student_contacts (
                                  student_id INTEGER PRIMARY KEY,
                                  email VARCHAR(100),
                                  phone VARCHAR(20)
);

-- Таблица оценок
CREATE TABLE IF NOT EXISTS grades (
                                      id SERIAL PRIMARY KEY,
                                      student_id INTEGER NOT NULL,
                                      course_id INTEGER NOT NULL,
                                      teacher_id INTEGER NOT NULL,
                                      grade_value INTEGER CHECK (grade_value >= 2 AND grade_value <= 5),
    grade_type VARCHAR(50), -- EXAM, TEST, COURSE_WORK, EXAM_SESSION
    semester INTEGER,
    academic_year VARCHAR(20),
    comment TEXT,
    graded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_grades_student FOREIGN KEY (student_id) REFERENCES students(id),
    CONSTRAINT fk_grades_course FOREIGN KEY (course_id) REFERENCES courses(id),
    CONSTRAINT fk_grades_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id)
    );

-- Индексы для быстрого поиска
CREATE INDEX idx_grades_student ON grades(student_id);
CREATE INDEX idx_grades_course ON grades(course_id);
CREATE INDEX idx_grades_teacher ON grades(teacher_id);
CREATE INDEX idx_grades_semester ON grades(semester);


-- Добавить внешний ключ
ALTER TABLE student_contacts
    ADD CONSTRAINT fk_student_contacts_student
        FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE;

-- =====================================================
-- 2. ВНЕШНИЕ КЛЮЧИ
-- =====================================================

ALTER TABLE students ADD CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users(id);
ALTER TABLE students ADD CONSTRAINT fk_students_group FOREIGN KEY (group_id) REFERENCES groups(id);

ALTER TABLE applicants ADD CONSTRAINT fk_applicants_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE applications ADD CONSTRAINT fk_applications_applicant FOREIGN KEY (applicant_id) REFERENCES applicants(id);

ALTER TABLE security_questions ADD CONSTRAINT fk_security_questions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;

ALTER TABLE orders ADD CONSTRAINT fk_orders_created_by FOREIGN KEY (created_by) REFERENCES users(id);

ALTER TABLE order_items ADD CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id);
ALTER TABLE order_items ADD CONSTRAINT fk_order_items_student FOREIGN KEY (student_id) REFERENCES students(id);
ALTER TABLE order_items ADD CONSTRAINT fk_order_items_from_group FOREIGN KEY (from_group_id) REFERENCES groups(id);
ALTER TABLE order_items ADD CONSTRAINT fk_order_items_to_group FOREIGN KEY (to_group_id) REFERENCES groups(id);

ALTER TABLE groups ADD COLUMN IF NOT EXISTS specialization_id INT REFERENCES specializations(id);
ALTER TABLE groups ADD COLUMN IF NOT EXISTS degree_level_id INT REFERENCES degree_levels(id);
ALTER TABLE groups ADD COLUMN IF NOT EXISTS start_year INT;
ALTER TABLE groups ADD COLUMN IF NOT EXISTS end_year INT;
ALTER TABLE groups ADD COLUMN IF NOT EXISTS curator_id INT REFERENCES teachers(id);

ALTER TABLE teacher_course ADD CONSTRAINT fk_teacher_course_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id);
ALTER TABLE teacher_course ADD CONSTRAINT fk_teacher_course_course FOREIGN KEY (course_id) REFERENCES courses(id);

ALTER TABLE curriculum ADD CONSTRAINT fk_curriculum_group FOREIGN KEY (group_id) REFERENCES groups(id);
ALTER TABLE curriculum ADD CONSTRAINT fk_curriculum_course FOREIGN KEY (course_id) REFERENCES courses(id);
ALTER TABLE curriculum ADD CONSTRAINT fk_curriculum_teacher FOREIGN KEY (teacher_id) REFERENCES teachers(id);

ALTER TABLE student_group_history ADD CONSTRAINT fk_history_student FOREIGN KEY (student_id) REFERENCES students(id);
ALTER TABLE student_group_history ADD CONSTRAINT fk_history_from_group FOREIGN KEY (from_group_id) REFERENCES groups(id);
ALTER TABLE student_group_history ADD CONSTRAINT fk_history_to_group FOREIGN KEY (to_group_id) REFERENCES groups(id);
ALTER TABLE student_group_history ADD CONSTRAINT fk_history_order FOREIGN KEY (order_id) REFERENCES orders(id);

ALTER TABLE academic_leave ADD CONSTRAINT fk_academic_student FOREIGN KEY (student_id) REFERENCES students(id);
ALTER TABLE academic_leave ADD CONSTRAINT fk_academic_order FOREIGN KEY (order_id) REFERENCES orders(id);

ALTER TABLE journal ADD CONSTRAINT fk_journal_user FOREIGN KEY (user_id) REFERENCES users(id);

ALTER TABLE student_contacts ADD CONSTRAINT fk_contacts_student FOREIGN KEY (student_id) REFERENCES students(id);


-- =====================================================
-- 3. ТЕСТОВЫЕ ДАННЫЕ (расширенные)
-- =====================================================

-- Пользователи
INSERT INTO users (login, password, role) VALUES
                                              ('ivanov_app', 'pass1', 'APPLICANT'),
                                              ('petrov_stud', 'pass2', 'STUDENT'),
                                              ('sidorov_dean', 'pass3', 'DEAN'),
                                              ('smirnov_teacher', 'pass4', 'TEACHER'),
                                              ('admin_user', 'adminpass', 'ADMIN'),
                                              ('bychkov', 'pass123', 'TEACHER'),
                                              ('nosovitsky', 'pass123', 'DEAN'),
                                              ('lifantev', 'pass123', 'STUDENT'),
                                              ('kozlov_stud', 'pass123', 'STUDENT'),
                                              ('morozova_stud', 'pass123', 'STUDENT'),
                                              ('volkov_stud', 'pass123', 'STUDENT'),
                                              ('sokolova_stud', 'pass123', 'STUDENT'),
                                              ('popov_stud', 'pass123', 'STUDENT'),
                                              ('medvedeva_teacher', 'pass123', 'TEACHER'),
                                              ('kuznetsov_teacher', 'pass123', 'TEACHER')
    ON CONFLICT (login) DO NOTHING;

-- Группы
INSERT INTO groups (name) VALUES
                              ('ИНФО-101'),
                              ('ИНФО-102'),
                              ('БИЗ-201'),
                              ('ИНФО-103'),
                              ('ПМИ-101')
    ON CONFLICT (name) DO NOTHING;

-- Студенты (добавляем больше)
INSERT INTO students (user_id, full_name, group_id, status) VALUES
                                                                ((SELECT id FROM users WHERE login = 'petrov_stud'), 'Петров Пётр Петрович', 1, 'ACTIVE'),
                                                                ((SELECT id FROM users WHERE login = 'lifantev'), 'Лифантьев Дмитрий Андреевич', 1, 'ACTIVE'),
                                                                ((SELECT id FROM users WHERE login = 'kozlov_stud'), 'Козлов Алексей Сергеевич', 1, 'ACTIVE'),
                                                                ((SELECT id FROM users WHERE login = 'morozova_stud'), 'Морозова Анна Владимировна', 2, 'ACTIVE'),
                                                                ((SELECT id FROM users WHERE login = 'volkov_stud'), 'Волков Денис Николаевич', 2, 'ACTIVE'),
                                                                ((SELECT id FROM users WHERE login = 'sokolova_stud'), 'Соколова Екатерина Дмитриевна', 3, 'ACTIVE'),
                                                                ((SELECT id FROM users WHERE login = 'popov_stud'), 'Попов Андрей Игоревич', 4, 'ACTIVE')
    ON CONFLICT (user_id) DO NOTHING;

-- Контакты студентов
INSERT INTO student_contacts (student_id, email, phone)
SELECT
    s.id,
    CASE u.login
        WHEN 'petrov_stud' THEN 'petrov@university.ru'
        WHEN 'lifantev' THEN 'lifantev@university.ru'
        WHEN 'kozlov_stud' THEN 'kozlov@university.ru'
        WHEN 'morozova_stud' THEN 'morozova@university.ru'
        WHEN 'volkov_stud' THEN 'volkov@university.ru'
        WHEN 'sokolova_stud' THEN 'sokolova@university.ru'
        WHEN 'popov_stud' THEN 'popov@university.ru'
        END,
    CASE u.login
        WHEN 'petrov_stud' THEN '+79991234567'
        WHEN 'lifantev' THEN '+79991234568'
        WHEN 'kozlov_stud' THEN '+79991234569'
        WHEN 'morozova_stud' THEN '+79991234570'
        WHEN 'volkov_stud' THEN '+79991234571'
        WHEN 'sokolova_stud' THEN '+79991234572'
        WHEN 'popov_stud' THEN '+79991234573'
        END
FROM students s
         JOIN users u ON s.user_id = u.id
    ON CONFLICT (student_id) DO NOTHING;

-- Абитуриент
INSERT INTO applicants (user_id, full_name) VALUES
    ((SELECT id FROM users WHERE login = 'ivanov_app'), 'Иванов Иван Иванович')
    ON CONFLICT (user_id) DO NOTHING;

-- Заявления
INSERT INTO applications (applicant_id, program_id, status) VALUES
    ((SELECT id FROM applicants WHERE user_id = (SELECT id FROM users WHERE login = 'ivanov_app')), 101, 'PENDING');

-- Секретные вопросы
INSERT INTO security_questions (user_id, question, answer_hash) VALUES
                                                                    ((SELECT id FROM users WHERE login = 'petrov_stud'), 'Ваш любимый цвет?', 'red'),
                                                                    ((SELECT id FROM users WHERE login = 'sidorov_dean'), 'Ваша любимая книга?', 'java'),
                                                                    ((SELECT id FROM users WHERE login = 'smirnov_teacher'), 'Ваше любимое блюдо?', 'pasta'),
                                                                    ((SELECT id FROM users WHERE login = 'kozlov_stud'), 'Кличка собаки?', 'rex'),
                                                                    ((SELECT id FROM users WHERE login = 'morozova_stud'), 'Любимый город?', 'moscow')
    ON CONFLICT DO NOTHING;

-- Преподаватели
INSERT INTO teachers (user_id, full_name, position, academic_degree, phone) VALUES
                                                                                ((SELECT id FROM users WHERE login = 'smirnov_teacher'), 'Смирнова Анна Викторовна', 'Доцент', 'Кандидат наук', '+79161234560'),
                                                                                ((SELECT id FROM users WHERE login = 'bychkov'), 'Бычков Сергей Юрьевич', 'Профессор', 'Доктор технических наук', '+79161234567'),
                                                                                ((SELECT id FROM users WHERE login = 'medvedeva_teacher'), 'Медведева Ольга Павловна', 'Старший преподаватель', 'Кандидат наук', '+79161234568'),
                                                                                ((SELECT id FROM users WHERE login = 'kuznetsov_teacher'), 'Кузнецов Андрей Владимирович', 'Доцент', 'Кандидат наук', '+79161234569')
    ON CONFLICT DO NOTHING;

-- Специальности
INSERT INTO specializations (code, name) VALUES
                                             ('09.03.03', 'Прикладная информатика'),
                                             ('38.03.05', 'Бизнес-информатика'),
                                             ('01.03.02', 'Прикладная математика и информатика')
    ON CONFLICT (code) DO NOTHING;

-- Уровни образования
INSERT INTO degree_levels (name) VALUES
                                     ('Бакалавриат'),
                                     ('Магистратура')
    ON CONFLICT (name) DO NOTHING;

-- Обновляем группы
UPDATE groups SET
                  specialization_id = (SELECT id FROM specializations WHERE code = '09.03.03'),
                  degree_level_id = (SELECT id FROM degree_levels WHERE name = 'Бакалавриат'),
                  start_year = 2023,
                  end_year = 2027,
                  curator_id = (SELECT id FROM teachers WHERE user_id = (SELECT id FROM users WHERE login = 'smirnov_teacher'))
WHERE name = 'ИНФО-101';

UPDATE groups SET
                  specialization_id = (SELECT id FROM specializations WHERE code = '09.03.03'),
                  degree_level_id = (SELECT id FROM degree_levels WHERE name = 'Бакалавриат'),
                  start_year = 2023,
                  end_year = 2027,
                  curator_id = (SELECT id FROM teachers WHERE user_id = (SELECT id FROM users WHERE login = 'bychkov'))
WHERE name = 'ИНФО-102';

UPDATE groups SET
                  specialization_id = (SELECT id FROM specializations WHERE code = '38.03.05'),
                  degree_level_id = (SELECT id FROM degree_levels WHERE name = 'Бакалавриат'),
                  start_year = 2023,
                  end_year = 2027
WHERE name = 'БИЗ-201';

UPDATE groups SET
                  specialization_id = (SELECT id FROM specializations WHERE code = '09.03.03'),
                  degree_level_id = (SELECT id FROM degree_levels WHERE name = 'Бакалавриат'),
                  start_year = 2024,
                  end_year = 2028
WHERE name = 'ИНФО-103';

UPDATE groups SET
                  specialization_id = (SELECT id FROM specializations WHERE code = '01.03.02'),
                  degree_level_id = (SELECT id FROM degree_levels WHERE name = 'Бакалавриат'),
                  start_year = 2023,
                  end_year = 2027
WHERE name = 'ПМИ-101';

-- Курсы
INSERT INTO courses (name, hours) VALUES
                                      ('Базы данных', 120),
                                      ('Java-разработка', 180),
                                      ('Web-технологии', 150),
                                      ('Python программирование', 140),
                                      ('Алгоритмы и структуры данных', 160),
                                      ('Операционные системы', 130)
    ON CONFLICT DO NOTHING;

-- Связь преподавателей с курсами
INSERT INTO teacher_course (teacher_id, course_id)
SELECT t.id, c.id
FROM teachers t
         JOIN users u ON t.user_id = u.id
         CROSS JOIN courses c
WHERE u.login = 'smirnov_teacher'
  AND c.name IN ('Базы данных', 'Java-разработка', 'Web-технологии')
    ON CONFLICT DO NOTHING;

INSERT INTO teacher_course (teacher_id, course_id)
SELECT t.id, c.id
FROM teachers t
         JOIN users u ON t.user_id = u.id
         CROSS JOIN courses c
WHERE u.login = 'bychkov'
  AND c.name IN ('Базы данных', 'Java-разработка', 'Python программирование')
    ON CONFLICT DO NOTHING;

INSERT INTO teacher_course (teacher_id, course_id)
SELECT t.id, c.id
FROM teachers t
         JOIN users u ON t.user_id = u.id
         CROSS JOIN courses c
WHERE u.login = 'medvedeva_teacher'
  AND c.name IN ('Web-технологии', 'Python программирование')
    ON CONFLICT DO NOTHING;

INSERT INTO teacher_course (teacher_id, course_id)
SELECT t.id, c.id
FROM teachers t
         JOIN users u ON t.user_id = u.id
         CROSS JOIN courses c
WHERE u.login = 'kuznetsov_teacher'
  AND c.name IN ('Алгоритмы и структуры данных', 'Операционные системы')
    ON CONFLICT DO NOTHING;

-- Учебный план (curriculum)
-- Для группы ИНФО-101
INSERT INTO curriculum (group_id, course_id, teacher_id, semester)
SELECT g.id, c.id, t.id,
       CASE c.name
           WHEN 'Базы данных' THEN 3
           WHEN 'Java-разработка' THEN 4
           WHEN 'Web-технологии' THEN 5
           ELSE 3
           END
FROM groups g
         CROSS JOIN courses c
         CROSS JOIN teachers t
         JOIN users u ON t.user_id = u.id
WHERE g.name = 'ИНФО-101'
  AND c.name IN ('Базы данных', 'Java-разработка', 'Web-технологии')
  AND u.login = 'smirnov_teacher'
    ON CONFLICT DO NOTHING;

-- Для группы ИНФО-102
INSERT INTO curriculum (group_id, course_id, teacher_id, semester)
SELECT g.id, c.id, t.id,
       CASE c.name
           WHEN 'Базы данных' THEN 3
           WHEN 'Java-разработка' THEN 4
           WHEN 'Python программирование' THEN 3
           ELSE 3
           END
FROM groups g
         CROSS JOIN courses c
         CROSS JOIN teachers t
         JOIN users u ON t.user_id = u.id
WHERE g.name = 'ИНФО-102'
  AND c.name IN ('Базы данных', 'Java-разработка', 'Python программирование')
  AND u.login = 'bychkov'
    ON CONFLICT DO NOTHING;

-- Для группы БИЗ-201
INSERT INTO curriculum (group_id, course_id, teacher_id, semester)
SELECT g.id, c.id, t.id, 3
FROM groups g
         CROSS JOIN courses c
         CROSS JOIN teachers t
         JOIN users u ON t.user_id = u.id
WHERE g.name = 'БИЗ-201'
  AND c.name IN ('Web-технологии', 'Python программирование')
  AND u.login = 'medvedeva_teacher'
    ON CONFLICT DO NOTHING;

-- Журнал
INSERT INTO journal (user_id, action, entity_type, entity_id) VALUES
    ((SELECT id FROM users WHERE login = 'sidorov_dean'), 'APPROVE_APPLICATION', 'APPLICATION', 1);

-- =====================================================
-- 4. ТЕСТОВЫЕ ОЦЕНКИ
-- =====================================================

-- Очищаем старые оценки
DELETE FROM grades;

-- Оценки для студентов группы ИНФО-101 (преподаватель Смирнова)
INSERT INTO grades (student_id, course_id, teacher_id, grade_value, grade_type, semester, academic_year, comment)
SELECT
    s.id,
    c.id,
    t.id,
    CASE s.full_name
        WHEN 'Петров Пётр Петрович' THEN 5
        WHEN 'Лифантьев Дмитрий Андреевич' THEN 4
        WHEN 'Козлов Алексей Сергеевич' THEN 4
        ELSE 3
        END,
    'EXAM',
    cur.semester,
    '2024/2025',
    CASE s.full_name
        WHEN 'Петров Пётр Петрович' THEN 'Отлично, хорошие знания'
        WHEN 'Лифантьев Дмитрий Андреевич' THEN 'Хорошо, но есть пробелы'
        ELSE 'Удовлетворительно, нужно больше практики'
        END
FROM students s
         JOIN users u ON s.user_id = u.id
         JOIN curriculum cur ON cur.group_id = s.group_id
         JOIN courses c ON cur.course_id = c.id
         JOIN teachers t ON cur.teacher_id = t.id
WHERE cur.group_id = (SELECT id FROM groups WHERE name = 'ИНФО-101')
  AND c.name = 'Базы данных'
    ON CONFLICT DO NOTHING;

INSERT INTO grades (student_id, course_id, teacher_id, grade_value, grade_type, semester, academic_year, comment)
SELECT
    s.id,
    c.id,
    t.id,
    CASE s.full_name
        WHEN 'Петров Пётр Петрович' THEN 4
        WHEN 'Лифантьев Дмитрий Андреевич' THEN 5
        WHEN 'Козлов Алексей Сергеевич' THEN 3
        ELSE 4
        END,
    'EXAM',
    cur.semester,
    '2024/2025',
    'Средний уровень владения Java'
FROM students s
         JOIN users u ON s.user_id = u.id
         JOIN curriculum cur ON cur.group_id = s.group_id
         JOIN courses c ON cur.course_id = c.id
         JOIN teachers t ON cur.teacher_id = t.id
WHERE cur.group_id = (SELECT id FROM groups WHERE name = 'ИНФО-101')
  AND c.name = 'Java-разработка'
    ON CONFLICT DO NOTHING;

-- Оценки для группы ИНФО-102 (преподаватель Бычков)
INSERT INTO grades (student_id, course_id, teacher_id, grade_value, grade_type, semester, academic_year, comment)
SELECT
    s.id,
    c.id,
    t.id,
    CASE s.full_name
        WHEN 'Морозова Анна Владимировна' THEN 5
        WHEN 'Волков Денис Николаевич' THEN 4
        ELSE 3
        END,
    'EXAM',
    cur.semester,
    '2024/2025',
    'Хорошие результаты'
FROM students s
         JOIN users u ON s.user_id = u.id
         JOIN curriculum cur ON cur.group_id = s.group_id
         JOIN courses c ON cur.course_id = c.id
         JOIN teachers t ON cur.teacher_id = t.id
WHERE cur.group_id = (SELECT id FROM groups WHERE name = 'ИНФО-102')
  AND c.name = 'Базы данных'
    ON CONFLICT DO NOTHING;

-- Оценки для группы БИЗ-201 (преподаватель Медведева)
INSERT INTO grades (student_id, course_id, teacher_id, grade_value, grade_type, semester, academic_year, comment)
SELECT
    s.id,
    c.id,
    t.id,
    4,
    'TEST',
    cur.semester,
    '2024/2025',
    'Зачёт получен'
FROM students s
         JOIN users u ON s.user_id = u.id
         JOIN curriculum cur ON cur.group_id = s.group_id
         JOIN courses c ON cur.course_id = c.id
         JOIN teachers t ON cur.teacher_id = t.id
WHERE cur.group_id = (SELECT id FROM groups WHERE name = 'БИЗ-201')
  AND c.name = 'Web-технологии'
    ON CONFLICT DO NOTHING;