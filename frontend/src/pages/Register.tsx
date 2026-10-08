import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { apiClient } from '../api/client';
import { ShieldCheck, Eye, EyeOff, UserPlus } from 'lucide-react';

const Register = () => {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      await apiClient.post('/auth/register', formData);
      navigate('/login');
    } catch (err: any) {
      if (err.response?.status === 400 && typeof err.response.data === 'object') {
        const data = err.response.data;
        if (data.message) {
           setError(data.message);
        } else {
           const firstError = Object.values(data)[0] as string;
           setError(firstError || 'Thông tin đăng ký không hợp lệ');
        }
      } else {
        setError(err.response?.data?.message || 'Đăng ký thất bại. Vui lòng thử lại.');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  return (
    <div className="min-h-[calc(100vh-80px)] flex bg-slate-50 -mt-8">
      {/* Left side - Visual/Branding */}
      <div className="hidden lg:flex lg:w-1/2 bg-slate-900 text-white flex-col justify-between p-16 relative overflow-hidden">
        <div className="absolute inset-0 opacity-20">
          <div className="absolute -top-24 -left-24 w-96 h-96 bg-primary-500 rounded-full blur-3xl"></div>
          <div className="absolute bottom-0 right-0 w-96 h-96 bg-blue-500 rounded-full blur-3xl"></div>
        </div>
        
        <div className="relative z-10">
          <Link to="/" className="text-3xl font-black tracking-tight text-white mb-16 block">
            NOVA<span className="text-primary-500">.</span>
          </Link>
          
          <h1 className="text-4xl md:text-5xl font-bold leading-tight mb-6">
            Bắt đầu hành trình mua sắm của bạn.
          </h1>
          <p className="text-slate-300 text-lg max-w-md leading-relaxed mb-12">
            Đăng ký tài khoản NOVA để tận hưởng các ưu đãi độc quyền, quản lý đơn hàng dễ dàng và trải nghiệm mua sắm bảo mật tuyệt đối.
          </p>
        </div>

        <div className="relative z-10 bg-white/10 backdrop-blur-md p-8 rounded-2xl border border-white/10">
          <div className="flex items-center mb-4">
            <ShieldCheck className="w-8 h-8 text-primary-400 mr-3" />
            <h3 className="font-bold text-xl">Bảo mật đa tầng</h3>
          </div>
          <ul className="space-y-3 text-slate-300">
            <li className="flex items-center"><div className="w-1.5 h-1.5 bg-primary-400 rounded-full mr-3"></div> Mật khẩu được băm an toàn (Argon2id)</li>
            <li className="flex items-center"><div className="w-1.5 h-1.5 bg-primary-400 rounded-full mr-3"></div> Dữ liệu thanh toán mã hóa 100%</li>
            <li className="flex items-center"><div className="w-1.5 h-1.5 bg-primary-400 rounded-full mr-3"></div> Bảo vệ chống tấn công Brute-force</li>
          </ul>
        </div>
      </div>

      {/* Right side - Form */}
      <div className="w-full lg:w-1/2 flex items-center justify-center p-8 sm:p-12 lg:p-24 relative z-10">
        <div className="w-full max-w-md">
          <div className="text-center lg:text-left mb-10">
            <div className="w-16 h-16 bg-primary-50 rounded-2xl flex items-center justify-center mb-6 mx-auto lg:mx-0">
              <UserPlus className="w-8 h-8 text-primary-600" />
            </div>
            <h2 className="text-3xl font-black text-slate-900 mb-3 tracking-tight">Tạo tài khoản mới</h2>
            <p className="text-slate-500">Đã có tài khoản? <Link to="/login" className="text-primary-600 font-semibold hover:underline transition">Đăng nhập ngay</Link></p>
          </div>

          {error && (
            <div className="mb-8 p-4 rounded-xl bg-red-50 text-red-600 text-sm border border-red-100 flex items-start">
              <div className="shrink-0 mr-3 mt-0.5">
                <ShieldCheck className="w-5 h-5 text-red-500" />
              </div>
              <div>
                <span className="font-semibold block mb-1">Đăng ký không thành công</span>
                {error}
              </div>
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-2 gap-5">
              <div>
                <label className="block text-sm font-semibold text-slate-900 mb-2">Họ</label>
                <input
                  type="text"
                  name="lastName"
                  required
                  value={formData.lastName}
                  onChange={handleChange}
                  className="w-full px-4 py-3.5 bg-white border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all"
                  placeholder="Nguyễn"
                />
              </div>
              <div>
                <label className="block text-sm font-semibold text-slate-900 mb-2">Tên</label>
                <input
                  type="text"
                  name="firstName"
                  required
                  value={formData.firstName}
                  onChange={handleChange}
                  className="w-full px-4 py-3.5 bg-white border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all"
                  placeholder="Văn A"
                />
              </div>
            </div>
            
            <div>
              <label className="block text-sm font-semibold text-slate-900 mb-2">Email đăng nhập</label>
              <input
                type="email"
                name="email"
                required
                value={formData.email}
                onChange={handleChange}
                className="w-full px-4 py-3.5 bg-white border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all"
                placeholder="email@example.com"
              />
            </div>

            <div>
              <label className="block text-sm font-semibold text-slate-900 mb-2">Mật khẩu</label>
              <div className="relative">
                <input
                  type={showPassword ? "text" : "password"}
                  name="password"
                  required
                  value={formData.password}
                  onChange={handleChange}
                  className="w-full pl-4 pr-12 py-3.5 bg-white border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all"
                  placeholder="Tối thiểu 8 ký tự"
                />
                <button 
                  type="button" 
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-slate-600 focus:outline-none"
                >
                  {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                </button>
              </div>
              <p className="text-xs text-slate-500 mt-2">Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường và số.</p>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-primary-600 text-white font-semibold py-4 rounded-xl mt-4 hover:bg-primary-700 focus:ring-4 focus:ring-primary-100 transition-all disabled:opacity-70 shadow-lg shadow-primary-600/20"
            >
              {loading ? 'Đang xử lý...' : 'Đăng ký tài khoản'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default Register;
