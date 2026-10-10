export const getProductImage = (productId: number | string | undefined): string => {
  if (!productId) return '/products/fallback.jpg';

  const id = Number(productId);
  
  // Mapping strict database ID to local static image
  const imageMap: Record<number, string> = {
    1: '/products/product-1.jpg',   // Smartphone X
    2: '/products/product-2.jpg',   // Laptop Pro
    3: '/products/product-3.jpg',   // Wireless Earbuds
    4: '/products/product-4.jpg',   // Programming in Java
    5: '/products/product-5.jpg',   // Cybersecurity Basics
    6: '/products/product-6.jpg',   // Cotton T-Shirt
    7: '/products/product-7.jpg',   // Running Shoes
    8: '/products/product-8.jpg',   // Coffee Maker
    9: '/products/product-9.jpg',   // Blender
    10: '/products/product-10.jpg', // Yoga Mat
    11: '/products/product-11.jpg', // Dumbbell Set
  };

  return imageMap[id] || '/products/fallback.jpg';
};

export const getProductImages = (productId: number | string | undefined): string[] => {
  const mainImage = getProductImage(productId);
  if (!productId) return [mainImage];
  
  return [mainImage];
};
