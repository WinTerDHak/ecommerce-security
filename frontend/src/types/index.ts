export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: 'CUSTOMER' | 'ADMIN';
}

export interface AuthResponse {
  token: string;
  refreshToken: string;
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  role: 'CUSTOMER' | 'ADMIN';
}

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  categoryId: number;
  categoryName: string;
  active: boolean;
}

export interface Category {
  id: number;
  name: string;
  description: string;
}

export interface CartItem {
  id: number;
  productId: number;
  productName: string;
  price: number;
  quantity: number;
}

export interface Order {
  id: number;
  userId: number;
  totalAmount: number;
  status: 'PENDING' | 'PAID' | 'FAILED' | 'CANCELLED';
  shippingStreet: string;
  shippingCity: string;
  shippingZip: string;
  shippingCountry: string;
  createdAt: string;
}

export interface PaymentUrl {
  paymentUrl: string;
}


