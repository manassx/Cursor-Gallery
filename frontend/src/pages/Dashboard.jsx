import {Plus, Image as ImageIcon} from 'lucide-react';
import {Link} from 'react-router-dom';
import {motion} from 'framer-motion';
import useAuthStore from '../store/authStore';
import {useTheme} from '../context/ThemeContext';
import useGalleryStore from '../store/galleryStore';
import {useState, useEffect} from 'react';

const Dashboard = () => {
    const {user} = useAuthStore();
    const {isDark, currentTheme} = useTheme();
    const {galleries, isLoading} = useGalleryStore();
    const [userGalleries, setUserGalleries] = useState([]);

    useEffect(() => {
        if (galleries) {
            setUserGalleries(galleries);
        }
    }, [galleries]);

    return (
        <div
            className="min-h-screen relative overflow-hidden transition-colors duration-500"
            style={{backgroundColor: currentTheme.bg, paddingTop: '64px'}}
        >
            {/* Animated noise scanline overlay */}
            <div
                className="fixed inset-0 pointer-events-none z-50 mix-blend-overlay transition-opacity duration-500"
                style={{
                    opacity: isDark ? 0.15 : 0.08,
                    background: `repeating-linear-gradient(
                        0deg,
                        ${isDark ? 'rgba(255, 255, 255, 0.03)' : 'rgba(0, 0, 0, 0.03)'} 0px,
                        ${isDark ? 'rgba(255, 255, 255, 0.03)' : 'rgba(0, 0, 0, 0.03)'} 1px,
                        transparent 1px,
                        transparent 2px
                    )`,
                    animation: 'scanlines 8s linear infinite',
                    backgroundSize: '100% 4px',
                }}
            />

            {/* Grain texture overlay */}
            <div
                className="fixed inset-0 pointer-events-none z-40 transition-opacity duration-500"
                style={{
                    opacity: isDark ? 0.08 : 0.05,
                    backgroundImage: `url("data:image/svg+xml,%3Csvg viewBox='0 0 400 400' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulance type='fractalNoise' baseFrequency='2.5' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)'/%3E%3C/svg%3E")`,
                    backgroundRepeat: 'repeat',
                }}
            />

            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 md:py-8 relative z-10">
                {/* Header */}
                <motion.div
                    className="mb-8 md:mb-12"
                    initial={{opacity: 0, y: -20}}
                    animate={{opacity: 1, y: 0}}
                    transition={{duration: 0.6}}
                >
                    <h1
                        className="text-3xl md:text-4xl lg:text-5xl font-black tracking-tight transition-colors duration-500"
                        style={{fontFamily: 'Arial Black, sans-serif', color: currentTheme.text}}
                    >
                        Welcome back, {user?.name || 'User'}!
                    </h1>
                    <p
                        className="text-sm md:text-base lg:text-lg mt-3 md:mt-4 transition-colors duration-500"
                        style={{fontFamily: 'Georgia, serif', color: currentTheme.textMuted}}
                    >
                        Create and manage your interactive photo galleries
                    </p>
                </motion.div>

                {/* Quick stats */}
                <motion.div
                    className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4 md:gap-6 mb-8 md:mb-12"
                    initial={{opacity: 0, y: 20}}
                    animate={{opacity: 1, y: 0}}
                    transition={{duration: 0.6, delay: 0.2}}
                >
                    {[
                        {label: 'Total Galleries', value: userGalleries.length, icon: ImageIcon},
                        {label: 'Total Images', value: '0', icon: ImageIcon},
                        {label: 'Total Views', value: '0', icon: ImageIcon}
                    ].map((stat, idx) => (
                        <motion.div
                            key={idx}
                            className="p-4 md:p-6 border transition-all duration-300"
                            style={{
                                backgroundColor: currentTheme.bgAlt,
                                borderColor: currentTheme.border,
                            }}
                            whileHover={{
                                borderColor: currentTheme.accent,
                                y: -4
                            }}
                        >
                            <div className="flex items-center justify-between">
                                <div>
                                    <p
                                        className="text-xs md:text-sm mb-2 transition-colors duration-500"
                                        style={{color: currentTheme.textMuted}}
                                    >
                                        {stat.label}
                                    </p>
                                    <p
                                        className="text-3xl md:text-4xl font-black transition-colors duration-500"
                                        style={{color: currentTheme.text}}
                                    >
                                        {stat.value}
                                    </p>
                                </div>
                                <div
                                    className="p-2 md:p-3 rounded-lg transition-colors duration-300"
                                    style={{
                                        backgroundColor: isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.05)'
                                    }}
                                >
                                    <stat.icon
                                        className="w-5 h-5 md:w-6 md:h-6"
                                        style={{color: currentTheme.accent}}
                                    />
                                </div>
                            </div>
                        </motion.div>
                    ))}
                </motion.div>

                {/* Empty state */}
                <motion.div
                    className="p-8 md:p-12 lg:p-16 border text-center transition-all duration-500"
                    style={{
                        backgroundColor: currentTheme.bgAlt,
                        borderColor: currentTheme.border,
                    }}
                    initial={{opacity: 0, y: 20}}
                    animate={{opacity: 1, y: 0}}
                    transition={{duration: 0.6, delay: 0.4}}
                >
                    <div
                        className="w-16 h-16 md:w-20 md:h-20 mx-auto mb-4 md:mb-6 rounded-full flex items-center justify-center transition-colors duration-300"
                        style={{
                            backgroundColor: isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.05)'
                        }}
                    >
                        <ImageIcon
                            className="w-8 h-8 md:w-10 md:h-10"
                            style={{color: currentTheme.textDim}}
                        />
                    </div>
                    <h3
                        className="text-2xl md:text-3xl font-black mb-3 md:mb-4 transition-colors duration-500"
                        style={{color: currentTheme.text}}
                    >
                        No galleries yet
                    </h3>
                    <p
                        className="text-sm md:text-base mb-6 md:mb-8 max-w-md mx-auto px-4 transition-colors duration-500"
                        style={{fontFamily: 'Georgia, serif', color: currentTheme.textMuted}}
                    >
                        Create your first interactive gallery to get started. Transform your photos into a dynamic,
                        cursor-driven experience.
                    </p>
                    <Link
                        to="/create"
                        className="inline-flex items-center gap-2 md:gap-3 px-6 md:px-8 py-3 md:py-4 font-bold text-xs md:text-sm tracking-wide transition-all duration-300"
                        style={{
                            backgroundColor: currentTheme.accent,
                            color: isDark ? '#0a0a0a' : '#f5f3ef'
                        }}
                        onMouseEnter={(e) => {
                            e.target.style.backgroundColor = currentTheme.accentHover;
                            e.target.style.transform = 'scale(1.05)';
                        }}
                        onMouseLeave={(e) => {
                            e.target.style.backgroundColor = currentTheme.accent;
                            e.target.style.transform = 'scale(1)';
                        }}
                    >
                        <Plus size={window.innerWidth < 768 ? 16 : 20}/>
                        <span>Create Your First Gallery</span>
                    </Link>
                </motion.div>
            </div>

            <style jsx>{`
                @keyframes scanlines {
                    0% {
                        background-position: 0 0;
                    }
                    100% {
                        background-position: 0 100%;
                    }
                }

                /* Text selection styling */
                ::selection {
                    background-color: ${isDark ? '#e8e8e8' : '#2a2520'};
                    color: ${isDark ? '#0a0a0a' : '#f5f3ef'};
                }

                ::-moz-selection {
                    background-color: ${isDark ? '#e8e8e8' : '#2a2520'};
                    color: ${isDark ? '#0a0a0a' : '#f5f3ef'};
                }
            `}</style>
        </div>
    );
};

export default Dashboard;