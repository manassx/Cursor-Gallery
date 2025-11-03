import {v4 as uuid} from "uuid";
import {useEffect, useRef, useState} from "react";
import {X, ChevronLeft, ChevronRight} from "lucide-react";
import styles from "./CursorTrailGallery.module.css";

function CursorTrailGallery({
                                images,
                                threshold: initialThreshold = 80,
                                showControls = true,
                                theme = null,
                                clearOnLeave = false
                            }) {
    const [nextImage, setNextImage] = useState(0);
    const [placedImages, setPlacedImages] = useState([]);
    const [threshold, setThreshold] = useState(initialThreshold);
    const [lightboxOpen, setLightboxOpen] = useState(false);
    const [lightboxIndex, setLightboxIndex] = useState(0);

    const lastPosition = useRef({x: 0, y: 0});
    const containerRef = useRef(null);

    // Default theme if none provided
    const defaultTheme = {
        controlsBg: 'transparent',
        controlsText: '#f0f0f0'
    };

    const currentTheme = theme || defaultTheme;

    function DecThreshold() {
        setThreshold((prev) => {
            let next = prev;
            if (prev > 140) {
                next = prev - 60;
            } else if (prev > 80) {
                next = prev - 40;
            } else if (prev > 40) {
                next = prev - 20;
            } else if (prev > 20) {
                next = prev - 20;
            }
            if (next < 20) next = 20;
            return next;
        });
    }

    function IncThreshold() {
        setThreshold((prev) => {
            let next = prev;
            if (prev < 40) {
                next = prev + 20;
            } else if (prev < 80) {
                next = prev + 40;
            } else if (prev < 140) {
                next = prev + 60;
            } else if (prev < 200) {
                next = prev + 60;
            }
            if (next > 200) next = 200;
            return next;
        });
    }

    function handleImageClick(imageSrc) {
        // Find the index of clicked image in the full images array
        const index = images.findIndex(img => (img.url || img.src) === imageSrc);
        if (index !== -1) {
            setLightboxIndex(index);
            setLightboxOpen(true);
        }
    }

    function handlePosition(e) {
        const rect = containerRef.current.getBoundingClientRect();
        const currentX = e.clientX - rect.left;
        const currentY = e.clientY - rect.top;
        const distanceX = Math.abs(currentX - lastPosition.current.x);
        const distanceY = Math.abs(currentY - lastPosition.current.y);

        if (distanceX > threshold || distanceY > threshold) {
            lastPosition.current = {x: currentX, y: currentY};
            const nextImageSrc = images[nextImage].url || images[nextImage].src;

            setPlacedImages((prevState) => {
                const newImage = {
                    id: uuid(),
                    src: nextImageSrc,
                    x: currentX,
                    y: currentY,
                };

                let maxLength;
                if (threshold <= 20) {
                    maxLength = 15;
                } else if (threshold <= 40) {
                    maxLength = 10;
                } else {
                    maxLength = 6;
                }
                const newArray = [...prevState, newImage];

                while (newArray.length > maxLength) {
                    newArray.shift();
                }
                return newArray;
            });
            setNextImage((prevState) => (prevState + 1) % images.length);
        }
    }

    function closeLightbox() {
        setLightboxOpen(false);
    }

    function nextLightboxImage() {
        setLightboxIndex((prev) => (prev + 1) % images.length);
    }

    function prevLightboxImage() {
        setLightboxIndex((prev) => (prev - 1 + images.length) % images.length);
    }

    // Keyboard navigation for lightbox
    useEffect(() => {
        if (!lightboxOpen) return;

        const handleKeyDown = (e) => {
            if (e.key === 'Escape') closeLightbox();
            if (e.key === 'ArrowRight') nextLightboxImage();
            if (e.key === 'ArrowLeft') prevLightboxImage();
        };

        window.addEventListener('keydown', handleKeyDown);
        return () => window.removeEventListener('keydown', handleKeyDown);
    }, [lightboxOpen, lightboxIndex]);

    useEffect(() => {
        const container = containerRef.current;

        const handleMouseLeave = () => {
            setPlacedImages([]);
            lastPosition.current = {x: 0, y: 0};
        };

        container.addEventListener("mousemove", handlePosition);
        if (clearOnLeave) {
            container.addEventListener("mouseleave", handleMouseLeave);
        }

        return function () {
            container.removeEventListener("mousemove", handlePosition);
            if (clearOnLeave) {
                container.removeEventListener("mouseleave", handleMouseLeave);
            }
        };
    }, [threshold, nextImage, clearOnLeave]);

    return (
        <div className={styles.container}>
            <div ref={containerRef} className={styles.galleryContainer}>
                {placedImages.map((image, index) => (
                    <img
                        key={image.id}
                        src={image.src}
                        alt=""
                        className={styles.placedImage}
                        style={{
                            zIndex: index,
                            transform: `translate(-50%, -50%) translate(${image.x}px, ${image.y}px)`,
                            cursor: 'pointer'
                        }}
                        onClick={() => handleImageClick(image.src)}
                    />
                ))}
            </div>

            {/* Lightbox */}
            {lightboxOpen && (
                <div className={styles.lightbox} onClick={closeLightbox}>
                    {/* Close button */}
                    <button
                        className={styles.lightboxClose}
                        onClick={closeLightbox}
                        aria-label="Close"
                    >
                        <X size={32}/>
                    </button>

                    {/* Previous button */}
                    <button
                        className={styles.lightboxPrev}
                        onClick={(e) => {
                            e.stopPropagation();
                            prevLightboxImage();
                        }}
                        aria-label="Previous"
                    >
                        <ChevronLeft size={48}/>
                    </button>

                    {/* Image */}
                    <div className={styles.lightboxContent} onClick={(e) => e.stopPropagation()}>
                        <img
                            src={images[lightboxIndex].url || images[lightboxIndex].src}
                            alt=""
                            className={styles.lightboxImage}
                        />
                    </div>

                    {/* Next button */}
                    <button
                        className={styles.lightboxNext}
                        onClick={(e) => {
                            e.stopPropagation();
                            nextLightboxImage();
                        }}
                        aria-label="Next"
                    >
                        <ChevronRight size={48}/>
                    </button>
                </div>
            )}

            {showControls && (
                <div className={styles.controls}
                     style={{backgroundColor: currentTheme.controlsBg, color: currentTheme.controlsText}}>
                    <a href="https://www.instagram.com/manas.sx/" target="_blank" rel="noopener noreferrer"
                       className={styles.label}
                       style={{color: currentTheme.controlsText}}>
                        manas.
                    </a>
                    <div className={styles.threshold}>
                        <span style={{color: currentTheme.controlsText}}>Threshold: </span>
                        <button onClick={DecThreshold} className={styles.adjustBtn}>
                            -
                        </button>
                        <span className={styles.value} style={{color: currentTheme.controlsText}}>{threshold}</span>
                        <button onClick={IncThreshold} className={styles.adjustBtn}>
                            +
                        </button>
                    </div>
                    <span className={styles.label}
                          style={{color: currentTheme.controlsText}}>saxenamanas04@gmail.com</span>
                </div>
            )}
        </div>
    );
}

export default CursorTrailGallery;
