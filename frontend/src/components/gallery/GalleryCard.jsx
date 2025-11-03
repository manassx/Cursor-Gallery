import {Link} from 'react-router-dom';
import {Eye, Edit, Trash2, Share2, Image as ImageIcon} from 'lucide-react';
import {formatRelativeTime} from '../../utils/helpers';

const GalleryCard = ({gallery, onDelete}) => {
    const {id, name, description, imageCount, views = 0, status, createdAt, thumbnailUrl} = gallery;

    const getStatusBadge = () => {
        const statusConfig = {
            draft: {label: 'Draft', className: 'bg-gray-100 text-gray-700'},
            processing: {label: 'Processing', className: 'bg-yellow-100 text-yellow-700'},
            ready: {label: 'Ready', className: 'bg-green-100 text-green-700'},
            error: {label: 'Error', className: 'bg-red-100 text-red-700'}
        };

        const config = statusConfig[status] || statusConfig.draft;
        return (
            <span className={`px-2 py-1 rounded-full text-xs font-medium ${config.className}`}>
        {config.label}
      </span>
        );
    };

    return (
        <div className="card hover:shadow-xl transition-shadow duration-200">
            {/* Thumbnail */}
            <div
                className="aspect-video bg-gradient-to-br from-primary-100 to-secondary-100 rounded-lg mb-4 overflow-hidden">
                {thumbnailUrl ? (
                    <img src={thumbnailUrl} alt={name} className="w-full h-full object-cover"/>
                ) : (
                    <div className="w-full h-full flex items-center justify-center">
                        <ImageIcon className="w-12 h-12 text-gray-400"/>
                    </div>
                )}
            </div>

            {/* Gallery Info */}
            <div className="mb-4">
                <div className="flex items-start justify-between mb-2">
                    <h3 className="text-lg font-semibold text-gray-900 flex-1">
                        {name}
                    </h3>
                    {getStatusBadge()}
                </div>

                {description && (
                    <p className="text-sm text-gray-600 line-clamp-2 mb-2">
                        {description}
                    </p>
                )}

                <div className="flex items-center space-x-4 text-sm text-gray-500">
                    <div className="flex items-center space-x-1">
                        <ImageIcon size={14}/>
                        <span>{imageCount} images</span>
                    </div>
                    <div className="flex items-center space-x-1">
                        <Eye size={14}/>
                        <span>{views} views</span>
                    </div>
                    <span>•</span>
                    <span>{formatRelativeTime(createdAt)}</span>
                </div>
            </div>

            {/* Actions */}
            <div className="flex items-center space-x-2 pt-4 border-t border-gray-200">
                <Link
                    to={`/gallery/${id}/edit`}
                    className="flex-1 btn-primary text-sm py-2 flex items-center justify-center space-x-1"
                >
                    <Edit size={14}/>
                    <span>Edit</span>
                </Link>

                <Link
                    to={`/gallery/user/${id}`}
                    className="flex-1 btn-outline text-sm py-2 flex items-center justify-center space-x-1"
                >
                    <Eye size={14}/>
                    <span>View</span>
                </Link>

                <button
                    onClick={() => onDelete && onDelete(id)}
                    className="p-2 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition-colors"
                    title="Delete gallery"
                >
                    <Trash2 size={16}/>
                </button>

                <button
                    className="p-2 text-gray-400 hover:text-primary-600 hover:bg-primary-50 rounded-lg transition-colors"
                    title="Share gallery"
                >
                    <Share2 size={16}/>
                </button>
            </div>
        </div>
    );
};

export default GalleryCard;