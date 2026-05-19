let userId = null;
let token = null;
let currentUser = null;
let currentLogin = null;

// =======================
// ОБЩИЕ ФУНКЦИИ
// =======================

function showMessage(elementId, message, isError = false) {
    const el = document.getElementById(elementId);
    if (el) {
        el.innerText = message;
        el.classList.remove('hidden');
        if (isError) {
            el.style.background = '#fdecea';
            el.style.borderLeftColor = '#e74c3c';
            el.style.color = '#e74c3c';
        } else {
            el.style.background = '#fef9e7';
            el.style.borderLeftColor = '#f39c12';
            el.style.color = '#f39c12';
        }
        setTimeout(() => el.classList.add('hidden'), 5000);
    }
}

function hideMessage(elementId) {
    const el = document.getElementById(elementId);
    if (el) {
        el.classList.add('hidden');
    }
}

// =======================
// АУТЕНТИФИКАЦИЯ
// =======================

async function login() {
    const loginVal = document.getElementById('login').value;
    const passwordVal = document.getElementById('password').value;
    currentLogin = loginVal;

    // Скрываем старые сообщения
    hideMessage('attempts-info');

    try {
        const response = await fetch(`/auth/login?login=${loginVal}&password=${passwordVal}`, { method: 'POST' });

        if (!response.ok) {
            let errorMessage = 'Неверный логин или пароль';

            try {
                const errorData = await response.json();
                errorMessage = errorData.message || errorMessage;
            } catch(e) {
                const text = await response.text();
                errorMessage = text || errorMessage;
            }

            // Показываем ОДНО сообщение
            showMessage('attempts-info', '❌ ' + errorMessage, true);
            return;
        }

        const data = await response.json();

        if (data.userId) {
            userId = data.userId;

            if (!data.requiresSecurityQuestion) {
                const verifyRes = await fetch(`/auth/verify-security?userId=${userId}&answer=`, { method: 'POST' });
                const verifyData = await verifyRes.json();
                token = verifyData.token;
                localStorage.setItem('token', token);

                const payload = JSON.parse(atob(token.split('.')[1]));
                currentUser = { id: payload.userId, role: payload.role };

                redirectByRole(payload.role);
                return;
            }

            const qRes = await fetch(`/auth/security-question?userId=${userId}`);
            const qData = await qRes.json();

            if (qData.skipped === 'true') {
                const verifyRes = await fetch(`/auth/verify-security?userId=${userId}&answer=`, { method: 'POST' });
                const verifyData = await verifyRes.json();
                token = verifyData.token;
                localStorage.setItem('token', token);

                const payload = JSON.parse(atob(token.split('.')[1]));
                currentUser = { id: payload.userId, role: payload.role };

                redirectByRole(payload.role);
            } else {
                document.getElementById('question').innerText = qData.question;
                document.getElementById('security-section').classList.remove('hidden');
                hideMessage('attempts-info');
            }
        }
    } catch (err) {
        showMessage('attempts-info', '❌ Ошибка соединения с сервером', true);
    }
}

function redirectByRole(role) {
    if (role === 'STUDENT') window.location.href = 'student.html';
    else if (role === 'DEAN') window.location.href = 'dean.html';
    else if (role === 'APPLICANT') window.location.href = 'applicant.html';
    else if (role === 'ADMIN') window.location.href = 'admin.html';
    else if (role === 'TEACHER') window.location.href = 'teacher.html';
    else alert('Нет интерфейса для роли: ' + role);
}

async function verify() {
    const answer = document.getElementById('answer').value;

    try {
        const res = await fetch(`/auth/verify-security?userId=${userId}&answer=${answer}`, { method: 'POST' });

        if (!res.ok) {
            let errorMessage = 'Неверный ответ';
            try {
                const errorData = await res.json();
                errorMessage = errorData.message || errorMessage;
            } catch(e) {
                const text = await res.text();
                errorMessage = text || errorMessage;
            }
            showMessage('attempts-info', '❌ ' + errorMessage, true);
            return;
        }

        const data = await res.json();
        token = data.token;
        localStorage.setItem('token', token);

        const payload = JSON.parse(atob(token.split('.')[1]));
        currentUser = { id: payload.userId, role: payload.role };

        redirectByRole(payload.role);
    } catch (err) {
        showMessage('attempts-info', '❌ Ошибка: ' + err.message, true);
    }
}

function logout() {
    localStorage.removeItem('token');
    window.location.href = 'index.html';
}

// =======================
// СТУДЕНТ
// =======================

async function loadProfile() {
    try {
        const res = await fetch('/student/my-profile', { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        const data = await res.json();
        let html = `
            <div class="profile-card">
                <p><strong>ФИО:</strong> ${data.fullName || '—'}</p>
                <p><strong>Группа:</strong> ${data.groupId || '—'}</p>
                <p><strong>Статус:</strong> <span class="badge ${data.status === 'ACTIVE' ? 'badge-active' : 'badge-expelled'}">${data.status || '—'}</span></p>
            </div>
        `;
        document.getElementById('profile').innerHTML = html;
    } catch (err) {
        document.getElementById('profile').innerHTML = '<div class="error">Ошибка загрузки профиля</div>';
    }
}

async function loadGroup() {
    try {
        const res = await fetch('/student/group/1', { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        const students = await res.json();

        if (students.length === 0) {
            document.getElementById('group-students').innerHTML = '<p>Нет студентов в группе</p>';
            return;
        }

        let html = '<table class="data-table"><thead><tr><th>ФИО</th><th>Статус</th></tr></thead><tbody>';
        students.forEach(s => {
            html += `<tr><td>${s.fullName || '—'}</td><td><span class="badge ${s.status === 'ACTIVE' ? 'badge-active' : 'badge-expelled'}">${s.status || '—'}</span></td></tr>`;
        });
        html += '</tbody></table>';
        document.getElementById('group-students').innerHTML = html;
    } catch (err) {
        document.getElementById('group-students').innerHTML = '<div class="error">Ошибка загрузки группы</div>';
    }
}

async function loadCourses() {
    try {
        const res = await fetch('/student/my-courses', { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        const courses = await res.json();

        if (!courses || courses.length === 0) {
            document.getElementById('my-courses').innerHTML = '<p>Нет назначенных дисциплин</p>';
            return;
        }

        let html = '<table class="data-table"><thead><tr><th>Дисциплина</th><th>Часы</th><th>Семестр</th><th>Преподаватель</th></tr></thead><tbody>';
        courses.forEach(c => {
            html += `<tr><td>${c.name || '—'}</td><td>${c.hours || '—'}</td><td>${c.semester || '—'}</td><td>${c.teacherName || '—'}</td></tr>`;
        });
        html += '</tbody></table>';
        document.getElementById('my-courses').innerHTML = html;
    } catch (err) {
        document.getElementById('my-courses').innerHTML = '<div class="error">Ошибка загрузки дисциплин</div>';
    }
}

// =======================
// ДЕКАН
// =======================

async function loadDeanDashboard() {
    const token = localStorage.getItem('token');

    try {
        const appsRes = await fetch('/dean/applications', { headers: { 'Authorization': 'Bearer ' + token } });
        const applications = await appsRes.json();
        let appsHtml = '';
        applications.forEach(app => {
            appsHtml += `<tr><td>${app.id}</td><td>${app.applicantId}</td><td>${app.programId}</td><td>${app.status}</td><td><button class="btn btn-success btn-sm" onclick="approveApp(${app.id})">Одобрить</button></td></tr>`;
        });
        document.getElementById('applications-list').innerHTML = appsHtml;

        const studentsRes = await fetch('/dean/students', { headers: { 'Authorization': 'Bearer ' + token } });
        const students = await studentsRes.json();
        let studentsHtml = '';
        students.forEach(s => {
            studentsHtml += `<tr><td>${s.id}</td><td>${s.fullName}</td><td><input type="number" id="group-${s.id}" value="${s.groupId}" style="width:60px"></td><td>${s.status}</td><td><button class="btn btn-warning btn-sm" onclick="transferStudent(${s.id})">Перевести</button><button class="btn btn-danger btn-sm" onclick="expelStudent(${s.id})">Отчислить</button></td></tr>`;
        });
        document.getElementById('students-list').innerHTML = studentsHtml;
    } catch (err) {
        showMessage('dean-error', err.message, true);
    }
}

async function approveApp(appId) {
    try {
        await fetch(`/dean/approve-application/${appId}`, { method: 'POST', headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        showMessage('dean-success', 'Заявление одобрено', false);
        loadDeanDashboard();
    } catch (err) {
        showMessage('dean-error', err.message, true);
    }
}

async function transferStudent(studentId) {
    const groupId = document.getElementById(`group-${studentId}`).value;
    try {
        await fetch(`/dean/transfer-student/${studentId}/to-group/${groupId}`, { method: 'POST', headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        showMessage('dean-success', 'Студент переведён', false);
        loadDeanDashboard();
    } catch (err) {
        showMessage('dean-error', err.message, true);
    }
}

async function expelStudent(studentId) {
    if (!confirm('Отчислить студента?')) return;
    try {
        await fetch(`/dean/expel-student/${studentId}`, { method: 'POST', headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        showMessage('dean-success', 'Студент отчислен', false);
        loadDeanDashboard();
    } catch (err) {
        showMessage('dean-error', err.message, true);
    }
}

// =======================
// АБИТУРИЕНТ
// =======================

async function submitApplication() {
    const programId = document.getElementById('programId').value;
    try {
        await fetch('/applicant/submit-application', {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token'), 'Content-Type': 'application/json' },
            body: JSON.stringify({ programId: parseInt(programId) })
        });
        showMessage('apply-success', 'Заявление подано', false);
        loadApplications();
    } catch (err) {
        showMessage('apply-error', err.message, true);
    }
}

async function loadApplications() {
    try {
        const res = await fetch('/applicant/my-applications', { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        const applications = await res.json();
        let html = '';
        applications.forEach(app => {
            html += `<tr><td>${app.id}</td><td>${app.programId}</td><td>${app.status}</td></tr>`;
        });
        document.getElementById('applications-list').innerHTML = html;
    } catch (err) {
        console.error(err);
    }
}

// =======================
// АДМИНИСТРАТОР
// =======================

async function loadUsers() {
    try {
        const res = await fetch('/admin/users', { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        const users = await res.json();
        let html = '';
        users.forEach(u => {
            html += `<tr>
                        <td>${u.id}</td>
                        <td>${u.login}</td>
                        <td>
                            <select id="role-${u.id}" class="role-select">
                                <option value="APPLICANT" ${u.role === 'APPLICANT' ? 'selected' : ''}>Абитуриент</option>
                                <option value="STUDENT" ${u.role === 'STUDENT' ? 'selected' : ''}>Студент</option>
                                <option value="DEAN" ${u.role === 'DEAN' ? 'selected' : ''}>Декан</option>
                                <option value="TEACHER" ${u.role === 'TEACHER' ? 'selected' : ''}>Преподаватель</option>
                                <option value="ADMIN" ${u.role === 'ADMIN' ? 'selected' : ''}>Администратор</option>
                            </select>
                        </td>
                        <td><button class="btn btn-primary btn-sm" onclick="changeRole(${u.id})">Сохранить</button></td>
                     </tr>`;
        });
        document.getElementById('users-list').innerHTML = html;
    } catch (err) {
        showMessage('admin-error', err.message, true);
    }
}

async function changeRole(userId) {
    const newRole = document.getElementById(`role-${userId}`).value;
    try {
        await fetch(`/admin/change-role?userId=${userId}&role=${newRole}`, {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        showMessage('admin-success', 'Роль изменена', false);
        loadUsers();
    } catch (err) {
        showMessage('admin-error', err.message, true);
    }
}

// =======================
// ПРЕПОДАВАТЕЛЬ
// =======================

async function loadTeacherProfile() {
    try {
        const res = await fetch('/teacher/my-profile', { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        const teacher = await res.json();

        let html = `
            <table class="info-table">
                <tr><th>ФИО</th><td>${teacher.fullName || '—'}</td></tr>
                <tr><th>Должность</th><td>${teacher.position || '—'}</td></tr>
                <tr><th>Учёная степень</th><td>${teacher.academicDegree || '—'}</td></tr>
                <tr><th>Телефон</th><td>${teacher.phone || '—'}</td></tr>
            </table>
        `;
        document.getElementById('profile-info').innerHTML = html;
    } catch (err) {
        document.getElementById('profile-info').innerHTML = '<div class="error">Ошибка загрузки профиля</div>';
    }
}

async function loadTeacherCourses() {
    try {
        const res = await fetch('/teacher/my-courses', { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        const courses = await res.json();

        if (!courses || courses.length === 0) {
            document.getElementById('courses-list').innerHTML = '<p>Нет назначенных дисциплин</p>';
            return;
        }

        let html = '<table class="data-table"><thead><tr><th>Дисциплина</th><th>Часы</th><th>Семестр</th></tr></thead><tbody>';
        courses.forEach(c => {
            html += `<tr><td>${c.name}</td><td>${c.hours}</td><td>${c.semester}</td></tr>`;
        });
        html += '</tbody></table>';
        document.getElementById('courses-list').innerHTML = html;
    } catch (err) {
        document.getElementById('courses-list').innerHTML = '<div class="error">Ошибка загрузки дисциплин</div>';
    }
}

async function loadTeacherSchedule() {
    try {
        const res = await fetch('/teacher/my-schedule', { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        const schedule = await res.json();

        if (!schedule || schedule.length === 0) {
            document.getElementById('schedule-list').innerHTML = '<p>Расписание не загружено</p>';
            return;
        }

        let html = '<table class="data-table"><thead><tr><th>Дисциплина</th><th>Группа</th><th>День</th><th>Время</th><th>Аудитория</th></tr></thead><tbody>';
        schedule.forEach(lesson => {
            html += `<tr>
                <td>${lesson.courseName || '—'}</td>
                <td>${lesson.groupName || lesson.groupId || '—'}</td>
                <td>${lesson.weekday || '—'}</td>
                <td>${lesson.startTime ? lesson.startTime + ' - ' + lesson.endTime : '—'}</td>
                <td>${lesson.classroom || '—'}</td>
            </tr>`;
        });
        html += '</tbody></table>';
        document.getElementById('schedule-list').innerHTML = html;
    } catch (err) {
        document.getElementById('schedule-list').innerHTML = '<div class="error">Ошибка загрузки расписания</div>';
    }
}

async function loadTeacherStudents() {
    try {
        const res = await fetch('/teacher/my-students', { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        const students = await res.json();

        if (!students || students.length === 0) {
            document.getElementById('students-list').innerHTML = '<p>Нет студентов</p>';
            return;
        }

        let html = '<table class="data-table"><thead><tr><th>ФИО</th><th>Группа</th><th>Статус</th></tr></thead><tbody>';
        students.forEach(s => {
            html += `<tr><td>${s.fullName}</td><td>${s.groupName || '—'}</td><td>${s.status}</td></tr>`;
        });
        html += '</tbody></table>';
        document.getElementById('students-list').innerHTML = html;
    } catch (err) {
        document.getElementById('students-list').innerHTML = '<div class="error">Ошибка загрузки студентов</div>';
    }
}

// =======================
// ЗАГРУЗКА ПРИ ЗАГРУЗКЕ СТРАНИЦЫ
// =======================

document.addEventListener('DOMContentLoaded', function() {
    const token = localStorage.getItem('token');
    if (!token) return;

    const path = window.location.pathname;

    if (path.includes('dean.html')) {
        loadDeanDashboard();
        const payload = JSON.parse(atob(token.split('.')[1]));
        document.getElementById('user-login').innerText = payload.userId || 'User';
        document.getElementById('user-role').innerText = payload.role || 'DEAN';
    } else if (path.includes('student.html')) {
        const payload = JSON.parse(atob(token.split('.')[1]));
        document.getElementById('user-login').innerText = payload.userId || 'User';
        document.getElementById('user-role').innerText = payload.role || 'STUDENT';
        loadProfile();
        loadGroup();
        loadCourses();
    } else if (path.includes('applicant.html')) {
        loadApplications();
        const payload = JSON.parse(atob(token.split('.')[1]));
        document.getElementById('user-login').innerText = payload.userId || 'User';
        document.getElementById('user-role').innerText = payload.role || 'APPLICANT';
    } else if (path.includes('admin.html')) {
        loadUsers();
        const payload = JSON.parse(atob(token.split('.')[1]));
        document.getElementById('user-login').innerText = payload.userId || 'User';
        document.getElementById('user-role').innerText = payload.role || 'ADMIN';
    } else if (path.includes('teacher.html')) {
        const payload = JSON.parse(atob(token.split('.')[1]));
        document.getElementById('user-login').innerText = payload.userId || 'User';
        document.getElementById('user-role').innerText = payload.role || 'TEACHER';
        loadTeacherProfile();
        loadTeacherCourses();
        loadTeacherSchedule();
        loadTeacherStudents();
    }
});


// Добавьте в конец файла app.js

async function updateProfile() {
    const fullName = document.getElementById('newFullName')?.value;
    const email = document.getElementById('newEmail')?.value;
    const phone = document.getElementById('newPhone')?.value;

    try {
        const response = await fetch('/student/update-profile', {
            method: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token'),
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ fullName, email, phone })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message);
        }

        const data = await response.json();
        showMessage('attempts-info', '✅ ' + data.message, false);
        setTimeout(() => location.reload(), 1500);
    } catch (err) {
        showMessage('attempts-info', '❌ ' + err.message, true);
    }
}

async function changePassword() {
    const oldPassword = document.getElementById('oldPassword')?.value;
    const newPassword = document.getElementById('newPassword')?.value;
    const confirmPassword = document.getElementById('confirmPassword')?.value;

    if (newPassword !== confirmPassword) {
        showMessage('attempts-info', '❌ Пароли не совпадают', true);
        return;
    }

    try {
        const response = await fetch('/student/change-password', {
            method: 'POST',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token'),
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ oldPassword, newPassword })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message);
        }

        const data = await response.json();
        showMessage('attempts-info', '✅ ' + data.message, false);
        document.getElementById('oldPassword').value = '';
        document.getElementById('newPassword').value = '';
        document.getElementById('confirmPassword').value = '';
    } catch (err) {
        showMessage('attempts-info', '❌ ' + err.message, true);
    }
}

async function updateSecurityQuestion() {
    const question = document.getElementById('securityQuestion')?.value;
    const answer = document.getElementById('securityAnswer')?.value;

    if (!question || !answer) {
        showMessage('attempts-info', '❌ Заполните вопрос и ответ', true);
        return;
    }

    try {
        const response = await fetch('/student/security-question', {
            method: 'PUT',
            headers: {
                'Authorization': 'Bearer ' + localStorage.getItem('token'),
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ question, answer })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message);
        }

        const data = await response.json();
        showMessage('attempts-info', '✅ ' + data.message, false);
    } catch (err) {
        showMessage('attempts-info', '❌ ' + err.message, true);
    }
}