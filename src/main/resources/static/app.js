let userId = null;
let token = null;
let currentUser = null;
let currentLogin = null;

// =======================
// ОБЩИЕ ФУНКЦИИ
// =======================

function showError(elementId, message) {
    const el = document.getElementById(elementId);
    if (el) {
        el.innerText = message;
        el.classList.remove('hidden');
        setTimeout(() => el.classList.add('hidden'), 5000);
    }
}

function showInfo(elementId, message, isError = false) {
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
    }
}

function hideInfo(elementId) {
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

    // Скрыть старые сообщения
    hideInfo('attempts-info');
    hideInfo('login-error');

    try {
        const response = await fetch(`/auth/login?login=${loginVal}&password=${passwordVal}`, { method: 'POST' });

        if (!response.ok) {
            let errorMessage = 'Неверный логин или пароль';
            let remainingInfo = '';

            try {
                const errorData = await response.json();
                errorMessage = errorData.message || errorMessage;

                // Извлекаем информацию о попытках из сообщения
                const attemptsMatch = errorMessage.match(/Осталось попыток:\s*(\d+)/i);
                if (attemptsMatch) {
                    remainingInfo = ` ⚠️ Осталось попыток: ${attemptsMatch[1]}`;
                }

                const blockedMatch = errorMessage.match(/заблокирован/i);
                if (blockedMatch) {
                    showInfo('attempts-info', '🔒 ' + errorMessage, true);
                } else {
                    showInfo('attempts-info', '❌ ' + errorMessage + remainingInfo, true);
                }

            } catch(e) {
                const text = await response.text();
                errorMessage = text || errorMessage;
                showInfo('attempts-info', '❌ ' + errorMessage, true);
            }

            showError('login-error', errorMessage);
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
                hideInfo('attempts-info');
                document.getElementById('login-error').classList.add('hidden');
            }
        }
    } catch (err) {
        showInfo('attempts-info', '❌ Ошибка соединения с сервером', true);
        showError('login-error', err.message);
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
            showInfo('attempts-info', '❌ ' + errorMessage, true);
            showError('login-error', errorMessage);
            return;
        }

        const data = await res.json();
        token = data.token;
        localStorage.setItem('token', token);

        const payload = JSON.parse(atob(token.split('.')[1]));
        currentUser = { id: payload.userId, role: payload.role };

        redirectByRole(payload.role);
    } catch (err) {
        showInfo('attempts-info', '❌ Ошибка: ' + err.message, true);
        showError('login-error', err.message);
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
        document.getElementById('profile').innerHTML = '<pre>' + JSON.stringify(data, null, 2) + '</pre>';
    } catch (err) {
        alert('Ошибка: ' + err.message);
    }
}

async function loadGroup() {
    try {
        const res = await fetch('/student/group/1', { headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        const data = await res.json();
        let html = '<table> hilab<th>ФИО</th><th>Статус</th></tr>';
        data.forEach(s => html += `<tr><td>${s.fullName}</td><td>${s.status}</td></tr>`);
        html += '</table>';
        document.getElementById('group-students').innerHTML = html;
    } catch (err) {
        alert('Ошибка: ' + err.message);
    }
}

// =======================
// ДЕКАН
// =======================

async function loadDeanDashboard() {
    const token = localStorage.getItem('token');

    const appsRes = await fetch('/dean/applications', { headers: { 'Authorization': 'Bearer ' + token } });
    const applications = await appsRes.json();
    let appsHtml = '';
    applications.forEach(app => {
        appsHtml += `<tr>
                            <td>${app.id}</td>
                            <td>${app.applicantId}</td>
                            <td>${app.programId}</td>
                            <td>${app.status}</td>
                            <td><button class="btn btn-success btn-sm" onclick="approveApp(${app.id})">Одобрить</button></td>
                        </tr>`;
    });
    document.getElementById('applications-list').innerHTML = appsHtml;

    const studentsRes = await fetch('/dean/students', { headers: { 'Authorization': 'Bearer ' + token } });
    const students = await studentsRes.json();
    let studentsHtml = '';
    students.forEach(s => {
        studentsHtml += `<tr>
                            <td>${s.id}</td>
                            <td>${s.fullName}</td>
                            <td><input type="number" id="group-${s.id}" value="${s.groupId}" style="width:60px"></td>
                            <td>${s.status}</td>
                            <td>
                                <button class="btn btn-warning btn-sm" onclick="transferStudent(${s.id})">Перевести</button>
                                <button class="btn btn-danger btn-sm" onclick="expelStudent(${s.id})">Отчислить</button>
                            </td>
                        </tr>`;
    });
    document.getElementById('students-list').innerHTML = studentsHtml;
}

async function approveApp(appId) {
    try {
        await fetch(`/dean/approve-application/${appId}`, { method: 'POST', headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        showSuccess('dean-success', 'Заявление одобрено');
        loadDeanDashboard();
    } catch (err) {
        showError('dean-error', err.message);
    }
}

async function transferStudent(studentId) {
    const groupId = document.getElementById(`group-${studentId}`).value;
    try {
        await fetch(`/dean/transfer-student/${studentId}/to-group/${groupId}`, { method: 'POST', headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        showSuccess('dean-success', 'Студент переведён');
        loadDeanDashboard();
    } catch (err) {
        showError('dean-error', err.message);
    }
}

async function expelStudent(studentId) {
    if (!confirm('Отчислить студента?')) return;
    try {
        await fetch(`/dean/expel-student/${studentId}`, { method: 'POST', headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') } });
        showSuccess('dean-success', 'Студент отчислен');
        loadDeanDashboard();
    } catch (err) {
        showError('dean-error', err.message);
    }
}

function showSuccess(elementId, message) {
    const el = document.getElementById(elementId);
    if (el) {
        el.innerText = message;
        el.classList.remove('hidden');
        setTimeout(() => el.classList.add('hidden'), 3000);
    }
}

// =======================
// АБИТУРИЕНТ
// =======================

async function submitApplication() {
    const programId = document.getElementById('programId').value;
    try {
        const res = await fetch('/applicant/submit-application', {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token'), 'Content-Type': 'application/json' },
            body: JSON.stringify({ programId: parseInt(programId) })
        });
        showSuccess('apply-success', 'Заявление подано');
        loadApplications();
    } catch (err) {
        showError('apply-error', err.message);
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
        showError('admin-error', err.message);
    }
}

async function changeRole(userId) {
    const newRole = document.getElementById(`role-${userId}`).value;
    try {
        await fetch(`/admin/change-role?userId=${userId}&role=${newRole}`, {
            method: 'POST',
            headers: { 'Authorization': 'Bearer ' + localStorage.getItem('token') }
        });
        showSuccess('admin-success', 'Роль изменена');
        loadUsers();
    } catch (err) {
        showError('admin-error', err.message);
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
    } else if (path.includes('student.html')) {
        const payload = JSON.parse(atob(token.split('.')[1]));
        document.getElementById('user-login').innerText = payload.sub || 'User';
        document.getElementById('user-role').innerText = payload.role || 'STUDENT';
    } else if (path.includes('applicant.html')) {
        loadApplications();
        const payload = JSON.parse(atob(token.split('.')[1]));
        document.getElementById('user-login').innerText = payload.sub || 'User';
        document.getElementById('user-role').innerText = payload.role || 'APPLICANT';
    } else if (path.includes('admin.html')) {
        loadUsers();
        const payload = JSON.parse(atob(token.split('.')[1]));
        document.getElementById('user-login').innerText = payload.sub || 'User';
        document.getElementById('user-role').innerText = payload.role || 'ADMIN';
    }
});