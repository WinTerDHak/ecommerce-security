import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { apiClient } from '../api/client';
import type { Product } from '../types';
import { ShieldCheck, Truck, RotateCcw, CreditCard, ChevronRight, ShoppingBag, Star, Zap } from 'lucide-react';
import { getProductImage } from '../utils/productImageMap';
import { formatVND } from '../utils/formatCurrency';


const Home = () => {
  const [featuredProducts, setFeaturedProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const { data } = await apiClient.get<Product[]>('/products');
        setFeaturedProducts(data.slice(0, 8)); // Just show top 8
      } catch (err) {
        console.error('Failed to fetch featured products', err);
      } finally {
        setLoading(false);
      }
    };
    fetchProducts();
  }, []);

  return (
    <div className="-mt-8"> {/* Negative margin to offset App.tsx main padding for full bleed sections */}
      
      {/* Hero Section */}
      <section className="bg-slate-900 text-white py-20 lg:py-28 relative overflow-hidden">
        <div className="absolute inset-0 opacity-10">
          <div className="absolute -top-24 -right-24 w-96 h-96 bg-primary-500 rounded-full blur-3xl"></div>
          <div className="absolute top-1/2 -left-24 w-72 h-72 bg-blue-500 rounded-full blur-3xl"></div>
        </div>
        <div className="container mx-auto px-4 max-w-7xl relative z-10 flex flex-col md:flex-row items-center">
          <div className="md:w-1/2 mb-12 md:mb-0">
            <span className="inline-block py-1 px-3 rounded-full bg-primary-500/20 text-primary-300 text-sm font-semibold tracking-wider mb-6 border border-primary-500/30">
              SMART SHOPPING 2026
            </span>
            <h1 className="text-4xl md:text-5xl lg:text-6xl font-black leading-tight mb-6 tracking-tight">
              Công nghệ hiện đại <br/>
              <span className="text-transparent bg-clip-text bg-gradient-to-r from-cyan-400 to-blue-400">
                nâng tầm cuộc sống.
              </span>
            </h1>
            <p className="text-lg text-slate-300 mb-8 max-w-lg leading-relaxed">
              Khám phá các bộ sưu tập thiết bị điện tử, lifestyle và phụ kiện cao cấp. Trải nghiệm mua sắm an toàn và tin cậy tuyệt đối tại NOVA.
            </p>
            <div className="flex flex-wrap gap-4">
              <Link to="/products" className="bg-primary-600 hover:bg-primary-500 text-white font-medium px-8 py-3.5 rounded-full transition-all shadow-lg shadow-primary-600/30 flex items-center">
                Khám phá ngay <ChevronRight className="w-5 h-5 ml-1" />
              </Link>
              <Link to="/products?category=1" className="bg-slate-800 hover:bg-slate-700 text-white font-medium px-8 py-3.5 rounded-full border border-slate-700 transition-all">
                Xem ưu đãi
              </Link>
            </div>
          </div>
          <div className="md:w-1/2 flex justify-center md:justify-end">
            <div className="relative w-full max-w-md aspect-square rounded-2xl bg-gradient-to-tr from-slate-800 to-slate-700 p-8 shadow-2xl border border-slate-700 transform rotate-2 hover:rotate-0 transition-transform duration-500">
              <div className="absolute inset-0 bg-white/5 rounded-2xl backdrop-blur-sm"></div>
              <div className="w-full h-full border border-slate-600/50 rounded-xl flex items-center justify-center flex-col text-slate-400 bg-slate-900/50">
                 <ShoppingBag className="w-20 h-20 mb-4 opacity-50" />
                 <span className="font-medium tracking-widest uppercase">Nova Collection</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Trust Strip */}
      <section className="border-b bg-white">
        <div className="container mx-auto px-4 max-w-7xl">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 py-8">
            <div className="flex items-center justify-center space-x-3 text-slate-700">
              <ShieldCheck className="w-6 h-6 text-primary-600" />
              <span className="font-medium text-sm">Thanh toán an toàn</span>
            </div>
            <div className="flex items-center justify-center space-x-3 text-slate-700">
              <RotateCcw className="w-6 h-6 text-primary-600" />
              <span className="font-medium text-sm">Đổi trả 30 ngày</span>
            </div>
            <div className="flex items-center justify-center space-x-3 text-slate-700">
              <Truck className="w-6 h-6 text-primary-600" />
              <span className="font-medium text-sm">Giao hàng tận nơi</span>
            </div>
            <div className="flex items-center justify-center space-x-3 text-slate-700">
              <CreditCard className="w-6 h-6 text-primary-600" />
              <span className="font-medium text-sm">Bảo mật thẻ 100%</span>
            </div>
          </div>
        </div>
      </section>

      {/* Featured Categories */}
      <section className="py-20 bg-slate-50">
        <div className="container mx-auto px-4 max-w-7xl">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-slate-900 mb-4 tracking-tight">Danh mục nổi bật</h2>
            <p className="text-slate-500 max-w-2xl mx-auto">Lựa chọn từ các danh mục sản phẩm được yêu thích nhất của chúng tôi</p>
          </div>
          
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
            {[
              { id: 1, name: 'Điện tử & Công nghệ', icon: Zap, color: 'bg-blue-100 text-blue-600' },
              { id: 2, name: 'Sách & Học tập', icon: Star, color: 'bg-purple-100 text-purple-600' },
              { id: 3, name: 'Thời trang', icon: ShoppingBag, color: 'bg-rose-100 text-rose-600' },
              { id: 4, name: 'Nhà cửa & Đời sống', icon: ShieldCheck, color: 'bg-green-100 text-green-600' }
            ].map((cat) => (
              <Link to={`/products?category=${cat.id}`} key={cat.id} className="group bg-white p-6 rounded-2xl shadow-sm border border-slate-100 hover:shadow-md transition text-center flex flex-col items-center">
                <div className={`w-14 h-14 rounded-full flex items-center justify-center mb-4 transition-transform group-hover:scale-110 ${cat.color}`}>
                  <cat.icon className="w-6 h-6" />
                </div>
                <h3 className="font-semibold text-slate-900">{cat.name}</h3>
                <span className="text-sm text-slate-500 mt-1 flex items-center opacity-0 group-hover:opacity-100 transition-opacity">
                  Khám phá <ChevronRight className="w-4 h-4 ml-1" />
                </span>
              </Link>
            ))}
          </div>
        </div>
      </section>

      {/* Featured Products */}
      <section className="py-20 bg-white">
        <div className="container mx-auto px-4 max-w-7xl">
          <div className="flex justify-between items-end mb-10">
            <div>
              <h2 className="text-3xl font-bold text-slate-900 mb-3 tracking-tight">Sản phẩm mới nhất</h2>
              <p className="text-slate-500">Cập nhật công nghệ và xu hướng mới nhất</p>
            </div>
            <Link to="/products" className="text-primary-600 font-medium hover:text-primary-700 hidden sm:flex items-center transition">
              Xem tất cả <ChevronRight className="w-5 h-5 ml-1" />
            </Link>
          </div>

          {loading ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
              {[1, 2, 3, 4].map(i => (
                <div key={i} className="animate-pulse">
                  <div className="bg-slate-200 aspect-[4/3] rounded-xl mb-4"></div>
                  <div className="h-4 bg-slate-200 rounded w-3/4 mb-3"></div>
                  <div className="h-3 bg-slate-200 rounded w-1/2 mb-4"></div>
                  <div className="h-5 bg-slate-200 rounded w-1/4"></div>
                </div>
              ))}
            </div>
          ) : featuredProducts.length === 0 ? (
            <div className="text-center py-12 text-slate-500 bg-slate-50 rounded-2xl">
              Chưa có sản phẩm nào.
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-8">
              {featuredProducts.map(product => (
                <Link to={`/products/${product.id}`} key={product.id} className="group flex flex-col h-full">
                  <div className="relative aspect-[4/3] bg-slate-100 rounded-xl mb-4 overflow-hidden flex items-center justify-center border border-slate-200 group-hover:border-primary-200 transition-colors">
                    <img 
                      src={getProductImage(product.id)} 
                      alt={product.name} 
                      className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                      onError={(e) => {
                        e.currentTarget.onerror = null;
                        e.currentTarget.src = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='48' height='48' viewBox='0 0 24 24' fill='none' stroke='%23e2e8f0' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z'/%3E%3Cpath d='M3 6h18'/%3E%3Cpath d='M16 10a4 4 0 0 1-8 0'/%3E%3C/svg%3E";
                        e.currentTarget.classList.remove('object-cover', 'w-full', 'h-full');
                        e.currentTarget.classList.add('w-12', 'h-12');
                      }}
                    />
                    {/* Hover Overlay */}
                    <div className="absolute inset-0 bg-slate-900/5 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                      <span className="bg-white text-slate-900 font-medium px-4 py-2 rounded-full shadow-sm text-sm transform translate-y-4 group-hover:translate-y-0 transition-all">
                        Xem chi tiết
                      </span>
                    </div>
                  </div>
                  <div className="flex-grow">
                    <span className="text-xs font-semibold text-primary-600 tracking-wider uppercase mb-1 block">
                      {product.categoryName || 'Sản phẩm'}
                    </span>
                    <h3 className="font-semibold text-slate-900 mb-1 line-clamp-1 group-hover:text-primary-600 transition-colors">
                      {product.name}
                    </h3>
                    <p className="text-slate-500 text-sm line-clamp-2 mb-3">
                      {product.description}
                    </p>
                  </div>
                  <div className="flex justify-between items-center mt-auto pt-2">
                    <span className="font-bold text-lg text-slate-900">
                      {formatVND(product.price)}
                    </span>
                    {product.stockQuantity < 10 && (
                      <span className="text-xs font-medium text-orange-600 bg-orange-50 px-2 py-1 rounded">
                        Sắp hết
                      </span>
                    )}
                  </div>
                </Link>
              ))}
            </div>
          )}
          
          <div className="mt-8 text-center sm:hidden">
            <Link to="/products" className="inline-flex items-center text-primary-600 font-medium">
              Xem tất cả <ChevronRight className="w-5 h-5 ml-1" />
            </Link>
          </div>
        </div>
      </section>

      {/* Security USP Section */}
      <section className="py-24 bg-slate-900 text-white relative overflow-hidden">
        <div className="absolute top-0 right-0 w-1/2 h-full bg-gradient-to-l from-primary-900/40 to-transparent"></div>
        <div className="container mx-auto px-4 max-w-7xl relative z-10">
          <div className="max-w-2xl">
            <ShieldCheck className="w-12 h-12 text-primary-400 mb-6" />
            <h2 className="text-3xl md:text-4xl font-bold mb-6 tracking-tight">
              An toàn tuyệt đối trong từng giao dịch.
            </h2>
            <p className="text-lg text-slate-300 mb-8 leading-relaxed">
              Chúng tôi cam kết bảo vệ thông tin của bạn. Không lưu trữ thông tin thẻ nhạy cảm, sử dụng xác thực bảo mật đa tầng, và cổng thanh toán được mã hóa 100%.
            </p>
            <ul className="space-y-4 mb-10">
              <li className="flex items-center text-slate-200">
                <div className="w-6 h-6 rounded-full bg-primary-500/20 flex items-center justify-center mr-3">
                  <div className="w-2 h-2 rounded-full bg-primary-400"></div>
                </div>
                Thanh toán an toàn qua cổng VNPAY nội địa
              </li>
              <li className="flex items-center text-slate-200">
                <div className="w-6 h-6 rounded-full bg-primary-500/20 flex items-center justify-center mr-3">
                  <div className="w-2 h-2 rounded-full bg-primary-400"></div>
                </div>
                Dữ liệu được mã hóa và bảo vệ theo tiêu chuẩn OWASP
              </li>
              <li className="flex items-center text-slate-200">
                <div className="w-6 h-6 rounded-full bg-primary-500/20 flex items-center justify-center mr-3">
                  <div className="w-2 h-2 rounded-full bg-primary-400"></div>
                </div>
                Kiểm soát quyền truy cập nghiêm ngặt
              </li>
            </ul>
            <Link to="/products" className="inline-flex items-center bg-white text-slate-900 font-medium px-8 py-3.5 rounded-full hover:bg-slate-100 transition-colors">
              Mua sắm an tâm ngay <ChevronRight className="w-5 h-5 ml-2" />
            </Link>
          </div>
        </div>
      </section>

    </div>
  );
};

export default Home;
