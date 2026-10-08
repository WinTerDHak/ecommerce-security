import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { apiClient } from '../api/client';
import type { Order } from '../types';
import { ShieldCheck, ArrowRight, Truck } from 'lucide-react';

const Checkout = () => {
  const [formData, setFormData] = useState({
    shippingStreet: '',
    shippingCity: '',
    shippingZip: '',
    shippingCountry: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const { data } = await apiClient.post<Order>('/orders', formData);
      navigate(`/orders/${data.id}`);
    } catch (err: any) {
      if (err.response?.status === 400) {
        setError(err.response.data.message || err.response.data.shippingStreet || 'Thông tin giao hàng không hợp lệ (Không được chứa ký tự < >)');
      } else {
        setError('Lỗi khi tạo đơn hàng');
      }
    } finally {
      setLoading(false);
    }
  };

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  return (
    <div className="container mx-auto px-4 py-8 lg:py-12 max-w-4xl">
      <h1 className="text-3xl lg:text-4xl font-black text-slate-900 mb-8 tracking-tight text-center">Hoàn tất đặt hàng</h1>
      
      <div className="flex justify-center mb-10">
        <div className="flex items-center space-x-4 text-sm font-medium">
          <div className="flex items-center text-primary-600">
            <span className="w-8 h-8 rounded-full bg-primary-600 text-white flex items-center justify-center mr-2">1</span>
            Giỏ hàng
          </div>
          <div className="w-12 h-px bg-primary-600"></div>
          <div className="flex items-center text-primary-600">
            <span className="w-8 h-8 rounded-full bg-primary-600 text-white flex items-center justify-center mr-2">2</span>
            Giao hàng
          </div>
          <div className="w-12 h-px bg-slate-200"></div>
          <div className="flex items-center text-slate-400">
            <span className="w-8 h-8 rounded-full bg-slate-100 border border-slate-200 flex items-center justify-center mr-2">3</span>
            Thanh toán
          </div>
        </div>
      </div>

      <div className="bg-white rounded-3xl p-6 lg:p-10 shadow-sm border border-slate-100 max-w-2xl mx-auto">
        <div className="flex items-center mb-8 border-b border-slate-100 pb-4">
          <Truck className="w-6 h-6 text-primary-600 mr-3" />
          <h2 className="text-xl font-bold text-slate-900">Thông tin giao hàng</h2>
        </div>
        
        {error && (
          <div className="mb-6 p-4 rounded-xl bg-red-50 text-red-600 text-sm border border-red-100">
            {error}
          </div>
        )}
        
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">Đường/Phố</label>
            <input 
              required 
              name="shippingStreet" 
              value={formData.shippingStreet} 
              onChange={handleChange} 
              placeholder="Ví dụ: 123 Nguyễn Văn Cừ"
              className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500 focus:bg-white transition-all text-sm" 
            />
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Thành phố</label>
              <input 
                required 
                name="shippingCity" 
                value={formData.shippingCity} 
                onChange={handleChange} 
                className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500 focus:bg-white transition-all text-sm" 
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-2">Mã bưu điện (Zip)</label>
              <input 
                required 
                name="shippingZip" 
                value={formData.shippingZip} 
                onChange={handleChange} 
                className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500 focus:bg-white transition-all text-sm" 
              />
            </div>
          </div>
          
          <div>
            <label className="block text-sm font-medium text-slate-700 mb-2">Quốc gia</label>
            <input 
              required 
              name="shippingCountry" 
              value={formData.shippingCountry} 
              onChange={handleChange} 
              className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500 focus:bg-white transition-all text-sm" 
            />
          </div>
          
          <div className="mt-8 pt-6 border-t border-slate-100">
            <button 
              type="submit" 
              disabled={loading} 
              className="w-full bg-primary-600 text-white py-4 rounded-xl font-medium hover:bg-primary-700 disabled:opacity-70 transition flex justify-center items-center shadow-lg shadow-primary-600/20"
            >
              {loading ? 'Đang xử lý...' : 'Xác nhận & Tiếp tục thanh toán'}
              {!loading && <ArrowRight className="w-5 h-5 ml-2" />}
            </button>
            <div className="flex justify-center mt-4 text-xs text-slate-500 items-center">
              <ShieldCheck className="w-4 h-4 mr-1 text-green-600" />
              Thông tin được mã hóa bảo mật tuyệt đối
            </div>
          </div>
        </form>
      </div>
    </div>
  );
};

export default Checkout;
