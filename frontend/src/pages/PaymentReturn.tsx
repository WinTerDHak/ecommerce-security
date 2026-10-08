import { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { apiClient } from '../api/client';
import { CheckCircle, XCircle } from 'lucide-react';

const PaymentReturn = () => {
  const [searchParams] = useSearchParams();
  const [status, setStatus] = useState<'loading' | 'success' | 'failed'>('loading');
  const [message, setMessage] = useState('Đang xác thực thanh toán...');

  useEffect(() => {
    const verifyPayment = async () => {
      try {
        const { data } = await apiClient.get<string>(`/payments/vnpay/return?${searchParams.toString()}`);
        const responseText = data.toLowerCase();
        if (responseText.includes('success') || responseText.includes('thành công')) {
          setStatus('success');
          setMessage('Cảm ơn bạn đã mua sắm tại NOVA. Đơn hàng của bạn đang được chuẩn bị.');
        } else {
          setStatus('failed');
          setMessage('Giao dịch chưa được hoàn tất hoặc đã bị hủy. Vui lòng thử lại.');
        }
      } catch (err) {
        setStatus('failed');
        setMessage('Xác thực thanh toán thất bại. Vui lòng kiểm tra lại trạng thái đơn hàng.');
      }
    };
    
    if (searchParams.toString()) {
      verifyPayment();
    }
  }, [searchParams]);

  return (
    <div className="min-h-[calc(100vh-250px)] flex items-center justify-center py-12 px-4">
      <div className="max-w-md w-full bg-white rounded-3xl p-8 shadow-xl border border-slate-100 text-center relative overflow-hidden">
        {status === 'loading' && (
          <div className="py-12 flex flex-col items-center">
            <div className="animate-spin w-12 h-12 border-4 border-primary-500 border-t-transparent rounded-full mb-6"></div>
            <h2 className="text-xl font-bold text-slate-900 mb-2">Đang xác thực</h2>
            <p className="text-slate-500">{message}</p>
          </div>
        )}
        
        {status === 'success' && (
          <div className="py-6">
            <div className="w-20 h-20 bg-green-50 rounded-full flex items-center justify-center mx-auto mb-6">
              <CheckCircle className="w-12 h-12 text-green-500" />
            </div>
            <h2 className="text-3xl font-black text-slate-900 mb-3 tracking-tight">Thanh toán thành công!</h2>
            <p className="text-slate-500 mb-8 leading-relaxed">{message}</p>
            
            <div className="flex flex-col gap-3">
              <Link to="/orders" className="bg-primary-600 text-white font-medium px-6 py-3.5 rounded-xl hover:bg-primary-700 transition shadow-sm">
                Xem chi tiết đơn hàng
              </Link>
              <Link to="/products" className="bg-slate-50 text-slate-700 font-medium px-6 py-3.5 rounded-xl hover:bg-slate-100 transition border border-slate-200">
                Tiếp tục mua sắm
              </Link>
            </div>
          </div>
        )}

        {status === 'failed' && (
          <div className="py-6">
            <div className="w-20 h-20 bg-red-50 rounded-full flex items-center justify-center mx-auto mb-6">
              <XCircle className="w-12 h-12 text-red-500" />
            </div>
            <h2 className="text-3xl font-black text-slate-900 mb-3 tracking-tight">Thanh toán thất bại</h2>
            <p className="text-slate-500 mb-8 leading-relaxed">{message}</p>
            
            <div className="flex flex-col gap-3">
              <Link to="/orders" className="bg-primary-600 text-white font-medium px-6 py-3.5 rounded-xl hover:bg-primary-700 transition shadow-sm">
                Trở lại đơn hàng để thử lại
              </Link>
              <Link to="/" className="bg-slate-50 text-slate-700 font-medium px-6 py-3.5 rounded-xl hover:bg-slate-100 transition border border-slate-200">
                Về trang chủ
              </Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default PaymentReturn;
