import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { apiClient } from '../api/client';
import type { CartItem } from '../types';
import { Trash2, ShoppingBag, ArrowRight, ShieldCheck } from 'lucide-react';
import { getProductImage } from '../utils/productImageMap';
import { formatVND } from '../utils/formatCurrency';


const Cart = () => {
  const [items, setItems] = useState<CartItem[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  const fetchCart = async () => {
    try {
      const { data } = await apiClient.get<CartItem[]>('/cart');
      setItems(data);
    } catch (err) {
      console.error('Failed to load cart');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCart();
  }, []);

  const updateQuantity = async (itemId: number, productId: number, quantity: number) => {
    if (quantity < 1) return;
    try {
      await apiClient.put(`/cart/items/${itemId}`, { productId, quantity });
      fetchCart();
    } catch (err) {
      console.error('Failed to update quantity');
    }
  };

  const removeItem = async (itemId: number) => {
    try {
      await apiClient.delete(`/cart/items/${itemId}`);
      fetchCart();
    } catch (err) {
      console.error('Failed to remove item');
    }
  };

  const subtotal = items.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  const shipping = items.length > 0 ? (subtotal > 1000 ? 0 : 15) : 0;
  const total = subtotal + shipping;

  if (loading) return (
    <div className="py-12 flex justify-center">
      <div className="animate-spin w-8 h-8 border-4 border-primary-500 border-t-transparent rounded-full"></div>
    </div>
  );

  return (
    <div className="container mx-auto px-4 max-w-7xl py-8 lg:py-12">
      <h1 className="text-3xl lg:text-4xl font-black text-slate-900 mb-8 tracking-tight">Giỏ hàng của bạn</h1>
      
      {items.length === 0 ? (
        <div className="text-center py-20 bg-white border border-slate-100 rounded-3xl shadow-sm flex flex-col items-center">
          <div className="w-24 h-24 bg-slate-50 rounded-full flex items-center justify-center mb-6">
            <ShoppingBag className="w-12 h-12 text-slate-300" />
          </div>
          <h2 className="text-2xl font-bold text-slate-900 mb-3">Giỏ hàng đang trống</h2>
          <p className="text-slate-500 mb-8 max-w-sm">Chưa có sản phẩm nào trong giỏ hàng. Hãy khám phá các sản phẩm tuyệt vời của NOVA.</p>
          <Link to="/products" className="bg-primary-600 text-white font-medium px-8 py-3 rounded-full hover:bg-primary-700 transition shadow-sm">
            Bắt đầu mua sắm
          </Link>
        </div>
      ) : (
        <div className="flex flex-col lg:flex-row gap-8 items-start">
          {/* Cart Items List */}
          <div className="w-full lg:w-2/3 space-y-4">
            <div className="bg-white border border-slate-100 rounded-2xl p-6 shadow-sm">
              <h2 className="font-bold text-slate-900 mb-6 flex items-center border-b border-slate-100 pb-4">
                Sản phẩm ({items.length})
              </h2>
              
              <div className="space-y-6">
                {items.map(item => (
                  <div key={item.id} className="flex gap-4 sm:gap-6 pb-6 border-b border-slate-100 last:border-0 last:pb-0">
                    <div className="w-24 h-24 sm:w-32 sm:h-32 bg-slate-50 rounded-xl border border-slate-100 flex-shrink-0 flex items-center justify-center overflow-hidden">
                      <img 
                        src={getProductImage(item.productId)} 
                        alt={item.productName} 
                        className="w-full h-full object-cover"
                        onError={(e) => {
                          e.currentTarget.onerror = null;
                          e.currentTarget.src = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='48' height='48' viewBox='0 0 24 24' fill='none' stroke='%23e2e8f0' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z'/%3E%3Cpath d='M3 6h18'/%3E%3Cpath d='M16 10a4 4 0 0 1-8 0'/%3E%3C/svg%3E";
                          e.currentTarget.classList.remove('object-cover', 'w-full', 'h-full');
                          e.currentTarget.classList.add('w-10', 'h-10');
                        }}
                      />
                    </div>
                    
                    <div className="flex-grow flex flex-col justify-between py-1">
                      <div className="flex justify-between items-start gap-4">
                        <div>
                          <Link to={`/products/${item.productId}`} className="font-semibold text-slate-900 hover:text-primary-600 transition text-lg line-clamp-2 mb-1">
                            {item.productName}
                          </Link>
                          <div className="text-primary-600 font-bold">{formatVND(item.price)}</div>
                        </div>
                        <button 
                          onClick={() => removeItem(item.id)} 
                          className="text-slate-400 hover:text-red-500 hover:bg-red-50 p-2 rounded-lg transition"
                          title="Xóa khỏi giỏ hàng"
                        >
                          <Trash2 className="w-5 h-5" />
                        </button>
                      </div>
                      
                      <div className="flex justify-between items-end mt-4">
                        <div className="flex items-center border border-slate-200 rounded-lg p-1 bg-white">
                          <button 
                            onClick={() => updateQuantity(item.id, item.productId, item.quantity - 1)}
                            className="w-8 h-8 flex items-center justify-center rounded-md text-slate-500 hover:bg-slate-100 transition"
                          >-</button>
                          <span className="w-10 text-center font-medium text-slate-900 text-sm">
                            {item.quantity}
                          </span>
                          <button 
                            onClick={() => updateQuantity(item.id, item.productId, item.quantity + 1)}
                            className="w-8 h-8 flex items-center justify-center rounded-md text-slate-500 hover:bg-slate-100 transition"
                          >+</button>
                        </div>
                        <div className="font-bold text-slate-900">
                          {formatVND(item.price * item.quantity)}
                        </div>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>

          {/* Order Summary */}
          <div className="w-full lg:w-1/3">
            <div className="bg-white border border-slate-100 rounded-2xl p-6 shadow-sm sticky top-24">
              <h2 className="font-bold text-slate-900 mb-6 border-b border-slate-100 pb-4">Tổng quan đơn hàng</h2>
              
              <div className="space-y-4 mb-6 text-sm">
                <div className="flex justify-between text-slate-600">
                  <span>Tạm tính</span>
                  <span className="font-medium text-slate-900">{formatVND(subtotal)}</span>
                </div>
                <div className="flex justify-between text-slate-600">
                  <span>Phí vận chuyển</span>
                  <span className="font-medium text-slate-900">
                    {shipping === 0 ? <span className="text-green-600">Miễn phí</span> : formatVND(shipping)}
                  </span>
                </div>
              </div>
              
              <div className="border-t border-slate-100 pt-4 mb-8">
                <div className="flex justify-between items-center mb-1">
                  <span className="font-bold text-slate-900">Tổng cộng</span>
                  <span className="font-black text-2xl text-primary-600">{formatVND(total)}</span>
                </div>
                <p className="text-xs text-slate-500 text-right">Đã bao gồm thuế (nếu có)</p>
              </div>
              
              <button 
                onClick={() => navigate('/checkout')}
                className="w-full bg-primary-600 text-white font-medium py-3.5 rounded-xl hover:bg-primary-700 transition flex justify-center items-center shadow-lg shadow-primary-600/20"
              >
                Tiến hành thanh toán <ArrowRight className="w-5 h-5 ml-2" />
              </button>

              <div className="mt-6 flex items-start p-3 bg-slate-50 rounded-lg border border-slate-100">
                <ShieldCheck className="w-5 h-5 text-green-600 mr-2 shrink-0 mt-0.5" />
                <p className="text-xs text-slate-600">
                  Giao dịch của bạn được bảo mật tuyệt đối. Chúng tôi không lưu trữ thông tin thẻ thanh toán của bạn.
                </p>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Cart;
