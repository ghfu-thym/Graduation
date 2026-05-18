import React from 'react';

const UploadProgress = ({ filesToUpload, uploadProgress, imagePreviews, handleRemoveImage }) => {
    if (imagePreviews.length === 0) {
        return null;
    }

    return (
        <div className="mt-space-6">
            <h3 className="font-caption text-sm text-gray-700 mb-space-4">Ảnh đã chọn</h3>
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-space-4">
                {imagePreviews.map((preview, index) => (
                    <div key={index} className="relative group aspect-square bg-gray-100 rounded-lg overflow-hidden">
                        <img src={preview.url} alt={`preview ${index}`} className="w-full h-full object-cover" />
                        <div className="absolute inset-0 bg-black/60 flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                            <button
                                onClick={() => handleRemoveImage(index)}
                                className="text-white"
                            >
                                <span className="material-symbols-outlined">delete</span>
                            </button>
                        </div>
                        {filesToUpload.find(f => f.name === preview.name) && uploadProgress[preview.name] !== undefined && uploadProgress[preview.name] < 100 && (
                            <div className="absolute bottom-0 left-0 right-0 h-1 bg-gray-300">
                                <div
                                    className="h-1 bg-indigo-600"
                                    style={{ width: `${uploadProgress[preview.name]}%` }}
                                ></div>
                            </div>
                        )}
                         {uploadProgress[preview.name] === 100 && (
                            <div className="absolute top-1 right-1 bg-green-500 text-white rounded-full p-1">
                                <span className="material-symbols-outlined text-sm">check</span>
                            </div>
                        )}
                    </div>
                ))}
            </div>
        </div>
    );
};

export default UploadProgress;


