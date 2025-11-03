import {useState} from 'react';
import {motion, AnimatePresence} from 'framer-motion';
import {
    Settings,
    Palette,
    Sparkles,
    Sliders,
    Eye,
    RotateCw,
    Move,
    ChevronDown,
    ChevronUp,
    X
} from 'lucide-react';

/**
 * GalleryControls - Customization panel for cursor gallery
 * Real-time adjustment of gallery behavior and appearance
 */
const GalleryControls = ({config, onChange, aiSuggestions = null}) => {
    const [activeSection, setActiveSection] = useState('sensitivity');
    const [isMinimized, setIsMinimized] = useState(false);

    const toggleSection = (section) => {
        setActiveSection(activeSection === section ? null : section);
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
                <div className="max-h-[calc(100vh-200px)] overflow-y-auto">
                    {/* AI Suggestions Banner */}
                    {aiSuggestions && (
                        <div className="p-4 bg-gradient-to-r from-purple-50 to-blue-50 border-b border-purple-100">
                            <div className="flex items-start gap-2">
                                <Sparkles className="w-5 h-5 text-purple-600 flex-shrink-0 mt-0.5"/>
                                <div>
                                    <p className="text-sm font-semibold text-purple-900">AI Optimized</p>
                                    <p className="text-xs text-purple-700 mt-1">
                                        Settings tuned based on mood analysis
                                    </p>
                                    {aiSuggestions.mood && (
                                        <div
                                            className="mt-2 inline-block px-2 py-1 bg-purple-200 text-purple-800 rounded text-xs font-medium capitalize">
                                            {aiSuggestions.mood} Mood
                                        </div>
                                    )}
                                </div>
                            </div>
                        </div>
                    )}

                    {/* Sensitivity Controls */}
                    <ControlSection
                        title="Interaction"
                        icon={<Sliders className="w-4 h-4"/>}
                        isActive={activeSection === 'sensitivity'}
                        onToggle={() => toggleSection('sensitivity')}
                    >
                        <SliderControl
                            label="Threshold"
                            value={config.threshold}
                            onChange={(val) => onChange({...config, threshold: val})}
                            min={50}
                            max={500}
                            step={10}
                            unit="px"
                            description="Distance where images start reacting"
                        />
                        <SliderControl
                            label="Sensitivity"
                            value={config.sensitivity}
                            onChange={(val) => onChange({...config, sensitivity: val})}
                            min={0}
                            max={1}
                            step={0.05}
                            description="How much images move towards cursor"
                        />
                        <SliderControl
                            label="Rotation"
                            value={config.rotationIntensity}
                            onChange={(val) => onChange({...config, rotationIntensity: val})}
                            min={0}
                            max={30}
                            step={1}
                            unit="°"
                            description="Maximum rotation angle"
                        />
                        <SliderControl
                            label="Scale"
                            value={config.scaleIntensity}
                            onChange={(val) => onChange({...config, scaleIntensity: val})}
                            min={1}
                            max={1.5}
                            step={0.05}
                            description="Size increase on interaction"
                        />
                    </ControlSection>

                    {/* Color Controls */}
                    <ControlSection
                        title="Colors"
                        icon={<Palette className="w-4 h-4"/>}
                        isActive={activeSection === 'colors'}
                        onToggle={() => toggleSection('colors')}
                    >
                        <ColorPicker
                            label="Overlay Color"
                            value={config.colorOverlay}
                            onChange={(color) => onChange({...config, colorOverlay: color})}
                        />
                        {aiSuggestions?.colors && (
                            <div className="mt-3">
                                <p className="text-xs text-gray-600 mb-2">AI Suggested Colors</p>
                                <div className="flex flex-wrap gap-2">
                                    {aiSuggestions.colors.slice(0, 5).map((color, idx) => (
                                        <button
                                            key={idx}
                                            onClick={() => onChange({...config, colorOverlay: color})}
                                            className="w-10 h-10 rounded-lg border-2 border-gray-200 hover:border-blue-500 transition-colors shadow-sm"
                                            style={{backgroundColor: color}}
                                            title={color}
                                        />
                                    ))}
                                </div>
                            </div>
                        )}
                    </ControlSection>

                    {/* Animation Style */}
                    <ControlSection
                        title="Animation"
                        icon={<RotateCw className="w-4 h-4"/>}
                        isActive={activeSection === 'animation'}
                        onToggle={() => toggleSection('animation')}
                    >
                        <RadioGroup
                            label="Animation Style"
                            options={[
                                {value: 'smooth', label: 'Smooth', description: 'Gentle, fluid motion'},
                                {value: 'bouncy', label: 'Bouncy', description: 'Springy, playful feel'},
                                {value: 'instant', label: 'Instant', description: 'Quick, snappy response'},
                            ]}
                            value={config.animationStyle}
                            onChange={(val) => onChange({...config, animationStyle: val})}
                        />
                    </ControlSection>

                    {/* Layout Controls */}
                    <ControlSection
                        title="Layout"
                        icon={<Move className="w-4 h-4"/>}
                        isActive={activeSection === 'layout'}
                        onToggle={() => toggleSection('layout')}
                    >
                        <SliderControl
                            label="Columns"
                            value={config.columns}
                            onChange={(val) => onChange({...config, columns: val})}
                            min={2}
                            max={6}
                            step={1}
                            description="Number of columns in grid"
                        />
                        <SliderControl
                            label="Spacing"
                            value={config.spacing}
                            onChange={(val) => onChange({...config, spacing: val})}
                            min={4}
                            max={40}
                            step={4}
                            unit="px"
                            description="Gap between images"
                        />
                    </ControlSection>

                    {/* Debug Options */}
                    <ControlSection
                        title="Display"
                        icon={<Eye className="w-4 h-4"/>}
                        isActive={activeSection === 'display'}
                        onToggle={() => toggleSection('display')}
                    >
                        <CheckboxControl
                            label="Show Cursor Indicator"
                            checked={config.showCursor || false}
                            onChange={(val) => onChange({...config, showCursor: val})}
                            description="Display animated cursor position"
                        />
                    </ControlSection>

                    {/* Reset Button */}
                    <div className="p-4 border-t border-gray-200">
                        <button
                            onClick={() => onChange({
                                threshold: 200,
                                sensitivity: 0.3,
                                rotationIntensity: 15,
                                scaleIntensity: 1.2,
                                colorOverlay: '#0ea5e9',
                                animationStyle: 'smooth',
                                spacing: 20,
                                columns: 4,
                                showCursor: false,
                            })}
                            className="w-full py-2 px-4 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-lg font-medium transition-colors text-sm"
                        >
                            Reset to Defaults
                        </button>
                    </div>
                </div>
            )}
        </motion.div>
    );
};

/**
 * Collapsible control section
 */
const ControlSection = ({title, icon, isActive, onToggle, children}) => {
    return (
        <div className="border-b border-gray-200">
            <button
                onClick={onToggle}
                className="w-full p-4 flex items-center justify-between hover:bg-gray-50 transition-colors"
            >
                <div className="flex items-center gap-2 text-gray-700">
                    {icon}
                    <span className="font-medium">{title}</span>
                </div>
                {isActive ? (
                    <ChevronUp className="w-4 h-4 text-gray-400"/>
                ) : (
                    <ChevronDown className="w-4 h-4 text-gray-400"/>
                )}
            </button>
            <AnimatePresence>
                {isActive && (
                    <motion.div
                        initial={{height: 0, opacity: 0}}
                        animate={{height: 'auto', opacity: 1}}
                        exit={{height: 0, opacity: 0}}
                        transition={{duration: 0.2}}
                        className="overflow-hidden"
                    >
                        <div className="p-4 pt-0 space-y-4 bg-gray-50">
                            {children}
                        </div>
                    </motion.div>
                )}
            </AnimatePresence>
        </div>
    );
};

/**
 * Slider control component
 */
const SliderControl = ({label, value, onChange, min, max, step, unit = '', description}) => {
    return (
        <div>
            <div className="flex items-center justify-between mb-2">
                <label className="text-sm font-medium text-gray-700">{label}</label>
                <span className="text-sm text-gray-600 font-mono">
          {typeof value === 'number' ? value.toFixed(step < 1 ? 2 : 0) : value}{unit}
        </span>
            </div>
            <input
                type="range"
                min={min}
                max={max}
                step={step}
                value={value}
                onChange={(e) => onChange(parseFloat(e.target.value))}
                className="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer slider"
            />
            {description && (
                <p className="text-xs text-gray-500 mt-1">{description}</p>
            )}
        </div>
    );
};

/**
 * Color picker control
 */
const ColorPicker = ({label, value, onChange}) => {
    const presetColors = [
        '#0ea5e9', // Blue
        '#8b5cf6', // Purple
        '#ec4899', // Pink
        '#f59e0b', // Amber
        '#10b981', // Green
        '#ef4444', // Red
    ];

    return (
        <div>
            <label className="text-sm font-medium text-gray-700 mb-2 block">{label}</label>
            <div className="flex items-center gap-2">
                <input
                    type="color"
                    value={value}
                    onChange={(e) => onChange(e.target.value)}
                    className="w-12 h-12 rounded-lg cursor-pointer border-2 border-gray-200"
                />
                <input
                    type="text"
                    value={value}
                    onChange={(e) => onChange(e.target.value)}
                    className="flex-1 px-3 py-2 border border-gray-300 rounded-lg text-sm font-mono"
                    placeholder="#0ea5e9"
                />
            </div>
            <div className="flex flex-wrap gap-2 mt-3">
                {presetColors.map((color) => (
                    <button
                        key={color}
                        onClick={() => onChange(color)}
                        className={`w-8 h-8 rounded-lg border-2 transition-all ${
                            value === color ? 'border-blue-500 scale-110' : 'border-gray-200'
                        }`}
                        style={{backgroundColor: color}}
                        title={color}
                    />
                ))}
            </div>
        </div>
    );
};

/**
 * Radio group control
 */
const RadioGroup = ({label, options, value, onChange}) => {
    return (
        <div>
            <label className="text-sm font-medium text-gray-700 mb-3 block">{label}</label>
            <div className="space-y-2">
                {options.map((option) => (
                    <label
                        key={option.value}
                        className={`flex items-start gap-3 p-3 rounded-lg border-2 cursor-pointer transition-all ${
                            value === option.value
                                ? 'border-blue-500 bg-blue-50'
                                : 'border-gray-200 hover:border-gray-300'
                        }`}
                    >
                        <input
                            type="radio"
                            value={option.value}
                            checked={value === option.value}
                            onChange={(e) => onChange(e.target.value)}
                            className="mt-0.5"
                        />
                        <div className="flex-1">
                            <div className="text-sm font-medium text-gray-900">{option.label}</div>
                            {option.description && (
                                <div className="text-xs text-gray-600 mt-0.5">{option.description}</div>
                            )}
                        </div>
                    </label>
                ))}
            </div>
        </div>
    );
};

/**
 * Checkbox control
 */
const CheckboxControl = ({label, checked, onChange, description}) => {
    return (
        <label className="flex items-start gap-3 cursor-pointer">
            <input
                type="checkbox"
                checked={checked}
                onChange={(e) => onChange(e.target.checked)}
                className="mt-1"
            />
            <div className="flex-1">
                <div className="text-sm font-medium text-gray-900">{label}</div>
                {description && (
                    <div className="text-xs text-gray-600 mt-0.5">{description}</div>
                )}
            </div>
        </label>
    );
};

export default GalleryControls;