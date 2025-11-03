import {useEffect, useState} from 'react';

/**
 * CustomCursor - Custom cursor that follows the mouse
 * 30px circle with semi-transparent border
 */
const CustomCursor = () => {
    const [position, setPosition] = useState({x: 0, y: 0});

    useEffect(() => {
        const handleMouseMove = (e) => {
            setPosition({x: e.clientX, y: e.clientY});
        };

        window.addEventListener('mousemove', handleMouseMove);
        return () => window.removeEventListener('mousemove', handleMouseMove);
    }, []);

    return (
        <div
            className="fixed top-0 left-0 pointer-events-none z-[9999]"
            style={{
                width: '30px',
                height: '30px',
                border: '2px solid rgba(255, 255, 255, 0.5)',
                borderRadius: '50%',
                transform: `translate3d(${position.x - 15}px, ${position.y - 15}px, 0)`,
                transition: 'transform 0.1s ease-out',
            }}
        />
    );
};

export default CustomCursor;
