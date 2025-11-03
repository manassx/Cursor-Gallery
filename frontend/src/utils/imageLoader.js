/**
 * Image Loader Utility
 * Manages loading images from the public/images folder
 */

// List of all available images in public/images folder
export const availableImages = [
    'IMG-20241017-WA0006.jpg',
    'IMG-20250224-WA0001.jpg',
    'IMG-20250224-WA0004.jpg',
    'IMG-20250224-WA0006.jpg',
    'IMG-20250628-WA0002.jpg',
    'IMG-20250628-WA0009.jpg',
    'IMG-20250628-WA0012.jpg',
    'IMG-20250628-WA0014.jpg',
    'IMG-20250628-WA0018.jpg',
    'IMG-20250628-WA0019.jpg',
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

/**
 * Get image URL from public folder
 * @param {string} filename - The image filename
 * @returns {string} Full image URL
 */
export const getImageUrl = (filename) => {
    return `/images/${filename}`;
};

/**
 * Generate mock gallery images from local files
 * @param {number} count - Number of images to return (max: availableImages.length)
 * @returns {Array} Array of image objects
 */
export const generateGalleryImages = (count = availableImages.length) => {
    const imagesToUse = availableImages.slice(0, Math.min(count, availableImages.length));

    return imagesToUse.map((filename, i) => ({
        id: i + 1,
        url: getImageUrl(filename),
        thumbnail: getImageUrl(filename),
        title: `Photo ${i + 1}`,
        filename: filename,
        mood: ['joyful', 'calm', 'energetic', 'dramatic', 'peaceful'][i % 5],
    }));
};

/**
 * Get all available images
 * @returns {Array} Array of all image objects
 */
export const getAllImages = () => {
    return generateGalleryImages(availableImages.length);
};

/**
 * Get random selection of images
 * @param {number} count - Number of random images to return
 * @returns {Array} Array of random image objects
 */
export const getRandomImages = (count) => {
    const shuffled = [...availableImages].sort(() => 0.5 - Math.random());
    const selected = shuffled.slice(0, Math.min(count, availableImages.length));

    return selected.map((filename, i) => ({
        id: i + 1,
        url: getImageUrl(filename),
        thumbnail: getImageUrl(filename),
        title: `Photo ${i + 1}`,
        filename: filename,
        mood: ['joyful', 'calm', 'energetic', 'dramatic', 'peaceful'][i % 5],
    }));
};

export default {
    availableImages,
    getImageUrl,
    generateGalleryImages,
    getAllImages,
    getRandomImages,
};