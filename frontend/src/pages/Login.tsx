import { useState } from 'react';
import { useNavigate, Link, useSearchParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { apiClient } from '../api/client';
import { ShieldCheck } from 'lucide-react';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  
  const navigate = useNavigate();
  const { login } = useAuth();
  const [searchParams] = useSearchParams();
  const registered = searchParams.get('registered');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const { data } = await apiClient.post('/auth/login', { email, password });
      login(data);
      navigate('/');
    } catch (err: any) {
      if (err.response?.status === 401) {
        setError('Email hoặc mật khẩu không chính xác.');
      } else if (err.response?.status === 429) {
        setError('Bạn đã đăng nhập sai quá nhiều lần. Vui lòng thử lại sau.');
      } else {
        setError('Có lỗi xảy ra. Vui lòng thử lại.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-[calc(100vh-200px)] flex items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="w-full max-w-md bg-white rounded-2xl shadow-xl border border-slate-100 overflow-hidden">
        <div className="px-8 pt-8 pb-6 border-b border-slate-100">
          <h2 className="text-2xl font-bold text-slate-900 text-center">Đăng nhập NOVA</h2>
          <p className="mt-2 text-sm text-slate-500 text-center">
            Chào mừng bạn quay lại
          </p>
        </div>
        
        <div className="p-8">
          {registered && (
            <div className="mb-6 p-4 rounded-lg bg-green-50 text-green-700 text-sm border border-green-100 font-medium">
              Đăng ký thành công! Vui lòng đăng nhập.
            </div>
          )}

          {error && (
            <div className="mb-6 p-4 rounded-lg bg-red-50 text-red-600 text-sm border border-red-100">
              {error}
            </div>
          )}
          
          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Email</label>
              <input 
                type="email" 
                required 
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-4 py-2 border border-slate-200 rounded-lg outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-shadow text-sm"
              />
            </div>
            
            <div>
              <div className="flex items-center justify-between mb-1">
                <label className="block text-sm font-medium text-slate-700">Mật khẩu</label>
              </div>
              <input 
                type="password" 
                required 
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-4 py-2 border border-slate-200 rounded-lg outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-shadow text-sm"
              />
            </div>
            
            <div className="flex items-center justify-between text-sm">
              <label className="flex items-center text-slate-600">
                <input type="checkbox" className="rounded border-slate-300 text-primary-600 focus:ring-primary-500 mr-2" />
                Ghi nhớ đăng nhập
              </label>
              <a href="#" className="font-medium text-primary-600 hover:text-primary-700">Quên mật khẩu?</a>
            </div>

            <button 
              type="submit" 
              disabled={loading}
              className="w-full bg-primary-600 text-white font-medium py-2.5 rounded-lg hover:bg-primary-700 focus:ring-4 focus:ring-primary-100 disabled:opacity-70 transition-all mt-4 shadow-sm flex justify-center items-center"
            >
              {loading ? 'Đang xác thực...' : 'Đăng nhập'}
              {!loading && <ShieldCheck className="w-4 h-4 ml-2" />}
            </button>
          </form>

          <p className="mt-6 text-center text-sm text-slate-600">
            Chưa có tài khoản?{' '}
            <Link to="/register" className="font-semibold text-primary-600 hover:text-primary-700 transition">
              Đăng ký miễn phí
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Login;
