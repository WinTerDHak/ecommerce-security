import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiClient } from '../api/client';
import type { Order } from '../types';
import { Package, ChevronRight, Clock, CheckCircle, XCircle } from 'lucide-react';
import { formatVND } from '../utils/formatCurrency';


const Orders = () => {
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchOrders = async () => {
      try {
        const { data } = await apiClient.get<Order[]>('/orders');
        setOrders(data);
      } catch (err) {
        console.error('Failed to load orders');
      } finally {
        setLoading(false);
      }
    };
    fetchOrders();
  }, []);

  if (loading) return (
    <div className="py-12 flex justify-center">
      <div className="animate-spin w-8 h-8 border-4 border-primary-500 border-t-transparent rounded-full"></div>
    </div>
  );

  return (
    <div className="container mx-auto px-4 py-8 max-w-5xl">
      <h1 className="text-3xl font-black text-slate-900 mb-8 tracking-tight">Đơn hàng của tôi</h1>
      
      {orders.length === 0 ? (
        <div className="text-center py-20 bg-white border border-slate-100 rounded-3xl shadow-sm flex flex-col items-center">
          <div className="w-20 h-20 bg-slate-50 rounded-full flex items-center justify-center mb-6">
            <Package className="w-10 h-10 text-slate-300" />
          </div>
          <h2 className="text-xl font-bold text-slate-900 mb-2">Chưa có đơn hàng nào</h2>
          <p className="text-slate-500 mb-6">Bạn chưa thực hiện giao dịch nào tại NOVA.</p>
          <Link to="/products" className="bg-primary-600 text-white font-medium px-6 py-2.5 rounded-full hover:bg-primary-700 transition">
            Mua sắm ngay
          </Link>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map(order => (
            <div key={order.id} className="bg-white border border-slate-100 rounded-2xl p-6 shadow-sm hover:shadow-md transition">
              <div className="flex flex-col sm:flex-row justify-between sm:items-center gap-4">
                <div>
                  <div className="flex items-center gap-3 mb-2">
                    <span className="font-bold text-lg text-slate-900">#{order.id}</span>
                    <span className={`px-2.5 py-1 rounded-md text-xs font-bold tracking-wider uppercase flex items-center ${order.status === 'PAID' ? 'bg-green-50 text-green-700' : order.status === 'PENDING' ? 'bg-yellow-50 text-yellow-700' : 'bg-red-50 text-red-700'}`}>
                      {order.status === 'PAID' && <CheckCircle className="w-3 h-3 mr-1" />}
                      {order.status === 'PENDING' && <Clock className="w-3 h-3 mr-1" />}
                      {order.status === 'FAILED' && <XCircle className="w-3 h-3 mr-1" />}
                      {order.status}
                    </span>
                  </div>
                  <p className="text-sm text-slate-500 flex items-center">
                    <Clock className="w-3.5 h-3.5 mr-1" />
                    {new Date(order.createdAt).toLocaleString('vi-VN')}
                  </p>
                </div>
                
                <div className="flex flex-col sm:items-end gap-3">
                  <div className="font-black text-xl text-primary-600">
                    {formatVND(order.totalAmount)}
                  </div>
                  <Link 
                    to={`/orders/${order.id}`}
                    className="inline-flex items-center justify-center text-sm font-medium bg-slate-50 hover:bg-slate-100 text-slate-700 px-4 py-2 rounded-lg transition border border-slate-200"
                  >
                    Xem chi tiết <ChevronRight className="w-4 h-4 ml-1" />
                  </Link>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Orders;
