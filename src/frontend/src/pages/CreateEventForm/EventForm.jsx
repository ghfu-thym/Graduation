import React from 'react';
import UploadProgress from './UploadProgress';

const EventForm = ({
    formData,
    setFormData,
    handleFileSelect,
    handleSubmit,
    ticketCategories,
    handleCategoryChange,
    handleAddCategory,
    handleRemoveCategory,
    imagePreviews,
    filesToUpload,
    uploadProgress,
    handleRemoveImage,
}) => {
    const handleChange = (e) => {
        const { name, value } = e.target;
        setFormData((prev) => ({ ...prev, [name]: value }));
    };

    return (
        <form onSubmit={handleSubmit} className="space-y-space-6 flex flex-col">
            <div>
                <label className="block font-caption text-sm text-gray-700 mb-space-2" htmlFor="name">Tên sự kiện <span className="text-red-500">*</span></label>
                <input
                    id="name"
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    placeholder="Nhập tên sự kiện"
                    className="w-full bg-gray-50 border border-gray-300 rounded-md px-space-4 py-space-3 text-black font-body-standard placeholder:text-gray-400 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-colors"
                />
            </div>

            <div>
                <label className="block font-caption text-sm text-gray-700 mb-space-2" htmlFor="location">Địa điểm <span className="text-red-500">*</span></label>
                <input
                    id="location"
                    type="text"
                    name="location"
                    value={formData.location}
                    onChange={handleChange}
                    placeholder="Nhập địa điểm tổ chức"
                    className="w-full bg-gray-50 border border-gray-300 rounded-md px-space-4 py-space-3 text-black font-body-standard placeholder:text-gray-400 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-colors"
                />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-space-5">
                <div>
                    <label className="block font-caption text-sm text-gray-700 mb-space-2" htmlFor="startTime">Thời gian bắt đầu <span className="text-red-500">*</span></label>
                    <input
                        id="startTime"
                        type="datetime-local"
                        name="startTime"
                        value={formData.startTime}
                        onChange={handleChange}
                        className="w-full bg-gray-50 border border-gray-300 rounded-md px-space-4 py-space-3 text-black font-body-standard focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-colors"
                    />
                </div>
                <div>
                    <label className="block font-caption text-sm text-gray-700 mb-space-2" htmlFor="endTime">Thời gian kết thúc <span className="text-red-500">*</span></label>
                    <input
                        id="endTime"
                        type="datetime-local"
                        name="endTime"
                        value={formData.endTime}
                        onChange={handleChange}
                        className="w-full bg-gray-50 border border-gray-300 rounded-md px-space-4 py-space-3 text-black font-body-standard focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-colors"
                    />
                </div>
            </div>
            <div className="mt-space-6">
                <label className="block font-caption text-sm text-gray-700 mb-space-2" htmlFor="ticketOpenTime">Thời gian mở bán vé <span className="text-red-500">*</span></label>
                <input
                  id="ticketOpenTime"
                  type="datetime-local"
                  name="ticketOpenTime"
                  value={formData.ticketOpenTime}
                  onChange={handleChange}
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-space-4 py-space-3 text-black font-body-standard focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-colors"
                />
            </div>

            <div>
                <label className="block font-caption text-sm text-gray-700 mb-space-2" htmlFor="description">Mô tả sự kiện</label>
                <textarea
                    id="description"
                    rows="4"
                    name="description"
                    value={formData.description}
                    onChange={handleChange}
                    placeholder="Cung cấp thông tin chi tiết về sự kiện của bạn..."
                    className="w-full bg-gray-50 border border-gray-300 rounded-md px-space-4 py-space-3 text-black font-body-standard placeholder:text-gray-400 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-colors"
                ></textarea>
            </div>

            <div>
                <label className="block font-caption text-sm text-gray-700 mb-space-2">Ảnh sự kiện (tối đa 5)</label>
                <div className="mt-2 rounded-lg border border-dashed border-gray-300 px-6 py-10">
                    <div className="flex flex-col gap-space-6">
                        <div className="text-center">
                            <svg className="mx-auto h-12 w-12 text-gray-400" stroke="currentColor" fill="none" viewBox="0 0 48 48" aria-hidden="true">
                                <path d="M28 8H12a4 4 0 00-4 4v20m32-12v8m0 0v8a4 4 0 01-4 4H12a4 4 0 01-4-4v-4m32-4l-3.172-3.172a4 4 0 00-5.656 0L28 28M8 32l9.172-9.172a4 4 0 015.656 0L28 28" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"></path>
                            </svg>
                            <div className="mt-4 flex text-sm leading-6 text-gray-600 justify-center">
                                <label htmlFor="file-upload" className="relative cursor-pointer rounded-md bg-white font-semibold text-indigo-600 focus-within:outline-none focus-within:ring-2 focus-within:ring-indigo-600 focus-within:ring-offset-2 hover:text-indigo-500">
                                    <span>Tải ảnh lên</span>
                                    <input id="file-upload" name="file-upload" type="file" className="sr-only" multiple accept="image/*" onChange={handleFileSelect} />
                                </label>
                                <p className="pl-1 text-gray-500">hoặc kéo thả</p>
                            </div>
                            <p className="text-xs leading-5 text-gray-500">PNG, JPG, GIF up to 10MB</p>
                        </div>
                        <UploadProgress
                            filesToUpload={filesToUpload}
                            uploadProgress={uploadProgress}
                            imagePreviews={imagePreviews}
                            handleRemoveImage={handleRemoveImage}
                        />
                    </div>
                </div>
            </div>

            <div>
                <div className="flex items-center justify-between mb-space-3">
                    <label className="block font-caption text-sm text-gray-700">Hạng vé</label>
                    <button
                        type="button"
                        className="rounded-full border border-gray-300 px-4 py-1 text-sm text-gray-700 hover:border-black hover:text-black transition-colors"
                        onClick={handleAddCategory}
                    >
                        + Thêm hạng vé
                    </button>
                </div>

                <div className="space-y-space-4">
                    {ticketCategories.map((category, index) => (
                        <div key={`ticket-category-${index}`} className="rounded-lg border border-gray-200 p-space-4 bg-white shadow-sm">
                            <div className="flex items-center justify-between mb-space-3">
                                <div className="font-body-medium text-sm text-gray-700">Hạng vé #{index + 1}</div>
                                {ticketCategories.length > 1 && (
                                    <button
                                        type="button"
                                        className="text-sm text-red-500 hover:text-red-600"
                                        onClick={() => handleRemoveCategory(index)}
                                    >
                                        Xóa
                                    </button>
                                )}
                            </div>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-space-4">
                                <div>
                                    <label className="block font-caption text-xs text-gray-600 mb-space-2">Tên hạng vé <span className="text-red-500">*</span></label>
                                    <input
                                        type="text"
                                        value={category.name}
                                        onChange={(e) => handleCategoryChange(index, 'name', e.target.value)}
                                        placeholder="Ví dụ: VIP"
                                        className="w-full bg-gray-50 border border-gray-300 rounded-md px-space-3 py-space-2 text-black font-body-standard placeholder:text-gray-400 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-colors"
                                    />
                                </div>
                                <div>
                                    <label className="block font-caption text-xs text-gray-600 mb-space-2">Giá (VNĐ) <span className="text-red-500">*</span></label>
                                    <input
                                        type="number"
                                        value={category.price}
                                        onChange={(e) => handleCategoryChange(index, 'price', e.target.value)}
                                        placeholder="1000000"
                                        className="w-full bg-gray-50 border border-gray-300 rounded-md px-space-3 py-space-2 text-black font-body-standard placeholder:text-gray-400 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-colors"
                                        min="0"
                                    />
                                </div>
                                <div>
                                    <label className="block font-caption text-xs text-gray-600 mb-space-2">Số lượng vé <span className="text-red-500">*</span></label>
                                    <input
                                        type="number"
                                        value={category.quantity}
                                        onChange={(e) => handleCategoryChange(index, 'quantity', e.target.value)}
                                        placeholder="500"
                                        className="w-full bg-gray-50 border border-gray-300 rounded-md px-space-3 py-space-2 text-black font-body-standard placeholder:text-gray-400 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-colors"
                                        min="1"
                                    />
                                </div>
                                <div>
                                    <label className="block font-caption text-xs text-gray-600 mb-space-2">Mô tả</label>
                                    <input
                                        type="text"
                                        value={category.description}
                                        onChange={(e) => handleCategoryChange(index, 'description', e.target.value)}
                                        placeholder="Khu vực gần sân khấu"
                                        className="w-full bg-gray-50 border border-gray-300 rounded-md px-space-3 py-space-2 text-black font-body-standard placeholder:text-gray-400 focus:outline-none focus:border-indigo-500 focus:ring-1 focus:ring-indigo-500 transition-colors"
                                    />
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            <div className="flex justify-end pt-space-5">
                <button type="submit" className="flex items-center justify-center rounded-full bg-black text-white font-bold text-lg px-space-8 py-space-3 transition-transform hover:scale-105">
                    Tạo sự kiện
                </button>
            </div>
        </form>
    );
};

export default EventForm;

