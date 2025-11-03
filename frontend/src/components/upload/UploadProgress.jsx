import {CheckCircle, Circle, Loader} from 'lucide-react';
import {useTheme} from '../../context/ThemeContext';

const UploadProgress = ({currentStep, uploadProgress, analysisProgress}) => {
    const {isDark, currentTheme} = useTheme();

    const steps = [
        {id: 1, name: 'Upload Photos', key: 'upload'},
        {id: 2, name: 'AI Analysis', key: 'analysis'},
        {id: 3, name: 'Generate Gallery', key: 'generate'},
    ];

    const getStepStatus = (stepId) => {
        if (stepId < currentStep) return 'complete';
        if (stepId === currentStep) return 'active';
        return 'pending';
    };

    const getProgress = (stepKey) => {
        if (stepKey === 'upload') return uploadProgress;
        if (stepKey === 'analysis') return analysisProgress;
        return 0;
    };

    return (
        <div className="w-full max-w-3xl mx-auto">
            {/* Steps */}
            <div className="flex items-center justify-between mb-8">
                {steps.map((step, index) => {
                    const status = getStepStatus(step.id);
                    const progress = getProgress(step.key);

                    return (
                        <div key={step.id} className="flex items-center flex-1">
                            {/* Step Circle */}
                            <div className="flex flex-col items-center">
                                <div
                                    className="w-12 h-12 rounded-full flex items-center justify-center border-2 transition-all duration-300"
                                    style={{
                                        backgroundColor: status === 'complete' || status === 'active' ? currentTheme.accent : (isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.05)'),
                                        borderColor: status === 'complete' || status === 'active' ? currentTheme.accent : currentTheme.border
                                    }}
                                >
                                    {status === 'complete' ? (
                                        <CheckCircle
                                            className="w-6 h-6"
                                            style={{color: isDark ? '#0a0a0a' : '#f5f3ef'}}
                                        />
                                    ) : status === 'active' ? (
                                        <Loader
                                            className="w-6 h-6 animate-spin"
                                            style={{color: isDark ? '#0a0a0a' : '#f5f3ef'}}
                                        />
                                    ) : (
                                        <Circle
                                            className="w-6 h-6"
                                            style={{color: currentTheme.textDim}}
                                        />
                                    )}
                                </div>

                                {/* Step Label */}
                                <div className="mt-2 text-center">
                                    <p
                                        className="text-sm font-bold transition-colors duration-300"
                                        style={{
                                            color: status === 'active' || status === 'complete'
                                                ? currentTheme.accent
                                                : currentTheme.textMuted
                                        }}
                                    >
                                        {step.name}
                                    </p>
                                    {status === 'active' && progress > 0 && (
                                        <p
                                            className="text-xs mt-1 transition-colors duration-300"
                                            style={{color: currentTheme.textMuted}}
                                        >
                                            {progress}%
                                        </p>
                                    )}
                                </div>
                            </div>

                            {/* Connecting Line */}
                            {index < steps.length - 1 && (
                                <div className="flex-1 h-0.5 mx-4 relative" style={{top: '-24px'}}>
                                    <div
                                        className="absolute inset-0 transition-colors duration-300"
                                        style={{backgroundColor: currentTheme.border}}
                                    />
                                    <div
                                        className="absolute inset-0 transition-all duration-500"
                                        style={{
                                            backgroundColor: currentTheme.accent,
                                            width:
                                                getStepStatus(step.id) === 'complete'
                                                    ? '100%'
                                                    : getStepStatus(step.id) === 'active'
                                                        ? `${progress}%`
                                                        : '0%',
                                        }}
                                    />
                                </div>
                            )}
                        </div>
                    );
                })}
            </div>

            {/* Progress Bar */}
            {currentStep <= 2 && (
                <div className="mb-4">
                    <div
                        className="w-full rounded-full h-2 overflow-hidden transition-colors duration-300"
                        style={{backgroundColor: isDark ? 'rgba(255, 255, 255, 0.1)' : 'rgba(0, 0, 0, 0.1)'}}
                    >
                        <div
                            className="h-full transition-all duration-300 ease-out"
                            style={{
                                backgroundColor: currentTheme.accent,
                                width: `${
                                    currentStep === 1 ? uploadProgress : currentStep === 2 ? analysisProgress : 0
                                }%`,
                            }}
                        />
                    </div>
                </div>
            )}
        </div>
    );
};

export default UploadProgress;