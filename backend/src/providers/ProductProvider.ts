import type { Product } from '../domain/product.js';

/**
 * Boundary between the catalogue and wherever product data lives.
 * Swap the data source (database, PIM, another API) by implementing this.
 */
export interface ProductProvider {
  listProducts(): Promise<Product[]>;
  /** Returns null when the product does not exist. */
  getProduct(id: string): Promise<Product | null>;
  listCategories(): Promise<string[]>;
}
