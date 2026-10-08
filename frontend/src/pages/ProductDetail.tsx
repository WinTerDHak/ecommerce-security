import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { apiClient } from '../api/client';
import type { Product } from '../types';
import { useAuth } from '../context/AuthContext';
import { ShieldCheck, Truck, ShoppingCart, CheckCircle, ChevronRight, ShoppingBag } from 'lucide-react';
import { getProductImage } from '../utils/productImageMap';
import { formatVND } from '../utils/formatCurrency';


const ProductDetail = () => {
  const { id } = useParams<{ id: string }>();
  const [product, setProduct] = useState<Product | null>(null);
  const [loading, setLoading] = useState(true);
  const [addingToCart, setAddingToCart] = useState(false);
  const [quantity, setQuantity] = useState(1);
  const [error, setError] = useState('');
  
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    const fetchProduct = async () => {
      try {
        const { data } = await apiClient.get<Product>(`/products/${id}`);
        setProduct(data);
      } catch (err: any) {
        if (err.response?.status === 404) {
          setError('Sản phẩm không tồn tại');
        } else {
          setError('Không thể tải thông tin sản phẩm');
        }
      } finally {
        setLoading(false);
      }
    };
    fetchProduct();
  }, [id]);

  const handleAddToCart = async () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }

    setAddingToCart(true);
    try {
      await apiClient.post('/cart/items', { 
        productId: product?.id, 
        quantity 
      });
      navigate('/cart');
    } catch (err: any) {
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi thêm vào giỏ hàng');
    } finally {
      setAddingToCart(false);
    }
  };

  if (loading) return (
    <div className="py-12 flex justify-center">
      <div className="animate-spin w-8 h-8 border-4 border-primary-500 border-t-transparent rounded-full"></div>
    </div>
  );

  if (error || !product) return (
    <div className="py-24 text-center max-w-md mx-auto">
      <div className="w-16 h-16 bg-red-50 text-red-500 rounded-full flex items-center justify-center mx-auto mb-4">
        <ShoppingCart className="w-8 h-8" />
      </div>
      <h2 className="text-2xl font-bold text-slate-900 mb-2">{error || 'Không tìm thấy'}</h2>
      <button onClick={() => navigate('/products')} className="mt-6 bg-primary-600 text-white px-6 py-2 rounded-lg font-medium">
        Quay lại cửa hàng
      </button>
    </div>
  );

  return (
    <div className="container mx-auto px-4 max-w-7xl py-8 lg:py-12">
      {/* Breadcrumb */}
      <div className="flex items-center text-sm text-slate-500 mb-8">
        <button onClick={() => navigate('/')} className="hover:text-primary-600">Trang chủ</button>
        <ChevronRight className="w-4 h-4 mx-2" />
        <button onClick={() => navigate('/products')} className="hover:text-primary-600">Sản phẩm</button>
        <ChevronRight className="w-4 h-4 mx-2" />
        <span className="text-slate-900 font-medium truncate max-w-xs">{product.name}</span>
      </div>

      <div className="bg-white rounded-3xl p-6 lg:p-12 shadow-sm border border-slate-100 flex flex-col lg:flex-row gap-12">
        {/* Left: Product Visual */}
        <div className="lg:w-1/2">
          <div className="aspect-square bg-slate-50 rounded-2xl border border-slate-100 flex items-center justify-center mb-6 overflow-hidden">
            <img 
              src={getProductImage(product.id)} 
              alt={product.name} 
              className="w-full h-full object-cover"
              onError={(e) => {
                e.currentTarget.onerror = null;
                e.currentTarget.src = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='128' height='128' viewBox='0 0 24 24' fill='none' stroke='%23e2e8f0' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z'/%3E%3Cpath d='M3 6h18'/%3E%3Cpath d='M16 10a4 4 0 0 1-8 0'/%3E%3C/svg%3E";
                e.currentTarget.classList.remove('object-cover', 'w-full', 'h-full');
                e.currentTarget.classList.add('w-32', 'h-32');
              }}
            />
          </div>
          <div className="grid grid-cols-4 gap-4">
             {/* Thumbnail placeholders */}
             {[1, 2, 3, 4].map(i => (
                <div key={i} className={`aspect-square rounded-xl flex items-center justify-center border-2 cursor-pointer transition-all overflow-hidden ${i === 1 ? 'border-primary-500 bg-white' : 'border-slate-100 bg-slate-50 hover:border-slate-300'}`}>
                  {i === 1 ? (
                    <img src={getProductImage(product.id)} alt={product.name} className="w-full h-full object-cover" />
                  ) : (
                    <ShoppingBag className={`w-8 h-8 text-slate-200`} />
                  )}
                </div>
             ))}
          </div>
        </div>

        {/* Right: Product Details */}
        <div className="lg:w-1/2 flex flex-col">
          <div className="mb-6 border-b border-slate-100 pb-6">
            <div className="flex justify-between items-start mb-2">
              <span className="text-sm font-bold text-primary-600 tracking-wider uppercase">
                {product.categoryName || 'Sản phẩm'}
              </span>
              {product.stockQuantity > 0 ? (
                <span className="inline-flex items-center text-green-700 bg-green-50 px-2.5 py-1 rounded-md text-xs font-semibold">
                  <CheckCircle className="w-3 h-3 mr-1" /> Còn hàng ({product.stockQuantity})
                </span>
              ) : (
                <span className="inline-flex items-center text-red-700 bg-red-50 px-2.5 py-1 rounded-md text-xs font-semibold">
                  Hết hàng
                </span>
              )}
            </div>
            
            <h1 className="text-3xl md:text-4xl font-black text-slate-900 mb-4 leading-tight">
              {product.name}
            </h1>
            
            <div className="text-3xl font-bold text-slate-900 mb-4">
              {formatVND(product.price)}
            </div>
            
            <p className="text-slate-600 leading-relaxed text-lg">
              {product.description}
            </p>
          </div>

          <div className="mb-8">
            <label className="block text-sm font-semibold text-slate-900 mb-3">Số lượng</label>
            <div className="flex items-center space-x-4">
              <div className="flex items-center border border-slate-200 rounded-lg p-1 bg-white">
                <button 
                  onClick={() => setQuantity(Math.max(1, quantity - 1))}
                  className="w-10 h-10 flex items-center justify-center rounded-md text-slate-500 hover:bg-slate-100 transition"
                >
                  -
                </button>
                <input 
                  type="number"
                  value={quantity}
                  onChange={(e) => setQuantity(Math.max(1, parseInt(e.target.value) || 1))}
                  className="w-14 text-center font-semibold text-slate-900 border-none focus:ring-0 p-0"
                  min="1"
                  max={product.stockQuantity}
                />
                <button 
                  onClick={() => setQuantity(Math.min(product.stockQuantity, quantity + 1))}
                  className="w-10 h-10 flex items-center justify-center rounded-md text-slate-500 hover:bg-slate-100 transition"
                >
                  +
                </button>
              </div>
            </div>
          </div>

          <div className="flex gap-4 mb-10 mt-auto">
            <button 
              onClick={handleAddToCart}
              disabled={addingToCart || product.stockQuantity === 0}
              className="flex-1 bg-primary-600 text-white font-semibold py-4 rounded-xl hover:bg-primary-700 focus:ring-4 focus:ring-primary-100 disabled:opacity-50 transition-all flex justify-center items-center shadow-lg shadow-primary-600/20"
            >
              {addingToCart ? 'Đang thêm...' : 'Thêm vào giỏ hàng'}
            </button>
          </div>

          {/* Security & Shipping Guarantees */}
          <div className="bg-slate-50 rounded-xl p-5 border border-slate-100">
            <div className="flex items-start mb-4">
              <ShieldCheck className="w-5 h-5 text-green-600 mt-0.5 mr-3 shrink-0" />
              <div>
                <h4 className="font-semibold text-slate-900 text-sm">Thanh toán an toàn</h4>
                <p className="text-xs text-slate-500 mt-0.5">Mọi giao dịch được mã hóa và xử lý an toàn qua VNPAY.</p>
              </div>
            </div>
            <div className="flex items-start">
              <Truck className="w-5 h-5 text-primary-600 mt-0.5 mr-3 shrink-0" />
              <div>
                <h4 className="font-semibold text-slate-900 text-sm">Vận chuyển miễn phí</h4>
                <p className="text-xs text-slate-500 mt-0.5">Nhận hàng trong 2-3 ngày làm việc đối với đơn hàng tiêu chuẩn.</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ProductDetail;
