import {Link, useNavigate} from 'react-router-dom';
import {User, LogOut, Settings, Plus} from 'lucide-react';
import useAuthStore from '../../store/authStore';

const Navbar = () => {
    const {isAuthenticated, user, logout} = useAuthStore();
    const navigate = useNavigate();

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    return (
        <nav className="bg-[#1a1a1a] border-b border-[#2a2a2a] fixed top-0 left-0 right-0 z-50">
            <div className="max-w-[1600px] mx-auto px-8 md:px-16">
                <div className="flex justify-between items-center h-16">
                    {/* Logo and brand */}
                    <div className="flex items-center">
                        <Link to="/" className="flex items-center group">
                            <span
                                className="text-sm tracking-[0.3em] text-[#e8e8e8] group-hover:text-white transition-colors font-light">
                                CURSOR GALLERY
                            </span>
                        </Link>
                    </div>

                    {/* Navigation items */}
                    <div className="flex items-center gap-8">
                        {isAuthenticated ? (
                            <>
                                {/* Dashboard link */}
                                <Link
                                    to="/dashboard"
                                    className="text-[#b8b8b8] hover:text-white text-xs tracking-[0.2em] transition-colors font-light"
                                >
                                    DASHBOARD
                                </Link>

                                {/* Create gallery button */}
                                <Link
                                    to="/create"
                                    className="px-6 py-2 bg-white text-black text-xs tracking-[0.2em] font-medium hover:bg-[#e8e8e8] transition-colors"
                                >
                                    CREATE
                                </Link>

                                {/* User menu */}
                                <div className="relative group">
                                    <button
                                        className="flex items-center gap-2 text-[#b8b8b8] hover:text-white transition-colors">
                                        <User size={16} strokeWidth={1.5}/>
                                        <span
                                            className="text-xs tracking-[0.2em] font-light">{user?.name || 'USER'}</span>
                                    </button>

                                    {/* Dropdown menu */}
                                    <div
                                        className="absolute right-0 mt-4 w-48 bg-[#1a1a1a] border border-[#2a2a2a] opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50 shadow-xl">
                                        <div className="py-2">
                                            <Link
                                                to="/settings"
                                                className="flex items-center gap-3 px-4 py-3 text-xs text-[#b8b8b8] hover:text-white hover:bg-[#242424] transition-colors tracking-[0.2em] font-light"
                                            >
                                                <Settings size={14} strokeWidth={1.5}/>
                                                <span>SETTINGS</span>
                                            </Link>
                                            <button
                                                onClick={handleLogout}
                                                className="flex items-center gap-3 w-full px-4 py-3 text-xs text-[#b8b8b8] hover:text-white hover:bg-[#242424] transition-colors tracking-[0.2em] font-light"
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
                                {/* Login and signup buttons */}
                                <Link
                                    to="/login"
                                    className="text-[#b8b8b8] hover:text-white text-xs tracking-[0.2em] transition-colors font-light"
                                >
                                    LOGIN
                                </Link>
                                <Link
                                    to="/signup"
                                    className="px-6 py-2 border border-[#444] text-[#e8e8e8] text-xs tracking-[0.2em] font-light hover:border-white transition-colors"
                                >
                                    GET STARTED
                                </Link>
                            </>
                        )}
                    </div>
                </div>
            </div>
        </nav>
    );
};

export default Navbar;