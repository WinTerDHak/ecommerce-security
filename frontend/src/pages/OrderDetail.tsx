import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiClient } from '../api/client';
import type { Order, PaymentUrl } from '../types';
import { ShieldCheck, Package, Clock, CreditCard, CheckCircle, XCircle } from 'lucide-react';
import { formatVND } from '../utils/formatCurrency';


const OrderDetail = () => {
  const { id } = useParams();
  const [order, setOrder] = useState<Order | null>(null);
  const [loading, setLoading] = useState(true);
  const [payLoading, setPayLoading] = useState(false);
  const [payError, setPayError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    const fetchOrder = async () => {
      try {
        const { data } = await apiClient.get<Order>(`/orders/${id}`);
        setOrder(data);
      } catch (err) {
        console.error('Failed to load order');
      } finally {
        setLoading(false);
      }
    };
    fetchOrder();
  }, [id]);

  const handlePayment = async () => {
    setPayLoading(true);
    setPayError('');
    try {
      const { data } = await apiClient.post<PaymentUrl>(`/payments/orders/${id}/pay`);
      if (data.paymentUrl) {
        window.location.href = data.paymentUrl;
      }
    } catch (err: any) {
      if (err.response?.status === 500 && err.response?.data?.message?.includes('VNPAY is not configured')) {
        setPayError('Cổng thanh toán chưa được cấu hình trên server.');
      } else {
        setPayError('Lỗi khi tạo yêu cầu thanh toán.');
      }
      setPayLoading(false);
    }
  };

  if (loading) return (
    <div className="py-12 flex justify-center">
      <div className="animate-spin w-8 h-8 border-4 border-primary-500 border-t-transparent rounded-full"></div>
    </div>
  );
  
  if (!order) return (
    <div className="py-24 text-center">
      <div className="w-16 h-16 bg-slate-100 rounded-full flex items-center justify-center mx-auto mb-4">
        <Package className="w-8 h-8 text-slate-400" />
      </div>
      <h2 className="text-2xl font-bold text-slate-900 mb-2">Không tìm thấy đơn hàng</h2>
      <button onClick={() => navigate('/orders')} className="mt-4 text-primary-600 font-medium hover:underline">
        Quay lại danh sách đơn hàng
      </button>
    </div>
  );

  return (
    <div className="container mx-auto px-4 py-8 lg:py-12 max-w-3xl">
      {order.status === 'PENDING' && (
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
            <div className="w-12 h-px bg-primary-600"></div>
            <div className="flex items-center text-primary-600">
              <span className="w-8 h-8 rounded-full bg-primary-600 text-white flex items-center justify-center mr-2">3</span>
              Thanh toán
            </div>
          </div>
        </div>
      )}

      <div className="bg-white rounded-3xl p-6 lg:p-10 shadow-sm border border-slate-100">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-8 pb-6 border-b border-slate-100 gap-4">
          <div>
            <h1 className="text-2xl font-bold text-slate-900 mb-1">Đơn hàng #{order.id}</h1>
            <p className="text-sm text-slate-500 flex items-center">
              <Clock className="w-4 h-4 mr-1" />
              {new Date(order.createdAt).toLocaleString('vi-VN')}
            </p>
          </div>
          
          <div className={`px-4 py-2 rounded-full text-sm font-bold tracking-wide uppercase flex items-center shadow-sm ${order.status === 'PAID' ? 'bg-green-50 text-green-700 border border-green-200' : order.status === 'PENDING' ? 'bg-yellow-50 text-yellow-700 border border-yellow-200' : 'bg-red-50 text-red-700 border border-red-200'}`}>
            {order.status === 'PAID' && <CheckCircle className="w-4 h-4 mr-1.5" />}
            {order.status === 'PENDING' && <Clock className="w-4 h-4 mr-1.5" />}
            {order.status === 'FAILED' && <XCircle className="w-4 h-4 mr-1.5" />}
            {order.status}
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-8 bg-slate-50 p-6 rounded-2xl border border-slate-100">
          <div>
            <h3 className="font-semibold text-slate-900 mb-3 flex items-center text-sm uppercase tracking-wider">
              <Package className="w-4 h-4 mr-2 text-slate-400" />
              Giao hàng đến
            </h3>
            <p className="text-slate-700 font-medium">{order.shippingStreet}</p>
            <p className="text-slate-600">{order.shippingCity}, {order.shippingZip}</p>
            <p className="text-slate-600">{order.shippingCountry}</p>
          </div>
          <div>
            <h3 className="font-semibold text-slate-900 mb-3 flex items-center text-sm uppercase tracking-wider">
              <CreditCard className="w-4 h-4 mr-2 text-slate-400" />
              Thanh toán
            </h3>
            <div className="flex justify-between items-end">
              <span className="text-slate-600">Tổng số tiền cần thanh toán</span>
              <span className="font-black text-2xl text-primary-600">{formatVND(order.totalAmount)}</span>
            </div>
          </div>
        </div>

        {order.status === 'PENDING' && (
          <div className="mt-8 pt-8 border-t border-slate-100">
            <h3 className="font-bold text-slate-900 mb-6 text-center text-lg">Hoàn tất thanh toán an toàn</h3>
            
            {payError && (
              <div className="mb-6 p-4 rounded-xl bg-red-50 text-red-600 text-sm border border-red-100 text-center">
                {payError}
              </div>
            )}
            
            <button 
              onClick={handlePayment} 
              disabled={payLoading}
              className="w-full bg-[#005BAA] text-white py-4 rounded-xl font-medium hover:bg-[#004A8B] disabled:opacity-70 transition flex flex-col justify-center items-center shadow-lg shadow-blue-900/20"
            >
              <span className="text-lg">{payLoading ? 'Đang chuyển hướng...' : 'Thanh toán qua VNPAY'}</span>
              {!payLoading && <span className="text-xs text-blue-200 mt-1 font-normal">Thẻ ATM nội địa / Visa / Master / JCB</span>}
            </button>
            
            <div className="flex justify-center mt-6 text-sm text-slate-600 items-start max-w-md mx-auto">
              <ShieldCheck className="w-5 h-5 mr-2 text-green-600 shrink-0" />
              <p>Hệ thống không lưu trữ thông tin thẻ của bạn. Giao dịch được xử lý an toàn tuyệt đối bởi VNPAY.</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default OrderDetail;
