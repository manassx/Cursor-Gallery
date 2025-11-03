import {useEffect, useRef, useState} from 'react';
import {motion} from 'framer-motion';

/**
 * CursorGallery - Interactive cursor-following photo gallery
 * Photos react to cursor proximity with configurable behavior
 */
const CursorGallery = ({
                           images = [],
                           config = {},
                           onImageClick = null,
                           fullscreen = false
                       }) => {
    const containerRef = useRef(null);
    const [cursorPos, setCursorPos] = useState({x: 0, y: 0});
    const [imageElements, setImageElements] = useState([]);
    const [hoveredImage, setHoveredImage] = useState(null);

    // Default configuration
    const defaultConfig = {
        threshold: 200,           // Distance threshold in pixels
        sensitivity: 0.3,         // Movement sensitivity (0-1)
        rotationIntensity: 15,    // Max rotation in degrees
        scaleIntensity: 1.2,      // Scale factor on hover
        colorOverlay: '#0ea5e9',  // Overlay color
        animationStyle: 'smooth', // smooth, bouncy, instant
        spacing: 20,              // Gap between images
        columns: 4,               // Grid columns (responsive)
    };

    const settings = {...defaultConfig, ...config};

    // Calculate distance between cursor and element
    const getDistance = (x1, y1, x2, y2) => {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    };

    // Calculate image transformations based on cursor position
    const calculateTransform = (imageRect, cursorX, cursorY) => {
        const centerX = imageRect.left + imageRect.width / 2;
        const centerY = imageRect.top + imageRect.height / 2;

        const distance = getDistance(cursorX, cursorY, centerX, centerY);

        // If cursor is beyond threshold, no effect
        if (distance > settings.threshold) {
            return {
                x: 0,
                y: 0,
                rotation: 0,
                scale: 1,
                active: false
            };
        }

        // Calculate effect intensity (stronger when closer)
        const intensity = 1 - (distance / settings.threshold);

        // Calculate direction vector from image center to cursor
        const deltaX = cursorX - centerX;
        const deltaY = cursorY - centerY;

        // Apply movement
        const moveX = deltaX * intensity * settings.sensitivity * 0.3;
        const moveY = deltaY * intensity * settings.sensitivity * 0.3;

        // Apply rotation based on cursor position relative to center
        const rotation = (deltaX / imageRect.width) * settings.rotationIntensity * intensity;

        // Apply scale
        const scale = 1 + (intensity * (settings.scaleIntensity - 1));

        return {
            x: moveX,
            y: moveY,
            rotation,
            scale,
            active: true
        };
    };

    // Update cursor position
    useEffect(() => {
        const handleMouseMove = (e) => {
            setCursorPos({x: e.clientX, y: e.clientY});
        };

        window.addEventListener('mousemove', handleMouseMove);
        return () => window.removeEventListener('mousemove', handleMouseMove);
    }, []);

    // Animation spring configuration based on style
    const getSpringConfig = () => {
        switch (settings.animationStyle) {
            case 'bouncy':
                return {type: 'spring', stiffness: 300, damping: 15};
            case 'instant':
                return {type: 'spring', stiffness: 500, damping: 30};
            case 'smooth':
            default:
                return {type: 'spring', stiffness: 150, damping: 20};
        }
    };

    // Responsive grid columns
    const getGridColumns = () => {
        if (fullscreen) {
            return 'grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5';
        }
        return `grid-cols-2 md:grid-cols-3 lg:grid-cols-${Math.min(settings.columns, 5)}`;
    };

    return (
        <div
            ref={containerRef}
            className={`${fullscreen ? 'min-h-screen p-8' : 'w-full p-4'} bg-black`}
        >
            <div className={`grid ${getGridColumns()} gap-${Math.floor(settings.spacing / 4)}`}>
                {images.map((image, index) => (
                    <ImageCard
                        key={image.id || index}
                        image={image}
                        index={index}
                        cursorPos={cursorPos}
                        settings={settings}
                        calculateTransform={calculateTransform}
                        springConfig={getSpringConfig()}
                        onHover={setHoveredImage}
                        onClick={onImageClick}
                    />
                ))}
            </div>

            {/* Cursor indicator (optional, for demo) */}
            {settings.showCursor && (
                <motion.div
                    className="fixed w-4 h-4 rounded-full pointer-events-none z-50"
                    style={{
                        backgroundColor: settings.colorOverlay,
                        boxShadow: `0 0 20px ${settings.colorOverlay}`,
                        opacity: 0.5,
                    }}
                    animate={{
                        x: cursorPos.x - 8,
                        y: cursorPos.y - 8,
                    }}
                    transition={{type: 'spring', stiffness: 500, damping: 30}}
                />
            )}
        </div>
    );
};

/**
 * Individual image card with cursor interaction
 */
const ImageCard = ({
                       image,
                       index,
                       cursorPos,
                       settings,
                       calculateTransform,
                       springConfig,
                       onHover,
                       onClick
                   }) => {
    const imageRef = useRef(null);
    const [transform, setTransform] = useState({
        x: 0, y: 0, rotation: 0, scale: 1, active: false
    });

    useEffect(() => {
        if (!imageRef.current) return;

        const rect = imageRef.current.getBoundingClientRect();
        const newTransform = calculateTransform(rect, cursorPos.x, cursorPos.y);
        setTransform(newTransform);
    }, [cursorPos, calculateTransform]);

    return (
        <motion.div
            ref={imageRef}
            className="relative group cursor-pointer"
            initial={{opacity: 0, scale: 0.9}}
            animate={{
                opacity: 1,
                scale: transform.scale,
                x: transform.x,
                y: transform.y,
                rotateZ: transform.rotation,
            }}
            transition={springConfig}
            whileHover={{
                scale: settings.scaleIntensity,
                zIndex: 10,
            }}
            onHoverStart={() => onHover(index)}
            onHoverEnd={() => onHover(null)}
            onClick={() => onClick && onClick(image, index)}
            style={{
                transformOrigin: 'center center',
            }}
        >
            {/* Image container */}
            <div className="relative aspect-square overflow-hidden rounded-lg shadow-lg">
                <img
                    src={image.url || image.thumbnail || image}
                    alt={image.title || `Gallery image ${index + 1}`}
                    className="w-full h-full object-cover"
                    loading="lazy"
                />

                {/* Color overlay on hover */}
                <motion.div
                    className="absolute inset-0 pointer-events-none"
                    initial={{opacity: 0}}
                    animate={{
                        opacity: transform.active ? 0.2 : 0,
                    }}
                    style={{
                        backgroundColor: settings.colorOverlay,
                        mixBlendMode: 'multiply',
                    }}
                />

                {/* Hover info overlay */}
                {image.title && (
                    <motion.div
                        className="absolute inset-0 bg-gradient-to-t from-black/80 to-transparent flex items-end p-4"
                        initial={{opacity: 0}}
                        whileHover={{opacity: 1}}
                        transition={{duration: 0.2}}
                    >
                        <div className="text-white">
                            <h3 className="font-semibold text-sm">{image.title}</h3>
                            {image.mood && (
                                <p className="text-xs text-gray-300 capitalize">{image.mood}</p>
                            )}
                        </div>
                    </motion.div>
                )}
            </div>

            {/* Active indicator */}
            {transform.active && (
                <motion.div
                    className="absolute -inset-1 rounded-lg opacity-50 blur-sm -z-10"
                    style={{
                        backgroundColor: settings.colorOverlay,
                    }}
                    initial={{opacity: 0}}
                    animate={{opacity: 0.5}}
                />
            )}
        </motion.div>
    );
};

export default CursorGallery;