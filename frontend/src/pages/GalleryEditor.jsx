import {useState, useEffect} from 'react';
import {useParams, useNavigate, Link} from 'react-router-dom';
import {ArrowLeft, X} from 'lucide-react';
import CursorTrailGallery from '../components/gallery/CursorTrailGallery';
import {useTheme} from '../context/ThemeContext';

const GalleryEditor = () => {
    const {id} = useParams();
    const navigate = useNavigate();
    const {currentTheme, isDark} = useTheme();
    const [gallery, setGallery] = useState(null);

    // Load gallery data (simulated for now)
    useEffect(() => {
        loadGallery();
    }, [id]);

    const loadGallery = async () => {
        // Simulate API call
        await new Promise(resolve => setTimeout(resolve, 500));

        // Mock gallery data
        const mockGallery = {
            id: id,
            name: 'My Awesome Gallery',
            images: generateMockImages(28),
        };

        setGallery(mockGallery);
    };

    const generateMockImages = (count) => {
        // List of actual images from public/images folder
        const imageFiles = [
            'IMG-20241017-WA0006.jpg',
            'IMG-20250224-WA0001.jpg',
            'IMG-20250224-WA0004.jpg',
            'IMG-20250628-WA0009.jpg',
            'IMG-20250628-WA0012.jpg',
            'IMG-20250628-WA0014.jpg',
            'IMG-20250628-WA0018.jpg',
            'IMG-20250628-WA0019.jpg',
            'IMG-20250628-WA0002.jpg',
            'IMG-20250224-WA0006.jpg',
            'IMG-20250628-WA0021.jpg',
            'IMG-20250628-WA0022.jpg',
            'IMG-20250628-WA0023.jpg',
            'IMG-20250628-WA0024.jpg',
            'IMG-20250628-WA0025.jpg',
            'IMG-20250628-WA0026.jpg',
            'IMG-20250628-WA0027.jpg',
            'IMG-20250628-WA0029.jpg',
            'IMG-20250628-WA0032.jpg',
            'IMG-20250628-WA0033.jpg',
            'IMG-20250628-WA0036.jpg',
            'IMG-20250628-WA0037.jpg',
            'IMG-20250628-WA0039.jpg',
            'IMG-20250628-WA0040.jpg',
            'IMG-20250628-WA0041.jpg',
            'IMG-20250628-WA0042.jpg',
            'IMG-20250628-WA0044.jpg',
            'IMG_20240713_035003.jpg',
        ];

        // Use all available images or limit to count
        const imagesToUse = imageFiles.slice(0, Math.min(count, imageFiles.length));

        return imagesToUse.map((filename, i) => ({
            id: i + 1,
            url: `/images/${filename}`,
            thumbnail: `/images/${filename}`,
            title: `Photo ${i + 1}`,
        }));
    };

    if (!gallery) {
        return (
            <div style={{
                minHeight: '100vh',
                backgroundColor: currentTheme.bg,
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: currentTheme.text,
                fontFamily: '"Inter", sans-serif'
            }}>
                <p>Loading gallery...</p>
            </div>
        );
    }

    return (
        <div style={{
            minHeight: '100vh',
            backgroundColor: currentTheme.bg,
            color: currentTheme.text,
            fontFamily: '"Inter", sans-serif',
            userSelect: 'none',
            position: 'relative'
        }}>
            {/* Floating Exit Button */}
            <button
                onClick={() => navigate('/dashboard')}
                className="fixed top-6 left-6 z-50 flex items-center gap-2 px-4 py-2 rounded-lg font-bold text-xs tracking-wide transition-all duration-300 opacity-70 hover:opacity-100"
                style={{
                    backgroundColor: currentTheme.bgAlt,
                    color: currentTheme.text,
                    border: `1px solid ${currentTheme.border}`
                }}
                onMouseEnter={(e) => {
                    e.target.style.backgroundColor = currentTheme.bg;
                    e.target.style.borderColor = currentTheme.accent;
                }}
                onMouseLeave={(e) => {
                    e.target.style.backgroundColor = currentTheme.bgAlt;
                    e.target.style.borderColor = currentTheme.border;
                }}
            >
                <ArrowLeft size={16}/>
                <span>EXIT</span>
            </button>

            <CursorTrailGallery
                images={gallery.images}
                threshold={80}
                showControls={true}
                theme={{
                    controlsBg: currentTheme.bgAlt,
                    controlsText: currentTheme.text
                }}
            />

            {/* Mobile message */}
            <div style={{
                display: 'none',
                position: 'fixed',
                top: 0,
                left: 0,
                width: '100%',
                height: '100%',
                backgroundColor: currentTheme.bg,
                color: currentTheme.text,
                zIndex: 1000,
                flexDirection: 'column',
                justifyContent: 'center',
                alignItems: 'center',
                textAlign: 'center',
                padding: '2rem'
            }}
                 className="mobile-message">
                <p>This experience is optimized for desktop.</p>
            </div>
        </div>
    );
};

export default GalleryEditor;