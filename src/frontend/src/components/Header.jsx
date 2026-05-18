import React from 'react';

const Header = () => {
    return (
        <header className="bg-gray-100 py-4">
            <div className="container mx-auto flex justify-between items-center px-6">
                <a href="#" className="flex items-center space-x-3">
                    <span className="text-black font-bold text-2xl tracking-wider">SPIKE TICKET</span>
                </a>
                <nav className="hidden md:flex items-center space-x-8">
                    <a href="#" className="text-gray-500 hover:text-black transition-colors">Sự kiện</a>
                    <a href="#" className="text-gray-500 hover:text-black transition-colors">Về chúng tôi</a>
                    <a href="#" className="text-gray-500 hover:text-black transition-colors">Hỗ trợ</a>
                </nav>
                <div className="flex items-center space-x-4">
                    <button className="bg-transparent border border-neon-green text-neon-green rounded-full px-6 py-2 text-sm font-bold hover:bg-neon-green hover:text-background transition-colors">
                        Đăng nhập
                    </button>
                    <button className="md:hidden text-black">
                        <span className="material-symbols-outlined">menu</span>
                    </button>
                </div>
            </div>
        </header>
    );
};

export default Header;
