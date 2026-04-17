
// implement JWT header
function getAuthHeaders() {
            const token = localStorage.getItem("accessToken") || sessionStorage.getItem("accessToken");
      return {
        "Authorization": "Bearer " + token,
        "Content-Type": "application/json"
      };
    }

        async function authFetch(url, options = {}) {
        let accessToken = localStorage.getItem("accessToken") || sessionStorage.getItem("accessToken");

        const isFormData = options.body instanceof FormData;
        options.headers = {
                ...options.headers,
                'Authorization': `Bearer ${accessToken}`
        };
        if (!isFormData && !options.headers['Content-Type']) {
                options.headers['Content-Type'] = 'application/json';
        }

    let response = await fetch(url, options);

    // If Access Token is expired
    if (response.status === 401) {
        const refreshToken = localStorage.getItem("refreshToken") || sessionStorage.getItem("refreshToken");

        const refreshRes = await fetch("/api/v1/auth/refresh", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ refreshToken: refreshToken })
        });

        if (refreshRes.ok) {
            const data = await refreshRes.json();
            // Save NEW access token
            localStorage.setItem("accessToken", data.accessToken);
            sessionStorage.setItem("accessToken", data.accessToken);
            if (data.refreshToken) {
                localStorage.setItem("refreshToken", data.refreshToken);
                sessionStorage.setItem("refreshToken", data.refreshToken);
            }
            
            // Retry the original request with the new token
            options.headers['Authorization'] = `Bearer ${data.accessToken}`;
            return fetch(url, options); 
        } else {
            // Both tokens failed -> Logout
            localStorage.clear();
            sessionStorage.clear();
            window.location.href = "/login.html";
        }
    }
    return response;
}