import {BrowserRouter as Router, Routes, Route, Navigate, useLocation} from 'react-router-dom';
import {Toaster} from 'react-hot-toast';
import useAuthStore from './store/authStore';
import {ThemeProvider} from './context/ThemeContext';

// Import components (we'll create these next)
import Navbar from './components/layout/Navbar';
import ProtectedRoute from './components/auth/ProtectedRoute';
import LandingPage from './pages/LandingPage';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import Dashboard from './pages/Dashboard';
import GalleryEditor from './pages/GalleryEditor';
import PublicGallery from './pages/PublicGallery';
import CreateGallery from './pages/CreateGallery';

function AppContent() {
    const {isAuthenticated} = useAuthStore();
    const location = useLocation();
    const isLandingPage = location.pathname === '/';
    const isAuthPage = location.pathname === '/login' || location.pathname === '/signup';
    const isGalleryPage = location.pathname.includes('/gallery/') || location.pathname.startsWith('/g/');
    const isPublicGalleryPage = location.pathname.match(/^\/[^\/]+\/[^\/]+$/); // username/gallerySlug pattern
    const isCreatePage = location.pathname === '/create';

    // Show navbar only on dashboard
    const showNavbar = !isLandingPage && !isAuthPage && !isGalleryPage && !isPublicGalleryPage && !isCreatePage;

    return (
        <div className="min-h-screen bg-[#1a1a1a]">
            {/* Show navbar only on specific pages */}
            {showNavbar && <Navbar/>}

            <Routes>
                {/* Public Routes */}
                <Route path="/" element={<LandingPage/>}/>

                {/* Auth Routes - redirect to dashboard if already logged in */}
                <Route path="/login" element={
                    isAuthenticated ? <Navigate to="/dashboard" replace/> : <LoginPage/>
                }/>
                <Route path="/signup" element={
                    isAuthenticated ? <Navigate to="/dashboard" replace/> : <SignupPage/>
                }/>

                {/* Public Gallery View - Anyone can view shared galleries */}
                <Route path="/gallery/:id" element={<PublicGallery/>}/>
                <Route path="/g/:id" element={<PublicGallery/>}/>
                <Route path="/:username/:gallerySlug" element={<PublicGallery/>}/>

                {/* Protected Routes - Require Authentication */}
                <Route path="/dashboard" element={
                    <ProtectedRoute>
                        <Dashboard/>
                    </ProtectedRoute>
                }/>
                <Route path="/create" element={
                    <ProtectedRoute>
                        <CreateGallery/>
                    </ProtectedRoute>
                }/>
                <Route path="/gallery/:id/edit" element={
                    <ProtectedRoute>
                        <GalleryEditor/>
                    </ProtectedRoute>
                }/>

                {/* Catch all - redirect to home */}
                <Route path="*" element={<Navigate to="/" replace/>}/>
            </Routes>

            {/* Toast notifications */}
            <Toaster
                position="top-right"
                toastOptions={{
                    duration: 4000,
                    style: {
                        background: '#242424',
                        color: '#e8e8e8',
                        border: '1px solid #2a2a2a',
                    },
                    success: {
                        style: {
                            background: '#1a1a1a',
                            border: '1px solid #4a4a4a',
                        },
                    },
                    error: {
                        style: {
                            background: '#2a1a1a',
                            border: '1px solid #4a2a2a',
                        },
                    },
                }}
            />
        </div>
    );
}

function App() {
    return (
        <Router>
            <ThemeProvider>
                <AppContent/>
            </ThemeProvider>
        </Router>
    );
}

export default App;
