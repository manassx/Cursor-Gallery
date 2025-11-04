import {Link, useNavigate, useLocation} from 'react-router-dom';
import {User, LogOut, Settings, Moon, Sun, Menu, X} from 'lucide-react';
import {motion} from 'framer-motion';
import {useState} from 'react';
import useAuthStore from '../../store/authStore';
import {useTheme} from '../../context/ThemeContext';

const Navbar = () => {
    const {isAuthenticated, user, logout} = useAuthStore();
    const {isDark, setIsDark, currentTheme} = useTheme();
    const navigate = useNavigate();
    const location = useLocation();
    const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

    const isDashboard = location.pathname === '/dashboard';

    const handleLogout = () => {
        logout();
        navigate('/');
        setMobileMenuOpen(false);
    };

    return (
        <motion.nav
            className="fixed top-0 left-0 right-0 z-50 px-4 md:px-6 lg:px-12 py-3 md:py-4"
            style={{cursor: 'auto'}}
            initial={{opacity: 0, y: -20}}
            animate={{opacity: 1, y: 0}}
            transition={{duration: 0.4}}
        >
            <div
                className="max-w-[1400px] mx-auto flex items-center justify-between px-4 md:px-6 py-3 border transition-all duration-300"
                style={{
                    background: currentTheme.navBg,
                    borderColor: currentTheme.borderAlt,
                    boxShadow: `0 8px 32px 0 ${isDark ? 'rgba(0, 0, 0, 0.5)' : 'rgba(0, 0, 0, 0.1)'}`,
                    backdropFilter: 'blur(10px)',
                }}
            >
                {/* Logo */}
                <Link to={isDashboard ? "/dashboard" : "/"} className="flex items-center gap-2 md:gap-3">
                    <div className="w-1.5 h-1.5 rounded-full transition-colors duration-300"
                         style={{backgroundColor: currentTheme.text}}></div>
                    <span
                        className="font-light text-xs md:text-sm tracking-[0.2em] md:tracking-[0.3em] transition-colors duration-300"
                          style={{color: currentTheme.text}}>
                        CURSOR GALLERY
                    </span>
                </Link>

                {/* Desktop Nav Links */}
                <div className="hidden md:flex items-center gap-4 lg:gap-8">
                    {isAuthenticated ? (
                        <>
                            {/* Only show these if NOT on dashboard */}
                            {!isDashboard && (
                                <Link
                                    to="/dashboard"
                                    className="text-xs tracking-[0.2em] transition-colors duration-300"
                                    style={{color: currentTheme.textDim}}
                                    onMouseEnter={(e) => e.target.style.color = currentTheme.text}
                                    onMouseLeave={(e) => e.target.style.color = currentTheme.textDim}
                                >
                                    DASHBOARD
                                </Link>
                            )}

                            {/* Theme Toggle */}
                            <button
                                onClick={() => setIsDark(!isDark)}
                                className="p-2 rounded-full transition-all duration-300 hover:scale-110"
                                style={{
                                    backgroundColor: isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)',
                                    color: currentTheme.text
                                }}
                                aria-label="Toggle theme"
                            >
                                {isDark ? <Sun className="w-4 h-4"/> : <Moon className="w-4 h-4"/>}
                            </button>

                            {/* Only show Create button if NOT on dashboard */}
                            {!isDashboard && (
                                <Link
                                    to="/create"
                                    className="px-4 lg:px-6 py-2 text-xs tracking-[0.2em] font-medium transition-all duration-300"
                                    style={{
                                        backgroundColor: currentTheme.accent,
                                        color: isDark ? '#0a0a0a' : '#f5f3ef'
                                    }}
                                    onMouseEnter={(e) => e.target.style.backgroundColor = currentTheme.accentHover}
                                    onMouseLeave={(e) => e.target.style.backgroundColor = currentTheme.accent}
                                >
                                    CREATE
                                </Link>
                            )}

                            {/* User menu */}
                            <div className="relative group">
                                <button
                                    className="flex items-center gap-2 transition-colors duration-300"
                                    style={{color: currentTheme.textDim}}
                                    onMouseEnter={(e) => e.target.style.color = currentTheme.text}
                                    onMouseLeave={(e) => e.target.style.color = currentTheme.textDim}
                                >
                                    <User size={16} strokeWidth={1.5}/>
                                    <span className="text-xs tracking-[0.2em] font-light">
                                        {user?.name || 'USER'}
                                    </span>
                                </button>

                                {/* Dropdown menu */}
                                <div
                                    className="absolute right-0 mt-4 w-48 border opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50 shadow-xl"
                                    style={{
                                        backgroundColor: currentTheme.bgAlt,
                                        borderColor: currentTheme.border,
                                        backdropFilter: 'blur(10px)',
                                    }}
                                >
                                    <div className="py-2">
                                        <Link
                                            to="/settings"
                                            className="flex items-center gap-3 px-4 py-3 text-xs tracking-[0.2em] font-light transition-colors duration-300"
                                            style={{color: currentTheme.textDim}}
                                            onMouseEnter={(e) => {
                                                e.target.style.color = currentTheme.text;
                                                e.target.style.backgroundColor = isDark ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.05)';
                                            }}
                                            onMouseLeave={(e) => {
                                                e.target.style.color = currentTheme.textDim;
                                                e.target.style.backgroundColor = 'transparent';
                                            }}
                                        >
                                            <Settings size={14} strokeWidth={1.5}/>
                                            <span>SETTINGS</span>
                                        </Link>
                                        <button
                                            onClick={handleLogout}
                                            className="flex items-center gap-3 w-full px-4 py-3 text-xs tracking-[0.2em] font-light transition-colors duration-300"
                                            style={{color: currentTheme.textDim}}
                                            onMouseEnter={(e) => {
                                                e.target.style.color = currentTheme.text;
                                                e.target.style.backgroundColor = isDark ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.05)';
                                            }}
                                            onMouseLeave={(e) => {
                                                e.target.style.color = currentTheme.textDim;
                                                e.target.style.backgroundColor = 'transparent';
                                            }}
                                        >
                                            <LogOut size={14} strokeWidth={1.5}/>
                                            <span>LOGOUT</span>
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </>
                    ) : (
                        <>
                            <Link
                                to="/login"
                                className="text-xs tracking-[0.2em] transition-colors duration-300"
                                style={{color: currentTheme.textDim}}
                                onMouseEnter={(e) => e.target.style.color = currentTheme.text}
                                onMouseLeave={(e) => e.target.style.color = currentTheme.textDim}
                            >
                                LOGIN
                            </Link>

                            {/* Theme Toggle */}
                            <button
                                onClick={() => setIsDark(!isDark)}
                                className="p-2 rounded-full transition-all duration-300 hover:scale-110"
                                style={{
                                    backgroundColor: isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)',
                                    color: currentTheme.text
                                }}
                                aria-label="Toggle theme"
                            >
                                {isDark ? <Sun className="w-4 h-4"/> : <Moon className="w-4 h-4"/>}
                            </button>

                            <Link
                                to="/signup"
                                className="px-4 lg:px-6 py-2 text-xs tracking-[0.2em] font-medium transition-all duration-300"
                                style={{
                                    backgroundColor: currentTheme.accent,
                                    color: isDark ? '#0a0a0a' : '#f5f3ef'
                                }}
                                onMouseEnter={(e) => e.target.style.backgroundColor = currentTheme.accentHover}
                                onMouseLeave={(e) => e.target.style.backgroundColor = currentTheme.accent}
                            >
                                GET STARTED
                            </Link>
                        </>
                    )}
                </div>

                {/* Mobile Menu Button */}
                <div className="flex md:hidden items-center gap-3">
                    <button
                        onClick={() => setIsDark(!isDark)}
                        className="p-2 rounded-full transition-all duration-300"
                        style={{
                            backgroundColor: isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)',
                            color: currentTheme.text
                        }}
                        aria-label="Toggle theme"
                    >
                        {isDark ? <Sun className="w-4 h-4"/> : <Moon className="w-4 h-4"/>}
                    </button>
                    <button
                        onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
                        className="p-2"
                        style={{color: currentTheme.text}}
                        aria-label="Toggle menu"
                    >
                        {mobileMenuOpen ? <X className="w-5 h-5"/> : <Menu className="w-5 h-5"/>}
                    </button>
                </div>
            </div>

            {/* Mobile Menu */}
            {mobileMenuOpen && (
                <motion.div
                    initial={{opacity: 0, y: -20}}
                    animate={{opacity: 1, y: 0}}
                    exit={{opacity: 0, y: -20}}
                    className="md:hidden mt-2 mx-4 border transition-all duration-300"
                    style={{
                        background: currentTheme.navBg,
                        borderColor: currentTheme.borderAlt,
                        backdropFilter: 'blur(10px)',
                    }}
                >
                    <div className="flex flex-col p-4 gap-4">
                        {isAuthenticated ? (
                            <>
                                {!isDashboard && (
                                    <Link
                                        to="/dashboard"
                                        className="text-xs tracking-[0.2em] py-2 transition-colors duration-300"
                                        style={{color: currentTheme.textDim}}
                                        onClick={() => setMobileMenuOpen(false)}
                                    >
                                        DASHBOARD
                                    </Link>
                                )}
                                {!isDashboard && (
                                    <Link
                                        to="/create"
                                        className="px-4 py-3 text-xs tracking-[0.2em] font-medium transition-all duration-300 text-center"
                                        style={{
                                            backgroundColor: currentTheme.accent,
                                            color: isDark ? '#0a0a0a' : '#f5f3ef'
                                        }}
                                        onClick={() => setMobileMenuOpen(false)}
                                    >
                                        CREATE
                                    </Link>
                                )}
                                <Link
                                    to="/settings"
                                    className="text-xs tracking-[0.2em] py-2 transition-colors duration-300"
                                    style={{color: currentTheme.textDim}}
                                    onClick={() => setMobileMenuOpen(false)}
                                >
                                    SETTINGS
                                </Link>
                                <button
                                    onClick={handleLogout}
                                    className="text-xs tracking-[0.2em] py-2 text-left transition-colors duration-300"
                                    style={{color: currentTheme.textDim}}
                                >
                                    LOGOUT
                                </button>
                            </>
                        ) : (
                            <>
                                <Link
                                    to="/login"
                                    className="text-xs tracking-[0.2em] py-2 transition-colors duration-300"
                                    style={{color: currentTheme.textDim}}
                                    onClick={() => setMobileMenuOpen(false)}
                                >
                                    LOGIN
                                </Link>
                                <Link
                                    to="/signup"
                                    className="px-4 py-3 text-xs tracking-[0.2em] font-medium transition-all duration-300 text-center"
                                    style={{
                                        backgroundColor: currentTheme.accent,
                                        color: isDark ? '#0a0a0a' : '#f5f3ef'
                                    }}
                                    onClick={() => setMobileMenuOpen(false)}
                                >
                                    GET STARTED
                                </Link>
                            </>
                        )}
                    </div>
                </motion.div>
            )}
        </motion.nav>
    );
};

export default Navbar;