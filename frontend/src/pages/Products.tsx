import { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { apiClient } from '../api/client';
import type { Product } from '../types';
import { Search, ShoppingBag, Filter } from 'lucide-react';
import { getProductImage } from '../utils/productImageMap';
import { formatVND } from '../utils/formatCurrency';


const Products = () => {
  const [products, setProducts] = useState<Product[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchParams, setSearchParams] = useSearchParams();
  const q = searchParams.get('q') || '';
  const categoryId = searchParams.get('category') || '';
  const [searchInput, setSearchInput] = useState(q);

  const fetchProducts = async (searchQuery: string = '', catId: string = '') => {
    setLoading(true);
    try {
      let url = '/products';
      if (searchQuery) url += `?q=${encodeURIComponent(searchQuery)}`;
      
      const { data } = await apiClient.get<Product[]>(url);
      
      // Since backend doesn't support category filtering by query out-of-the-box in the old implementation, 
      // we filter locally if categoryId is provided
      let filteredData = data;
      if (catId) {
        filteredData = data.filter(p => p.categoryId?.toString() === catId);
      }
      
      setProducts(filteredData);
    } catch (err) {
      console.error('Failed to fetch products', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    setSearchInput(q);
    fetchProducts(q, categoryId);
  }, [q, categoryId]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchInput.trim()) {
      setSearchParams({ q: searchInput });
    } else {
      setSearchParams({});
    }
  };

  return (
    <div className="container mx-auto px-4 max-w-7xl py-8">
      {/* Header & Search */}
      <div className="mb-12">
        <h1 className="text-4xl font-black text-slate-900 tracking-tight mb-4">Khám phá sản phẩm</h1>
        <p className="text-lg text-slate-500 mb-8 max-w-3xl">
          Tìm kiếm những sản phẩm phù hợp nhất với phong cách và nhu cầu của bạn.
        </p>

        <form onSubmit={handleSearch} className="flex gap-4 max-w-2xl">
          <div className="relative flex-grow">
            <input 
              type="text" 
              placeholder="Tìm kiếm sản phẩm, thương hiệu..." 
              value={searchInput}
              onChange={(e) => setSearchInput(e.target.value)}
              className="w-full pl-12 pr-4 py-3 bg-white border border-slate-200 rounded-xl outline-none focus:ring-2 focus:ring-primary-500 focus:border-transparent transition-all shadow-sm text-slate-700"
            />
            <Search className="absolute left-4 top-3.5 w-5 h-5 text-slate-400" />
          </div>
          <button type="submit" className="bg-primary-600 text-white font-medium px-6 py-3 rounded-xl hover:bg-primary-700 transition shadow-sm">
            Tìm kiếm
          </button>
        </form>
      </div>

      <div className="flex flex-col md:flex-row gap-8">
        {/* Sidebar Filters */}
        <div className="w-full md:w-60 lg:w-64 shrink-0">
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm sticky top-24">
            <div className="flex items-center gap-2 font-bold text-slate-900 mb-4 pb-4 border-b">
              <Filter className="w-5 h-5" /> Bộ lọc
            </div>
            
            <h3 className="font-semibold text-slate-900 mb-3 text-sm uppercase tracking-wider">Danh mục</h3>
            <ul className="space-y-2 mb-6">
              <li>
                <button onClick={() => setSearchParams({})} className={`text-left w-full px-3 py-2 rounded-lg text-sm transition-colors ${!categoryId ? 'bg-primary-50 text-primary-700 font-medium' : 'text-slate-600 hover:bg-slate-50'}`}>
                  Tất cả sản phẩm
                </button>
              </li>
              {[
                { id: 1, name: 'Điện tử & Công nghệ' },
                { id: 2, name: 'Sách & Học tập' },
                { id: 3, name: 'Thời trang' },
                { id: 4, name: 'Nhà cửa & Đời sống' },
                { id: 5, name: 'Thể thao' },
              ].map(cat => (
                <li key={cat.id}>
                  <button 
                    onClick={() => setSearchParams({ category: cat.id.toString() })} 
                    className={`text-left w-full px-3 py-2 rounded-lg text-sm transition-colors ${categoryId === cat.id.toString() ? 'bg-primary-50 text-primary-700 font-medium' : 'text-slate-600 hover:bg-slate-50'}`}
                  >
                    {cat.name}
                  </button>
                </li>
              ))}
            </ul>
          </div>
        </div>

        {/* Product Grid */}
        <div className="flex-grow">
          {loading ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {[1, 2, 3, 4, 5, 6].map(i => (
                <div key={i} className="animate-pulse bg-white p-4 rounded-2xl border border-slate-100">
                  <div className="bg-slate-200 aspect-square rounded-xl mb-4"></div>
                  <div className="h-4 bg-slate-200 rounded w-3/4 mb-3"></div>
                  <div className="h-3 bg-slate-200 rounded w-1/2 mb-4"></div>
                  <div className="h-6 bg-slate-200 rounded w-1/3"></div>
                </div>
              ))}
            </div>
          ) : products.length === 0 ? (
            <div className="text-center py-24 bg-white rounded-2xl border border-slate-100 shadow-sm flex flex-col items-center">
              <div className="w-20 h-20 bg-slate-50 rounded-full flex items-center justify-center mb-4">
                <ShoppingBag className="w-10 h-10 text-slate-300" />
              </div>
              <h3 className="text-xl font-bold text-slate-900 mb-2">Không tìm thấy sản phẩm</h3>
              <p className="text-slate-500 mb-6">Thử điều chỉnh từ khóa tìm kiếm hoặc xóa bộ lọc.</p>
              {(q || categoryId) && (
                <button onClick={() => setSearchParams({})} className="bg-slate-900 text-white px-6 py-2 rounded-lg font-medium hover:bg-slate-800 transition">
                  Xóa bộ lọc
                </button>
              )}
            </div>
          ) : (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
              {products.map(product => (
                <Link to={`/products/${product.id}`} key={product.id} className="group bg-white p-4 rounded-2xl border border-slate-100 shadow-sm hover:shadow-xl hover:border-primary-100 transition-all flex flex-col h-full">
                  <div className="relative aspect-square bg-slate-50 rounded-xl mb-4 flex items-center justify-center overflow-hidden">
                    <img 
                      src={getProductImage(product.id)} 
                      alt={product.name} 
                      className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
                      onError={(e) => {
                        e.currentTarget.onerror = null;
                        e.currentTarget.src = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='48' height='48' viewBox='0 0 24 24' fill='none' stroke='%23e2e8f0' stroke-width='2' stroke-linecap='round' stroke-linejoin='round'%3E%3Cpath d='M6 2 3 6v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6l-3-4Z'/%3E%3Cpath d='M3 6h18'/%3E%3Cpath d='M16 10a4 4 0 0 1-8 0'/%3E%3C/svg%3E";
                        e.currentTarget.classList.remove('object-cover', 'w-full', 'h-full');
                        e.currentTarget.classList.add('w-12', 'h-12');
                      }}
                    />
                    {product.stockQuantity < 10 && (
                      <div className="absolute top-3 right-3 bg-rose-500 text-white text-xs font-bold px-2 py-1 rounded">
                        Sắp hết
                      </div>
                    )}
                  </div>
                  <div className="flex-grow">
                    <span className="text-xs font-semibold text-primary-600 tracking-wider uppercase mb-1 block">
                      {product.categoryName || 'Sản phẩm'}
                    </span>
                    <h3 className="font-semibold text-slate-900 mb-2 line-clamp-2 group-hover:text-primary-600 transition-colors">
                      {product.name}
                    </h3>
                    <div className="font-black text-lg text-slate-900 mb-3">
                      {formatVND(product.price)}
                    </div>
                  </div>
                </Link>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Products;
