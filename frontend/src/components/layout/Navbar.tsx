import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { apiClient } from '../../api/client';
import { ShoppingCart, LogOut, User as UserIcon, Shield, Search, Menu, X } from 'lucide-react';
import { useState } from 'react';

const Navbar = () => {
  const { user, isAuthenticated, logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  const handleLogout = async () => {
    try {
      await apiClient.post('/auth/logout');
    } catch (e) {
      console.error('Logout error', e);
    } finally {
      logout();
      navigate('/login');
    }
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchQuery.trim()) {
      navigate(`/products?q=${encodeURIComponent(searchQuery)}`);
      setSearchQuery('');
    }
  };

  const navLinks = [
    { name: 'Trang chủ', path: '/' },
    { name: 'Sản phẩm', path: '/products' },
  ];

  return (
    <nav className="bg-white shadow-sm border-b sticky top-0 z-50">
      <div className="container mx-auto px-4 max-w-7xl">
        <div className="flex justify-between items-center h-16">
          {/* Logo & Desktop Nav */}
          <div className="flex items-center gap-8">
            <Link to="/" className="text-2xl font-black tracking-tight text-primary-600">
              NOVA<span className="text-slate-900">.</span>
            </Link>
            
            <div className="hidden md:flex space-x-6">
              {navLinks.map((link) => (
                <Link 
                  key={link.name} 
                  to={link.path} 
                  className={`text-sm font-medium transition ${location.pathname === link.path ? 'text-primary-600' : 'text-slate-600 hover:text-slate-900'}`}
                >
                  {link.name}
                </Link>
              ))}
            </div>
          </div>

          {/* Desktop Search & Actions */}
          <div className="hidden md:flex items-center space-x-6">
            <form onSubmit={handleSearch} className="relative">
              <input 
                type="text" 
                placeholder="Tìm kiếm sản phẩm..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-64 pl-10 pr-4 py-2 border border-slate-200 rounded-full text-sm focus:outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent bg-slate-50 transition"
              />
              <Search className="absolute left-3 top-2.5 w-4 h-4 text-slate-400" />
            </form>

            <div className="flex items-center space-x-5">
              {isAuthenticated ? (
                <>
                  <Link to="/cart" className="text-slate-600 hover:text-primary-600 transition flex items-center gap-1">
                    <ShoppingCart className="w-5 h-5" />
                    <span className="text-sm font-medium hidden lg:block">Giỏ hàng</span>
                  </Link>
                  
                  {user?.role === 'ADMIN' && (
                    <Link to="/admin" className="text-slate-600 hover:text-primary-600 transition flex items-center gap-1">
                      <Shield className="w-5 h-5" />
                      <span className="text-sm font-medium hidden lg:block">Quản trị</span>
                    </Link>
                  )}
                  
                  <div className="h-6 w-px bg-slate-200 mx-1"></div>
                  
                  <Link to="/orders" className="text-slate-600 hover:text-primary-600 transition flex items-center gap-1">
                    <UserIcon className="w-5 h-5" />
                    <span className="text-sm font-medium hidden lg:block">{user?.firstName || 'Tài khoản'}</span>
                  </Link>
                  <button onClick={handleLogout} className="text-slate-600 hover:text-red-600 transition" title="Đăng xuất">
                    <LogOut className="w-5 h-5" />
                  </button>
                </>
              ) : (
                <>
                  <Link to="/login" className="text-sm font-medium text-slate-600 hover:text-slate-900 transition">
                    Đăng nhập
                  </Link>
                  <Link to="/register" className="bg-primary-600 text-white text-sm font-medium px-5 py-2 rounded-full hover:bg-primary-700 transition shadow-sm">
                    Đăng ký
                  </Link>
                </>
              )}
            </div>
          </div>

          {/* Mobile Menu Button */}
          <div className="md:hidden flex items-center gap-4">
            {isAuthenticated && (
              <Link to="/cart" className="text-slate-600">
                <ShoppingCart className="w-6 h-6" />
              </Link>
            )}
            <button onClick={() => setMobileMenuOpen(!mobileMenuOpen)} className="text-slate-900">
              {mobileMenuOpen ? <X className="w-6 h-6" /> : <Menu className="w-6 h-6" />}
            </button>
          </div>
        </div>
      </div>

      {/* Mobile Menu */}
      {mobileMenuOpen && (
        <div className="md:hidden border-t bg-white absolute w-full left-0 shadow-lg">
          <div className="px-4 py-4 space-y-4">
            <form onSubmit={handleSearch} className="relative mb-4">
              <input 
                type="text" 
                placeholder="Tìm kiếm sản phẩm..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                className="w-full pl-10 pr-4 py-2 border rounded-md"
              />
              <Search className="absolute left-3 top-2.5 w-5 h-5 text-slate-400" />
            </form>
            
            {navLinks.map((link) => (
              <Link key={link.name} to={link.path} onClick={() => setMobileMenuOpen(false)} className="block py-2 text-slate-700 font-medium">
                {link.name}
              </Link>
            ))}
            
            <div className="border-t pt-4 space-y-4">
              {isAuthenticated ? (
                <>
                  <Link to="/orders" onClick={() => setMobileMenuOpen(false)} className="flex items-center text-slate-700 py-2">
                    <UserIcon className="w-5 h-5 mr-3" /> Đơn hàng của tôi
                  </Link>
                  {user?.role === 'ADMIN' && (
                    <Link to="/admin" onClick={() => setMobileMenuOpen(false)} className="flex items-center text-slate-700 py-2">
                      <Shield className="w-5 h-5 mr-3" /> Quản trị hệ thống
                    </Link>
                  )}
                  <button onClick={() => { setMobileMenuOpen(false); handleLogout(); }} className="flex items-center text-red-600 py-2 w-full text-left">
                    <LogOut className="w-5 h-5 mr-3" /> Đăng xuất
                  </button>
                </>
              ) : (
                <div className="flex flex-col gap-3">
                  <Link to="/login" onClick={() => setMobileMenuOpen(false)} className="block text-center border border-slate-300 rounded-md py-2 text-slate-700 font-medium">
                    Đăng nhập
                  </Link>
                  <Link to="/register" onClick={() => setMobileMenuOpen(false)} className="block text-center bg-primary-600 text-white rounded-md py-2 font-medium">
                    Đăng ký
                  </Link>
                </div>
              )}
            </div>
          </div>
        </div>
      )}
    </nav>
  );
};

export default Navbar;
