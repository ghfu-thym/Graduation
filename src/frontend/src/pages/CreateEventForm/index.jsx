import React from 'react';
import useEventForm from './useEventForm';
import EventForm from './EventForm';

const CreateEventForm = () => {
    const {
        formData,
        setFormData,
        ticketCategories,
        filesToUpload,
        imagePreviews,
        uploadProgress,
        handleFileSelect,
        handleRemoveImage,
        handleCategoryChange,
        handleAddCategory,
        handleRemoveCategory,
        handleSubmit,
    } = useEventForm();

    return (
        <main className="flex-grow flex items-center justify-center p-space-6 md:p-space-10 bg-gray-50">
            <div className="w-full max-w-2xl bg-white rounded-lg border border-gray-200 shadow-md relative overflow-hidden">
                <div className="p-space-8 md:p-space-10">
                    <h1 className="font-heading-3 text-3xl text-black mb-space-2 text-center">Tạo sự kiện</h1>
                    <p className="font-body-medium text-base text-gray-500 text-center mb-space-8">Điền thông tin chi tiết để tạo sự kiện mới trên Spike Ticket.</p>
                    <EventForm
                        formData={formData}
                        setFormData={setFormData}
                        ticketCategories={ticketCategories}
                        handleCategoryChange={handleCategoryChange}
                        handleAddCategory={handleAddCategory}
                        handleRemoveCategory={handleRemoveCategory}
                        handleFileSelect={handleFileSelect}
                        handleSubmit={handleSubmit}
                        imagePreviews={imagePreviews}
                        filesToUpload={filesToUpload}
                        uploadProgress={uploadProgress}
                        handleRemoveImage={handleRemoveImage}
                    />
                </div>
            </div>
        </main>
    );
};

export default CreateEventForm;
