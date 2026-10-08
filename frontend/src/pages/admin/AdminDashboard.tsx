import { useState, useEffect } from "react";
import { ShieldAlert, Users, Package, Plus, X, FileText } from "lucide-react";
import { useAuth } from "../../context/AuthContext";
import { apiClient } from "../../api/client";
import type { Product, Category } from "../../types";
import { formatVND } from '../../utils/formatCurrency';


interface AdminOrder {
  id: number;
  orderNumber: string;
  customerName: string;
  customerEmail: string;
  totalAmount: number;
  orderStatus: string;
  paymentStatus: string;
  createdAt: string;
}

interface AdminOrderItem {
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
}

interface AdminOrderDetail extends AdminOrder {
  paymentRef: string | null;
  paymentTime: string | null;
  items: AdminOrderItem[];
}

interface AdminUserList {
  id: number;
  name: string;
  email: string;
  role: string;
  active: boolean;
  createdAt: string;
}

interface AdminUserDetail extends AdminUserList {
  firstName: string;
  lastName: string;
  phone: string;
  updatedAt: string;
  orderCount: number;
}

const AdminDashboard = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<'products' | 'orders' | 'users'>('products');
  
  const [products, setProducts] = useState<Product[]>([]);
  const [categories, setCategories] = useState<Category[]>([]);
  const [isProductModalOpen, setIsProductModalOpen] = useState(false);
  const [editingProduct, setEditingProduct] = useState<Product | null>(null);
  const [productForm, setProductForm] = useState({
    name: "", price: "", stockQuantity: "", categoryId: "", description: "", active: true
  });

  const [orders, setOrders] = useState<AdminOrder[]>([]);
  const [selectedOrder, setSelectedOrder] = useState<AdminOrderDetail | null>(null);
  const [isOrderModalOpen, setIsOrderModalOpen] = useState(false);
  const [orderStatusForm, setOrderStatusForm] = useState("");

  const [users, setUsers] = useState<AdminUserList[]>([]);
  const [selectedUser, setSelectedUser] = useState<AdminUserDetail | null>(null);
  const [isUserModalOpen, setIsUserModalOpen] = useState(false);
  const [searchUserStr, setSearchUserStr] = useState("");

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [prodRes, catRes, ordRes, usrRes] = await Promise.all([
        apiClient.get<Product[]>("/admin/products"),
        apiClient.get<Category[]>("/categories"),
        apiClient.get<AdminOrder[]>("/admin/orders"),
        apiClient.get<AdminUserList[]>("/admin/users")
      ]);
      setProducts(prodRes.data);
      setCategories(catRes.data);
      setOrders(ordRes.data);
      setUsers(usrRes.data);
    } catch (err: any) {
      setError("Không thể tải dữ liệu.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleOpenProductModal = (product?: Product) => {
    if (product) {
      setEditingProduct(product);
      setProductForm({
        name: product.name, price: product.price.toString(), stockQuantity: product.stockQuantity.toString(),
        categoryId: product.categoryId.toString(), description: product.description || "", active: product.active
      });
    } else {
      setEditingProduct(null);
      setProductForm({
        name: "", price: "", stockQuantity: "", categoryId: categories.length > 0 ? categories[0].id.toString() : "",
        description: "", active: true
      });
    }
    setError(null); setSuccess(null); setIsProductModalOpen(true);
  };

  const handleSaveProduct = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null); setSuccess(null);
    if (!productForm.name.trim()) return setError("Vui lòng nhập tên sản phẩm.");
    if (Number(productForm.price) <= 0) return setError("Giá phải lớn hơn 0.");
    if (Number(productForm.stockQuantity) < 0) return setError("Số lượng không được âm.");
    if (!productForm.categoryId) return setError("Vui lòng chọn danh mục.");

    const payload = {
      name: productForm.name, price: Number(productForm.price), stockQuantity: Number(productForm.stockQuantity),
      categoryId: Number(productForm.categoryId), description: productForm.description, active: productForm.active
    };

    try {
      if (editingProduct) {
        await apiClient.put(`/admin/products/${editingProduct.id}`, payload);
        setSuccess("Cập nhật sản phẩm thành công!");
      } else {
        await apiClient.post("/admin/products", payload);
        setSuccess("Thêm sản phẩm thành công!");
      }
      setIsProductModalOpen(false);
      fetchData();
    } catch (err: any) {
      setError(err.response?.data?.message || "Lỗi khi lưu sản phẩm.");
    }
  };

  const handleDeactivate = async (id: number) => {
    if (window.confirm("Bạn có chắc muốn vô hiệu hóa sản phẩm này?")) {
      try {
        await apiClient.delete(`/admin/products/${id}`);
        setSuccess("Vô hiệu hóa thành công.");
        fetchData();
      } catch (err: any) {
        setError(err.response?.data?.message || "Lỗi khi vô hiệu hóa.");
      }
    }
  };

  const handleOpenOrderModal = async (orderId: number) => {
    setError(null); setSuccess(null);
    try {
      const res = await apiClient.get<AdminOrderDetail>(`/admin/orders/${orderId}`);
      setSelectedOrder(res.data);
      setOrderStatusForm(res.data.orderStatus);
      setIsOrderModalOpen(true);
    } catch (err: any) {
      setError("Không thể lấy thông tin đơn hàng.");
    }
  };

  const handleUpdateOrderStatus = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedOrder) return;
    setError(null); setSuccess(null);
    try {
      await apiClient.put(`/admin/orders/${selectedOrder.id}/status`, { status: orderStatusForm });
      setSuccess("Cập nhật trạng thái đơn hàng thành công!");
      setIsOrderModalOpen(false);
      fetchData();
    } catch (err: any) {
      setError(err.response?.data?.message || "Lỗi cập nhật trạng thái.");
    }
  };

  const handleOpenUserModal = async (userId: number) => {
    setError(null); setSuccess(null);
    try {
      const res = await apiClient.get<AdminUserDetail>(`/admin/users/${userId}`);
      setSelectedUser(res.data);
      setIsUserModalOpen(true);
    } catch (err: any) {
      setError("Không thể lấy thông tin người dùng.");
    }
  };

  const handleToggleUserStatus = async () => {
    if (!selectedUser) return;
    const newStatus = !selectedUser.active;
    if (!window.confirm(`Bạn có chắc muốn ${newStatus ? 'MỞ KHÓA' : 'KHÓA'} tài khoản này?`)) return;

    setError(null); setSuccess(null);
    try {
      await apiClient.put(`/admin/users/${selectedUser.id}/status`, { active: newStatus });
      setSuccess(`Đã ${newStatus ? 'mở khóa' : 'khóa'} tài khoản thành công!`);
      setIsUserModalOpen(false);
      fetchData();
    } catch (err: any) {
      setError(err.response?.data?.message || "Lỗi khi thay đổi trạng thái tài khoản.");
    }
  };

  if (loading) return <div className="p-8 text-center text-slate-500">Đang tải dữ liệu...</div>;

  const filteredUsers = users.filter(u => 
    u.name.toLowerCase().includes(searchUserStr.toLowerCase()) || 
    u.email.toLowerCase().includes(searchUserStr.toLowerCase())
  );

  return (
    <div className="flex min-h-[calc(100vh-4rem)] bg-slate-50">
      <div className="w-64 bg-white border-r border-slate-200 flex flex-col hidden md:flex">
        <div className="p-6 border-b border-slate-100">
          <div className="flex items-center gap-2 text-primary-600 font-bold text-xl">
            <ShieldAlert className="w-6 h-6" /> Admin Panel
          </div>
        </div>
        <nav className="flex-1 p-4 space-y-2">
          <button onClick={() => setActiveTab('products')} className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition ${activeTab === 'products' ? 'bg-primary-50 text-primary-700' : 'text-slate-600 hover:bg-slate-50'}`}>
            <Package className="w-5 h-5" /> Sản phẩm
          </button>
          <button onClick={() => setActiveTab('orders')} className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition ${activeTab === 'orders' ? 'bg-primary-50 text-primary-700' : 'text-slate-600 hover:bg-slate-50'}`}>
            <FileText className="w-5 h-5" /> Đơn hàng
          </button>
          <button onClick={() => setActiveTab('users')} className={`w-full flex items-center gap-3 px-4 py-3 rounded-lg font-medium transition ${activeTab === 'users' ? 'bg-primary-50 text-primary-700' : 'text-slate-600 hover:bg-slate-50'}`}>
            <Users className="w-5 h-5" /> Người dùng
          </button>
        </nav>
      </div>

      <div className="flex-1 flex flex-col min-w-0 overflow-hidden">
        <div className="p-8 flex-1 overflow-auto">
          {success && <div className="mb-6 bg-green-50 text-green-700 p-4 rounded-lg border border-green-200">{success}</div>}
          {error && <div className="mb-6 bg-red-50 text-red-700 p-4 rounded-lg border border-red-200">{error}</div>}

          {activeTab === 'products' && (
            <>
              <div className="flex justify-between items-center mb-8">
                <div>
                  <h1 className="text-3xl font-bold text-slate-900">Quản lý Sản phẩm</h1>
                  <p className="text-slate-500">Xin chào, {user?.firstName}. Quản lý danh mục và sản phẩm.</p>
                </div>
                <button onClick={() => handleOpenProductModal()} className="bg-primary-600 hover:bg-primary-700 text-white px-5 py-2.5 rounded-lg font-medium flex items-center gap-2 transition shadow-sm">
                  <Plus className="w-5 h-5" /> Thêm sản phẩm
                </button>
              </div>
              <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
                <table className="w-full text-left text-sm">
                  <thead className="bg-slate-50 border-b border-slate-200 text-slate-600">
                    <tr>
                      <th className="py-4 px-6 font-semibold">Tên sản phẩm</th>
                      <th className="py-4 px-6 font-semibold">Giá</th>
                      <th className="py-4 px-6 font-semibold">Kho</th>
                      <th className="py-4 px-6 font-semibold">Trạng thái</th>
                      <th className="py-4 px-6 font-semibold text-right">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {products.map(p => (
                      <tr key={p.id} className="hover:bg-slate-50 transition">
                        <td className="py-4 px-6 font-medium text-slate-900">{p.name}</td>
                        <td className="py-4 px-6 text-slate-900">{formatVND(p.price)}</td>
                        <td className="py-4 px-6">
                          <span className="text-slate-900 font-medium">{p.stockQuantity}</span>
                          {p.stockQuantity === 0 ? <span className="ml-2 text-xs text-red-600 font-semibold">Hết hàng</span> : <span className="ml-2 text-xs text-green-600 font-semibold">Còn hàng</span>}
                        </td>
                        <td className="py-4 px-6">
                          <span className={`px-3 py-1 text-xs font-semibold rounded-full ${p.active ? "bg-green-100 text-green-700" : "bg-slate-100 text-slate-600"}`}>
                            {p.active ? "Active" : "Inactive"}
                          </span>
                        </td>
                        <td className="py-4 px-6 text-right space-x-3">
                          <button onClick={() => handleOpenProductModal(p)} className="text-blue-600 font-medium text-sm">Sửa</button>
                          {p.active && <button onClick={() => handleDeactivate(p.id)} className="text-red-600 font-medium text-sm">Vô hiệu hóa</button>}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}

          {activeTab === 'orders' && (
            <>
              <div className="mb-8">
                <h1 className="text-3xl font-bold text-slate-900">Quản lý Đơn hàng</h1>
                <p className="text-slate-500">Xem và cập nhật trạng thái giao hàng.</p>
              </div>
              <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
                <table className="w-full text-left text-sm">
                  <thead className="bg-slate-50 border-b border-slate-200 text-slate-600">
                    <tr>
                      <th className="py-4 px-6 font-semibold">ID</th>
                      <th className="py-4 px-6 font-semibold">Khách hàng</th>
                      <th className="py-4 px-6 font-semibold">Tổng</th>
                      <th className="py-4 px-6 font-semibold">Thanh toán</th>
                      <th className="py-4 px-6 font-semibold">Trạng thái</th>
                      <th className="py-4 px-6 font-semibold text-right">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {orders.map(o => (
                      <tr key={o.id} className="hover:bg-slate-50 transition">
                        <td className="py-4 px-6 font-medium text-slate-900">#{o.orderNumber.substring(0, 8)}</td>
                        <td className="py-4 px-6 text-slate-900">
                          <div>{o.customerName}</div>
                          <div className="text-xs text-slate-500">{o.customerEmail}</div>
                        </td>
                        <td className="py-4 px-6 text-slate-900">{formatVND(o.totalAmount)}</td>
                        <td className="py-4 px-6">
                          <span className={`px-3 py-1 text-xs font-semibold rounded-full ${o.paymentStatus === 'SUCCESS' ? "bg-green-100 text-green-700" : "bg-orange-100 text-orange-700"}`}>
                            {o.paymentStatus}
                          </span>
                        </td>
                        <td className="py-4 px-6">
                          <span className={`px-3 py-1 text-xs font-semibold rounded-full ${o.orderStatus === 'DELIVERED' ? "bg-blue-100 text-blue-700" : "bg-slate-100 text-slate-700"}`}>
                            {o.orderStatus}
                          </span>
                        </td>
                        <td className="py-4 px-6 text-right space-x-3">
                          <button onClick={() => handleOpenOrderModal(o.id)} className="text-primary-600 hover:text-primary-800 font-medium text-sm">Chi tiết & Cập nhật</button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}

          {activeTab === 'users' && (
            <>
              <div className="flex justify-between items-center mb-8">
                <div>
                  <h1 className="text-3xl font-bold text-slate-900">Quản lý Người dùng</h1>
                  <p className="text-slate-500">Khóa hoặc mở khóa tài khoản khách hàng.</p>
                </div>
                <input 
                  type="text" 
                  placeholder="Tìm kiếm email/tên..." 
                  value={searchUserStr}
                  onChange={(e) => setSearchUserStr(e.target.value)}
                  className="border border-slate-300 rounded-lg px-4 py-2 outline-none focus:ring-2 focus:ring-primary-500"
                />
              </div>
              <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
                <table className="w-full text-left text-sm">
                  <thead className="bg-slate-50 border-b border-slate-200 text-slate-600">
                    <tr>
                      <th className="py-4 px-6 font-semibold">ID</th>
                      <th className="py-4 px-6 font-semibold">Khách hàng</th>
                      <th className="py-4 px-6 font-semibold">Vai trò</th>
                      <th className="py-4 px-6 font-semibold">Trạng thái</th>
                      <th className="py-4 px-6 font-semibold text-right">Thao tác</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {filteredUsers.map(u => (
                      <tr key={u.id} className="hover:bg-slate-50 transition">
                        <td className="py-4 px-6 font-medium text-slate-900">{u.id}</td>
                        <td className="py-4 px-6 text-slate-900">
                          <div>{u.name}</div>
                          <div className="text-xs text-slate-500">{u.email}</div>
                        </td>
                        <td className="py-4 px-6 text-slate-900">
                          <span className={`px-2 py-1 text-xs font-semibold rounded-md ${u.role === 'ADMIN' ? 'bg-purple-100 text-purple-700' : 'bg-slate-100 text-slate-600'}`}>
                            {u.role}
                          </span>
                        </td>
                        <td className="py-4 px-6">
                          <span className={`px-3 py-1 text-xs font-semibold rounded-full ${u.active ? "bg-green-100 text-green-700" : "bg-red-100 text-red-700"}`}>
                            {u.active ? "ACTIVE" : "DISABLED"}
                          </span>
                        </td>
                        <td className="py-4 px-6 text-right space-x-3">
                          <button onClick={() => handleOpenUserModal(u.id)} className="text-primary-600 hover:text-primary-800 font-medium text-sm">
                            Xem chi tiết
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </>
          )}

        </div>
      </div>

      {isProductModalOpen && (
        <div className="fixed inset-0 bg-slate-900/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-2xl overflow-hidden flex flex-col max-h-[90vh]">
            <div className="p-6 border-b border-slate-100 flex justify-between items-center bg-slate-50">
              <h3 className="text-xl font-bold text-slate-900">{editingProduct ? "CẬP NHẬT SẢN PHẨM" : "THÊM SẢN PHẨM"}</h3>
              <button onClick={() => setIsProductModalOpen(false)} className="text-slate-400 hover:text-slate-600"><X className="w-6 h-6" /></button>
            </div>
            <div className="p-6 overflow-y-auto">
              <form id="productForm" onSubmit={handleSaveProduct} className="space-y-5">
                <div>
                  <label className="block text-sm font-semibold text-slate-700 mb-1">Tên sản phẩm *</label>
                  <input type="text" required value={productForm.name} onChange={e => setProductForm({...productForm, name: e.target.value})} className="w-full border border-slate-300 rounded-lg px-4 py-2 outline-none" />
                </div>
                <div className="grid grid-cols-2 gap-5">
                  <div>
                    <label className="block text-sm font-semibold text-slate-700 mb-1">Danh mục *</label>
                    <select required value={productForm.categoryId} onChange={e => setProductForm({...productForm, categoryId: e.target.value})} className="w-full border border-slate-300 rounded-lg px-4 py-2 outline-none">
                      {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                    </select>
                  </div>
                  <div>
                    <label className="block text-sm font-semibold text-slate-700 mb-1">Trạng thái</label>
                    <select value={productForm.active ? "true" : "false"} onChange={e => setProductForm({...productForm, active: e.target.value === "true"})} className="w-full border border-slate-300 rounded-lg px-4 py-2 outline-none">
                      <option value="true">Active</option><option value="false">Inactive</option>
                    </select>
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-5">
                  <div>
                    <label className="block text-sm font-semibold text-slate-700 mb-1">Giá (VND) *</label>
                    <input type="number" step="1" min="0" required value={productForm.price} onChange={e => setProductForm({...productForm, price: e.target.value})} className="w-full border border-slate-300 rounded-lg px-4 py-2 outline-none" />
                  </div>
                  <div>
                    <label className="block text-sm font-semibold text-slate-700 mb-1">Số lượng kho *</label>
                    <input type="number" min="0" required value={productForm.stockQuantity} onChange={e => setProductForm({...productForm, stockQuantity: e.target.value})} className="w-full border border-slate-300 rounded-lg px-4 py-2 outline-none" />
                  </div>
                </div>
              </form>
            </div>
            <div className="p-6 border-t border-slate-100 bg-slate-50 flex justify-end gap-3">
              <button onClick={() => setIsProductModalOpen(false)} className="px-5 py-2.5 rounded-lg font-medium text-slate-600">Hủy</button>
              <button type="submit" form="productForm" className="px-5 py-2.5 rounded-lg font-medium text-white bg-primary-600">Lưu</button>
            </div>
          </div>
        </div>
      )}

      {isOrderModalOpen && selectedOrder && (
        <div className="fixed inset-0 bg-slate-900/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-3xl overflow-hidden flex flex-col max-h-[90vh]">
            <div className="p-6 border-b border-slate-100 flex justify-between items-center bg-slate-50">
              <h3 className="text-xl font-bold text-slate-900">CHI TIẾT ĐƠN HÀNG #{selectedOrder.orderNumber.substring(0,8)}</h3>
              <button onClick={() => setIsOrderModalOpen(false)} className="text-slate-400 hover:text-slate-600"><X className="w-6 h-6" /></button>
            </div>
            <div className="p-6 overflow-y-auto space-y-6">
              
              <div className="grid grid-cols-2 gap-6">
                <div className="bg-slate-50 p-4 rounded-xl border border-slate-100">
                  <h4 className="font-semibold text-slate-900 mb-2">Khách hàng</h4>
                  <p className="text-sm text-slate-600">{selectedOrder.customerName}</p>
                  <p className="text-sm text-slate-600">{selectedOrder.customerEmail}</p>
                </div>
                <div className="bg-slate-50 p-4 rounded-xl border border-slate-100">
                  <h4 className="font-semibold text-slate-900 mb-2">Thanh toán (Read-Only)</h4>
                  <p className="text-sm text-slate-600">Trạng thái: <strong className={selectedOrder.paymentStatus === 'SUCCESS' ? 'text-green-600' : 'text-orange-600'}>{selectedOrder.paymentStatus}</strong></p>
                  {selectedOrder.paymentRef && <p className="text-sm text-slate-600">Mã GD: {selectedOrder.paymentRef}</p>}
                </div>
              </div>

              <div>
                <h4 className="font-semibold text-slate-900 mb-3">Sản phẩm ({selectedOrder.items.length})</h4>
                <div className="border border-slate-200 rounded-lg overflow-hidden">
                  <table className="w-full text-sm text-left">
                    <thead className="bg-slate-50 border-b border-slate-200">
                      <tr>
                        <th className="px-4 py-2">Sản phẩm</th>
                        <th className="px-4 py-2">SL</th>
                        <th className="px-4 py-2 text-right">Giá</th>
                        <th className="px-4 py-2 text-right">Tổng</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {selectedOrder.items.map((item, idx) => (
                        <tr key={idx}>
                          <td className="px-4 py-2">{item.productName}</td>
                          <td className="px-4 py-2">{item.quantity}</td>
                          <td className="px-4 py-2 text-right">{formatVND(item.unitPrice)}</td>
                          <td className="px-4 py-2 text-right font-medium">{formatVND((item.unitPrice * item.quantity))}</td>
                        </tr>
                      ))}
                    </tbody>
                    <tfoot className="bg-slate-50 border-t border-slate-200">
                      <tr>
                        <td colSpan={3} className="px-4 py-3 text-right font-semibold">TỔNG CỘNG:</td>
                        <td className="px-4 py-3 text-right font-bold text-primary-600">{formatVND(selectedOrder.totalAmount)}</td>
                      </tr>
                    </tfoot>
                  </table>
                </div>
              </div>

              <div className="bg-blue-50 p-4 rounded-xl border border-blue-100">
                <h4 className="font-semibold text-blue-900 mb-2">Cập nhật trạng thái đơn hàng</h4>
                <form id="orderStatusForm" onSubmit={handleUpdateOrderStatus} className="flex gap-4">
                  <select 
                    value={orderStatusForm} 
                    onChange={(e) => setOrderStatusForm(e.target.value)}
                    className="flex-1 border border-blue-200 rounded-lg px-4 py-2 outline-none"
                  >
                    <option value="PENDING">PENDING (Chờ xử lý)</option>
                    <option value="PAID">PAID (Đã thanh toán)</option>
                    <option value="SHIPPED">SHIPPED (Đang giao hàng)</option>
                    <option value="DELIVERED">DELIVERED (Đã giao hàng)</option>
                    <option value="CANCELLED">CANCELLED (Đã hủy)</option>
                  </select>
                  <button type="submit" className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2 rounded-lg font-medium">
                    Lưu trạng thái
                  </button>
                </form>
              </div>

            </div>
          </div>
        </div>
      )}

      {isUserModalOpen && selectedUser && (
        <div className="fixed inset-0 bg-slate-900/50 flex items-center justify-center z-50 p-4 backdrop-blur-sm">
          <div className="bg-white rounded-2xl shadow-xl w-full max-w-lg overflow-hidden flex flex-col max-h-[90vh]">
            <div className="p-6 border-b border-slate-100 flex justify-between items-center bg-slate-50">
              <h3 className="text-xl font-bold text-slate-900">CHI TIẾT NGƯỜI DÙNG</h3>
              <button onClick={() => setIsUserModalOpen(false)} className="text-slate-400 hover:text-slate-600"><X className="w-6 h-6" /></button>
            </div>
            <div className="p-6 overflow-y-auto space-y-4">
              <div className="flex flex-col gap-2">
                <div className="flex justify-between border-b border-slate-100 pb-2">
                  <span className="text-slate-500 font-medium">ID</span>
                  <span className="font-semibold text-slate-900">{selectedUser.id}</span>
                </div>
                <div className="flex justify-between border-b border-slate-100 pb-2">
                  <span className="text-slate-500 font-medium">Họ & Tên</span>
                  <span className="font-semibold text-slate-900">{selectedUser.firstName} {selectedUser.lastName}</span>
                </div>
                <div className="flex justify-between border-b border-slate-100 pb-2">
                  <span className="text-slate-500 font-medium">Email</span>
                  <span className="font-semibold text-slate-900">{selectedUser.email}</span>
                </div>
                <div className="flex justify-between border-b border-slate-100 pb-2">
                  <span className="text-slate-500 font-medium">Vai trò</span>
                  <span className="font-semibold text-slate-900">{selectedUser.role}</span>
                </div>
                <div className="flex justify-between border-b border-slate-100 pb-2">
                  <span className="text-slate-500 font-medium">Trạng thái</span>
                  <span className={`font-bold ${selectedUser.active ? "text-green-600" : "text-red-600"}`}>
                    {selectedUser.active ? "ACTIVE (Hoạt động)" : "DISABLED (Đã khóa)"}
                  </span>
                </div>
                <div className="flex justify-between border-b border-slate-100 pb-2">
                  <span className="text-slate-500 font-medium">Số đơn hàng</span>
                  <span className="font-semibold text-slate-900">{selectedUser.orderCount} đơn</span>
                </div>
                <div className="flex justify-between border-b border-slate-100 pb-2">
                  <span className="text-slate-500 font-medium">Ngày tạo</span>
                  <span className="font-semibold text-slate-900">{new Date(selectedUser.createdAt).toLocaleDateString()}</span>
                </div>
              </div>
            </div>
            <div className="p-6 border-t border-slate-100 bg-slate-50 flex justify-end gap-3">
              <button onClick={() => setIsUserModalOpen(false)} className="px-5 py-2.5 rounded-lg font-medium text-slate-600">Đóng</button>
              {selectedUser.id !== user?.id && (
                <button 
                  onClick={handleToggleUserStatus} 
                  className={`px-5 py-2.5 rounded-lg font-medium text-white transition ${selectedUser.active ? 'bg-red-600 hover:bg-red-700' : 'bg-green-600 hover:bg-green-700'}`}
                >
                  {selectedUser.active ? "Khóa tài khoản" : "Mở khóa tài khoản"}
                </button>
              )}
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

export default AdminDashboard;
