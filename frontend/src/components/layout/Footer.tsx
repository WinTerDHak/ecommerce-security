import { Link } from 'react-router-dom';
import { ShieldCheck, CreditCard, Mail, Phone, MapPin, ChevronRight } from 'lucide-react';

const Footer = () => {
  return (
    <footer className="bg-slate-900 pt-16 pb-8 text-slate-300">
      <div className="container mx-auto px-4 max-w-7xl">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-10 mb-12">
          
          {/* Company Info */}
          <div>
            <Link to="/" className="text-2xl font-black tracking-tight text-white mb-6 block">
              NOVA<span className="text-primary-500">.</span>
            </Link>
            <p className="text-sm text-slate-400 mb-6 leading-relaxed">
              Cửa hàng công nghệ & phong cách sống hiện đại. Chúng tôi cung cấp các sản phẩm chất lượng cao với trải nghiệm mua sắm an toàn và tin cậy.
            </p>
            
          </div>

          {/* Quick Links */}
          <div>
            <h3 className="text-white font-bold text-lg mb-6">Danh mục</h3>
            <ul className="space-y-4">
              <li>
                <Link to="/products?category=1" className="text-slate-400 hover:text-white transition flex items-center">
                  <ChevronRight className="w-4 h-4 mr-2 text-primary-500" /> Điện tử & Công nghệ
                </Link>
              </li>
              <li>
                <Link to="/products?category=2" className="text-slate-400 hover:text-white transition flex items-center">
                  <ChevronRight className="w-4 h-4 mr-2 text-primary-500" /> Sách & Học tập
                </Link>
              </li>
              <li>
                <Link to="/products?category=3" className="text-slate-400 hover:text-white transition flex items-center">
                  <ChevronRight className="w-4 h-4 mr-2 text-primary-500" /> Thời trang
                </Link>
              </li>
              <li>
                <Link to="/products?category=4" className="text-slate-400 hover:text-white transition flex items-center">
                  <ChevronRight className="w-4 h-4 mr-2 text-primary-500" /> Nhà cửa & Đời sống
                </Link>
              </li>
            </ul>
          </div>

          {/* Support */}
          <div>
            <h3 className="text-white font-bold text-lg mb-6">Hỗ trợ khách hàng</h3>
            <ul className="space-y-4">
              <li>
                <a href="#" className="text-slate-400 hover:text-white transition flex items-center">
                  <ChevronRight className="w-4 h-4 mr-2 text-primary-500" /> Trung tâm trợ giúp
                </a>
              </li>
              <li>
                <a href="#" className="text-slate-400 hover:text-white transition flex items-center">
                  <ChevronRight className="w-4 h-4 mr-2 text-primary-500" /> Chính sách bảo mật
                </a>
              </li>
              <li>
                <a href="#" className="text-slate-400 hover:text-white transition flex items-center">
                  <ChevronRight className="w-4 h-4 mr-2 text-primary-500" /> Điều khoản dịch vụ
                </a>
              </li>
              <li>
                <a href="#" className="text-slate-400 hover:text-white transition flex items-center">
                  <ChevronRight className="w-4 h-4 mr-2 text-primary-500" /> Trả hàng & Hoàn tiền
                </a>
              </li>
            </ul>
          </div>

          {/* Contact */}
          <div>
            <h3 className="text-white font-bold text-lg mb-6">Liên hệ</h3>
            <ul className="space-y-4">
              <li className="flex items-start">
                <MapPin className="w-5 h-5 text-primary-500 mr-3 mt-0.5 shrink-0" />
                <span className="text-slate-400 leading-relaxed">
                  Học viện kĩ thuật mật mã - KMA<br />
                  17A Cộng Hòa, P.Tân Sơn Nhất, Tp.Hồ Chí Minh.
                </span>
              </li>
              <li className="flex items-center">
                <Phone className="w-5 h-5 text-primary-500 mr-3 shrink-0" />
                <span className="text-slate-400">097 3939084</span>
              </li>
              <li className="flex items-center">
                <Mail className="w-5 h-5 text-primary-500 mr-3 shrink-0" />
                <span className="text-slate-400">toanvo1951975@gmail.com</span>
              </li>
            </ul>
          </div>
          
        </div>

        {/* Security Badges */}
        <div className="border-t border-slate-800 pt-8 pb-4 flex flex-col md:flex-row justify-between items-center">
          <div className="flex flex-wrap items-center gap-4 mb-4 md:mb-0">
             <span className="text-xs font-bold text-slate-500 uppercase tracking-widest mr-2">Secure Payments</span>
             <div className="bg-white/10 px-3 py-1.5 rounded flex items-center gap-2">
               <ShieldCheck className="w-4 h-4 text-green-400" />
               <span className="text-xs font-medium text-white">100% SECURE</span>
             </div>
             <div className="bg-white/10 px-3 py-1.5 rounded flex items-center gap-2">
               <CreditCard className="w-4 h-4 text-blue-400" />
               <span className="text-xs font-medium text-white">VNPAY</span>
             </div>
          </div>
          <div className="text-sm text-slate-500 text-center md:text-right">
            &copy; {new Date().getFullYear()} NOVA Shop. E-Commerce Security Demo Project.
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
