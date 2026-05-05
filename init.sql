-- Создание таблиц
CREATE TABLE IF NOT EXISTS users (
                                     id SERIAL PRIMARY KEY,
                                     login VARCHAR(50) UNIQUE,
    password VARCHAR(100),
    role VARCHAR(20)
    );

CREATE TABLE IF NOT EXISTS groups (
                                      id SERIAL PRIMARY KEY,
                                      name VARCHAR(50) UNIQUE
    );

CREATE TABLE IF NOT EXISTS students (
                                        id SERIAL PRIMARY KEY,
                                        user_id INTEGER UNIQUE,
                                        full_name VARCHAR(150),
    group_id INTEGER,
    status VARCHAR(20),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (group_id) REFERENCES groups(id)
    );

CREATE TABLE IF NOT EXISTS applicants (
                                          id SERIAL PRIMARY KEY,
                                          user_id INTEGER UNIQUE,
                                          full_name VARCHAR(150),
    FOREIGN KEY (user_id) REFERENCES users(id)
    );

CREATE TABLE IF NOT EXISTS applications (
                                            id SERIAL PRIMARY KEY,
                                            applicant_id INTEGER,
                                            program_id INTEGER,
                                            status VARCHAR(20),
    FOREIGN KEY (applicant_id) REFERENCES applicants(id)
    );

CREATE TABLE IF NOT EXISTS security_questions (
                                                  id SERIAL PRIMARY KEY,
                                                  user_id INTEGER NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    question TEXT NOT NULL,
    answer_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

-- Вставка тестовых данных
INSERT INTO users (login, password, role) VALUES
                                              ('ivanov_app', 'pass1', 'APPLICANT'),
                                              ('petrov_stud', 'pass2', 'STUDENT'),
                                              ('sidorov_dean', 'pass3', 'DEAN'),
                                              ('smirnov_teacher', 'pass4', 'TEACHER'),
                                              ('admin_user', 'adminpass', 'ADMIN')
    ON CONFLICT (login) DO NOTHING;

INSERT INTO groups (name) VALUES
                              ('ИНФО-101'),
                              ('ИНФО-102'),
                              ('БИЗ-201')
    ON CONFLICT (name) DO NOTHING;

INSERT INTO students (user_id, full_name, group_id, status) VALUES
    (2, 'Петров Пётр Петрович', 1, 'ACTIVE')
    ON CONFLICT (user_id) DO NOTHING;

INSERT INTO applicants (user_id, full_name) VALUES
    (1, 'Иванов Иван Иванович')
    ON CONFLICT (user_id) DO NOTHING;

INSERT INTO applications (applicant_id, program_id, status) VALUES
    (1, 101, 'PENDING');

INSERT INTO security_questions (user_id, question, answer_hash) VALUES
                                                                    (2, 'Ваш любимый цвет?', 'red'),
                                                                    (3, 'Ваша любимая книга?', 'java'),
                                                                    (4, 'Ваше любимое блюдо?', 'pasta');
