// Keep-alive utility to prevent backend cold starts
const BACKEND_URL = import.meta.env.VITE_API_URL || 'http://localhost:8000';
const PING_INTERVAL = 10 * 60 * 1000; // 10 minutes
const INACTIVITY_THRESHOLD = 30 * 60 * 1000; // 30 minutes

class KeepAlive {
    constructor() {
        this.lastActivity = Date.now();
        this.pingInterval = null;
        this.isActive = true;

        this.setupActivityListeners();
        this.startPinging();
    }

    setupActivityListeners() {
        // Track user activity
        const activities = ['mousedown', 'mousemove', 'keypress', 'scroll', 'touchstart', 'click'];

        const updateActivity = () => {
            this.lastActivity = Date.now();
            if (!this.isActive) {
                this.isActive = true;
                this.startPinging();
            }
        };

        activities.forEach(activity => {
            document.addEventListener(activity, updateActivity, {passive: true});
        });

        // Check for inactivity
        setInterval(() => {
            const inactive = Date.now() - this.lastActivity > INACTIVITY_THRESHOLD;
            if (inactive && this.isActive) {
                this.isActive = false;
                this.stopPinging();
            }
        }, 60000); // Check every minute
    }

    async pingBackend() {
        try {
            const response = await fetch(`${BACKEND_URL}/health`, {
                method: 'GET',
                cache: 'no-cache'
            });

            if (response.ok) {
                console.log('✅ Backend keep-alive ping successful');
            }
        } catch (error) {
            console.log('⚠️ Backend keep-alive ping failed (normal during cold starts)');
        }
    }

    startPinging() {
        if (this.pingInterval) return;

        // Ping immediately
        this.pingBackend();

        // Then ping every 10 minutes
        this.pingInterval = setInterval(() => {
            if (this.isActive) {
                this.pingBackend();
            }
        }, PING_INTERVAL);
    }

    stopPinging() {
        if (this.pingInterval) {
            clearInterval(this.pingInterval);
            this.pingInterval = null;
        }
    }
}

// Auto-start keep-alive when imported
let keepAliveInstance = null;

export const initKeepAlive = () => {
    if (!keepAliveInstance && typeof window !== 'undefined') {
        keepAliveInstance = new KeepAlive();
    }
    return keepAliveInstance;
};

export default initKeepAlive;