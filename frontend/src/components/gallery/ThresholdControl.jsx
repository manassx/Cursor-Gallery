import {useState} from 'react';
import {motion} from 'framer-motion';
import {Settings, ChevronDown, X, Sliders} from 'lucide-react';

/**
 * ThresholdControl - Simple control panel for cursor trail gallery
 * Adjusts threshold (distance before new image appears)
 */
const ThresholdControl = ({config, onChange}) => {
    const [isMinimized, setIsMinimized] = useState(false);

    const handleThresholdChange = (value) => {
        onChange({threshold: value});
    };

    return (
        <motion.div
            className={`fixed right-4 top-20 z-40 bg-white rounded-xl shadow-2xl border border-gray-200 overflow-hidden transition-all ${
                isMinimized ? 'w-16' : 'w-80'
            }`}
            initial={{x: 400, opacity: 0}}
            animate={{x: 0, opacity: 1}}
            transition={{type: 'spring', stiffness: 200, damping: 25}}
        >
            {/* Header */}
            <div className="bg-gradient-to-r from-blue-500 to-purple-600 p-4 flex items-center justify-between">
                {!isMinimized && (
                    <div className="flex items-center gap-2 text-white">
                        <Settings className="w-5 h-5"/>
                        <h3 className="font-semibold">Gallery Settings</h3>
                    </div>
                )}
                <button
                    onClick={() => setIsMinimized(!isMinimized)}
                    className="text-white hover:bg-white/20 rounded-lg p-1 transition-colors"
                >
                    {isMinimized ? <ChevronDown className="w-5 h-5"/> : <X className="w-5 h-5"/>}
                </button>
            </div>

            {/* Controls */}
            {!isMinimized && (
                <div className="p-6">
                    <div className="mb-6">
                        <div className="flex items-center gap-2 mb-4">
                            <Sliders className="w-4 h-4 text-gray-600"/>
                            <span className="font-medium text-gray-700">Cursor Sensitivity</span>
                        </div>

                        <div>
                            <div className="flex items-center justify-between mb-2">
                                <label className="text-sm font-medium text-gray-700">Threshold</label>
                                <span className="text-sm text-gray-600 font-mono">
                                    {config.threshold}px
                                </span>
                            </div>
                            <input
                                type="range"
                                min={20}
                                max={200}
                                step={1}
                                value={config.threshold}
                                onChange={(e) => handleThresholdChange(parseInt(e.target.value))}
                                className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer slider"
                            />
                            <p className="text-xs text-gray-500 mt-2">
                                Distance the cursor must move before a new image appears
                            </p>
                        </div>

                        {/* Quick presets */}
                        <div className="mt-4 space-y-2">
                            <p className="text-xs font-medium text-gray-600 mb-2">Quick Presets:</p>
                            <div className="grid grid-cols-3 gap-2">
                                <button
                                    onClick={() => handleThresholdChange(20)}
                                    className={`py-2 px-3 rounded-lg text-xs font-medium transition-colors ${
                                        config.threshold === 20
                                            ? 'bg-blue-500 text-white'
                                            : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                                    }`}
                                >
                                    Dense
                                </button>
                                <button
                                    onClick={() => handleThresholdChange(80)}
                                    className={`py-2 px-3 rounded-lg text-xs font-medium transition-colors ${
                                        config.threshold === 80
                                            ? 'bg-blue-500 text-white'
                                            : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                                    }`}
                                >
                                    Normal
                                </button>
                                <button
                                    onClick={() => handleThresholdChange(140)}
                                    className={`py-2 px-3 rounded-lg text-xs font-medium transition-colors ${
                                        config.threshold === 140
                                            ? 'bg-blue-500 text-white'
                                            : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                                    }`}
                                >
                                    Sparse
                                </button>
                            </div>
                        </div>
                    </div>

                    {/* Info */}
                    <div className="p-3 bg-blue-50 rounded-lg border border-blue-100">
                        <p className="text-xs text-blue-800">
                            💡 Lower values = more images appear. Higher values = fewer images.
                        </p>
                    </div>
                </div>
            )}
        </motion.div>
    );
};

export default ThresholdControl;
