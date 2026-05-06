-- =====================================================
-- 1. ТАБЛИЦЫ БЕЗ ВНЕШНИХ КЛЮЧЕЙ
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

-- Контакты студентов (создаём таблицу, FK добавим позже)
CREATE TABLE IF NOT EXISTS student_contacts (
                                                student_id INT PRIMARY KEY,
                                                email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20)
    );

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

-- Добавляем поля в groups
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
-- 3. ТЕСТОВЫЕ ДАННЫЕ (В ПРАВИЛЬНОМ ПОРЯДКЕ)
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
                                              ('lifantiev', 'pass123', 'STUDENT')
    ON CONFLICT (login) DO NOTHING;

-- Группы
INSERT INTO groups (name) VALUES
                              ('ИНФО-101'),
                              ('ИНФО-102'),
                              ('БИЗ-201')
    ON CONFLICT (name) DO NOTHING;

-- =====================================================
-- СТУДЕНТЫ (СНАЧАЛА, ПОТОМ КОНТАКТЫ)
-- =====================================================

-- Студент Петров (user_id = 2)
INSERT INTO students (user_id, full_name, group_id, status) VALUES
    (2, 'Петров Пётр Петрович', 1, 'ACTIVE')
    ON CONFLICT (user_id) DO NOTHING;

-- Студент Лифантьев (user_id = 8)
INSERT INTO students (user_id, full_name, group_id, status) VALUES
    ((SELECT id FROM users WHERE login = 'lifantiev'), 'Лифантьев Дмитрий Андреевич', 1, 'ACTIVE')
    ON CONFLICT (user_id) DO NOTHING;

-- =====================================================
-- КОНТАКТЫ СТУДЕНТОВ (ПОСЛЕ СОЗДАНИЯ СТУДЕНТОВ)
-- =====================================================

-- Контакты Петрова (получаем реальный id студента)
INSERT INTO student_contacts (student_id, email, phone)
SELECT id, 'petrov@university.ru', '+79991234567'
FROM students WHERE user_id = 2
    ON CONFLICT (student_id) DO NOTHING;

-- Контакты Лифантьева
INSERT INTO student_contacts (student_id, email, phone)
SELECT id, 'lifantiev@university.ru', '+79998887766'
FROM students WHERE user_id = (SELECT id FROM users WHERE login = 'lifantiev')
    ON CONFLICT (student_id) DO NOTHING;

-- =====================================================
-- ОСТАЛЬНЫЕ ДАННЫЕ
-- =====================================================

-- Абитуриент
INSERT INTO applicants (user_id, full_name) VALUES
    (1, 'Иванов Иван Иванович')
    ON CONFLICT (user_id) DO NOTHING;

-- Заявления
INSERT INTO applications (applicant_id, program_id, status) VALUES
    (1, 101, 'PENDING');

-- Секретные вопросы
INSERT INTO security_questions (user_id, question, answer_hash) VALUES
                                                                    (2, 'Ваш любимый цвет?', 'red'),
                                                                    (3, 'Ваша любимая книга?', 'java'),
                                                                    (4, 'Ваше любимое блюдо?', 'pasta');

-- Преподаватель Смирнова
INSERT INTO teachers (user_id, full_name, position) VALUES
    (4, 'Смирнова Анна Викторовна', 'Доцент')
    ON CONFLICT DO NOTHING;

-- Преподаватель Бычков
INSERT INTO teachers (user_id, full_name, position, academic_degree, phone) VALUES
    ((SELECT id FROM users WHERE login = 'bychkov'), 'Бычков Сергей Юрьевич', 'Профессор', 'Доктор технических наук', '+79161234567')
    ON CONFLICT DO NOTHING;

-- Специальности
INSERT INTO specializations (code, name) VALUES
                                             ('09.03.03', 'Прикладная информатика'),
                                             ('38.03.05', 'Бизнес-информатика')
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
                  curator_id = 1
WHERE id = 1;

-- Дисциплины
INSERT INTO courses (name, hours) VALUES
                                      ('Базы данных', 120),
                                      ('Java-разработка', 180),
                                      ('Web-технологии', 150)
    ON CONFLICT DO NOTHING;

-- Связь преподавателя Смирнова с дисциплинами
INSERT INTO teacher_course (teacher_id, course_id) VALUES
                                                       (1, 1), (1, 2), (1, 3)
    ON CONFLICT DO NOTHING;

-- Связь преподавателя Бычкова с дисциплинами
INSERT INTO teacher_course (teacher_id, course_id)
SELECT
    (SELECT id FROM teachers WHERE user_id = (SELECT id FROM users WHERE login = 'bychkov')),
    id
FROM courses WHERE name IN ('Базы данных', 'Java-разработка', 'Web-технологии')
    ON CONFLICT DO NOTHING;

-- Учебный план для Смирнова
INSERT INTO curriculum (group_id, course_id, teacher_id, semester) VALUES
                                                                       (1, 1, 1, 3),
                                                                       (1, 2, 1, 4),
                                                                       (1, 3, 1, 5)
    ON CONFLICT DO NOTHING;

-- Учебный план для Бычкова
INSERT INTO curriculum (group_id, course_id, teacher_id, semester)
SELECT
    1,
    c.id,
    (SELECT id FROM teachers WHERE user_id = (SELECT id FROM users WHERE login = 'bychkov')),
    CASE c.name
        WHEN 'Базы данных' THEN 3
        WHEN 'Java-разработка' THEN 4
        WHEN 'Web-технологии' THEN 5
        END
FROM courses c
WHERE c.name IN ('Базы данных', 'Java-разработка', 'Web-технологии')
    ON CONFLICT DO NOTHING;

-- История переводов
INSERT INTO student_group_history (student_id, from_group_id, to_group_id)
SELECT id, NULL, 1 FROM students WHERE user_id = 2
    ON CONFLICT DO NOTHING;

-- Журнал
INSERT INTO journal (user_id, action, entity_type, entity_id) VALUES
    (3, 'APPROVE_APPLICATION', 'Application', 1);