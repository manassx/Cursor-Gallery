import {createContext, useContext, useState, useEffect} from 'react';

const ThemeContext = createContext();

export const useTheme = () => {
    const context = useContext(ThemeContext);
    if (!context) {
        throw new Error('useTheme must be used within ThemeProvider');
    }
    return context;
};

export const ThemeProvider = ({children}) => {
    const [isDark, setIsDark] = useState(() => {
        const savedTheme = localStorage.getItem('theme');
        return savedTheme ? savedTheme === 'dark' : true;
    });

    useEffect(() => {
        localStorage.setItem('theme', isDark ? 'dark' : 'light');
    }, [isDark]);

    const theme = {
        dark: {
            bg: '#0a0a0a',
            bgAlt: '#1a1a1a',
            text: '#e8e8e8',
            textMuted: '#a8a8a8',
            textDim: '#666',
            border: '#1a1a1a',
            borderAlt: '#2a2a2a',
            accent: '#e8e8e8',
            accentHover: '#ffffff',
            navBg: 'rgba(10, 10, 10, 0.8)',
            navBgTransparent: 'rgba(10, 10, 10, 0.4)',
            cursor: '#e8e8e8',
            overlayText: '#2a2a2a',
            overlayTextDim: '#3a3a3a',
            controlsBg: 'rgba(255, 255, 255, 0.1)',
            controlsText: '#e8e8e8',
        },
        light: {
            bg: '#f5f3ef',
            bgAlt: '#e8e3d8',
            text: '#2a2520',
            textMuted: '#5a5248',
            textDim: '#8a7f70',
            border: '#d8d0c0',
            borderAlt: '#c4b8a0',
            accent: '#2a2520',
            accentHover: '#3a3530',
            navBg: 'rgba(245, 243, 239, 0.95)',
            navBgTransparent: 'rgba(245, 243, 239, 0.7)',
            cursor: '#2a2520',
            overlayText: '#d8d0c0',
            overlayTextDim: '#c4b8a0',
            controlsBg: 'rgba(0, 0, 0, 0.1)',
            controlsText: '#2a2520',
        }
    };

    const currentTheme = isDark ? theme.dark : theme.light;

    return (
        <ThemeContext.Provider value={{isDark, setIsDark, currentTheme, theme}}>
            {children}
        </ThemeContext.Provider>
    );
};
