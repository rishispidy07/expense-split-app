document.addEventListener('DOMContentLoaded', () => {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');

    // If already logged in, go to dashboard
    if (API.isLoggedIn() && (window.location.pathname === '/login.html' || window.location.pathname === '/register.html')) {
        window.location.href = '/dashboard.html';
        return;
    }

    // Clear any stale form data
    document.querySelectorAll('input[type="password"]').forEach(i => i.value = '');

    if (loginForm) {
        loginForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;
            const errorDiv = document.getElementById('errorMessage');
            errorDiv.style.display = 'none';

            if (!username || !password) {
                errorDiv.textContent = 'Please fill in all fields';
                errorDiv.style.display = 'block';
                return;
            }

            const result = await API.post('/api/auth/login', { username, password });

            if (result && result.success) {
                if (result.otpRequired) {
                    sessionStorage.setItem('otpUsername', result.username);
                    sessionStorage.setItem('otpMaskedEmail', result.maskedEmail);
                    if (result.otp) sessionStorage.setItem('otpDemo', result.otp);
                    window.location.href = '/otp.html';
                    return;
                }
                API.setToken(result.token);
                API.setUser(result.user);
                window.location.href = '/dashboard.html';
            } else {
                errorDiv.textContent = result ? result.message : 'Login failed. Please try again.';
                errorDiv.style.display = 'block';
                document.getElementById('password').value = '';
                document.getElementById('password').focus();
            }
        });
    }

    if (registerForm) {
        const passwordInput = document.getElementById('password');
        const strengthDiv = document.getElementById('passwordStrength');

        if (passwordInput) {
            passwordInput.addEventListener('input', (e) => {
                const val = e.target.value;
                let score = 0;
                if (val.length >= 8) score++;
                if (/[A-Z]/.test(val)) score++;
                if (/[a-z]/.test(val)) score++;
                if (/[0-9]/.test(val)) score++;
                if (/[^A-Za-z0-9]/.test(val)) score++;

                strengthDiv.className = 'password-strength';
                if (score <= 2) strengthDiv.classList.add('weak');
                else if (score <= 4) strengthDiv.classList.add('medium');
                else strengthDiv.classList.add('strong');

                document.getElementById('req-length').className = val.length >= 8 ? 'met' : '';
                document.getElementById('req-upper').className = /[A-Z]/.test(val) ? 'met' : '';
                document.getElementById('req-lower').className = /[a-z]/.test(val) ? 'met' : '';
                document.getElementById('req-digit').className = /[0-9]/.test(val) ? 'met' : '';
                document.getElementById('req-special').className = /[^A-Za-z0-9]/.test(val) ? 'met' : '';
            });
        }

        registerForm.addEventListener('submit', async (e) => {
            e.preventDefault();
            const fullName = document.getElementById('fullName').value.trim();
            const username = document.getElementById('username').value.trim();
            const email = document.getElementById('email').value.trim();
            const phone = document.getElementById('phone').value.trim();
            const password = document.getElementById('password').value;
            const confirmPassword = document.getElementById('confirmPassword').value;
            const errorDiv = document.getElementById('errorMessage');
            errorDiv.style.display = 'none';

            if (!fullName || !username || !email || !password) {
                errorDiv.textContent = 'Please fill in all required fields';
                errorDiv.style.display = 'block';
                return;
            }

            if (password !== confirmPassword) {
                errorDiv.textContent = 'Passwords do not match';
                errorDiv.style.display = 'block';
                return;
            }

            const result = await API.post('/api/auth/register', {
                fullName, username, email, phone, password
            });

            if (result && result.success) {
                API.setToken(result.token);
                API.setUser({ id: result.userId, username, email, fullName });
                window.location.href = '/dashboard.html';
            } else {
                errorDiv.textContent = result ? result.message : 'Registration failed';
                errorDiv.style.display = 'block';
            }
        });
    }
});
