import React, { useEffect, useState } from 'react';

const Header = () => {
    const [isMenuOpen, setIsMenuOpen] = useState(false);
    const [username, setUsername] = useState('');

    useEffect(() => {
        const syncUsername = () => {
            const storedName = localStorage.getItem('authUsername') || '';
            setUsername(storedName);
        };

        syncUsername();
        window.addEventListener('storage', syncUsername);
        window.addEventListener('hashchange', syncUsername);
        return () => {
            window.removeEventListener('storage', syncUsername);
            window.removeEventListener('hashchange', syncUsername);
        };
    }, []);

    const handleLogout = () => {
        localStorage.removeItem('authToken');
        localStorage.removeItem('authUsername');
        localStorage.removeItem('postLoginRedirect');
        setIsMenuOpen(false);
        window.location.hash = '#/home';
    };

    return (
        <header className="bg-gray-100 py-4 relative z-50">
            <div className="container mx-auto flex justify-between items-center px-6">
                <a href="#" className="flex items-center space-x-3">
                    <span className="text-black font-bold text-2xl tracking-wider">SPIKE TICKET</span>
                </a>
                <nav className="hidden md:flex items-center space-x-8">
                    <a href="#" className="text-gray-500 hover:text-black transition-colors">Sự kiện</a>
                    <a href="#" className="text-gray-500 hover:text-black transition-colors">Về chúng tôi</a>
                    <a href="#/create-event" className="text-gray-500 hover:text-black transition-colors">Tạo sự kiện</a>
                </nav>
                <div className="flex items-center space-x-4 relative">
                    {username && (
                        <div className="relative">
                            <button
                                type="button"
                                onClick={() => setIsMenuOpen((prev) => !prev)}
                                className="bg-white border border-gray-200 text-gray-700 rounded-full px-5 py-2 text-sm font-semibold hover:border-gray-400 transition-colors"
                            >
                                {username}
                            </button>
                            {isMenuOpen && (
                                <div className="absolute right-0 mt-2 w-40 rounded-lg border border-gray-200 bg-white shadow-lg z-50">
                                    <button
                                        type="button"
                                        onClick={handleLogout}
                                        className="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-100"
                                    >
                                        Đăng xuất
                                    </button>
                                </div>
                            )}
                        </div>
                    )}
                    <a href="#/login" className="bg-transparent border border-neon-green text-neon-green rounded-full px-6 py-2 text-sm font-bold hover:bg-neon-green hover:text-background transition-colors">
                        Tài khoản
                    </a>
                    <button className="md:hidden text-black">
                        <span className="material-symbols-outlined">menu</span>
                    </button>
                </div>
            </div>
        </header>
    );
};

export default Header;
