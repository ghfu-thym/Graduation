import { useState } from 'react';
import { getUploadUrl, uploadFileToS3, createEvent as apiCreateEvent } from '../../api';

const VITE_CLOUDFRONT_DOMAIN = import.meta.env.VITE_CLOUDFRONT_DOMAIN || 'https://d1cpe6xn6cl1ii.cloudfront.net';

const useEventForm = () => {
    const [formData, setFormData] = useState({
        name: '',
        location: '',
        startTime: '',
        endTime: '',
        ticketOpenTime: '',
        description: '',
    });
    const [ticketCategories, setTicketCategories] = useState([
        { name: '', price: '', quantity: '', description: '' },
    ]);
    const [filesToUpload, setFilesToUpload] = useState([]);
    const [imagePreviews, setImagePreviews] = useState([]);
    const [uploadProgress, setUploadProgress] = useState({});
    const [uploadedImageUrls, setUploadedImageUrls] = useState([]);

    const handleFileSelect = async (event) => {
        const files = Array.from(event.target.files).slice(0, 5 - imagePreviews.length);
        if (files.length === 0) return;

        console.info('[upload] selected files:', files.map(f => ({ name: f.name, type: f.type, size: f.size })));

        const newFilesToUpload = [...filesToUpload, ...files];
        setFilesToUpload(newFilesToUpload);

        const newImagePreviews = files.map(file => ({
            url: URL.createObjectURL(file),
            name: file.name
        }));
        setImagePreviews(prev => [...prev, ...newImagePreviews]);

        for (const file of files) {
            try {
                console.info('[upload] requesting presign:', { name: file.name, type: file.type });
                const response = await getUploadUrl(file.name, file.type);
                const { uploadUrl, fileKey } = response.data || {};
                console.info('[upload] presign response:', { uploadUrl: !!uploadUrl, fileKey });

                if (!uploadUrl || !fileKey) {
                    throw new Error('Missing uploadUrl or fileKey from presign response');
                }

                await uploadFileToS3(uploadUrl, file, (progressEvent) => {
                    const percentCompleted = Math.round((progressEvent.loaded * 100) / progressEvent.total);
                    setUploadProgress(prev => ({ ...prev, [file.name]: percentCompleted }));
                    console.debug('[upload] progress:', file.name, percentCompleted + '%');
                });

                const finalUrl = `${VITE_CLOUDFRONT_DOMAIN}/${fileKey}`;
                console.info('[upload] success:', { file: file.name, finalUrl });
                setUploadedImageUrls(prev => {
                    const next = [...prev, finalUrl];
                    console.info('[upload] uploadedImageUrls:', next);
                    return next;
                });

            } catch (error) {
                console.error('[upload] failed:', file.name, error);
                setUploadProgress(prev => ({ ...prev, [file.name]: 'failed' }));
            }
        }
    };

    const handleRemoveImage = (index) => {
        const newImagePreviews = [...imagePreviews];
        const newFilesToUpload = [...filesToUpload];
        const newUploadedImageUrls = [...uploadedImageUrls];

        const removedPreview = newImagePreviews.splice(index, 1)[0];
        const removedFileIndex = newFilesToUpload.findIndex(file => file.name === removedPreview.name);
        if (removedFileIndex > -1) {
            newFilesToUpload.splice(removedFileIndex, 1);
        }

        const removedUrlIndex = newUploadedImageUrls.findIndex(url => url.includes(removedPreview.name));
        if (removedUrlIndex > -1) {
            newUploadedImageUrls.splice(removedUrlIndex, 1);
        }


        setImagePreviews(newImagePreviews);
        setFilesToUpload(newFilesToUpload);
        setUploadedImageUrls(newUploadedImageUrls);

        URL.revokeObjectURL(removedPreview.url);
    };

    const handleCategoryChange = (index, field, value) => {
        setTicketCategories((prev) => {
            const next = [...prev];
            next[index] = { ...next[index], [field]: value };
            return next;
        });
    };

    const handleAddCategory = () => {
        setTicketCategories((prev) => [...prev, { name: '', price: '', quantity: '', description: '' }]);
    };

    const handleRemoveCategory = (index) => {
        setTicketCategories((prev) => prev.filter((_, i) => i !== index));
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        const isUploading = Object.values(uploadProgress).some(p => p < 100);
        if (isUploading) {
            alert('Please wait for all images to finish uploading.');
            return;
        }

        const normalizedCategories = ticketCategories
            .map((category) => ({
                name: category.name?.trim(),
                price: category.price,
                quantity: category.quantity,
                description: category.description?.trim(),
            }))
            .filter((category) => category.name || category.price || category.quantity || category.description);

        const hasValidCategory = normalizedCategories.some(
            (category) => category.name && category.price && category.quantity
        );

        if (!hasValidCategory) {
            alert('Vui lòng thêm ít nhất 1 hạng vé với tên, giá và số lượng.');
            return;
        }

        console.info('[submit] uploadedImageUrls:', uploadedImageUrls);
        const eventData = {
            ...formData,
            listOfImageUrls: uploadedImageUrls,
            ticketCategoryList: normalizedCategories,
        };
        console.info('[submit] payload:', eventData);

        try {
            await apiCreateEvent(eventData);
            alert('Event created successfully!');
            // Reset form
            setFormData({ name: '', location: '', startTime: '', endTime: '', ticketOpenTime: '', description: '' });
            setTicketCategories([{ name: '', price: '', quantity: '', description: '' }]);
            setFilesToUpload([]);
            setImagePreviews([]);
            setUploadProgress({});
            setUploadedImageUrls([]);

        } catch (error) {
            console.error('Failed to create event:', error);
            alert('Failed to create event. Please check the console for details.');
        }
    };

    return {
        formData,
        setFormData,
        ticketCategories,
        filesToUpload,
        imagePreviews,
        uploadProgress,
        uploadedImageUrls,
        handleFileSelect,
        handleRemoveImage,
        handleCategoryChange,
        handleAddCategory,
        handleRemoveCategory,
        handleSubmit,
    };
};

export default useEventForm;

