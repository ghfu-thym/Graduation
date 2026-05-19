import React from 'react';

const UploadProgress = ({ filesToUpload, uploadProgress, imagePreviews, handleRemoveImage }) => {
    if (imagePreviews.length === 0) {
        return null;
    }

    return (
        <div className="w-full">
            <h3 className="font-caption text-sm text-gray-700 mb-space-3 text-left">Ảnh đã chọn</h3>
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-space-4">
                {imagePreviews.map((preview, index) => (
                    <div key={index} className="relative aspect-square bg-gray-100 rounded-md overflow-hidden">
                        <img src={preview.url} alt={`preview ${index}`} className="w-full h-full object-cover" />
                        <button
                            type="button"
                            onClick={() => handleRemoveImage(index)}
                            className="absolute top-2 right-2 h-8 w-8 rounded-full bg-white/90 text-gray-700 shadow-sm flex items-center justify-center hover:bg-white transition-colors"
                            aria-label="Xoa anh"
                        >
                            <span className="material-symbols-outlined text-[18px]">close</span>
                        </button>
                        {filesToUpload.find((fileItem) => fileItem.name === preview.name) && uploadProgress[preview.name] !== undefined && uploadProgress[preview.name] < 100 && (
                            <div className="absolute bottom-0 left-0 right-0 h-1 bg-gray-300">
                                <div
                                    className="h-1 bg-indigo-600"
                                    style={{ width: `${uploadProgress[preview.name]}%` }}
                                ></div>
                            </div>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
};

export default UploadProgress;
